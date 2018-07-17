# Overview

Suppose we are given
* Two types of devices, `A` and `B`
* An unspecified number of devices of each type
* A continuous data stream from each device, where `A` devices stream integers and `B` devices follow the structure outlined in the [`ModelB`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java) class

and we wish to create a streaming API that provides
* The latest element in each `A` device's stream
* The latest element in each `B` device's stream
* All elements in the each `A` device's stream
* The last five elements in each `B` device's stream

We will show here that this problem can be cleanly solved by a simple Swim server. Recall from the [top-level README](https://github.com/swimit/swim-academy/) that all we need to run a Swim application is

1. [Write Swim `Services`](#writing-the-swim-services) with the appropriate `Lane` definitions
2. [Write a `Plane`](#writing-the-plane) with the `Service` URI definitions and the application configuration
3. [Ingest data](#data-ingestion) into `Lanes` using `commands` or `Links`.

# Writing the Swim Services

[Services](https://developer.swim.ai/server/services)

[Lanes](https://developer.swim.ai/server/lanes)

If we take an object-oriented approach to our API, we may imagine an `A` class and a `B` class. Each class could store its latest stream element and its list of historical elements as a `value` field and a `list` field, respectively. Each endpoint corresponding to a specific device is an `instance` of one of these `classes`.

The Swim equivalent of a `class` is a `Service`. `fields` translate to either `ValueLanes` or `MapLanes`. `methods` and `functions` manifest themselves as the callback functions on `Lanes`, including any `CommandLanes`.

## AService

* To access the Swim API, each user-defined `Service` must [extend](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L3-L5) `swim.api.AbstractService`.
* Let's enable the simplest spec requirement: storing the latest stream element. This simply requires [declaring](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L7-L17) a `ValueLane`. Things to note:
    * A `@SwimLane` annotation must precede every `Lane` declaration. The `String` inside the annotation defines the `Lane`'s globally addressable URI and does not need to match the `Lane`'s field name.
    * `Lanes` are parametrized. Any parameters that are not explicitly provided (e.g. using `ValueLane.valueClass(), MapLane.keyClass()`) default to `<recon.Value>`. We use an `Integer` here because `A`-type devices stream `Integers`.
* Capturing a stream's history simply requires [declaring](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L19-L31) a `MapLane` instead of a single-dimensional `ValueLane`, and providing a key to order the historical values. Typically, this is accomplished by using a key of type `Long` and storing the relevant Unix timestamp.

And, at least in terms of the final API, that's all! You likely have noticed the additional `CommandLane`, but it will make more sense to discuss this in the [Data Ingestion](#data-ingestion) section.

## BService

Filling out `BService.java` follows almost identically, but we'll need to utilize a few more tools here.

* To store the latest stream element, we again need a `ValueLane`. But this time, defining our stream data type requires a custom Java object.

    Thankfully, any Java class can be used as a lane parameter, _provided that_ `recon` _serializations and deserializations are defined for the class_. If we [annotate the class fields](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java#L9-L20) with `@ReconName` appropriately, Swim will automatically generate and store these transformations using reflection. Although this annotation-based process is simple and sufficient for this example, it proves rather restrictive in general; alternative means to generate the recon transforms can be found [here](https://developer.swim.ai/recon/what-is-recon).
    
* After defining these transforms, we can set `model.ModelB` to be the `valueClass` of both the [`latest`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L15) and the [`history`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L30) `Lanes`. However, there's one more thing we should do.

    "History" is simply a collection of all "latest" values. Thus, it would be nice if every update to the `latest` `ValueLane` automatically populated the `history` `MapLane`.
    
    This is the beauty of lane callback functions. Users can override the `didSet(newValue, oldValue)` callback of any `ValueLane` with custom logic that will take place every time the `ValueLane` is set. In this case, we only need to [add to the `history` `MapLane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L20). While we could (and arguably should) have done this in `AService` as well, we have intentionally left the `MapLane.put()` logic in the `CommandLane` to provide more diverse `Lane` manipulation examples.
    
    The `MapLane` equivalent of `didSet` is `didUpdate`. We [override it here](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L33-L35) to drop all but the last 5 events in `history` as defined in our spec.
    
We will again save the `CommandLane` discussion for the [Data Ingestion](#data-ingestion) section.

# Writing the Plane

[Planes](https://developer.swim.ai/server/planes)

A Swim application is relatively unintrusive. Despite exposing a potentially huge number of API endpoints, an application only utilizes a single configurable port because Swim `Service` URIs are resolved internally. Persistent data is written to a configurable location on disk.

The object that manages such runtime behavior of Swim elements is called the Swim `Plane`, and while its responsibilities are complex, it is straightforward to configure.

1. For every `Service` in the application, [declare](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L11-L18) a `ServiceType<?>` instance in the plane.

2. Identify all desired plane configuration properties. [Here](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L26), we only set the application's port binding, so we can do this inline when running the Swim bundle. We will cover more involved `Plane` configurations in the `join` services [example](https://github.com/swimit/swim-academy/tree/master/joins/services#writing-the-plane)

# Data Ingestion

[Relevant reading](https://developer.swim.ai/client/swim-client)

Data ingestion is a two-part problem. Swim `Services` must expose means to receive data, and data sources must be able to send data to Swim `Services`.

## Ingress by Services

Swim `Services` and `Lanes` form a bidirectional API. We utilize them here to enable receiving external data, and we will use them again to view data.

Let's revisit `AService`. We have declared two `Lanes` to store data, but nowhere do we write to them.

`CommandLanes` are specialized Swim `Lane` that receive but do not store data. Each `CommandLane` permits overriding an `onCommand` callback that contains a reference to this data and will execute whenever it is `commanded`, such as in the [Egress by Sources](#egress-by-sources) section). We [declare](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L33-L45) one here to set the `latest` and `history` `Lanes` upon being `commanded`.

The equivalent in `BService` is nearly identical. However, because the `didSet` callback on `latest` already `puts` to `history`, we only need to `set` `latest` in this `CommandLane`.

## Egress by Data Sources

To simulate a single `AService` stream for an `A` type device with id `1`, we simply [command](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java#L52-L55) the `addLatest` `CommandLane` at node `a/1`. Recall that the `node` and `lane` identifiers utilize the `@SwimRoute` defined in the [`Plane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L13) and the [`@SwimLane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L37) defined in `AService`, respectively.

Simulation for a single `BService` stream [follows nearly identically](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java#L88-L92).

The remaining code in [`Client`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java) simply opens subscriptions to the API that we've developed.


# UI Overview
Here we go through examples of how to display the data in `ValueLanes` and `MapLanes` into the `Swim UI` components. We will use the `lanes` in the [`AService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java) and the [`BService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java) to illustrate these concepts

## UI Components

All `Swim UI` components need to link to a streaming API provided by the `Swim Service`. The streaming API is identified using the `host`, `node` and `lane`. In this specific example- the `Swim Server` is running locally and is listening on port 9001. The `AService` instance has the URi `a/1` and the `BService` instance has the URI `b/1` since the [Client](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java) simulates data for these instances. Consequently all the UI components below use one of the following values for the above mentioned variables depending on their functionality.
* `host`: swim://localhost:9001
* `node`: 'a/1' or 'b/1'
* `lane`: 'latest' or 'history'

Given the `host`, `node` and `lane`, there are two steps involved in integrating `Swim UI` components to  `Swim Services`. 
1. A link needs to be established. To create a link use the `@link` tag. 
2. The data can be obtained from the `link` created from the `@link` tag. However the data has to be parsed and injected into the `Swim UI` components. This requires some knowledge of the data format that will be obtained from the different lanes. 

Here are the examples for the different `service` and `lane` combinations

* 'latest' lane of a/1: Example of a **value** lane that has **primitive** values i.e. booleans, integers, doubles, strings etc. 
```
host: 'swim://localhost:9001'
node: 'a/1'
linkA: @link(lane: 'latest', type: value) 
value: $linkA.* 
```
'latest' is a value lane, use the **`type:value`** in the `@link` tag. Data in the 'latest' lane for 'a/1' contains a simple integer value, so you can use the **`$linkA.*`** expression to get the integer value. 

* 'latest' lane of b/1: Example of a **value** lane that has **custom types**.
```
host: 'swim://localhost:9001'
node: 'b/1'
linkB: @link(lane: 'latest', type: value) 
value: $linkB.i 
```
'latest' is a value lane, use the **`type:value`** in the `@link` tag. Data in the 'latest' lane for 'b/1' contains a custom type, [`ModelB`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java). Remember that `ModelB` has a recon representation as follows: '@modelB{b:false,s:"1",i:1,l:1,f:1,d:1}', so you can use the **`$linkB.i`** expression to get the 'i' value. You can following the same pattern for getting the other fields, eg: **`$linkB.l`** expression to get the field 'l'.

* 'history' lane of a/1: Example of a **map** lane that has **primitive** values i.e. booleans, integers, doubles, strings etc. 
```
host: 'swim://localhost:9001'
node: 'a/1'
linkA: @link(lane: 'history', type: map) 
@each(t: $linkA.*:) {
  key: $t
  value: $linkA.($t)
} 
```
'history' is a value lane, use the **`type:map`** in the `@link` tag. Iterate through the elements of the map using the code snippet: `@each(t: $linkA.*:)`, here 't' is the key to the element in the map. Data in the 'history' lane of 'a/1' contains a simple integer value, use the **`$linkA.($t)`** expression to get the integer value. 

* 'history' lane of b/1: Example of a **map** lane that has **custom types**.
```
host: 'swim://localhost:9001'
node: 'b/1'
linkB: @link(lane: 'history', type: map) 
@each(t: $linkB.*:) {
  key: $t
  value: $linkA.($t)
} 
```
'history' is a value lane, use the **`type:map`** in the `@link` tag. Iterate through the elements of the map using the code snippet: `@each(t: $linkB.*:)`, here 't' is the key to the element in the map. Data in the 'history' lane of 'b/1' contains a custom type, [`ModelB`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java). Remember that `ModelB` has a recon representation as follows: '@modelB{b:false,s:"1",i:1,l:1,f:1,d:1}', so you can use the **`$linkB.($t).i`** expression to get the 'i' value. You can following the same pattern for getting the other fields, eg: **`$linkB.($t).l`** expression to get the field 'l'. 

### Gauge
Relevant Reading: [Gauges](https://developer.swim.ai/components/gauge)

1. [gaugeA.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/gaugeA.html): Real-time gauge with values from the `latest` lane of `AService` instance 'a/1'
2. [gaugeB.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/gaugeB.html): Real-time gauge with values from the `latest` lane of `BService` instance 'b/1'


### Pie
Relevant Reading: [Pies](https://developer.swim.ai/components/pie)

1. [pieA.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/pieA.html): Real-time pie with values from the `latest` lane of `AService` instance 'a/1'
2. [pieB.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/pieB.html): Real-time pie with values from the `latest` lane of `BService` instance 'b/1'
3. [pieA-history.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/pieA-history.html): Real-time pie with values from the `history` lane of `AService` instance 'a/1'
4. [pieB-history.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/pieB-history.html): Real-time pie with values from the `history` lane of `BService` instance 'b/1'

### Chart
Relevant Reading: [Charts](https://developer.swim.ai/components/chart)

1. [chartA.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/chartA.html): Real-time chart with values from the `history` lane of `AService` instance 'a/1'
2. [chartB.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/chartB.html): Real-time chart with values from the `history` lane of `BService` instance 'b/1'

### KPI
Relevant Reading: [KPI Cards](https://developer.swim.ai/components/kpi)

1. [kpiA.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/kpiA.html): Real-time KPI card with values from the `latest` lane of `AService` instance 'a/1'
2. [kpiB.html](https://github.com/swimit/swim-academy/blob/master/basics/services/ui/kpiB.html): Real-time KPI card with values from the `latest` lane of `BService` instance 'b/1'


# Run

## Run the application
Execute the command `./gradlew run` from a shell pointed to the application's home directory. This will start the Swim plane.
   ```console
    user@machine:~$ ./gradlew run
   ```

## Run the client
Execute the command `./gradlew runClient` from a shell pointed to the application's home directory. This will start the client.
   ```console
    user@machine:~$ ./gradlew runClient
   ```
## Run the UI
Navigate to the `ui` directory which is in the root directory of this installation. This directory contains the html files for 
the different ui components. 

Load the different html pages in your browser and see the SWIM UI components in action


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

[Relevant reading](https://github.com/swimit/swim-academy/wiki/Services#configuration)

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

    Thankfully, any Java class can be used as a lane parameter, _provided that_ `recon` _serializations and deserializations are defined for the class_. If we [annotate the class fields](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java#L9-L20) with `@ReconName` appropriately, Swim will automatically generate and store these transformations using reflection. Although this annotation-based process is simple and sufficient for this example, it proves rather restrictive in general; alternative means to generate the recon transforms can be found [here](https://github.com/swimit/swim-academy/wiki/Recon).
    
* After defining these transforms, we can set `model.ModelB` to be the `valueClass` of both the [`latest`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L15) and the [`history`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L30) `Lanes`. However, there's one more thing we should do.

    "History" is simply a collection of all "latest" values. Thus, it would be nice if every update to the `latest` `ValueLane` automatically populated the `history` `MapLane`.
    
    This is the beauty of lane callback functions. Users can override the `didSet(newValue, oldValue)` callback of any `ValueLane` with custom logic that will take place every time the `ValueLane` is set. In this case, we only need to [add to the `history` `MapLane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L20). While we could (and arguably should) have done this in `AService` as well, we have intentionally left the `MapLane.put()` logic in the `CommandLane` to provide more diverse `Lane` manipulation examples.
    
    The `MapLane` equivalent of `didSet` is `didUpdate`. We [override it here](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java#L33-L35) to drop all but the last 5 events in `history` as defined in our spec.
    
We will again save the `CommandLane` discussion for the [Data Ingestion](#data-ingestion) section.

# Writing the Plane

[Relevant reading](https://github.com/swimit/swim-academy/wiki/Planes-and-Main#configuration)

A Swim application is relatively unintrusive. Despite exposing a potentially huge number of API endpoints, an application only utilizes a single configurable port because Swim `Service` URIs are resolved internally. Persistent data is written to a configurable location on disk.

The object that manages such runtime behavior of Swim elements is called the Swim `Plane`, and while its responsibilities are complex, it is straightforward to configure.

1. For every `Service` in the application, [declare](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L11-L18) a `ServiceType<?>` instance in the plane.

2. Identify all desired plane configuration properties. [Here](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L26), we only set the application's port binding, so we can do this inline when running the Swim bundle. We will cover more involved `Plane` configurations in the `join` services [example](https://github.com/swimit/swim-academy/tree/master/joins/services#writing-the-plane)

# Data Ingestion

[Relevant reading](https://github.com/swimit/swim-academy/wiki/Data-Ingestion)

Data ingestion is a two-part problem. Swim `Services` must expose means to receive data, and data sources must be able to send data to Swim `Services`.

## Ingress by Services

Swim `Services` and `Lanes` form a bidirectional API. We utilize them here to enable receiving external data, and we will use them again to view data.

Let's revisit `AService`. We have declared two `Lanes` to store data, but nowhere do we write to them.

`CommandLanes` are specialized Swim `Lane` that receive but do not store data. Each `CommandLane` permits overriding an `onCommand` callback that contains a reference to this data and will execute whenever it is `commanded`, such as in the [Egress by Sources](#egress-by-sources) section). We [declare](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L33-L45) one here to set the `latest` and `history` `Lanes` upon being `commanded`.

The equivalent in `BService` is nearly identical. However, because the `didSet` callback on `latest` already `puts` to `history`, we only need to `set` `latest` in this `CommandLane`.

## Egress by Data Sources

Refer to the [documentation](https://github.com/swimit/swim-academy/wiki/Data-Ingestion#egress-by-data-sources) on how an external client can write to Swim. We implement the `SwimClient` strategy here. We leave implementing the `Websocket Message` strategy as an exercise in your language of choice.

To simulate a single `AService` stream for an `A` type device with id `1`, we simply [command](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java#L52-L55) the `addLatest` `CommandLane` at node `a/1`. Recall that the `node` and `lane` identifiers utilize the `@SwimRoute` defined in the [`Plane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/App.java#L13) and the [`@SwimLane`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java#L37) defined in `AService`, respectively.

Simulation for a single `BService` stream [follows nearly identically](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java#L88-L92).

The remaining code in [`Client`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java) simply opens subscriptions to the API that we've developed.


# Run

## Run the application
Execute the command `gradle run` from a shell pointed to the application's home directory. This will start the Swim plane.
   ```console
    user@machine:~$ gradle run
   ```

## Run the client
Execute the command `gradle runClient` from a shell pointed to the application's home directory. This will start the client.
   ```console
    user@machine:~$ gradle runClient
   ```

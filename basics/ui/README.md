# Overview
Here we go through examples of how to display the data in `ValueLanes` and `MapLanes` into the `Swim UI` components. We will use the `lanes` in the [`AService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java) and the [`BService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java) to illustrate these concepts

# UI Components and Services

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
'latest' is a value lane, use the **`type:value`** in the `@link` tag. Data in the 'latest' lane for 'b/1' contains a custom type, `[ModelB]`(https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/model/ModelB.java). Remember that `ModelB` has a recon representation as follows: '@modelB{b:false,s:"1",i:1,l:1,f:1,d:1}', so you can use the **`$linkB.i`** expression to get the 'i' value. You can following the same pattern for getting the other fields, eg: **`$linkB.l`** expression to get the field 'l'.

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

## Gauge
Relevant Reading: [Gauges](http://developer.swim.ai/components/gauge)

1. [gaugeA.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/gaugeA.html): Real-time gauge with values from the `latest` lane of `AService` instance 'a/1'
2. [gaugeB.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/gaugeB.html): Real-time gauge with values from the `latest` lane of `BService` instance 'b/1'


## Pie
Relevant Reading: [Pies](http://developer.swim.ai/components/pie)

1. [pieA.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/pieA.html): Real-time pie with values from the `latest` lane of `AService` instance 'a/1'
2. [pieB.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/pieB.html): Real-time pie with values from the `latest` lane of `BService` instance 'b/1'
3. [pieA-history.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/pieA-history.html): Real-time pie with values from the `history` lane of `AService` instance 'a/1'
4. [pieB-history.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/pieB-history.html): Real-time pie with values from the `history` lane of `BService` instance 'b/1'

## Chart
Relevant Reading: [Charts](http://developer.swim.ai/components/chart)

1. [chartA.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/chartA.html): Real-time chart with values from the `history` lane of `AService` instance 'a/1'
2. [chartB.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/chartB.html): Real-time chart with values from the `history` lane of `BService` instance 'b/1'

## KPI
Relevant Reading: [KPI Cards](http://developer.swim.ai/components/kpi)

1. [kpiA.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/kpiA.html): Real-time KPI card with values from the `latest` lane of `AService` instance 'a/1'
2. [kpiB.html](https://github.com/swimit/swim-academy/blob/master/basics/ui/kpiB.html): Real-time KPI card with values from the `latest` lane of `BService` instance 'b/1'


# Installation
## Run
* Run the  application and the client using the instructions documented [here](https://github.com/swimit/swim-academy/blob/master/basics/services/README.md).

* Load the different html pages in your browser and see the SWIM UI components in action 



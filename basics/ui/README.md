# Overview
Here we go through examples of how to display the data in `ValueLanes` and `MapLanes` into the `Swim UI` components. We will use the `lanes` in the [`AService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/AService.java) and the [`BService`](https://github.com/swimit/swim-academy/blob/master/basics/services/src/main/java/ai/swim/service/BService.java) to illustrate these concepts

# UI Components and Services
There are two steps involved in integrating `Swim UI` components to  `Swim Services` 

1. All `Swim UI` components need to link to a streaming API provided by the `Swim Service`. The streaming API is identified using the `host`, `node` and `lane`. In this specific example- the `Swim Server` is running locally and is listening on port 9001. The `AService` instance has the URi `a/1` and the `BService` instance has the URI `b/1` since the [Client](https://github.com/swimit/swim-academy/blob/master/basics/services/src/test/java/ai/swim/client/Client.java) simulates data for these instances. Consequently all the UI components below use one of the following values for the above mentioned variables depending on their functionality.
* `host`: swim://localhost:9001
* `node`: 'a/1' or 'b/1'
* `lane`: 'latest' or 'history'

2. The data obtained from the links has to be parsed and injected into the `Swim UI` components. This requires some knowledge of the data format that will be obtained from the different lanes.


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

## Prerequisites
`Swim UI` components require a `web server`. Please install a `web server` of your choice. 

## Run
* Run the `web server` in the `basics/ui` directory. 

* Run the  application and the client using the instructions documented [here](https://github.com/swimit/swim-academy/blob/master/basics/services/README.md).

* Load the different html pages in your browser using the `web-server`. For example if your `web server` is running on port 8080 then load the 'chartA.html' by typing the following URL on your browser: 'http://localhost:8080/chartA.html'. Follow the same pattern for the other html files



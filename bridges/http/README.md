# Overview

This project is an [ingress bridge](https://developer.swim.ai/bridges/ingress) that sends data from an HTTP endpoint to SWIM `Lanes`. The steps to create this bridge have the following implementations in this project:

1. *Logic to receive source data*
  * Logic that periodically polls an HTTP server and receives a JSON response with each request
2. *Transforming this data to SWIM API calls*
  * Constructing sets from each response that each contain a single `nodeUri`, `laneUri`, and value (in this case, a `Recon Record`), then creating a SWIM `command` message from each set
3. *Running a SWIM Server with the Service and Lane definitions required to accept these API calls*
  * Writing appropriate `Services`, `Lanes`, and a `Plane` to ensure that the above SWIM `commands` have valid endpoints

Our using of polling is an *implementation*, not at a *restriction*. An external process could equivalently push data directly to the SWIM Server via SWIM-Client or websockets.

While it may be tempting to place the poll logic inside the SWIM `Services` themselves, we **strongly recommend you do not do this** -- at least not trivially. Like disk reads, trivial HTTP calls are blocking in Java; a `Lane` or `Service` callback that includes such a call cannot complete until the call ends, which delays other callbacks within the same `Service` instance.

# Source Walkthrough

## package util

### [HttpRequester.java](https://github.com/swimit/swim-academy/blob/master/bridges/http/src/main/java/ai/swim/util/HttpRequester.java)

We designate the `HttpRequester` class responsible for Steps 1. and 2. of the aforementioned steps. Its only public method, `relayExternalData()`, has three tasks:

1. Poll an HTTP endpoint; we use [http://stockmarket.streamdata.io/v2/prices](http://stockmarket.streamdata.io/v2/prices) here.
2. Construct a SWIM `command` message from the data available in the response
3. Send the message to a SWIM Server.

## package service

The logic in step 2. above is very fine-tuned, and thus the following two files are very straightforward.

### [Stock.java](https://github.com/swimit/swim-academy/blob/master/bridges/http/src/main/java/ai/swim/service/Stock.java)

The sole SWIM `Service` definition. Each instance contains an `addLatest` `CommandLane` of which the logic in step 3. above assumes existence.

### [MyPlane.java](https://github.com/swimit/swim-academy/blob/master/bridges/http/src/main/java/ai/swim/service/MyPlane.java)

As expected, contains the `@SwimRoute` to handle requests to `nodeUris` of the form `/stock/:something`.

## main

### [Main.java](https://github.com/swimit/swim-academy/blob/master/bridges/http/src/main/java/ai/swim/Main.java)

Nothing special here, either. Run a SWIM Server in the background, then send a reference to the `Plane` into `HttpRequester` to enable direct usage of the SWIM API; no SWIM-Client or websockets needed here.

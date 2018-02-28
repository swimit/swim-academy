# Overview

The two modes of inter-service communication are through `Commands` and `Links`. The difference between commands and links are subtle. 

A `Command` can be thought of as an ad-hoc invocation over an ephemeral channel where:
1. a communication channel (websocket) is opened
2. a command is sent over the just opened channel
3. the communication channel is composed upon transmitting the command

Conversely, a `Link` can be thought of as an open channel that enables continuous updates to be received as state changes on the other side of the channel, while also providing a means to send any number of commands or invoke any number of mutations upon the state residing at the other side of the channel. For this reason, a `Command` is a degenerate case of `Link` that is ephemeral and constrained to the transmission of a single command.

Swim provides a higher level abstraction to `Links` called `Joins`. A `Join` abstraction applies to either a `ValueLane` or a `MapLane`, called `JoinValueLane` and `JoinMapLane` respectively, and provides a declarative means of composing higher order distributed objects. The most common case is to perform simple aggregation at a higher level. A `Join` lane provides a streamlined means of linking to several different services and assigning to each service the aggregation function. Because lanes are persistent unless opted out, the join relationship is likewise persistent, which becomes particularly useful between service restarts as data integrity can be maintained.

To show the power of joins as clearly as possible, we will consider a very simple service, called `UnitService`, that will be aggregated over by another service called `JoinService`. `UnitService` has a `ValueLane` called `latest` that simply maintains the latest value seen by the service. There are two classes of approach for getting data into any lane: (1) commands; (2) links.

`JoinService` utilizes a `JoinValueLane` called [joinLatest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L31-L43) that receives updates from all lanes that have been linked to it -- in this case, every [latest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/UnitService.java#L31-L35) lane of the `UnitService`.

`joinLatest` illustrates a simple "aggregate all" functionality by writing all updates to a `MapLane` called [allLatest] (https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L49-L53), and where each value is keyed by the corresponding service id (`nodeUri`). A second `MapLane`, [latestOdd] (https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L62-L66), will illustrate filtering by only passing through odd numbers, and which otherwise behaves in the same fashion as `allLatest`.
    

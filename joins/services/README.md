# Overview

To show the power of joins as clearly as possible, we will consider a very simple service, called `UnitService`, that will be aggregated over by another service called `JoinService`. `UnitService` has a `ValueLane` called `latest` that simply maintains the latest value seen by the service. There are two classes of approach for getting data into any lane: (1) commands; (2) links.

`JoinService` utilizes a `JoinValueLane` called [joinLatest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L31-L43) that receives updates from all lanes that have been linked to it -- in this case, every [latest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/UnitService.java#L31-L35) lane of the `UnitService`.

`joinLatest` illustrates a simple "aggregate all" functionality by writing all updates to a `MapLane` called [allLatest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L49-L53), and where each value is keyed by the corresponding service id (`nodeUri`). A second `MapLane`, [latestOdd](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L62-L66), will illustrate filtering by only passing through odd numbers, and which otherwise behaves in the same fashion as `allLatest`.
    
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

# Overview

Suppose we are given
* An unspecified number of entities, each of type `Unit`
* Some metadata for each stream `Unit` entity
* A continuous integer data stream from each `Unit` entity

and we wish to create a streaming API that provides
* Aggregate the latest element in each `Unit` entity's stream into one stream
* Aggregate the latest element in each `Unit` entity's stream into another stream provided th latest element is an odd integer otherwise that `Unit` entity has to be dropped from the stream.

# Writing the Swim Services
The fundamental principles behind writing `services`, `lane` definitions and `lane` callbacks are documented in the ["basics"](https://github.com/swimit/swim-academy/blob/master/basics/services/README.md#writing-the-swim-services) repository.

To show the power of joins as clearly as possible, we will consider a very simple service, called `UnitService`, that will be aggregated over by another service called `JoinService`. 

## UnitService
1. Has a `ValueLane` called `latest` that simply maintains the latest value seen by the service. (an Integer type)
2. Also has a `ValueLane` called `info`. The interesting bit is in the `didSet` callback of the `info`lane which sends a [command](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/UnitService.java#L17) to the `unit/add` lane of a `JoinService` whose `uri` is `join/all`. The command body is the `nodeUri` of the specific instance of the UnitService. Note that commands can be sent from one `Service` to another just as it can be sent by a `SwimClient` to a `Service` 

The 2 command lanes `addInfo` and `addLatest` are covered in the in the [Data Ingestion](#data-ingestion) section.

## JoinService
1. Has a `MapLane`called [`allLatest`](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L49-L53) to store the latest stream value for all `UnitService` instances and where each value is keyed by the corresponding service id (`nodeUri`).
2. Has a `MapLane`called [`latestOdd`](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L62-L66), will illustrate filtering by only passing through odd numbers, and which otherwise behaves in the same fashion as `allLatest`.
3. Has a `JoinValueLane` called [joinLatest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L31-L43) that receives updates from all lanes that have been linked to it -- in this case, every [latest](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/UnitService.java#L31-L35) lane of the `UnitService`. The `JoinValue` first has to establish `downlinks` to all instances of `UnitService`. This is covered in the [Data Ingestion](#data-ingestion) section.

The command lane `unit/add` is covered in the [Data Ingestion](#data-ingestion) section.

# Writing the Plane
The fundamental principles behind writing `planes` documented in the ["basics"](https://github.com/swimit/swim-academy/tree/master/basics/services#writing-the-plane) repository.

1. Define a `SwimRoute` (uri pattern) for the [`UnitService`](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/App.java#L21) and the [`JoinService`](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/App.java#L24-L25)

2. Identify all desired plane configuration properties. [Here](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/App.java#L36-L73) we read a `.recon` file and parse it as a `ServerDef`, and materialize it into the `SwimServer` [here](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/App.java#L29). The file read in this application is the [join-app.recon](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/resources/join-app.recon). The definitions here are the name of the plane, the port binding (9002) and the directory where the persistent data gets written. It also has parameters for configuring TLS settings that are commented out. Please use this as a reference file for all your applications.

# Data Ingestion

## Ingress by Services
By now you must be familiar that `Command Lanes` are used to inject data from within and outside a service
1. The 2 command lanes in the `UnitService`, `addInfo` and `addLatest`, are used to receive data from external sources 
2. The command lane `unit/add` is used to receive data from the `didSet` callback in the `info` lane of all `UnitService` instances. The callback on the `unit/add` `comandlane` [invokes](https://github.com/swimit/swim-academy/blob/master/joins/services/src/main/java/ai/swim/service/JoinService.java#L16-L19) the downlinkg method on the join lane and makes it create a `downlink` to the `latest` lane of that particular instance of the `UnitService` that sent the command. This downlink is persistent and is then used to receive changes in the `latest` lane of this `UnitService` instance

## Egress by Data Sources
 We implement the `SwimClient` strategy as before to send messages to the `UnitService`. Here we send [messages](https://github.com/swimit/swim-academy/blob/master/joins/services/src/test/java/ai/swim/client/Client.java#L53-L65) to the `addInfo` and `addLatest` lanes of 3 `UnitServices` whose `uris` are `unit/1`, `unit/2` and `unit/3`. 
 
 The remaining code in the `Client` simply opens [subscriptions](https://github.com/swimit/swim-academy/blob/master/joins/services/src/test/java/ai/swim/client/Client.java#L26-L49) to the `allLatest` and `latestOdd` lanes of `JoinService` whose `uri` is `join\all`.
 
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

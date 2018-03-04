# Getting Started

Follow the [Installation](#installation) instructions below to create your environment for developing SWIM applications.

Refer to the [SWIM Academy Wiki](https://github.com/swimit/swim-academy/wiki) for an overview of SWIM concepts.

We highly recommend that you go through at LEAST the [basics](basics/services) tutorial to see how these concepts manifest themselves in a real SWIM application.

# Installation

## Prerequisites

* Install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Ensure that your JAVA_HOME environment variable is pointed to the Java 8 installation location. Ensure that your PATH includes $JAVA_HOME.

* Install [Gradle](https://gradle.org/install/). Ensure that your PATH includes the Gradle `bin` directory.

* Refer to the sample [build.gradle](basics/services/build.gradle) for reference. You may use this build.gradle for all your applications.

* To build the application execute the command `gradle build` from a shell pointed to the application's home directory. This will create a `.zip` and a `.tar` in `APP_HOME/build/distributions` directory. Unpackage the `.zip` or the `.tar` file. The unpackaged contents will contain a `bin/` directory which contains scripts that can be used to run the application.
    ```console
    user@machine:~$ gradle build
    ```
  

* Alternatively, if you don't mind running through the Gradle VM, execute the command `gradle run`. This will both build and run the application.
    ```console
    user@machine:~$ gradle run
    ```


# Creating Your Own SWIM Application

SWIM is an eventually consistent, real-time, distributed object system. The building blocks of a SWIM server are `Services`, `Lanes`, `Links`, and a single `Plane`, where

* `Services` are objects
* `Lanes` are the fields and methods, of `Services`
* `Links` are references to `Lanes` in `Services`
* The `Plane` is a collection of `Service` definitions.

Public `Services` and `Lanes` form a SWIM API (streaming API over web-sockets).

We again refer you to the [SWIM Academy Wiki](https://github.com/swimit/swim-academy/wiki) for a detailed overview of these SWIM concepts.
 
There are just three steps to build a SWIM Application.

1. Write SWIM `Services` with appropriate `Lane` declarations and configurations
2. Define a `Plane` with all `ServiceType` fields appropriately declared and all desired configurations loaded
3. Ingest data into `Lanes` using `commands` or `Downlinks`, via either a `SwimClient` instance or an external program

That's it! `Services` spawn lazily when a URI associated with a `Service` or `Lane` instance is invoked for the first time, so SWIM `Services` will process data immediately upon its availability without requiring their explicit instantiation.

Visit the following tutorials to see concrete applications built through these steps.

* [Basics](https://github.com/swimit/swim-academy/blob/master/basics/services)
* [Joins](https://github.com/swimit/swim-academy/blob/master/joins/services)

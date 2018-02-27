# Introduction
Swim is an eventually consistent, real-time distributed object system. The building blocks of SWIM are Services, Lanes, Links & Plane

* Services are objects
* Lanes are members of Services (properties and methods)
* Links are references to Lanes in a Service
* Public Services and Lanes are a Swim API (streaming API over web-sockets)
* Plane is a collection of Service definitions 

# Swim Concepts

## Services
Swim Services are distributed objects. A Service has the following features
 
* Stores state (data) using Lanes 
* Has execution code (methods)
* Is Thread-safe
* Can dynamically change type; implement many types (dynamic polymorphism) 
* Has a persistent universal address expressed as a URI
  
## Lanes
Lanes are members of Services. A Lane has the following features

* Contains data and/or execution code (methods)
* Has a relative URI which is resolved into a fully qualified URI by pre-pending the URI of it's parent Service
* Accessible via Links from within a Service and outside a Service
* Is persistent by default i.e the data is retained when an application/service restarts. They can be configured to be 
  transient (non-persistent) in which case the data is kept in-memory.
* Backed internally using [Recon](https://github.com/swimit/recon-java)  
* Is a parameterized class. Any class type can be specified provided there is a Recon transformation from/to that class.   

The most prominent lane types are

1. ValueLane: Stores a single item and is accessed with a ValueDownLink. Provides call-back functions to execute code 
               when it is updated.
2. MapLane: Stores a key-value map and is accessed with MapDownlink. Provides call-back functions to execute code 
            when it is updated.
3. CommandLane: A stateless lane for taking action and invoked with commands. Provides call-back functions to execute code 
                (take action). 
4. JoinLane: Aggregates multiple downlinks in a single lane. Automatically relink to its aggregated lanes if a service
             restarts.
              
## Links
A Link is a subscription to a Lane of some Service instance. A Link has the following features

* Is resilient to network congestion and failures 
* Is multiplexed to handle multiple connections to different Lanes in a Service instance over a single connection

The main prominent link types are

1. ValueDownLink: Links to a ValueLane of a Service instance. Provides call-back functions to execute code when the
                  ValueLane gets updated.
2. MapDownLink: Links to a MapLane of a Service instance. Provides call-back functions to execute code when the
                MapLane gets updated.

## Planes
A Plane is used to start a Swim application. A Plane has the following features

* Has a collection of Service URI definitions
* Has application configuration- http/https protocol parameters, port bindings, TLS parameters, store directory to save the lane data
* Starts the Swim application
 
# Wiring it all together
These are the steps to build a Swim Application

1. Write Swim Services with the appropriate lane definitions
2. Write a SwimPlane with the Service URI definitions and the application configuration
3. Ingest data into lanes using commands or downlinks. This can be done by a SwimClient (java) or via an external 
   program (non-Java)  
4. Services are instantiated lazily when a URI associated with a service/lane instance is invoked for the first time.  
    
    
For further reading 
Basics
Joins

    
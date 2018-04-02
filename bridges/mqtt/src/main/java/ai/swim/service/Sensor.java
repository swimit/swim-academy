package ai.swim.service;

import recon.*;
import swim.api.*;

public class Sensor extends AbstractService {

  @SwimLane("fromBroker")
  public CommandLane<Value> fromBroker = commandLane()
     .onCommand(v -> {
       System.out.println(nodeUri().toUri() + " received: " + v.toRecon());
     });
}

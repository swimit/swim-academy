package ai.swim.service;

import recon.Value;
import swim.api.AbstractService;
import swim.api.CommandLane;
import swim.api.SwimLane;

public class Sensor extends AbstractService {

  @SwimLane("fromBroker")
  public CommandLane<Value> fromBroker = commandLane()
     .onCommand(v -> {
       System.out.println(nodeUri().toUri() + " received: " + v.toRecon());
     });

}

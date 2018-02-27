package ai.swim.service;

import recon.Value;
import swim.api.*;

public class UnitService extends AbstractService {

  /**
   * Store info. When the info is received send the uri to the "join/all" JoinService using 'context.command'.
   *
   * context.command can be used for inter-service communication
   */
  @SwimLane("info")
  ValueLane<String> info = valueLane().valueClass(String.class)
      .didSet((newValue, oldValue) -> {
        System.out.println("info lane set with value: " + newValue);
        context.command("/join/all", "unit/add", Value.of(nodeUri()));
      });

  @SwimLane("addInfo")
  CommandLane<String> addInfo = commandLane().valueClass(String.class)
      .onCommand(i -> {
        System.out.println("addInfo lane received value: " + i);
        // set the info lane
        info.set(i);
      });

  /**
   * Latest stream data
   */
  @SwimLane("latest")
  ValueLane<Integer> latest = valueLane().valueClass(Integer.class)
      .didSet((newValue, oldValue) -> {
        System.out.println("latest lane set with value: " + newValue);
      });


  @SwimLane("addLatest")
  CommandLane<Integer> addLatest = commandLane().valueClass(Integer.class)
      .onCommand(i -> {
        System.out.println("addLatest lane received value: " + i);
        // set the latest lane with the integer that was passed in
        latest.set(i);
      });

}


package ai.swim.service;

import swim.api.*;
import swim.util.Uri;

public class JoinService extends AbstractService {

  /**
   * Command lane that receives a message with the uri of an Unit service instance.
   * Invoke the downLink method on the joinValueLane with the appropriate node and lane uri
   */
  @SwimLane("unit/add")
  public CommandLane<Uri> addUnit = commandLane().valueClass(Uri.class)
      .onCommand(v -> {
        System.out.println("unit/add lane received value: " + v.toUri());
        this.joinLatest.downlink(v) // v has to be of the same type as the first param of the JoinValueLane
            .nodeUri(v) // the uri of the Unit service instance passed to the command lane
            .laneUri("latest") // name of the lane in the Unit service instance
            .open();
      });

  /**
   * Use a JoinValueLane to link to a service's Value lane to receives the current state of the value lane and all
   * subsequent changes to the value lane. JoinValueLanes are parameterized classes whose second parameter's type should
   * match the type of the lane that is being linked to.
   *
   * In this case the latest lane's type is Integer, hence the 2nd parameter's type is Integer
   *
   * didUpdate is called when a specific Unit service's latest lane is updated
   */
  @SwimLane("joinLatest")
  public JoinValueLane<Uri, Integer> joinLatest = joinValueLane().keyClass(Uri.class).valueClass(Integer.class)
      .didUpdate((key, newValue, oldValue) -> {
        // update the allLatest lane with the latest value
        this.allLatest.put(key, newValue);

        // update the latestOdd lane when the latest value is odd, otherwise remove the entry from the latestOdd lane
        if (newValue %2 == 0)  {
          this.latestOdd.remove(key);
        } else {
          this.latestOdd.put(key, newValue);
        }
      });

  /**
   * Map lane that keeps the latest data of all the instances of the Unit service. The key is the uri of the Unit
   * service instance
   */
  @SwimLane("allLatest")
  MapLane<Uri, Integer> allLatest = mapLane().keyClass(Uri.class).valueClass(Integer.class)
      .didUpdate((key, newValue, oldValue) -> {
        System.out.println("allLatest lane updated (key, value): (" + key + "," + newValue + ")");
      });

  /**
   * Map lane that keeps the latest data of all the instances of the Unit service provided the latest value is odd
   * (The logic for this computation is performed in the didUpdate callback of the joinLatest lane)
   *
   * The key is the uri of the Unit service instance
   *
   */
  @SwimLane("latestOdd")
  MapLane<Uri, Integer> latestOdd = mapLane().keyClass(Uri.class).valueClass(Integer.class)
      .didUpdate((key, newValue, oldValue) -> {
        System.out.println("latestOdd lane updated (key, value): (" + key + "," + newValue + ")");
      });
}


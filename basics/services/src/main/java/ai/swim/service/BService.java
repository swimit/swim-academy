package ai.swim.service;

import ai.swim.model.ModelB;
import swim.api.*;

public class BService extends AbstractService {

  /**
   * Use a value lane to store a single data item, the class type of the item needs to be specified
   * In this case store value lane is of type ModelB
   * <p>
   * didSet is called when the ValueLane gets updated with a new value
   */
  @SwimLane("latest")
  ValueLane<ModelB> latest = valueLane().valueClass(ModelB.class)
      .didSet((newValue, oldValue) -> {
        System.out.println("latest lane set with value: " + newValue);

        // Update the history lane when this lane gets updated
        this.history.put(System.currentTimeMillis(), newValue);
      });

  /**
   * Use a map lane to store a keyed collection of data items of a specific type. The class type of the key and the data item needs to be specified
   * In this case store the key to the map lane is of type Long and the value of the map lane is of type ModelB
   * <p>
   * This map lane keeps only 5 elements, so on each update drop the all elements but last 5 by using the drop function
   */
  @SwimLane("history")
  MapLane<Long, ModelB> history = mapLane().keyClass(Long.class).valueClass(ModelB.class)
      .didUpdate((key, newValue, oldValue) -> {
        System.out.println("history lane updated (key, value): (" + key + "," + newValue + ")");
        if (this.history.size() > 5) {
          this.history.drop(this.history.size() - 5);
        }
      });

  /**
   * Use a command lane to ingest data from an external client, the class type of the data item needs to be specified
   * In this case the command lane is of type ModelB
   */
  @SwimLane("addLatest")
  CommandLane<ModelB> addLatest = commandLane().valueClass(ModelB.class)
      .onCommand(i -> {
        System.out.println("addLatest lane received value: " + i);
        // set the latest lane with the integer that was passed in
        latest.set(i);
      });

}

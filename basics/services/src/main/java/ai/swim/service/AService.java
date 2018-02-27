package ai.swim.service;

import swim.api.*;

public class AService extends AbstractService {

  /**
   * Use a value lane to store a single data item, the class type of the item needs to be specified
   * In this case store value lane is of type Integer
   *
   * didSet is called when the ValueLane gets updated with a new value
   */
  @SwimLane("latest")
  ValueLane<Integer> latest = valueLane().valueClass(Integer.class)
      .didSet((newValue, oldValue) -> {
        System.out.println("latest lane set with value: " + newValue);
      });

  /**
   * Use a map lane to store a keyed collection of data items of a specific type. The class type of the key and the
   * data item needs to be specified
   *
   * In this case store the key to the map lane is of type Long and the value of the map lane is of type Integer
   *
   * didUpdate is called when the MapLane gets updated
   */
  @SwimLane("history")
  MapLane<Long, Integer> history = mapLane().keyClass(Long.class).valueClass(Integer.class)
      .didUpdate((key, newValue, oldValue) -> {
        System.out.println("history lane updated (key, value): (" + key + "," + newValue + ")");
      });

  /**
   * Use a command lane to ingest data from an external client, the class type of the data item needs to be specified
   * In this case the command lane is of type Integer
   */
  @SwimLane("addLatest")
  CommandLane<Integer> addLatest = commandLane().valueClass(Integer.class)
      .onCommand(i -> {
        System.out.println("addLatest lane received value: " + i);
        // set the latest lane with the integer that was passed in
        latest.set(i);
        final long now = System.currentTimeMillis();
        history.put(now, i);
      });

}

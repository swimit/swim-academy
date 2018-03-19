package ai.swim.service;

import recon.*;
import swim.api.*;

public class FileService extends AbstractService {

  @SwimLane("avgAndCount")
  private MapLane<Value, Value> avgAndCount = mapLane()
    .didUpdate((k,n,o) -> {
      System.out.println(nodeUri().toUri() + " k: " + k.toRecon() + ", n:" + n.toRecon());
  });

  @SwimLane("addLatest")
  private CommandLane<Value> addLatest = commandLane()
    .onCommand(v -> {
      // Precondition: every Item in v is maps a String to a Number
      System.out.println("addlatest: " + v.toRecon());
      // For a v that looks like {length:21,wingspan:28}...
      v.asRecord().stream().map(Item::asField).forEach(f -> {
        // ...f looks like one of:
        // length:21
        // wingspan:28
        Value val = avgAndCount.get(f.getKey());
        if (val.isAbsent()) { // i.e. if no prior entries for this field name
          avgAndCount.put(f.getKey(), Record.of().slot("avg", f.getValue().doubleValue()).slot("count", 1));
        } else {
          // Compute a moving average
          final int count = val.get("count").intValue();
          avgAndCount.put(f.getKey(), Record.of()
            .slot("avg", (val.get("avg").doubleValue() * count + f.getValue().doubleValue()) / (count + 1))
            .slot("count", count+1)
          );
        }
      });
    });
}
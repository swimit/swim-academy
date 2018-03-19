package ai.swim.service;

import recon.*;
import swim.api.*;

public class ColumnService extends AbstractService {

  @SwimLane("history")
  private MapLane<Long, Value> history = mapLane()
    .keyForm(Form.LONG)
    .didUpdate((k,n,o) -> System.out.println(nodeUri().toUri() + " k: " + k + ", n: " + n.toRecon()));

  @SwimLane("latest")
  private ValueLane<Value> latest = valueLane()
    .didSet((n,o) -> {
      history.put(System.currentTimeMillis(), n);
    });

  @SwimLane("addLatest")
  private CommandLane<Value> addLatest = commandLane()
    .onCommand(v -> {
      latest.set(v);
    });
}
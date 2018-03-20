package ai.swim.service;

import recon.*;
import swim.api.*;

public class Stock extends AbstractService {

  @SwimLane("history")
  private MapLane<Long, Value> history = mapLane().keyForm(Form.LONG)
    .didUpdate((k,n,o) -> {
      System.out.println(prop("title").toRecon() + " " + k + ": " + n.toRecon());
    });

  @SwimLane("latest")
  private ValueLane<Value> latest = valueLane()
    .didSet((n,o) -> history.put(System.currentTimeMillis(), n));

  @SwimLane("addLatest")
  public CommandLane<Value> addLatest = commandLane()
    .onCommand(v -> {
      latest.set(v);
    });
}

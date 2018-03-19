package ai.swim.service;

import ai.swim.util.HttpRequester;
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

  private void poll() {
    final Value v = HttpRequester.poll(prop("title").toRecon());
    if (v != null) latest.set(v);
    schedulePoll();
  }

  private void schedulePoll() {
    setTimer(1000, () -> poll());
  }

  @Override
  public void didStart() {
    schedulePoll();
  }
}

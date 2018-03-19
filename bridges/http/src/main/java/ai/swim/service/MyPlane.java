package ai.swim.service;

import swim.api.*;

public class MyPlane extends AbstractPlane {

  @SwimRoute("/stock/:title")
  public final ServiceType<?> stockService = serviceClass(Stock.class);
}

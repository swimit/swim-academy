package ai.swim.service;

import swim.api.*;

public class MyPlane extends AbstractPlane {

  @SwimRoute("/sensor/:id")
  private ServiceType<?> sensor = serviceClass(Sensor.class);
}

package ai.swim.service;

import swim.api.*;

public class MyPlane extends AbstractPlane {

  @SwimRoute("/file/:filename")
  public final ServiceType<?> myService1 = serviceClass(FileService.class);

  @SwimRoute("/column/:field")
  public final ServiceType<?> myService2 = serviceClass(ColumnService.class);

  @SwimRoute("/combo/:filename/:field")
  public final ServiceType<?> myService3 = serviceClass(ComboService.class);
}
package ai.swim;

import ai.swim.service.JoinService;
import ai.swim.service.UnitService;
import swim.api.AbstractPlane;
import swim.api.ServiceType;
import swim.api.SwimRoute;
import swim.server.SwimPlane;
import swim.server.SwimServer;

public class App extends AbstractPlane {

  @SwimRoute("/unit/:id")
  final ServiceType<?> UnitService = serviceClass(UnitService.class);

  @SwimRoute("/join/:id")
  final ServiceType<?> JoinService = serviceClass(JoinService.class);

  public static void main(String[] args) {
    final SwimServer server = new SwimServer();
    final SwimPlane plane = server.materializePlane("plane", App.class);
    plane.bind("0.0.0.0", 9001);
    System.out.println("Listening on port 9001");

    // Run the swim server, this stays alive until termination
    server.run();
  }
}

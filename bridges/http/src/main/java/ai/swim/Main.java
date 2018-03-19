package ai.swim;

import ai.swim.service.MyPlane;
import recon.Value;
import swim.server.*;

public class Main {

  private static SwimPlane startServer() {
    final SwimServer server = new SwimServer();
    final SwimPlane plane = server.materializePlane("plane", MyPlane.class);
    final int port = Integer.parseInt(System.getProperty("port", "5620"));
    plane.bind("0.0.0.0", port);
    System.out.println("Listening on port " + port);
    server.run();
    return plane;
  }

  private static void startService(SwimPlane plane, String title) {
    plane.command("/stock/"+title, "unused", Value.ABSENT);
  }

  public static void main(String[] args) {
    final SwimPlane plane = startServer();
    startService(plane, "Microsoft");
    startService(plane, "Google");
  }
}

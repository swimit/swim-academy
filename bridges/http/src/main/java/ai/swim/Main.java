package ai.swim;

import ai.swim.service.MyPlane;
import ai.swim.util.HttpRequester;
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

  private static void sendData(SwimPlane sp) {
    final String[] stocks = new String[]{"Microsoft", "Google", "Netflix"};
    new HttpRequester(sp, stocks).relayExternalData();
  }

  public static void main(String[] args) {
    final SwimPlane plane = startServer();
    sendData(plane);
  }
}

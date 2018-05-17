package ai.swim;

import ai.swim.service.MyPlane;
import ai.swim.util.CSVUtil;
import swim.server.*;

import java.io.*;

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

  private static void sendData(SwimPlane sp) throws IOException, InterruptedException {
    new CSVUtil(sp, System.getProperty("path", "/test.csv"))
      .relayExternalData();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    final SwimPlane plane = startServer();
    sendData(plane);
  }
}
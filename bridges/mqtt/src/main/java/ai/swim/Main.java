package ai.swim;

import ai.swim.mqtt.*;
import ai.swim.service.MyPlane;
import org.eclipse.paho.client.mqttv3.*;
import swim.server.*;

public final class Main {

  private Main() { }

  static SwimServer server;
  static SwimPlane plane;

  private static void startServer() {
    server = new SwimServer();
    plane = server.materializePlane("plane", MyPlane.class);
    final int port = Integer.parseInt(System.getProperty("port", "5620"));
    plane.bind("0.0.0.0", port);
    System.out.println("Listening on port " + port);
    server.run();
  }

  private static void sendData() throws MqttException, InterruptedException {

    // Install Mosquitto, or change `localhost` to one of these:
    //    https://github.com/mqtt/mqtt.github.io/wiki/public_brokers
    final String broker = "tcp://localhost:1883";

    // In a separate Thread, synchronously simulate Sensor output to MQTT broker
    final Runnable simulator = new Runnable() {
      final Sensors s = new Sensors(broker);
      @Override
      public void run() {
        s.simulate();
      }
    };
    new Thread(simulator).start();

    // Asynchronously send MQTT broker updates to SWIM services
    Subscriber.listen(broker, plane);
  }

  public static void main(String[] args) throws MqttException, InterruptedException {
    startServer();
    sendData();
  }
}
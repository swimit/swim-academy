package ai.swim;

import ai.swim.mqtt.Sensors;
import ai.swim.mqtt.Subscriber;
import ai.swim.service.MyPlane;
import org.eclipse.paho.client.mqttv3.*;
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

  private static void sendData(SwimPlane sp) throws MqttException, InterruptedException {

    final String broker = "tcp://localhost:1883";

    // In a Thread, synchronously simulate Sensor output to MQTT broker
    final Runnable simulator = new Runnable() {
      final Sensors s = new Sensors(broker);
      @Override
      public void run() {
        s.simulate();
      }
    };
    new Thread(simulator).start();

    // Asynchronously send MQTT broker updates to SWIM services
    new Subscriber("tcp://localhost:1883",
      new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
          System.err.println("connection lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
          sp.command("/"+topic, "fromBroker", Value.of(new String(message.getPayload())));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          System.err.println("delivery complete");
        }
      }
    );
  }

  public static void main(String[] args) throws MqttException, InterruptedException {
    final SwimPlane plane = startServer();
    sendData(plane);
  }
}
package ai.swim.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import recon.Value;
import swim.server.SwimPlane;

public final class Subscriber {

  private Subscriber() { }

  public static void listen(String broker, SwimPlane sp) throws MqttException, InterruptedException {
    MqttAsyncClient client = new MqttAsyncClient(broker, "Listener");
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);

    client.setCallback(new MqttCallback() {
      @Override
      public void connectionLost(Throwable cause) {
        System.err.println("connection lost");
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) {
        // Route every MQTT broker update to the right `Service`
        sp.command("/"+topic, "fromBroker", Value.of(new String(message.getPayload())));
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
        System.err.println("delivery complete");
      }
    });
    client.connect(connOpts);
    System.out.println("Subscriber connected");
    // Let the network stack stabilize
    Thread.sleep(1000);
    // Listen to sensor/1, sensor/2, etc
    client.subscribe("sensor/#", 1);
    System.out.println("Subscribed");
  }
}

package ai.swim.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import recon.Record;

public final class Sensors {

  private final MqttClient client;

  public Sensors(String broker) throws MqttException {
    client = new MqttClient(broker, "Writer");
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    System.out.println("Connecting to broker: "+broker);
    client.connect(connOpts);
    System.out.println("Sensors connected");
  }

  public void simulate() {
    final int qos = 1;
    while (true) {
      for (int i = 1; i <= 10; i++) {
        final String topic = "sensor/" + i;
        final String content = Record.of().attr("sensor", i)
          .slot("t", System.currentTimeMillis())
          .slot("temp", ((int) (Math.random() * 15) + 65))
          .slot("humidity", Math.random() / 5.0)
          .toRecon();
        System.out.println("Publishing: " + content);
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        try {
          client.publish(topic, message);
          Thread.sleep(100);
        } catch (MqttException | InterruptedException e) {
          System.out.println("Publication error");
          e.printStackTrace();
          return;
        }
      }
    }
  }
}

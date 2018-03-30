package ai.swim.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public final class Subscriber {

  private final MqttAsyncClient client;

  public Subscriber(String broker, MqttCallback cb) throws MqttException, InterruptedException {
    client = new MqttAsyncClient(broker, "Listener");
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    client.setCallback(cb);
    client.connect(connOpts);
    System.out.println("Connected");
    Thread.sleep(1000);
    client.subscribe("sensor/#", 2);
    System.out.println("Subscribed");
  }

}

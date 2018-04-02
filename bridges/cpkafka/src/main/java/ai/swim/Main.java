package ai.swim;

import ai.swim.kafka.SwimProducer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import ai.swim.service.MyPlane;
import org.apache.kafka.common.serialization.StringDeserializer;
import recon.Value;
import swim.server.*;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


public class Main {

  public final static String TOPIC = "swim-test";
  public final static String BOOTSTRAP_SERVERS = "localhost:9092";

  private static SwimPlane startServer() {
    final SwimServer server = new SwimServer();
    final SwimPlane plane = server.materializePlane("plane", MyPlane.class);
    final int port = Integer.parseInt(System.getProperty("port", "5620"));
    plane.bind("0.0.0.0", port);
    System.out.println("Listening on port " + port);
    server.run();
    return plane;
  }

  private static Consumer<String, String> getConsumer(String bootstrapServers, String topic) {
    // Asynchronously send kafka broker updates to SWIM services
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    final Consumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList(TOPIC));
    return consumer;
  }

  private static void sendData(SwimPlane sp) throws InterruptedException {

    // In a Thread, synchronously simulate Sensor output to kafka broker
    final Runnable simulator = new Runnable() {
      final SwimProducer s = new SwimProducer(BOOTSTRAP_SERVERS, TOPIC);

      @Override
      public void run() {
        s.simulate();
      }
    };
    new Thread(simulator).start();

    // In a Thread, poll the above broker and relay any messages to SWIM
    final Runnable ingest = new Runnable() {
      final Consumer<String, String> consumer = getConsumer(BOOTSTRAP_SERVERS, TOPIC);

      @Override
      public void run() {
        CompletableFuture.runAsync(() -> {
          try {
            while (true) {
              final ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
              consumerRecords.forEach(record -> {
                  System.out.printf("Consumed Record:(%s, %s, %d, %d)\n",
                    record.key(), record.value(), record.partition(), record.offset()
                  );
                  sp.command("/"+TOPIC, "fromBroker", Value.of(record.value()));
                });
              consumer.commitAsync();
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            // Ignore
          }
        });
      }
    };
    new Thread(ingest).start();
  }

  public static void main(String[] args) throws InterruptedException {
    final SwimPlane plane = startServer();
    sendData(plane);
  }
}
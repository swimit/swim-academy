package ai.swim.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import recon.Record;

import java.util.Properties;
import java.util.Random;

public class SwimProducer {
  private Producer<String, String> producer = null;
  public final String topic;
  public final String bootstrapServers;

  public SwimProducer(String bootstrapServers, String topic) {
    this.bootstrapServers = bootstrapServers;
    this.topic = topic;
    producer = createProducer();
  }

  private Producer<String, String> createProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "SwimExampleKafkaProducer");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  public void simulate() {
    Random rnd = new Random();

    while (true) {
      for (int i = 1; i <= 10; i++) {
        final String topic = this.topic;
        final String content = Record.of().attr("sensor", i)
                .slot("t", System.currentTimeMillis())
                .slot("temp", ((int) (Math.random() * 15) + 65))
                .slot("humidity", Math.random() / 5.0)
                .toRecon();
        System.out.println("Publishing: " + content);
        String ip = "192.168.2." + rnd.nextInt(255);
        try {

          producer.send(new ProducerRecord<>(topic, ip, content), (recordMetadata,e) ->{});
          Thread.sleep(100);
        } catch (InterruptedException e) {
          System.out.println("Publication error");
          System.exit(1);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          if (producer != null) {
            try {
//              producer.flush();
//              producer.close();
            } catch (Exception e) {
              // Ignore
            }
          }
        }
      }
    }
  }
}
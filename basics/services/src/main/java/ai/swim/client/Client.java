package ai.swim.client;

import ai.swim.model.ModelB;
import recon.Value;
import swim.client.SwimClient;

/**
 * Send commands to an instance of service A (uri: a/1) and an instance of service B (b/1)
 * Subscribe to value and map lanes of service a/1 and service b/1 to
 */
public class Client {

  public static void main(String[] args) throws Exception {
    // start the swim client
    final SwimClient sc = new SwimClient();
    sc.start();
    final String host = "ws://localhost:9001";
    runAService(sc, host);
    //runBService(sc, host);
  }

  private static void runAService(SwimClient sc, String host) throws InterruptedException {
    // Instantiate Serivce 'AService' with id as 1
    final String nodeUri = "a/1";

    // link to latest lane of service with uri /a/1
    // didReceive call back function receives the current state of the value lane and all subsequent changes to the value lane
    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("latest")
        .downlinkValue()
        .keepSynced(true)
        .open()
        .didReceive(value -> {
          System.out.println("Received latest value " + value.toRecon()); //.toRecon returns the recon representation of the value
        });

    // link to history lane of service with uri /a/1
    // didUpdate call back function receives the current state of the map and  all subsequent changes to the map lane
    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("history")
        .downlinkMap()
        .keepSynced(true)
        .open()
        .didUpdate((key, newValue, oldValue) -> {
          System.out.println("Updated history (key, value): (" + key.toRecon() + ":" + newValue.toRecon() + ")");
        });

    // send message to the addLatest command lane to an instance of service AService i.e. with id 1
    // since the command lane expects an integer, convert the integer to Value type
    for(int i = 0; i <=10; i++) {
      sc.command(host, nodeUri, "addLatest", Value.of(i));
      Thread.sleep(1000l);
    }
  }

  private static void runBService(SwimClient sc, String host) throws InterruptedException {
    // Instantiate Serivce 'BService' with id as 1
    final String nodeUri = "b/1";

    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("latest")
        .downlinkValue()
        .keepSynced(true)
        .open()
        .didReceive(value -> {
          System.out.println("Received latest value " + value.toRecon()); //.toRecon returns the recon representation of the value
        });

    // link to history lane of service with uri b/1
    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("history")
        .downlinkMap()
        .keepSynced(true)
        .open()
        .didUpdate((key, newValue, oldValue) -> {
          System.out.println("Updated history (key, value): (" + key.toRecon() + ":" + newValue.toRecon() + ")");
        })
        .didDrop(d -> {
          System.out.println("Dropped from history element count:" + d);
        });

    // send message to the addLatest command lane to an instance of service AService i.e. with id 1
    // since the command lane expects type ModelB, convert the instance of Model BService to a Recon Value type
    for(int i = 0; i <=10; i++) {
      ModelB b = new ModelB(i%2 ==0, Integer.toString(i), i, (long) i, (float) i, (double) i);
      sc.command(host, nodeUri, "addLatest", b.toValue());
      Thread.sleep(1000l);
    }
  }
}

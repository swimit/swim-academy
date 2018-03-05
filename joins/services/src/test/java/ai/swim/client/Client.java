package ai.swim.client;

import recon.Value;
import swim.api.MapDownlink;
import swim.client.SwimClient;

/**
 * Create 3 Unit service instances (unit/1, unit/2 and unit/3)
 * Subscribe to the allLatest and latestOdd lanes of the Join Service instance (join/1)
 */
public class Client {

  public static void main(String[] args) throws Exception {
    // start the swim client
    final SwimClient sc = new SwimClient();
    sc.start();
    final String host = "ws://localhost:9002";

    runJoinService(sc, host);
    runUnitService(sc, host);
  }

  private static void runJoinService(SwimClient sc, String host) throws InterruptedException {
    final String nodeUri = "join/all";

    // link to allLatest lane of service with uri join/all
    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("allLatest")
        .downlinkMap()
        .keepSynced(true)
        .open()
        .didUpdate((key, newValue, oldValue) -> {
          System.out.println("Updated allLatest (key, value): (" + key.toRecon() + ":" + newValue.toRecon() + ")");
        });

    // link to latestOdd lane of service with uri join/all
    sc.hostRef(host)
        .nodeRef(nodeUri)
        .laneRef("latestOdd")
        .downlinkMap()
        .keepSynced(true)
        .open()
        .didUpdate((key, newValue, oldValue) -> {
          System.out.println("Updated latestOdd (key, value): (" + key.toRecon() + ":" + newValue.toRecon() + ")");
        })
        .didRemove((key, value) -> {
          System.out.println("Removed from latestOdd key: " + key.toRecon());
        });
  }

  private static void runUnitService(SwimClient sc, String host) throws InterruptedException {
    final String unit1 = "unit/1";
    final String unit2 = "unit/2";
    final String unit3 = "unit/3";

    sc.command(host, unit1, "addInfo", Value.of("unit1 info"));
    sc.command(host, unit2, "addInfo", Value.of("unit2 info"));
    sc.command(host, unit3, "addInfo", Value.of("unit3 info"));

    for(int i = 1; i <=10; i++) {
      sc.command(host, unit1, "addLatest", Value.of(i));
      sc.command(host, unit2, "addLatest", Value.of(i));
      sc.command(host, unit3, "addLatest", Value.of(i));
      Thread.sleep(1000l);
    }
  }
}

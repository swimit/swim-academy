package ai.swim.util;

import recon.*;
import swim.server.SwimPlane;

import java.io.*;

public class CSVUtil {

  final SwimPlane sp;
  final String path;

  public CSVUtil(SwimPlane sp, String path) {
    this.sp = sp;
    this.path = path;
  }

  private static void sendReconToSwim1(Record rec, String fileName, SwimPlane sp) throws IOException {
    final String node = "/file/" + fileName;
    final String lane = "addLatest";
    final Record newRecord = Record.of();
    // Only send over the entries containing numeric fields
    rec.stream().map(Item::asField).forEach(f -> {
      try {
        newRecord.slot(f.getKey(), Value.of(f.getValue().numberValue()));
      } catch (NumberFormatException swallowed) {}
    });
    if (!newRecord.isEmpty()) sp.command(node, lane, newRecord);
  }

  private static void sendReconToSwim2(Record rec, String fileName, SwimPlane sp) throws IOException {
    for (Item i : rec) {
      final String node = "/column/" + i.getKey().toRecon();
      final String lane = "addLatest";
      sp.command(node, lane,
        Record.of().slot("name", fileName).slot("val", i.getValue().asValue()));
    }
  }

  private static void sendReconToSwim3(Record rec, String fileName, SwimPlane sp) throws IOException {
    for (Item i : rec) {
      final String node = "/combo/" + fileName + "/" + i.getKey().toRecon();
      final String lane = "addLatest";
      sp.command(node, lane, i.getValue().asValue());
    }
  }

  public void relayExternalData() throws IOException, InterruptedException {
    final String fileName = new File(path).getName();
    BufferedReader br = new BufferedReader(new FileReader(path));
    // Cache the header
    final String[] fields = br.readLine().split(",");
    String row;
    // For each line...
    while ((row = br.readLine()) != null) {
      // ...for each entry `e_i` corresponding to field name `f_i`...
      final String[] entries = row.split(",");
      final Record val = Record.of();
      // ...add a new slot to an initially empty Record of the form (`f_i`, `e_i`)
      for (int i = 0; i < fields.length; i++) {
        val.slot(fields[i], entries[i]);
      }
      // Blackbox to send this Value to the right SWIM Service instances, example implementations below
      sendReconToSwim1(val, fileName, sp);
      sendReconToSwim2(val, fileName, sp);
      sendReconToSwim3(val, fileName, sp);
      // Throttle if desired
      Thread.sleep(1000);
    }
  }
}

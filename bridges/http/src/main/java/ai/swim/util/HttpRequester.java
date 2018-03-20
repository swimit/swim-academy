package ai.swim.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.*;

import recon.*;
import swim.server.SwimPlane;

public final class HttpRequester {

  private final SwimPlane sp;
  private final String[] titles;
  private ScheduledExecutorService executor;

  public HttpRequester(SwimPlane sp, String[] titles) {
    this.sp = sp;
    this.titles = titles;
    this.executor = Executors.newScheduledThreadPool(Math.min(titles.length, 4));
  }

  public void sendToSwim() {
    for (String title: titles) {
      final Runnable r = () -> {
        try {
          URL url = new URL(String.format(
            "http://stockmarket.streamdata.io/v2/prices?ignored=%s", title));
          final Record stocks = parseResponse(url).asRecord();
          for (Item stock : stocks) {
            if (stock.get("title").stringValue("").equals(title)) {
              sp.command("/stock/"+title, "addLatest", stock.asValue());
            }
          }
        } catch (Exception e) { }
      };
      executor.scheduleAtFixedRate(r, 100, 2000, TimeUnit.MILLISECONDS);
    }
  }

  private static Value parseResponse(URL url) {
    HttpURLConnection urlConnection;
    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      return Value.parseJson(new Scanner(urlConnection.getInputStream()).useDelimiter("\\A").next());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
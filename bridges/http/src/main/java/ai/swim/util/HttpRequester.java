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

  // 1. Main entrypoint and perpetual loop.
  public void relayExternalData() {
    for (String title: titles) {
      final Runnable r = () -> {
        try {
          // One would expect that the `title` URL parameter filters the response to
          // contain only the information for `title`. Unfortunately, this is not
          // the case. We can still blackbox a `sendToSwim` call, but we'll have to
          // do some Recon processing by hand; see parseResponse() impl
          URL url = new URL(String.format(
            "http://stockmarket.streamdata.io/v2/prices?title=%s", title));
          final Value stocks = parseResponse(url, title);
          sendToSwim(stocks, title);
        } catch (Exception e) { }
      };
      // Do this work in a thread pool
      executor.scheduleAtFixedRate(r, 100, 2000, TimeUnit.MILLISECONDS);
    }
  }

  // 2. JSON -> Recon, then filter Recon
  private static Value parseResponse(URL url, String title) {
    HttpURLConnection urlConnection;
    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.disconnect();
      // Because Recon is a strict superset of JSON, there exists a Value.parseJson()
      // command that returns the deserialized Recon representation of a serialized
      // JSON String.
      final Record bigRecord = Value.parseJson(
        new Scanner(urlConnection.getInputStream()).useDelimiter("\\A").next()
      ).asRecord();
      // If JSON looks like:
      // [{"title":"Microsoft","company":"Microsoft Corporation","last":-70.94},
      //  {"title":"Amazon","company":"Amazon.com Inc","last":-373.31},
      //   ...]
      //
      // then deserialized Recon looks like:
      // Record.item(Record.slot("title","Microsoft").slot("company",...).slot(...))
      //    .item(Record.slot("title",Amazon).slot("company",...).slot(...))
      //    ...
      for (Item individual : bigRecord) {
        // From the above Recon, we only want to return the Item corresponding to
        // the `title` function parameter
        if (individual.get("title").stringValue("").equals(title)) {
          return individual.asValue();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  // 3. SWIM API call
  private void sendToSwim(Value stock, String title) {
    if (stock != null) {
      //           node             lane         value
      sp.command("/stock/"+title, "addLatest", stock.asValue());
    }
  }
}

package ai.swim.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import recon.*;

public final class HttpRequester {

  private HttpRequester() { }

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

  public static Value poll(String parameter) {
    try {
      URL url = new URL(String.format(
        "http://stockmarket.streamdata.io/v2/prices?ignored=%s", parameter));
      final Record stocks = parseResponse(url).asRecord();
      // Had the HTTP server been capable of filtering by our URL parameter,
      //  we wouldn't need this further processing step
      for (Item i : stocks) {
        if (i.get("title").stringValue("").equals(parameter)) {
          return i.asValue();
        }
      }
    } catch (Exception e) { }
    return null;
  }
}
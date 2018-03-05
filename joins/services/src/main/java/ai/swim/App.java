package ai.swim;

import ai.swim.service.JoinService;
import ai.swim.service.UnitService;
import recon.Recon;
import recon.Value;
import swim.api.AbstractPlane;
import swim.api.ServiceType;
import swim.api.SwimRoute;
import swim.server.ServerDef;
import swim.server.SwimServer;
import swim.util.Decodee;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class App extends AbstractPlane {

  @SwimRoute("/unit/:id")
  final ServiceType<?> UnitService = serviceClass(UnitService.class);

  @SwimRoute("/join/:id")
  final ServiceType<?> JoinService = serviceClass(JoinService.class);

  public static void main(String[] args) throws Exception {
    final SwimServer server = new SwimServer();
    server.materialize(loadReconConfig(args));
    System.out.println("Listening on port 9002");

    // Run the swim server, this stays alive until termination
    server.run();
  }

  private static ServerDef loadReconConfig(String[] args) throws IOException {
    return ServerDef.FORM.cast(
        loadRecon(args, "swim.config", "/join-app.recon")
    );
  }

  private static Value loadRecon(String[] args, String property, String defaultPath) throws IOException {
    InputStream input = null;
    Value value;
    try {
      final File file = new File(getConfigPath(args, property, defaultPath));
      if (file.exists()) {
        // 3. followed by the defaultPath argument...
        input = new FileInputStream(file);
      } else {
        // 4. followed by assuming the file is stored as a resource in the .jar
        input = App.class.getResourceAsStream(defaultPath);
      }
      value = Decodee.readUtf8(Recon.FACTORY.blockParser(), input);
    } finally {
      try {
        if (input != null) input.close();
      } catch (Exception ignored) {}
    }
    return value;
  }

  private static String getConfigPath(String[] args, String property, String defaultPath) {
    String configPath;
    if (args.length > 0) {
      // 1. Command-line argument takes highest precedence...
      return args[0];
    } else {
      // 2. followed by a system property...
      configPath = System.getProperty(property);
      return configPath == null ? defaultPath : configPath;
    }
  }

}

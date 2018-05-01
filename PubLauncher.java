import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

public class PubLauncher {

  public VehiclePublisher vp;
  //In the publauncher I initial a vehicle publisher as singleton publisher  multiple threads will write data to the publisher.
  //The properties are loaded from file.
  //I create pubthread for each vehicle the start.
  public PubLauncher() {
    vp = new VehiclePublisher(0);
  }

  public ArrayList<PubThread> PubLancherMain() throws InterruptedException, ParseException {

    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream("pub.properties");

      // load a properties file
      prop.load(input);
      ArrayList<PubThread> pubThreads = new ArrayList<>();
      ArrayList<Route> routes = new ArrayList<>();
      int numberOfRoutes = Integer.parseInt(prop.getProperty("numRoutes"));

      for (int i = 1; i <= numberOfRoutes; i++) {

        String routeName = prop.getProperty("route" + i);
        int numOfStops = Integer.valueOf(prop.getProperty("route" + i + "numStops"));
        double numOfSeconds = Double.valueOf(prop.getProperty("route" + i + "TimeBetweenStops"));

        Route currentRoute = new Route(routeName, numOfStops, numOfSeconds);
        routes.add(currentRoute);
        int numberOfVehicles = Integer.parseInt(prop.getProperty("numVehicles"));
        int numberOfBackupVehicles = Integer.parseInt(prop.getProperty("numInitialBackupVehicles"));

        for (int j = numberOfVehicles + 1; j <= numberOfBackupVehicles + numberOfVehicles; j++) {

          Vehicle currentVehicle = new Vehicle();
          currentVehicle.vId = prop.getProperty("route" + i + "Vehicle" + j);
          currentRoute.addBackupVehicle(currentVehicle);
          System.out.println("Added " + "vehicle" + currentVehicle.vId + " for " + "Route " + routeName);

        }

        for (int j = 1; j <= numberOfVehicles; j++) {

          Vehicle currentVehicle = new Vehicle();
          currentVehicle.vId = prop.getProperty("route" + i + "Vehicle" + j);
          currentRoute.addVehicle(currentVehicle);
          pubThreads.add(new PubThread(currentRoute, currentVehicle, numOfSeconds, vp));
        }

      }

      return pubThreads;

    } catch (IOException ex) {

      ex.printStackTrace();

    } finally {

      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }
}

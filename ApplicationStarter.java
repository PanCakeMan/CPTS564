import java.text.ParseException;
import java.util.ArrayList;

public class ApplicationStarter {
    // This is the main entrance for the publisher which starts all the threads controlled by PubLauncher
	public static void main(String[] args) throws InterruptedException, ParseException {
		
		PubLauncher pl = new PubLauncher();
		System.out.println("Launching publishers...");
		ArrayList<PubThread> threads = pl.PubLancherMain();
		
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).start();
		    System.out.println("Tread " + threads.get(i).getVehicle().vId + " started");
		}
		
		System.out.println("All buses have started. Waiting for them to terminate...");
	}
    
}

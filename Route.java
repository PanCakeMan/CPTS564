import java.util.ArrayList;

public class Route {
    // Data Structure for Routes
	private String routeNumber;
	private int numberOfStops;
	private double numberOfSeconds; // number of seconds
	private ArrayList<Vehicle> vehicles;
	private ArrayList<Vehicle> backupVehicles;

	public Route(String routeNumber, int numberOfStops, double numberOfSeconds) {

		this.routeNumber = routeNumber;
		this.numberOfStops = numberOfStops;
		this.numberOfSeconds = numberOfSeconds;
		this.vehicles = new ArrayList<>();
		this.backupVehicles = new ArrayList<>();

	}

	public ArrayList<Vehicle> getVehicles() {
		return this.vehicles;
	}

	public ArrayList<Vehicle> getBackupVehicles() {
		return this.backupVehicles;
	}

	public String getRouteNumber() {
		return routeNumber;
	}

	public int getNumOfStops() {
		return numberOfStops;
	}

	public double getNumberOfSeconds() {
		return numberOfSeconds;
	}

	public void setRouteNumber(String val) {
		routeNumber = val;
	}

	public void setNumOfStops(int val) {
		numberOfStops = val;
	}

	public void setNumberOfSeconds(double val) {
		numberOfSeconds = val;
	}

	public void addVehicle(Vehicle v) {
		this.vehicles.add(v);
	}

	public void addBackupVehicle(Vehicle v) {
		this.backupVehicles.add(v);
	}

	public void removeVehicle(String vId) {
		for (int i = 0; i < vehicles.size(); i++) {
			if (vehicles.get(i).vId.equals(String.valueOf(vId))) {
				vehicles.remove(i);
			}
		}
	}

	public void removeBackupVehicle(String vId) {
		for (int i = 0; i < backupVehicles.size(); i++) {
			if (backupVehicles.get(i).vId.equals(String.valueOf(vId))) {
				backupVehicles.remove(i);
			}
		}
	}

}

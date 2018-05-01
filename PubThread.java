import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Random;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

public class PubThread extends Thread {
    // In each thread we have vehicle, route and number of second.
	//For each thread we initial the info at first then update along the route
	//Once I update the info , I will write to the publisher.
	private Route route;
	private Vehicle vehicle;
	private double numOfSeconds;
	private VehiclePublisher vp;

	Random rand = new Random();
	RandomSimulator rs = new RandomSimulator();

	public PubThread(Route route, Vehicle vehicle, double numOfSeconds, VehiclePublisher vp) {

		this.route = route;
		this.vehicle = vehicle;
		this.numOfSeconds = numOfSeconds;
		this.vp = vp;

	}

	public void run() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Position currPosition = new Position();
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		currPosition.timestamp = sdf.format(currentTime);
		currPosition.route = this.route.getRouteNumber();
		currPosition.vehicle = this.vehicle.vId;
		currPosition.stopNumber = 1;
		currPosition.timeBetweenStops = route.getNumberOfSeconds();
		currPosition.trafficConditions = "Normal";
		currPosition.numStops = route.getNumOfStops();
		currPosition.fillInRatio = rand.nextInt(100) + 1;
		vehicle.position.copy_from(currPosition);

		try {
			startOperating();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startOperating() throws InterruptedException, ParseException {

		int numOfStops = this.route.getNumOfStops();
		int rounds = 3;

		for (int i = 1; i <= numOfStops * rounds; i++) {
			vp.publisherMain(vehicle);
			if (!vehicle.breakdown.timestamp.equals("")) {
				vehicle.breakdown.clear();
				break;

			}
			if (!vehicle.accident.timestamp.equals("")) {
				vehicle.accident.clear();
			}
			boolean flag = updateVehicle(vehicle, i, vp);
			if (!flag) {
				break;
			}

		}
        this.join();
		return;
	}

	private boolean updateVehicle(Vehicle vehicle, int i, VehiclePublisher vp)
			throws InterruptedException, ParseException {

		int j = i;

		Position currentPosition = vehicle.position;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Timestamp timestamp = new Timestamp(sdf.parse(currentPosition.timestamp).getTime());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp.getTime());
		cal.add(Calendar.SECOND, (int) currentPosition.timeBetweenStops);
		Timestamp later = new Timestamp(cal.getTimeInMillis());
		String newTime = sdf.format(later);
		currentPosition.timestamp = newTime;
		currentPosition.fillInRatio = rand.nextInt(100) + 1;

		if (j % route.getNumOfStops() == 0) {

			currentPosition.stopNumber = route.getNumOfStops();

		} else {

			currentPosition.stopNumber = (j % route.getNumOfStops());

		}
		if (RandomSimulator.IsBreakDown(i)) {
			if (route.getBackupVehicles().size() == 0) {
				System.out.println("No more backup vehicles...");
			}
			route.removeVehicle(vehicle.vId);

			Thread.sleep(1500);
			Vehicle newVehicle = null;
			;
			if (!route.getBackupVehicles().isEmpty()) {
				newVehicle = route.getBackupVehicles().get(0);
			}
			if (newVehicle.equals(null)) {
				return false;
			}
			route.removeBackupVehicle(newVehicle.vId);
			PubThread newPubThread = new PubThread(route, newVehicle, route.getNumberOfSeconds(), vp);
			newPubThread.start();
			Thread.sleep(500);
			route.addBackupVehicle(this.vehicle);

			Breakdown currentBreakdown = vehicle.breakdown;
			currentBreakdown.timestamp = currentPosition.timestamp;
			currentBreakdown.route = route.getRouteNumber();
			currentBreakdown.stopNumber = currentPosition.stopNumber;
			currentBreakdown.vehicle = vehicle.vId;

		}

		if (RandomSimulator.IsAccident(i)) {

			Accident currentAccident = vehicle.accident;

			Timestamp timestamp1 = new Timestamp(sdf.parse(currentPosition.timestamp).getTime());
			Calendar cal1 = Calendar.getInstance();
			cal1.setTimeInMillis(timestamp1.getTime());
			cal1.add(Calendar.SECOND, 10);
			Timestamp later1 = new Timestamp(cal.getTime().getTime());
			String newTime1 = sdf.format(later1);
			currentPosition.timestamp = newTime1;
			currentAccident.timestamp = newTime1;
			currentAccident.stopNumber = currentPosition.stopNumber;
			currentAccident.route = route.getRouteNumber();
			currentAccident.vehicle = vehicle.vId;

		}

		int condition = RandomSimulator.getTrafficCondition(i);
		if (condition == 1) { // Light traffic
			route.setNumberOfSeconds(route.getNumberOfSeconds() * 0.75);
			currentPosition.trafficConditions = "Light";
			currentPosition.timeBetweenStops = route.getNumberOfSeconds() * 0.75;
		}
		if (condition == 2) { //Heavy traffic

			route.setNumberOfSeconds(route.getNumberOfSeconds() * 1.5);
			currentPosition.timeBetweenStops = route.getNumberOfSeconds() * 1.5;
			currentPosition.trafficConditions = "Heavy";
		}
		if (condition == 0) {
			route.setNumberOfSeconds(numOfSeconds);
			currentPosition.trafficConditions = "Normal ";
			currentPosition.timeBetweenStops = numOfSeconds;
		}
		return true;
	}

	public Vehicle getVehicle() {

		return this.vehicle;

	}

}

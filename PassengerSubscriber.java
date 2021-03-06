
/* VehicleSubscriber.java

A publication of data of type Vehicle

This file is derived from code automatically generated by the rtiddsgen
command:

rtiddsgen -language java -example <arch> .idl

Example publication of type Vehicle automatically generated by
'rtiddsgen' To test them follow these steps:

(1) Compile this file and the example subscription.

(2) Start the subscription on the same domain used for RTI Data Distribution
Service with the command
java VehicleSubscriber <domain_id> <sample_count>

(3) Start the publication on the same domain used for RTI Data Distribution
Service with the command
java VehiclePublisher <domain_id> <sample_count>

(4) [Optional] Specify the list of discovery initial peers and
multicast receive addresses via an environment variable or a file
(in the current working directory) called NDDS_DISCOVERY_PEERS.

You can run any number of publishers and subscribers programs, and can
add and remove them dynamically from the domain.

Example:

To run the example application on domain <domain_id>:

Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
Java.

On UNIX systems:
add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
variable

On Windows systems:
add %NDDSHOME%\lib\<arch> to the 'Path' environment variable

Run the Java applications:

java -Djava.ext.dirs=$NDDSHOME/class VehiclePublisher <domain_id>

java -Djava.ext.dirs=$NDDSHOME/class VehicleSubscriber <domain_id>
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class PassengerSubscriber {
  // -----------------------------------------------------------------------
  // Public Methods
  // -----------------------------------------------------------------------

  public static void main(String[] args) {
    // --- Get domain ID --- //
    int domainId = 0;
    if (args.length >= 1) {
      domainId = Integer.valueOf(args[0]).intValue();
    }

    // -- Get max loop count; 0 means infinite loop --- //
    int sampleCount = 0;
    if (args.length >= 2) {
      sampleCount = Integer.valueOf(args[1]).intValue();
    }

    /* Uncomment this to turn on additional logging
    Logger.get_instance().set_verbosity_by_category(
        LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
        LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
    */

    // --- Run --- //
    subscriberMain(domainId, sampleCount);
  }

  // -----------------------------------------------------------------------
  // Private Methods
  // -----------------------------------------------------------------------

  // --- Constructors: -----------------------------------------------------

  private PassengerSubscriber() {
    super();
  }

  // -----------------------------------------------------------------------

  private static void subscriberMain(int domainId, int sampleCount) {

    DomainParticipant participant = null;
    Subscriber subscriber = null;
    Topic topic = null;
    DataReaderListener listener = null;
    VehicleDataReader reader = null;

    try {

      // --- Create participant --- //

      /* To customize participant QoS, use
      the configuration file
      USER_QOS_PROFILES.xml */

      participant = DomainParticipantFactory.TheParticipantFactory.create_participant(domainId,
          DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null /* listener */, StatusKind.STATUS_MASK_NONE);
      if (participant == null) {
        System.err.println("create_participant error\n");
        return;
      }

      // --- Create subscriber --- //

      /* To customize subscriber QoS, use
      the configuration file USER_QOS_PROFILES.xml */

      subscriber = participant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
          StatusKind.STATUS_MASK_NONE);
      if (subscriber == null) {
        System.err.println("create_subscriber error\n");
        return;
      }

      // --- Create topic --- //

      /* Register type before creating topic */
      String typeName = VehicleTypeSupport.get_type_name();
      VehicleTypeSupport.register_type(participant, typeName);

      /* To customize topic QoS, use
      the configuration file USER_QOS_PROFILES.xml */

      topic = participant.create_topic("Example Vehicle", typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
          null /* listener */, StatusKind.STATUS_MASK_NONE);
      if (topic == null) {
        System.err.println("create_topic error\n");
        return;
      }

      // --- Create reader --- //

      listener = new VehicleListener();

      /* To customize data reader QoS, use
      the configuration file USER_QOS_PROFILES.xml */

      reader = (VehicleDataReader) subscriber.create_datareader(topic, Subscriber.DATAREADER_QOS_DEFAULT, listener,
          StatusKind.STATUS_MASK_ALL);
      if (reader == null) {
        System.err.println("create_datareader error\n");
        return;
      }

      // --- Wait for data --- //
      System.out.println("Waiting for buses....");
      for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

        try {
          Thread.sleep(2 * 1000); // in millisec
        } catch (InterruptedException ix) {
          System.err.println("INTERRUPTED");
          break;
        }
      }
    } finally {

      // --- Shutdown --- //

      if (participant != null) {
        participant.delete_contained_entities();

        DomainParticipantFactory.TheParticipantFactory.delete_participant(participant);
      }
      /* RTI Data Distribution Service provides the finalize_instance()
      method for users who want to release memory used by the
      participant factory singleton. Uncomment the following block of
      code for clean destruction of the participant factory
      singleton. */
      //DomainParticipantFactory.finalize_instance();
    }
  }

  // -----------------------------------------------------------------------
  // Private Types
  // -----------------------------------------------------------------------

  // =======================================================================

  private static class VehicleListener extends DataReaderAdapter {

    VehicleSeq _dataSeq = new VehicleSeq();
    SampleInfoSeq _infoSeq = new SampleInfoSeq();
    Passenger p = new Passenger(1, 4, "", "Express2", true);
    // I initial a passenger instance and check if there is a valid way from start to destination.
    public void on_data_available(DataReader reader) {
      VehicleDataReader VehicleReader = (VehicleDataReader) reader;

      try {
        VehicleReader.take(_dataSeq, _infoSeq, ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
            SampleStateKind.ANY_SAMPLE_STATE, ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);

        for (int i = 0; i < _dataSeq.size(); ++i) {
          SampleInfo info = (SampleInfo) _infoSeq.get(i);

          if (info.valid_data) {
            Vehicle currV = (Vehicle) _dataSeq.get(i);

            if (p.needbus) {

              if (currV.breakdown.timestamp.equals("") && currV.position.route.equals(p.route)
                  && currV.position.stopNumber == p.start) {
                p.ride = currV.position.vehicle;

                if (currV.accident.timestamp.equals("")) {
                  System.out.println("Gettinging on " + currV.position.vehicle + " at " + currV.position.timestamp + " "
                      + currV.position.trafficConditions + " " + (p.end - p.start) + " stops left");
                } else {
                  System.out.println("Gettinging on " + currV.position.vehicle + " at " + currV.position.timestamp + " "
                      + currV.position.trafficConditions + " " + "accident " + (p.end - p.start) + " stops left");
                }
                p.needbus = false;
              }

            } else {

              if (currV.position.vehicle.equals(p.ride)) {
                p.start = currV.position.stopNumber;
                if (p.start == p.end) {
                  System.out.println(
                      "Arriving at destination by " + currV.position.vehicle + " at " + currV.position.timestamp);
                  System.exit(0);

                } else if (!currV.breakdown.timestamp.equals("")) {

                  p.ride = "";
                  p.needbus = true;
                  System.out.println("Arriving at " + currV.breakdown.stopNumber + " at " + currV.breakdown.timestamp
                      + ", breakdown " + (p.end - p.start) + " stops left");

                } else if (!currV.accident.timestamp.equals("")) {
                  System.out.println("Arriving at " + currV.accident.stopNumber + " at " + currV.accident.timestamp
                      + ", accident " + (p.end - p.start) + " stops left");

                } else {
                  System.out.println("Arriving at " + currV.position.stopNumber + " at " + currV.position.timestamp
                      + ", " + currV.position.trafficConditions + " " + (p.end - p.start) + " stops left");
                }

              }

            }

          }

        }

      } catch (RETCODE_NO_DATA noData) {
        // No data to process
      } finally {
        VehicleReader.return_loan(_dataSeq, _infoSeq);
      }
    }
  }
}

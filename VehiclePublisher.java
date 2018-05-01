
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

public class VehiclePublisher {

  DomainParticipant participant = null;
  Publisher publisher = null;
  Topic topic = null;
  VehicleDataWriter writer = null;

  public VehiclePublisher(int domainId) {
    super();

    participant = DomainParticipantFactory.TheParticipantFactory.create_participant(domainId,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null /* listener */, StatusKind.STATUS_MASK_NONE);
    if (participant == null) {
      System.err.println("create_participant error\n");
      return;
    }

    // --- Create publisher --- //

    /* To customize publisher QoS, use
    the configuration file USER_QOS_PROFILES.xml */

    publisher = participant.create_publisher(DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
        StatusKind.STATUS_MASK_NONE);
    if (publisher == null) {
      System.err.println("create_publisher error\n");
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

    // --- Create writer --- //

    /* To customize data writer QoS, use
    the configuration file USER_QOS_PROFILES.xml */

    writer = (VehicleDataWriter) publisher.create_datawriter(topic, Publisher.DATAWRITER_QOS_DEFAULT,
        null /* listener */, StatusKind.STATUS_MASK_NONE);
    if (writer == null) {
      System.err.println("create_datawriter error\n");
      return;
    }

  }

  // -----------------------------------------------------------------------

  public void publisherMain(Vehicle vehicle) throws InterruptedException {

    InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
    if (!vehicle.breakdown.timestamp.equals("")) {
      System.out.println(vehicle.vId + " published a breakdown message at stop #" + vehicle.breakdown.stopNumber
          + " on the route " + vehicle.position.route + " at " + vehicle.breakdown.timestamp);
      System.out.println(vehicle.vId + " stopped " + "A backup should arrive in 15 seconds");
    } else {

      if (!vehicle.accident.timestamp.equals("")) {
        System.out.println(vehicle.vId + " published a accident message at stop #" + vehicle.accident.stopNumber
            + " on the route " + vehicle.position.route + " at " + vehicle.accident.timestamp);
      }

      System.out.println(vehicle.vId + " published a position message at stop #" + vehicle.position.stopNumber
          + " on the route " + vehicle.position.route + " at " + vehicle.position.timestamp);

    }

    Thread.sleep(2000L);
    writer.write(vehicle, instance_handle);

    writer.unregister_instance(vehicle, instance_handle);

  }

  public void release() {

    if (participant != null) {
      participant.delete_contained_entities();

      DomainParticipantFactory.TheParticipantFactory.delete_participant(participant);
    }

  }

}

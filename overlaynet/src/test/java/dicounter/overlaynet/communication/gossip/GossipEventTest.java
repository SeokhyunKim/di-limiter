package dicounter.overlaynet.communication.gossip;

import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.ObjectMappers;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class GossipEventTest {

    @Test
    public void serialization_deserialization_NodeJoinEvent() {
        NodeJoinEvent event = NodeJoinEvent.builder()
                                           .eventSourceAddress(NodeAddress.builder()
                                                                          .ipAddress("127.0.0.1")
                                                                          .port(10)
                                                                          .build())
                                           .timestamp(DateTime.now())
                                           .build();
        final String serialized = ObjectMappers.writeValueAsString(event);
        NodeJoinEvent deserialized = ObjectMappers.readValue(serialized, NodeJoinEvent.class);
        Assertions.assertEquals(deserialized, event);
    }

    @Test
    public void serialization_deserialization_NodeLeaveEvent() {
        NodeLeaveEvent event = NodeLeaveEvent.builder()
                                             .eventSourceAddress(NodeAddress.builder()
                                                                            .ipAddress("127.0.0.1")
                                                                            .port(10)
                                                                            .build())
                                             .timestamp(DateTime.now())
                                             .build();
        final String serialized = ObjectMappers.writeValueAsString(event);
        NodeLeaveEvent deserialized = ObjectMappers.readValue(serialized, NodeLeaveEvent.class);
        Assertions.assertEquals(deserialized, event);
    }
}

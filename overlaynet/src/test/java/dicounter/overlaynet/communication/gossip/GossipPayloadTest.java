package dicounter.overlaynet.communication.gossip;

import com.google.common.collect.ImmutableSet;
import dicounter.overlaynet.communication.gossip.GossipPayload;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.ObjectMappers;
import dicounter.overlaynet.utils.TestUtils;
import java.util.SortedSet;
import java.util.TreeSet;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class GossipPayloadTest {

    @Test
    public void serialize_deserialize_GossipPayload() {
        SortedSet<GossipEvent> gossipEvents = new TreeSet<>();
        gossipEvents.add(NodeJoinEvent.builder()
                                      .eventSourceAddress(TestUtils.createRandomTestNodeAddress())
                                      .timestamp(DateTime.now())
                                      .build());
        gossipEvents.add(NodeLeaveEvent.builder()
                                       .eventSourceAddress(TestUtils.createRandomTestNodeAddress())
                                       .timestamp(DateTime.now())
                                       .build());
        GossipPayload payload1 = GossipPayload.builder()
                                              .gossipEvents(gossipEvents)
                                              .gossipPayload("test")
                                              .build();
        GossipPayload payload2 = GossipPayload.builder()
                                              .gossipEvents(gossipEvents)
                                              .build();
        final String payloadStr1 = ObjectMappers.writeValueAsString(payload1);
        final String payloadStr2 = ObjectMappers.writeValueAsString(payload2);
        final GossipPayload deserialized1 = ObjectMappers.readValue(payloadStr1, GossipPayload.class);
        final GossipPayload deserialized2 = ObjectMappers.readValue(payloadStr2, GossipPayload.class);
        Assertions.assertEquals(payload1, deserialized1);
        Assertions.assertEquals(payload2, deserialized2);
    }
}

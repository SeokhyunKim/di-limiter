package dicounter.overlaynet.communication.gossip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dicounter.overlaynet.node.NodeAddress;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.joda.time.DateTime;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = NodeLeaveEvent.NodeLeaveEventBuilder.class)
public class NodeLeaveEvent extends GossipEvent {

    @Builder
    @JsonCreator
    private NodeLeaveEvent(@JsonProperty(value = "id") @Nullable final UUID id,
                           @JsonProperty(value = "eventSourceAddress") @NonNull final NodeAddress eventSourceAddress,
                           @JsonProperty(value = "timestamp") @NonNull final DateTime timestamp) {
        super(id, eventSourceAddress, timestamp);
    }

    @Override @JsonIgnore
    public GossipEventType getType() {
        return GossipEventType.NODE_LEAVE;
    }
}

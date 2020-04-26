package dicounter.overlaynet.communication.gossip;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dicounter.overlaynet.node.NodeAddress;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.joda.time.DateTime;

@ToString
@EqualsAndHashCode(of = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = NodeJoinEvent.class, name = "NODE_JOIN"),
               @JsonSubTypes.Type(value = NodeLeaveEvent.class, name = "NODE_LEAVE"),
              })
public abstract class GossipEvent implements Comparable<GossipEvent> {

    @Getter
    private final UUID id;
    @Getter @NonNull
    private final NodeAddress eventSourceAddress;
    @Getter @NonNull
    private final DateTime timestamp;

    // for deserialization
    protected GossipEvent(@Nullable final UUID id, @NonNull final NodeAddress eventSourceAddress, @NonNull final DateTime timestamp) {
        if (id == null) {
            this.id = UUID.randomUUID();
        } else {
            this.id = id;
        }
        this.eventSourceAddress = eventSourceAddress;
        this.timestamp = timestamp;
    }

    public abstract GossipEventType getType();

    @Override
    public int compareTo(@NonNull final GossipEvent event) {
        if (this.timestamp.isBefore(event.getTimestamp())) {
            return -1;
        } else if (this.timestamp.isAfter(event.getTimestamp())) {
            return 1;
        }
        final int sourceCompare = this.eventSourceAddress.compareTo(event.getEventSourceAddress());
        if (sourceCompare != 0) {
            return sourceCompare;
        }
        return this.id.compareTo(event.getId());
    }
}

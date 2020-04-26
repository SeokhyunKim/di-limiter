package dicounter.overlaynet.communication.gossip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@JsonDeserialize(builder = GossipPayload.GossipPayloadBuilder.class)
public class GossipPayload {

    private final SortedSet<GossipEvent> gossipEvents = new TreeSet<>();
    private final String gossipPayload;

    @Builder
    @JsonCreator
    private GossipPayload(@JsonProperty(value = "gossipEvents") @NonNull final Collection<GossipEvent> gossipEvents,
                          @JsonProperty(value = "gossipPayload") @Nullable final String gossipPayload) {
        this.gossipEvents.addAll(gossipEvents);
        this.gossipPayload = gossipPayload;
    }
}

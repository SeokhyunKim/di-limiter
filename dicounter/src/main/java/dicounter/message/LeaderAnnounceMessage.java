package dicounter.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dicounter.overlaynet.node.NodeAddress;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = LeaderAnnounceMessage.LeaderAnnounceMessageBuilder.class)
public class LeaderAnnounceMessage implements DicounterMessage {

    private final NodeAddress nodeAddress;

    @JsonIgnore
    @Override
    public DicounterMessageType getType() {
        return DicounterMessageType.LEADER_ANNOUNCE;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class LeaderAnnounceMessageBuilder {
    }
}

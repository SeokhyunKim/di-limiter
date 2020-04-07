package dicounter.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = LeaderDetectMessage.LeaderDetectMessageBuilder.class)
public class LeaderDetectMessage implements DicounterMessage {

    private final UUID countingTaskId;
    private final UUID id;
    private final int roundNo;
    private final int localThresholdValue;

    @JsonIgnore
    @Override
    public DicounterMessageType getType() {
        return DicounterMessageType.LEADER_DETECT;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class LeaderDetectMessageBuilder {
    }
}

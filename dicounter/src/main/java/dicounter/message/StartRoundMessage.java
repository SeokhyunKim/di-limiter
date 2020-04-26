package dicounter.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = StartRoundMessage.StartRoundMessageBuilder.class)
public class StartRoundMessage implements DicounterMessage {

    private final UUID countingTaskId;
    private final UUID id;
    private final int roundNo;
    private final long remainingTriggerToObserve;

    @JsonIgnore
    @Override
    public DicounterMessageType getType() {
        return DicounterMessageType.START_ROUND;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class StartRoundMessageBuilder {
    }
}

package dicounter.overlaynet.node.connectivity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dicounter.overlaynet.node.NodeAddress;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = OverlayNetEvent.OverlayNetEventBuilder.class)
public class OverlayNetEvent implements Comparable<OverlayNetEvent> {

    private final OverlayNetEventType eventType;

    private final NodeAddress eventSource;

    private final long order;

    public int compareTo(@NonNull final OverlayNetEvent other) {
        int compared = eventSource.compareTo(other.eventSource);
        if (compared != 0) {
            return  compared;
        }
        if (order < other.order) {
            return -1;
        } else if (order > other.order) {
            return 1;
        }
        return 0;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class OverlayNetEventBuilder {
    }
}

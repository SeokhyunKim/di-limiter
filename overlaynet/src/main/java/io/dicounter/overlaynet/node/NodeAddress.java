package io.dicounter.overlaynet.node;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
@JsonDeserialize(builder = NodeAddress.NodeAddressBuilder.class)
public class NodeAddress implements Comparable<NodeAddress> {
    @NonNull
    private final String ipAddress;

    private final int port;

    public int compareTo(@NonNull final NodeAddress na) {
        final int comp1 = ipAddress.compareTo(na.getIpAddress());
        if (comp1 != 0) {
            return comp1;
        }
        return Integer.compare(port, na.getPort());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class NodeAddressBuilder {
    }
}

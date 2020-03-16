package io.dicounter.overlaynet.node;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
@EqualsAndHashCode
@JsonDeserialize(builder = Node.NodeBuilder.class)
public class Node implements Comparable<Node> {

    @NonNull
    private final UUID id;
    @NonNull
    private final NodeAddress nodeAddress;

    @Override
    public String toString() {
        return "[" + id.toString() + ": " + nodeAddress.getIpAddress() + ", " + nodeAddress.getPort() + "]";
    }

    public int compareTo(@NonNull final Node nd) {
        return nodeAddress.compareTo(nd.getNodeAddress());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public final static class NodeBuilder {
    }
}

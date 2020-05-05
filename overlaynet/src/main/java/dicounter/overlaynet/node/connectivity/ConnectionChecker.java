package dicounter.overlaynet.node.connectivity;

import com.google.common.collect.Sets;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Check whether the neighbor nodes of a node are active by sending ping messages.
 */
@Slf4j
@AllArgsConstructor
public class ConnectionChecker {

    private final Node parent;

    /**
     * For each neighbor node of the parent node, send ping message and check whether it's alive.
     * If not, creating {@link OverlayNetEvent} for each not-responding neighbor node.
     * @return the set of {@link OverlayNetEvent} for not-responding nodes to ping messages.
     */
    public Set<OverlayNetEvent> checkNeightborConnectionsByChance() {
        return Sets.newHashSet();
    }

    public void checkConnection(@NonNull final NodeAddress nodeAddress, @NonNull final Consumer<OverlayNetEvent> callback) {

    }


}

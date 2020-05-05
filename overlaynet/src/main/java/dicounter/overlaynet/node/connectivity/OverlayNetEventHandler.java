package dicounter.overlaynet.node.connectivity;

import com.google.common.collect.Sets;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link OverlayNetEvent} means an event happening on a overlay-network that can make any change in the node connection graph
 * of the overlay network. {@link OverlayNetEventHandler} owns the logic to derive latest neighbor nodes from a set of overlay-network
 * events. Also, this has logic to do simplification of overlay-network events set. For example, if there are two events that node A joined
 * at t1 and left at t2 where t1 < t2, then the event for the participation of node A can be safely deleted.
 */
@Slf4j
@AllArgsConstructor
public class OverlayNetEventHandler {

    private final Node parent;
    private final ConnectionChecker connectionChecker;

    private final Map<NodeAddress, List<OverlayNetEvent>> netEvents = new HashMap<>();

    public void addEvent(@NonNull final OverlayNetEvent netEvent) {
        addEvents(Sets.newHashSet(netEvent));
    }

    public void addEvents(@NonNull final Collection<OverlayNetEvent> netEvents) {

    }

    public SortedSet<NodeAddress> getLatestNeighbors() {
        return null;
    }

    public Set<OverlayNetEvent> getLatestOverlayNetEvents() {
        return null;
    }
}

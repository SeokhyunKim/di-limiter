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

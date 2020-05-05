package dicounter.overlaynet.node.connectivity;

import com.google.common.collect.Sets;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ConnectionChecker {

    private final Node parent;

    public Set<OverlayNetEvent> checkNeightborConnectionsByChance() {
        return Sets.newHashSet();
    }

    public void checkConnection(@NonNull final NodeAddress nodeAddress, @NonNull final Consumer<OverlayNetEvent> callback) {

    }


}

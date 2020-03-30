package dicounter.overlaynet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.node.SocketNodeImpl;
import dicounter.overlaynet.utils.TestUtils;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class OverlayNetTest {

    @Test
    public void createHostedNodes_canCreateLocalHostedNodes() {
        OverlayNet overlayNet = new OverlayNet(1, 2);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(10, new MessageCallback(),
                                11, new MessageCallback(),
                                12, new MessageCallback());
        Set<Pair<Integer, Consumer<Message>>> callbacks =
                ImmutableSet.of(ImmutablePair.of(10, msg -> callbackMap.get(10).callback(msg)),
                                ImmutablePair.of(11, msg -> callbackMap.get(11).callback(msg)),
                                ImmutablePair.of(12, msg -> callbackMap.get(12).callback(msg)));
        SortedSet<NodeAddress> addresses = overlayNet.createHostedNodes("127.0.0.1", callbacks);
        int i = 0;
        for (NodeAddress address : addresses) {
            if (i == 0) {
                Assertions.assertEquals(address.getPort(), 10);
            } else if (i == 1) {
                Assertions.assertEquals(address.getPort(), 11);
            } else if (i == 2) {
                Assertions.assertEquals(address.getPort(), 12);
            }
            ++i;
        }
        overlayNet.destroyOverlayNet();
    }

    @Test
    public void join_canJoinExistingOverlayNet() throws InterruptedException {
        OverlayNet overlayNet1 = new OverlayNet(10, 10);
        OverlayNet overlayNet2 = new OverlayNet(10, 10);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(10, new MessageCallback(),
                                11, new MessageCallback(),
                                12, new MessageCallback(),
                                13, new MessageCallback(),
                                14, new MessageCallback());
        Set<Pair<Integer, Consumer<Message>>> callbacks1 =
                ImmutableSet.of(ImmutablePair.of(10, msg -> callbackMap.get(10).callback(msg)),
                                ImmutablePair.of(11, msg -> callbackMap.get(11).callback(msg)),
                                ImmutablePair.of(12, msg -> callbackMap.get(12).callback(msg)));
        Set<Pair<Integer, Consumer<Message>>> callbacks2 =
                ImmutableSet.of(ImmutablePair.of(13, msg -> callbackMap.get(13).callback(msg)),
                                ImmutablePair.of(14, msg -> callbackMap.get(14).callback(msg)));
        SortedSet<NodeAddress> nodes1 = overlayNet1.createHostedNodes("127.0.0.1", callbacks1);
        SortedSet<NodeAddress> nodes2 = overlayNet2.createHostedNodes("127.0.0.1", callbacks2);
        SortedSet<NodeAddress> allNodes = new TreeSet<>(nodes1);
        allNodes.addAll(nodes2);

        overlayNet1.join(nodes2);

        Map<NodeAddress, SocketNodeImpl> nodeMap1 =
                (Map<NodeAddress, SocketNodeImpl>)TestUtils.getMemberVariable(overlayNet1, "hostedNodeMap");
        Map<NodeAddress, SocketNodeImpl> nodeMap2 =
                (Map<NodeAddress, SocketNodeImpl>)TestUtils.getMemberVariable(overlayNet2, "hostedNodeMap");
        Thread.sleep(10000L);
        for (Node node : nodeMap1.values()) {
            Assertions.assertEquals(node.getKnownNodeAddresses(), allNodes);
        }
        for (Node node : nodeMap2.values()) {
            Assertions.assertEquals(node.getKnownNodeAddresses(), allNodes);
        }

        overlayNet1.destroyOverlayNet();
        overlayNet2.destroyOverlayNet();
    }
}

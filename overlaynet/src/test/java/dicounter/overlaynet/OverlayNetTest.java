package dicounter.overlaynet;

import com.google.common.collect.ImmutableMap;
import dicounter.overlaynet.communication.CommunicationType;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.node.SocketNode;
import dicounter.overlaynet.utils.TestUtils;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class OverlayNetTest {

    @Test
    public void createHostedNodes_canCreateLocalHostedNodes_whenSocketNode() {
        OverlayNet overlayNet = new OverlayNet(1, 2);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(10, new MessageCallback(),
                                11, new MessageCallback(),
                                12, new MessageCallback());
        Map<Integer, Consumer<Message>> callbacks =
                ImmutableMap.of(10, msg -> callbackMap.get(10).callback(msg),
                                11, msg -> callbackMap.get(11).callback(msg),
                                12, msg -> callbackMap.get(12).callback(msg));
        SortedSet<NodeAddress> addresses = overlayNet.createHostedNodes("127.0.0.1", CommunicationType.SOCKET, callbacks);
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
    public void createHostedNodes_canCreateLocalHostedNodes_whenHttpNode() {
        OverlayNet overlayNet = new OverlayNet(1, 2);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(2000, new MessageCallback(),
                                2001, new MessageCallback(),
                                2002, new MessageCallback());
        Map<Integer, Consumer<Message>> callbacks =
                ImmutableMap.of(2000, msg -> callbackMap.get(2000).callback(msg),
                                2001, msg -> callbackMap.get(2001).callback(msg),
                                2002, msg -> callbackMap.get(2002).callback(msg));
        SortedSet<NodeAddress> addresses = overlayNet.createHostedNodes("127.0.0.1", CommunicationType.HTTP, callbacks);
        int i = 0;
        for (NodeAddress address : addresses) {
            if (i == 0) {
                Assertions.assertEquals(address.getPort(), 2000);
            } else if (i == 1) {
                Assertions.assertEquals(address.getPort(), 2001);
            } else if (i == 2) {
                Assertions.assertEquals(address.getPort(), 2002);
            }
            ++i;
        }
        overlayNet.destroyOverlayNet();
    }

    @Test
    public void join_canJoinExistingOverlayNet_whenSocketNode() throws InterruptedException {
        OverlayNet overlayNet1 = new OverlayNet(10, 10);
        OverlayNet overlayNet2 = new OverlayNet(10, 10);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(10, new MessageCallback(),
                                11, new MessageCallback(),
                                12, new MessageCallback(),
                                13, new MessageCallback(),
                                14, new MessageCallback());
        Map<Integer, Consumer<Message>> callbacks1 =
                ImmutableMap.of(10, msg -> callbackMap.get(10).callback(msg),
                                11, msg -> callbackMap.get(11).callback(msg),
                                12, msg -> callbackMap.get(12).callback(msg));
        Map<Integer, Consumer<Message>> callbacks2 =
                ImmutableMap.of(13, msg -> callbackMap.get(13).callback(msg),
                                14, msg -> callbackMap.get(14).callback(msg));
        SortedSet<NodeAddress> nodes1 = overlayNet1.createHostedNodes("127.0.0.1", CommunicationType.SOCKET, callbacks1);
        SortedSet<NodeAddress> nodes2 = overlayNet2.createHostedNodes("127.0.0.1", CommunicationType.SOCKET, callbacks2);
        SortedSet<NodeAddress> allNodes = new TreeSet<>(nodes1);
        allNodes.addAll(nodes2);

        overlayNet1.join(nodes2);

        Map<NodeAddress, SocketNode> nodeMap1 =
                (Map<NodeAddress, SocketNode>)TestUtils.getMemberVariable(overlayNet1, "hostedNodeMap");
        Map<NodeAddress, SocketNode> nodeMap2 =
                (Map<NodeAddress, SocketNode>)TestUtils.getMemberVariable(overlayNet2, "hostedNodeMap");
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

    @Test
    public void join_canJoinExistingOverlayNet_whenHttpNode() throws InterruptedException {
        OverlayNet overlayNet1 = new OverlayNet(10, 10);
        OverlayNet overlayNet2 = new OverlayNet(10, 10);
        final Map<Integer, MessageCallback> callbackMap =
                ImmutableMap.of(2000, new MessageCallback(),
                                2001, new MessageCallback(),
                                2002, new MessageCallback(),
                                2003, new MessageCallback(),
                                2004, new MessageCallback());
        Map<Integer, Consumer<Message>> callbacks1 =
                ImmutableMap.of(2000, msg -> callbackMap.get(2000).callback(msg),
                                2001, msg -> callbackMap.get(2001).callback(msg),
                                2002, msg -> callbackMap.get(2002).callback(msg));
        Map<Integer, Consumer<Message>> callbacks2 =
                ImmutableMap.of(2003, msg -> callbackMap.get(2003).callback(msg),
                                2004, msg -> callbackMap.get(2004).callback(msg));
        SortedSet<NodeAddress> nodes1 = overlayNet1.createHostedNodes("127.0.0.1", CommunicationType.HTTP, callbacks1);
        SortedSet<NodeAddress> nodes2 = overlayNet2.createHostedNodes("127.0.0.1", CommunicationType.HTTP, callbacks2);
        SortedSet<NodeAddress> allNodes = new TreeSet<>(nodes1);
        allNodes.addAll(nodes2);

        overlayNet1.join(nodes2);

        Map<NodeAddress, SocketNode> nodeMap1 =
                (Map<NodeAddress, SocketNode>)TestUtils.getMemberVariable(overlayNet1, "hostedNodeMap");
        Map<NodeAddress, SocketNode> nodeMap2 =
                (Map<NodeAddress, SocketNode>)TestUtils.getMemberVariable(overlayNet2, "hostedNodeMap");
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

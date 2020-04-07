package dicounter.overlaynet.node;

import static org.mockito.Mockito.mock;

import com.google.common.testing.NullPointerTester;
import dicounter.overlaynet.MessageCallback;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.utils.Messages;
import dicounter.overlaynet.utils.TestUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class HttpNodeTest {

    private static ExecutorService executorService;

    @BeforeAll
    public static void initialize() {
        executorService = Executors.newFixedThreadPool(50);;
    }

    @AfterAll
    public static void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private final List<HttpNode> createdNodes = new ArrayList<>();

    @AfterEach
    public void stopNodes() throws InterruptedException {
        for (HttpNode node : createdNodes) {
            node.stopMessageListening();
        }
        Thread.sleep(1000L); // give time for stopping all threads
    }

    private HttpNode createNode(@NonNull final NodeAddress nodeAddress) {
        final HttpNode node = new HttpNode(nodeAddress, "/test", executorService);
        createdNodes.add(node);
        return node;
    }

    @Test
    public void sendMessage_canSendMessage() throws InterruptedException {
        NodeAddress na1 = NodeAddress.builder()
                                     .ipAddress("127.0.0.1")
                                     .port(2000)
                                     .build();
        NodeAddress na2 = NodeAddress.builder()
                                     .ipAddress("127.0.0.1")
                                     .port(2001)
                                     .build();
        HttpNode node1 = createNode(na1);
        HttpNode node2 = createNode(na2);
        final MessageCallback msgCallback = new MessageCallback();
        node2.setMessageCallback(msgCallback::callback);
        node1.startMessageListening();
        node2.startMessageListening();
        Thread.sleep(500L); // wait for message listening loop activation
        node1.sendMessage(na2, Message.PING);

        while (msgCallback.getReceived().isEmpty()) {
            Thread.sleep(100L);
        }
        List<Message> received = msgCallback.getReceived();
        Assertions.assertEquals(received.size(), 1);
        Assertions.assertEquals(received.get(0), Message.PING);
    }

    @Test
    public void stopMessageListeneing_canStopMessageListening() throws InterruptedException {
        NodeAddress na = NodeAddress.builder()
                                    .ipAddress("127.0.0.1")
                                    .port(2000)
                                    .build();
        HttpNode node = createNode(na);
        final MessageCallback msgCallback = new MessageCallback();
        node.setMessageCallback(msgCallback::callback);
        node.startMessageListening();
        Thread.sleep(500L); // wait for message listening loop activation
        node.sendMessage(na, Message.PING);
        node.sendMessage(na, Message.PING);
        node.sendMessage(na, Message.PING);
        Assertions.assertTrue(node.isListeningMessage());
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10000L) {
            log.info("Checking msgCallback {}. size: {}, msgs: {}", msgCallback, msgCallback.getReceived().size(), msgCallback.getReceived());
            if (msgCallback.getReceived().size() < 3) {
                Thread.sleep(100L);
            } else {
                break;
            }
        }
        if (System.currentTimeMillis() - start >= 10000L) {
            throw new RuntimeException("time out while waiting for arrivals of ping messages");
        }
        Assertions.assertEquals(msgCallback.getReceived().size(), 3);

        node.stopMessageListening();

        try {
            node.sendMessage(na, Message.PING);
        } catch (final Exception e) {
            log.error("Error while sending a ping", e);
        }
        int numReceived = msgCallback.getReceived().size();
        Assertions.assertTrue(numReceived == 3);
    }

    @Test
    public void sendMessage_canExchangeMessages_whenSendJoin() throws InterruptedException {
        NodeAddress na1 = NodeAddress.builder()
                                     .ipAddress("127.0.0.1")
                                     .port(2000)
                                     .build();
        NodeAddress na2 = NodeAddress.builder()
                                     .ipAddress("127.0.0.1")
                                     .port(2001)
                                     .build();
        HttpNode node1 = createNode(na1);
        HttpNode node2 = createNode(na2);
        final MessageCallback msgCallback1 = new MessageCallback();
        final MessageCallback msgCallback2 = new MessageCallback();
        node1.setMessageCallback(msgCallback1::callback);
        node2.setMessageCallback(msgCallback2::callback);
        node1.startMessageListening();
        node2.startMessageListening();
        Thread.sleep(500L); // wait for message listening loop activation

        node1.sendMessage(na2, Messages.createOverlayNetControlMessage(MessageType.JOIN_NODE, node1));
        List<Message> received1 = msgCallback1.getReceived();
        List<Message> received2 = msgCallback2.getReceived();
        while (received1.isEmpty() && received2.isEmpty()) {
            Thread.sleep(100L);
            received1 = msgCallback1.getReceived();
            received2 = msgCallback2.getReceived();
        }

        Assertions.assertEquals(received1.get(0).getType(), MessageType.RESPONSE_JOIN_NODE);
        Assertions.assertEquals(received2.get(0).getType(), MessageType.JOIN_NODE);
        SortedSet<NodeAddress> addresses = new TreeSet<>();
        addresses.add(na1);
        addresses.add(na2);
        Assertions.assertEquals(node1.getKnownNodeAddresses(), addresses);
        Assertions.assertEquals(node2.getKnownNodeAddresses(), addresses);
    }

    @Test
    public void sendMessage_throwsException_whenEmptyPayload() {
        SocketNode node = new SocketNode(mock(NodeAddress.class), mock(ExecutorService.class));
        Method processReceivedMessage = TestUtils.getMethod(node.getClass().getSuperclass(), "processReceivedMessage", Message.class);
        Assertions.assertThrows(IllegalStateException.class, () -> TestUtils.callMethod(node, processReceivedMessage,
                                                                                        Message.builder()
                                                                                               .type(MessageType.JOIN_NODE)
                                                                                               .build()));
    }

    @Test
    public void nullTest() {
        NullPointerTester npt = new NullPointerTester();
        HttpNode node = new HttpNode(NodeAddress.builder()
                                                .ipAddress("127.0.0.1")
                                                .port(3000)
                                                .build(),
                                     "/test", mock(ExecutorService.class));
        npt.setDefault(Message.class, mock(Message.class));
        npt.setDefault(NodeAddress.class, mock(NodeAddress.class));
        npt.testAllPublicInstanceMethods(node);
        npt.testAllPublicConstructors(HttpNode.class);
    }
}

package dicounter.overlaynet;

import dicounter.overlaynet.communication.socket.ExchangeMessage;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.SocketNodeImpl;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.Messages;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Main controlling class of OverlayNet where all the controlling features are provided.
 * Each {@link SocketNodeImpl} has an unique id. Through OverlayNet, easily send a message to any known node.
 */
@Slf4j
public class OverlayNet {

    private static final long THREAD_KEEP_ALIVE_TIME = 3000L;

    private final ExecutorService executorService;
    private final Map<NodeAddress, SocketNodeImpl> hostedNodeMap = new HashMap<>();

    public OverlayNet(final int minThreads, final int maxThreads) {
        this.executorService = new ThreadPoolExecutor(minThreads, maxThreads,
                                                      THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<>());
        log.info("Initialization params: minThreads {}, maxThreads {}, threadKeepAliveTime {} ms",
                 minThreads, maxThreads, THREAD_KEEP_ALIVE_TIME);
    }

    public void destroyOverlayNet() {
        destroyAllHostedNodes();
        executorService.shutdown();
        try {
            final long awaitingTimeForTerminationInMilliseconds = THREAD_KEEP_ALIVE_TIME * 3;
            if (!executorService.awaitTermination(awaitingTimeForTerminationInMilliseconds, TimeUnit.MILLISECONDS)) {
                log.warn("There are remaining threads after {} milliseconds.", awaitingTimeForTerminationInMilliseconds);
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Failed to shutdown now.");
            executorService.shutdownNow();
        }
    }

    public SortedSet<NodeAddress> createHostedNodes(@NonNull final String ipAddress,
                                                    final Set<Pair<Integer, Consumer<Message>>> portsAndCallbacks) {
        final SortedSet<NodeAddress> createdNodeAddresses = new TreeSet<>();
        for (final Pair<Integer, Consumer<Message>> portAndCallback : portsAndCallbacks) {
            final NodeAddress nodeAddress = NodeAddress.builder()
                                                       .ipAddress(ipAddress)
                                                       .port(portAndCallback.getLeft())
                                                       .build();
            final SocketNodeImpl node = new SocketNodeImpl(nodeAddress, executorService);
            node.startMessageListening();
            hostedNodeMap.put(nodeAddress, node);
            createdNodeAddresses.add(nodeAddress);
        }
        for (final Node node : hostedNodeMap.values()) {
            node.getKnownNodeAddresses().addAll(createdNodeAddresses);
        }
        return createdNodeAddresses;
    }

    public void destroyAllHostedNodes() {
        for (final SocketNodeImpl node : hostedNodeMap.values()) {
            node.stopMessageListening();
        }
        this.hostedNodeMap.clear();
    }

    public void join(@NonNull final Set<NodeAddress> seedNodes) {
        if (hostedNodeMap.isEmpty()) {
            return;
        }
        for (final Node node : hostedNodeMap.values()) {
            final Message joinMessage = Messages.createMessage(MessageType.JOIN_NODE, node);
            for (final NodeAddress seedNode : seedNodes) {
                node.sendMessage(seedNode, joinMessage);
            }
        }
    }

    public SortedSet<NodeAddress> getAllKnownNodes() {
        final SortedSet<NodeAddress> allNodes = new TreeSet<>();
        for (final Node node : hostedNodeMap.values()) {
            allNodes.addAll(node.getKnownNodeAddresses());
        }
        return allNodes;
    }

    public SortedSet<NodeAddress> getHostedNodes() {
        return new TreeSet<>(hostedNodeMap.keySet());
    }

    public void sendMessage(@NonNull final NodeAddress nodeAddress, @NonNull final Message message) {
        message.setReceiver(nodeAddress);
        ExchangeMessage.create(nodeAddress).sendMessage(message).close();
        log.debug("Message {} sent to {}", message, nodeAddress);
    }
}

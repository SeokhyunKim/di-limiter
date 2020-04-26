package dicounter.overlaynet;

import static dicounter.overlaynet.utils.Exceptions.logError;

import dicounter.overlaynet.communication.CommunicationType;
import dicounter.overlaynet.communication.ExchangeMessage;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.exception.BadRequestException;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.HttpNode;
import dicounter.overlaynet.node.SocketNode;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.communication.Messages;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Main controlling class of OverlayNet where all the controlling features are provided.
 * Each {@link SocketNode} has an unique id. Through OverlayNet, easily send a message to any known node.
 */
@Slf4j
public class OverlayNet {

    private static final long THREAD_KEEP_ALIVE_TIME = 3000L;
    private static final String API_PATH = "/dicounter/overlaynet";

    private final ExecutorService executorService;
    private final boolean isExternalExecutorService;
    private final Map<NodeAddress, Node> hostedNodeMap = new HashMap<>();

    public OverlayNet(final int minThreads, final int maxThreads) {
        this.executorService = new ThreadPoolExecutor(minThreads, maxThreads,
                                                      THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<>());
        isExternalExecutorService = false;
        log.info("OverlayNet created with initialization params: minThreads {}, maxThreads {}, threadKeepAliveTime {} ms",
                 minThreads, maxThreads, THREAD_KEEP_ALIVE_TIME);
    }

    public OverlayNet(@NonNull final ExecutorService executorService) {
        this.executorService = executorService;
        isExternalExecutorService = true;
        log.info("OverlayNet created with a given executorService.");
    }

    public void destroyOverlayNet() {
        destroyAllHostedNodes();
        if (!isExternalExecutorService && this.executorService != null) {
            executorService.shutdown();
            try {
                final long awaitingTimeForTerminationInMilliseconds = THREAD_KEEP_ALIVE_TIME * 3;
                if (!executorService.awaitTermination(awaitingTimeForTerminationInMilliseconds, TimeUnit.MILLISECONDS)) {
                    log.warn("There are remaining threads after {} milliseconds.", awaitingTimeForTerminationInMilliseconds);
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Failed to shutdown now. Trying shutdown again.");
                executorService.shutdownNow();
            }
        }
    }

    public Node createHostedNode(@NonNull final NodeAddress nodeAddress, @NonNull final CommunicationType communicationType) {
        final Node node;
        if (communicationType == CommunicationType.SOCKET) {
            node = new SocketNode(nodeAddress, executorService);
        } else if (communicationType == CommunicationType.HTTP) {
            node = new HttpNode(nodeAddress, API_PATH, executorService);
        } else {
            throw logError(new BadRequestException("Unsupported communication type: " + communicationType));
        }
        node.startMessageListening();
        hostedNodeMap.put(nodeAddress, node);
        return node;
    }

    public Set<Node> createHostedNodes(@NonNull final Set<NodeAddress> nodeAddresses,
                                       @NonNull final CommunicationType communicationType) {
        final SortedSet<NodeAddress> createdNodeAddresses = new TreeSet<>();
        for (final NodeAddress nodeAddress : nodeAddresses) {
            createdNodeAddresses.add(createHostedNode(nodeAddress, communicationType).getNodeAddress());
        }
        for (final Node node : hostedNodeMap.values()) {
            node.getKnownNodeAddresses().addAll(createdNodeAddresses);
        }
        return new HashSet<>(hostedNodeMap.values());
    }

    public SortedSet<NodeAddress> createHostedNodes(@NonNull final String ipAddress,
                                                    @NonNull final CommunicationType communicationType,
                                                    final Map<Integer, Consumer<Message>> portsAndCallbacks) {
        final SortedSet<NodeAddress> newNodeAddresses = portsAndCallbacks.keySet()
                                                                         .stream()
                                                                         .map(port -> NodeAddress.builder()
                                                                                                 .ipAddress(ipAddress)
                                                                                                 .port(port)
                                                                                                 .build())
                                                                         .collect(Collectors.toCollection(TreeSet::new));
        final Set<Node> newNodes = createHostedNodes(newNodeAddresses, communicationType);
        for (final Node newNode : newNodes) {
            newNode.setMessageCallback(portsAndCallbacks.get(newNode.getNodeAddress().getPort()));
        }
        return newNodeAddresses;
    }

    public void destroyAllHostedNodes() {
        for (final Node node : hostedNodeMap.values()) {
            node.stopMessageListening();
        }
        this.hostedNodeMap.clear();
    }

    public void join(@NonNull final Set<NodeAddress> seedNodes) {
        if (hostedNodeMap.isEmpty()) {
            return;
        }
        for (final Node node : hostedNodeMap.values()) {
            final Message joinMessage = Messages.createOverlayNetControlMessage(MessageType.JOIN_NODE, node);
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

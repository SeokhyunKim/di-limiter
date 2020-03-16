package io.dicounter.overlaynet;

import static java.util.stream.Collectors.toCollection;

import io.dicounter.overlaynet.communication.Message;
import io.dicounter.overlaynet.communication.MessageHandler;
import io.dicounter.overlaynet.exceptions.BadNodeIdException;
import io.dicounter.overlaynet.exceptions.NetworkException;
import io.dicounter.overlaynet.node.Node;
import io.dicounter.overlaynet.node.NodeAddress;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Main controlling class of OverlayNet where all the controlling features are provided.
 * Each {@link Node} has an unique id. Through OverlayNet, easily send a message to any known node.
 */
@Slf4j
public class OverlayNet {

    private static final long THREAD_KEEP_ALIVE_TIME = 3000L;

    private final ExecutorService executorService;
    private final Map<UUID, Node> nodeMap = new HashMap<>();

    public OverlayNet(final int minThreads, final int maxThreads, final int maxThreadQueueLength) {
        this.executorService = new ThreadPoolExecutor(minThreads, maxThreads,
                                                      THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                                                      new ArrayBlockingQueue<>(maxThreadQueueLength));
        log.info("Initialization params: minThreads {}, maxThreads {}, threadKeepAliveTime {}ms, threadQueueLength {}",
                 minThreads, maxThreads, THREAD_KEEP_ALIVE_TIME, maxThreadQueueLength);
    }

    public void createHostedNodes(@NonNull final String ipAddress, final int port, final int numNodes) {

    }

    public void join(@NonNull final Set<Node> seedNodes) {

    }

    public SortedSet<Node> getAllKnownNodes() {
        return new TreeSet<>(nodeMap.values());
    }

    public SortedSet<Node> getHostedNodes(@NonNull final String ipAddress) {
        return nodeMap.values()
                      .stream()
                      .filter(nd -> nd.getNodeAddress().getIpAddress().equals(ipAddress))
                      .collect(toCollection(TreeSet::new));
    }

    public void sendMessage(@NonNull final UUID nodeId, @NonNull final Message msg) {
        if (!nodeMap.containsKey(nodeId)) {
            throw new BadNodeIdException("Unknown nodeId " + nodeId + " was used to send a message");
        }
        log.debug("Trying to send a message {} to {}", msg, nodeId);

        final Node target = nodeMap.get(nodeId);
        final Socket socketToTarget = createSocketFromNodeAddress(target.getNodeAddress());
        executorService.submit(() -> new MessageHandler(socketToTarget).receiveMessage());
    }

    private Socket createSocketFromNodeAddress(@NonNull final NodeAddress na) {
        try {
            final InetAddress address = InetAddress.getByName(na.getIpAddress());
            return new Socket(address, na.getPort());
        } catch (final UnknownHostException e) {
            throw new NetworkException("Unknown host: " + na, e);
        } catch (final IOException e) {
            throw new NetworkException("Caught an exception while creating a socket to " + na, e);
        }
    }


}

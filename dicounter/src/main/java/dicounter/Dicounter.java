package dicounter;

import dicounter.message.DicounterMessage;
import dicounter.exception.DicounterException;
import dicounter.overlaynet.OverlayNet;
import dicounter.overlaynet.communication.CommunicationType;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.ObjectMappers;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Dicounter {

    private static final long THREAD_KEEP_ALIVE_TIME = 60 * 1000L;

    private final ExecutorService executorService;
    private final OverlayNet overlayNet;
    private final Node node;

    public Dicounter(@NonNull final NodeAddress nodeAddress, @NonNull final CommunicationType communicationType,
                     @NonNull final Set<NodeAddress> knownSeedNodes,
                     final int minThreads, final int maxThreads) {
        this.executorService = new ThreadPoolExecutor(minThreads, maxThreads,
                                                      THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                                                      new LinkedBlockingQueue<>());
        this.overlayNet = new OverlayNet(this.executorService);
        this.node = this.overlayNet.createHostedNode(nodeAddress, communicationType);
        this.node.setMessageCallback(this::messageCallback);
        this.overlayNet.join(knownSeedNodes);
        log.info("Dicounter created with initialization params: nodeAddress {}, communicationType: {}, knownSeedNodes: {}, " +
                 "minThreads {}, maxThreads {}, threadKeepAliveTime {} ms",
                 nodeAddress, communicationType, knownSeedNodes, minThreads, maxThreads, THREAD_KEEP_ALIVE_TIME);
    }

    public void destroy() {
        if (this.overlayNet != null) {
            this.overlayNet.destroyOverlayNet();
        }
        if (this.executorService != null) {
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

    private void messageCallback(@NonNull final Message message) {
        if (message.getType() != MessageType.PAYLOAD_TRANSMISSION) {
            return;
        }
        final DicounterMessage dicounterMessage = ObjectMappers.readValue(message.getPayload(), DicounterMessage.class);
        switch (dicounterMessage.getType()) {
            case DETECT: {

            }
            break;
            case LEADER_DETECT: {

            }
            break;
            default: {
                throw new DicounterException("Unrecognized dicounter-message type: " + dicounterMessage.getType() +
                                                     ", payload: " + message.getPayload());
            }
        }

    }
}

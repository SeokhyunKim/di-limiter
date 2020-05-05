package dicounter;

import dicounter.exception.BadRequestException;
import dicounter.message.AggregateTriggersRequest;
import dicounter.message.AggregateTriggersResponse;
import dicounter.message.DetectMessage;
import dicounter.message.DicounterMessage;
import dicounter.exception.DicounterException;
import dicounter.message.LeaderAnnounceMessage;
import dicounter.message.LeaderDetectMessage;
import dicounter.message.StartRoundMessage;
import dicounter.overlaynet.OverlayNet;
import dicounter.overlaynet.communication.CommunicationType;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.node.Node;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.communication.Messages;
import dicounter.overlaynet.utils.ObjectMappers;
import dicounter.utils.Probabilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

@Slf4j
public class Dicounter {

    private static final long THREAD_KEEP_ALIVE_TIME = 1000L;

    private final ExecutorService executorService;
    private final OverlayNet overlayNet;
    private final Node node;
    private final boolean isLeaderNode;
    private final List<NodeAddress> allKnownLeaderNodes = new ArrayList<>();
    private final Map<UUID, CountingTask> countingTasks = new HashMap<>();

    public Dicounter(@NonNull final NodeAddress nodeAddress, @NonNull final CommunicationType communicationType,
                     @NonNull final Set<NodeAddress> knownSeedNodes,
                     final int minThreads, final int maxThreads) {
        this.executorService = new ThreadPoolExecutor(minThreads, maxThreads,
                                                      THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                                                      new SynchronousQueue<>());
        this.overlayNet = new OverlayNet(this.executorService);
        this.node = this.overlayNet.createHostedNode(nodeAddress, communicationType);
        this.node.setMessageCallback(this::messageCallback);
        this.overlayNet.join(knownSeedNodes);
        this.isLeaderNode = Probabilities.binomialTrial(0.1);
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

    public NodeAddress getNodeAddress() {
        return node.getNodeAddress();
    }

    public SortedSet<NodeAddress> getKnownNodeAddresses() {
        return node.getNeighbors();
    }

    public UUID registerCountingTask(final long notificationThreshold, @NonNull final Consumer<CountingTask> notificationCallback) {
        return registerCountingTask(UUID.randomUUID(), notificationThreshold, notificationCallback);
    }

    public UUID registerCountingTask(@NonNull final UUID countingTaskId,
                                     final long notificationThreshold, @Nullable final Consumer<CountingTask> notificationCallback) {
        final CountingTask countingTask = new CountingTask(countingTaskId, notificationThreshold, notificationCallback);
        this.countingTasks.put(countingTask.getId(), countingTask);
        // todo: currently, assuming static overlaynet without churn which is unrealistic.
        if (isLeaderNode) {
            final LeaderAnnounceMessage message = LeaderAnnounceMessage.builder()
                                                                       .nodeAddress(node.getNodeAddress())
                                                                       .build();
            for (final NodeAddress nodeAddress : node.getNeighbors()) {
                node.sendMessage(nodeAddress, Message.builder()
                                                     .type(MessageType.PAYLOAD_TRANSMISSION)
                                                     .payload(ObjectMappers.writeValueAsString(message))
                                                     .build());
            }
        }
        return countingTask.getId();
    }

    public void startCounting(final UUID countingTaskId) {
        if (!countingTasks.containsKey(countingTaskId)) {
            throw new BadRequestException("Tried to start an unregistered counting task " + countingTaskId);
        }
        final CountingTask countingTask = countingTasks.get(countingTaskId);
        // todo: this is a temporary approach to see PDT is working with an ideal unrealistic static nodes graph scenario.
        countingTask.setNumParticipatingNodes(overlayNet.getAllKnownNodes().size());
        countingTask.startCounting();
        for (final NodeAddress nodeAddress : node.getNeighbors()) {
            final StartRoundMessage startRoundMessage =
                StartRoundMessage.builder()
                                 .countingTaskId(countingTaskId)
                                 .id(UUID.randomUUID())
                                 .roundNo(countingTask.getCurrentRound())
                                 .remainingTriggerToObserve(countingTask.getRemainingTriggersToObserveInCurrentRound())
                                 .build();
            final Message message = Messages.createPayloadTransmissionMessage(ObjectMappers.writeValueAsString(startRoundMessage));
            node.sendMessage(nodeAddress, message);
        }
    }

    public void notifyTrigger(final UUID countingTaskId) {
        if (!countingTasks.containsKey(countingTaskId)) {
            throw new BadRequestException("Tried to start an unregistered counting task " + countingTaskId);
        }
        final CountingTask countingTask = countingTasks.get(countingTaskId);
        countingTask.increaseLocallyObservedTriggers();
        final double detectMessageProbability = countingTask.getDetectMessageGenerationProbability();
        if (Probabilities.binomialTrial(detectMessageProbability)) {
            // send detect message to a leader
            final DetectMessage detectMessage =
                DetectMessage.builder()
                             .countingTaskId(countingTaskId)
                             .roundNo(countingTask.getCurrentRound())
                             .remainingTriggersToObserve(countingTask.getRemainingTriggersToObserveInCurrentRound())
                             .build();
            final Message message = Messages.createPayloadTransmissionMessage(ObjectMappers.writeValueAsString(detectMessage));
            // send DETECT message to a random leader node
            int idx = Probabilities.randomBetween(0, allKnownLeaderNodes.size());
            node.sendMessage(allKnownLeaderNodes.get(idx), message);
        }
    }

    private void messageCallback(@NonNull final Message message) {
        if (message.getType() != MessageType.PAYLOAD_TRANSMISSION) {
            return;
        }
        final DicounterMessage dicounterMessage = ObjectMappers.readValue(message.getPayload(), DicounterMessage.class);
        switch (dicounterMessage.getType()) {
            case START_ROUND:
                onStartRoundMessage(dicounterMessage);
                break;
            case DETECT:
                onDetectMessage(dicounterMessage);
                break;
            case LEADER_DETECT:
                onLeaderDetectMessage(dicounterMessage);
                break;
            case LEADER_ANNOUNCE:
                onLeaderAnnounceMessage(dicounterMessage);
                break;
            case AGGREGATE_TRIGGERS_REQUEST:
                onAggregateTriggersRequest(dicounterMessage);
                break;
            case AGGREGATE_TRIGGERS_RESPONSE:
                onAggregateTriggersResponse(dicounterMessage);
                break;
            default: {
                throw new DicounterException("Unrecognized dicounter-message type: " + dicounterMessage.getType() +
                                                     ", payload: " + message.getPayload());
            }
        }
    }

    private void onStartRoundMessage(@NonNull final DicounterMessage dicounterMessage) {
        StartRoundMessage startRoundMessage = (StartRoundMessage)dicounterMessage;
        registerCountingTask(startRoundMessage.getCountingTaskId(), startRoundMessage.getRemainingTriggerToObserve(),
                             null);


    }

    private void onDetectMessage(@NonNull final DicounterMessage dicounterMessage) {
        Validate.isTrue(this.isLeaderNode,
                        "DETECT message is delivered to a non-leader node. NodeAddress: " + node.getNodeAddress());
        final DetectMessage detectMessage = (DetectMessage)dicounterMessage;
        final CountingTask countingTask = countingTasks.get(detectMessage.getCountingTaskId());
        countingTask.increaseLocallyObservedDetectMessages();
        final long localDetectMessageThreshold = countingTask.getDetectMessageLocalThreshold(allKnownLeaderNodes.size());
        if (countingTask.getUnreportedLocallyObservedDetectMessages() >= localDetectMessageThreshold) {
            countingTask.decreaseUnreportedLocallyObservedDetectMessages(localDetectMessageThreshold);
            final LeaderDetectMessage leaderDetectMessage = LeaderDetectMessage.builder()
                                                                               .countingTaskId(countingTask.getId())
                                                                               .id(UUID.randomUUID())
                                                                               .roundNo(countingTask.getCurrentRound())
                                                                               .localThresholdValue(localDetectMessageThreshold)
                                                                               .build();
            final Message newMessage =
                    Messages.createPayloadTransmissionMessage(ObjectMappers.writeValueAsString(leaderDetectMessage));
            // broadcasting LeaderDetectMessage
            for (final NodeAddress leaderNodeAddress : allKnownLeaderNodes) {
                node.sendMessage(leaderNodeAddress, newMessage);
            }
        }
    }

    private void onLeaderDetectMessage(@NonNull final DicounterMessage dicounterMessage) {
        Validate.isTrue(this.isLeaderNode,
                        "LEADER_DETECT message is delivered to a non-leader node. NodeAddress: " + node.getNodeAddress());
        final LeaderDetectMessage leaderDetectMessage = (LeaderDetectMessage)dicounterMessage;
        final CountingTask countingTask = countingTasks.get(leaderDetectMessage.getCountingTaskId());
        countingTask.increaseLocallyObservedLeaderDetectMessages();
        if (countingTask.getUnreportedLocallyObservedLeaderDetectMessages() >= allKnownLeaderNodes.size()) {
            countingTask.decreaseUnreportedLocallyObservedLeaderDetectMessages(allKnownLeaderNodes.size());
            // collect all the triggers received so far
            final AggregateTriggersRequest aggregateRequest =
                    AggregateTriggersRequest.builder()
                                            .countingTaskId(countingTask.getId())
                                            .id(UUID.randomUUID())
                                            .aggregatingLeaderAddress(node.getNodeAddress())
                                            .roundNo(countingTask.getCurrentRound())
                                            .build();
            final Message newMessage =
                    Messages.createPayloadTransmissionMessage(ObjectMappers.writeValueAsString(aggregateRequest));
            for (final NodeAddress nodeAddress : node.getNeighbors()) {
                node.sendMessage(nodeAddress, newMessage);
            }
        }
    }

    private void onLeaderAnnounceMessage(@NonNull final DicounterMessage dicounterMessage) {
        final LeaderAnnounceMessage leaderAnnounceMessage = (LeaderAnnounceMessage)dicounterMessage;
        this.allKnownLeaderNodes.add(leaderAnnounceMessage.getNodeAddress());
    }

    private void onAggregateTriggersRequest(@NonNull final DicounterMessage dicounterMessage) {
        final AggregateTriggersRequest aggregateRequest = (AggregateTriggersRequest)dicounterMessage;
        final CountingTask countingTask = countingTasks.get(aggregateRequest.getCountingTaskId());
        Validate.isTrue(countingTask != null, "Wrong countingTaskId. Check: " + aggregateRequest);
        final AggregateTriggersResponse aggregateResponse =
            AggregateTriggersResponse.builder()
                                     .countingTaskId(aggregateRequest.getCountingTaskId())
                                     .id(UUID.randomUUID())
                                     .roundNo(countingTask.getCurrentRound())
                                     .aggregatingLeaderAddress(aggregateRequest.getAggregatingLeaderAddress())
                                     .reportingNodeAddress(node.getNodeAddress())
                                     .numReceivedTriggers(countingTask.getLocallyObservedTriggers())
                                     .build();
        final Message message = Messages.createPayloadTransmissionMessage(ObjectMappers.writeValueAsString(aggregateResponse));
        node.sendMessage(aggregateRequest.getAggregatingLeaderAddress(), message);
    }

    private void onAggregateTriggersResponse(@NonNull final DicounterMessage dicounterMessage) {
        final AggregateTriggersResponse response = (AggregateTriggersResponse)dicounterMessage;
        final CountingTask countingTask = countingTasks.get(response.getCountingTaskId());
        Validate.isTrue(countingTask != null, "Wrong countingTaskId. Check: " + response);
        countingTask.setAggregatedTriggersOfCurrentRound(countingTask.getAggregatedTriggersOfCurrentRound() + response.getNumReceivedTriggers());
        if (countingTask.areAllTheTriggersObserved()) {
            countingTask.notifyCountingThresholdObserved();
        }
    }
}

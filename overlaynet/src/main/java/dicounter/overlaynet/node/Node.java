package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;
import static dicounter.overlaynet.communication.Messages.createOverlayNetControlMessage;

import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessagePayloads;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.node.connectivity.ConnectionChecker;
import dicounter.overlaynet.node.connectivity.OverlayNetEventHandler;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@Slf4j
@ToString(exclude = {"callback", "executorService"})
@EqualsAndHashCode(exclude = {"neighbors", "executorService", "callback", "isRun"})
public abstract class Node {

    @NonNull @Getter
    private final NodeAddress nodeAddress;
    @NonNull @Getter
    private SortedSet<NodeAddress> neighbors = new TreeSet<>();
    @NonNull
    private final ConnectionChecker connectionChecker;
    @NonNull
    private final OverlayNetEventHandler overlayNetEventHandler;

    @NonNull
    protected final ExecutorService executorService;

    @NonNull @Getter
    protected Consumer<Message> callback;

    private boolean isRun = false;
    @Getter @Setter(onParam_ = {@NonNull})
    private String gossipPayload;

    public Node(@NonNull final NodeAddress nodeAddress, @NonNull final ExecutorService executorService) {
        this.nodeAddress = nodeAddress;
        this.executorService = executorService;
        this.connectionChecker = new ConnectionChecker(this);
        this.overlayNetEventHandler = new OverlayNetEventHandler(this, this.connectionChecker);
        log.info("Node created with the address {}", nodeAddress);
    }

    public void setMessageCallback(@NonNull final Consumer<Message> callback) {
        this.callback = callback;
    }

    public void startMessageListening() {
        isRun = true;
    }

    public void stopMessageListening() {
        isRun = false;
    }

    public boolean isListeningMessage() {
        return isRun;
    }

    public abstract void sendMessage(NodeAddress nodeAddress, Message message);

    protected void addKnownAddress(@NonNull final NodeAddress nodeAddress) {
        this.neighbors.add(nodeAddress);
    }

    protected void addKnownAddresses(@NonNull final Collection<NodeAddress> nodeAddresses) {
        this.neighbors.addAll(nodeAddresses);
    }

    protected boolean isMessageGettingResponse(@NonNull final Message message) {
        return message.getType() == MessageType.JOIN_NODE || message.getType() == MessageType.PING;
    }

    Optional<Message> createResponseMessage(@NonNull final Message message) {
        if (message.getType() == MessageType.JOIN_NODE) {
            final Message responseMessage = createOverlayNetControlMessage(MessageType.RESPONSE_JOIN_NODE, this);
            return Optional.of(responseMessage);
        } else if (message.getType() == MessageType.PING) {
            return Optional.of(Message.PING_RESPONSE);
        }
        return Optional.empty();
    }

    void processReceivedMessage(@NonNull final Message message) {
        if (message.getType() == MessageType.JOIN_NODE || message.getType() == MessageType.RESPONSE_JOIN_NODE) {
            final String payload = message.getPayload();
            try {
                final SortedSet<NodeAddress> knownAddressesByAnotherNode = MessagePayloads.readKnownAddressesPayload(payload);
                addKnownAddresses(knownAddressesByAnotherNode);
                log.debug("Processes {} message {}. Updated known addresses: {}", message.getType(), message, getNeighbors());
            } catch (final Exception e) {
                throw logError(new IllegalArgumentException("Received JOIN_NODE with empty payload. Message: " + message, e));
            }
        }
        // MAKE SURE TO DO THIS AFTER PROCESSING ALL THE MESSAGES SO THAT CALLBACK CAN BE CALLED WITH LATEST STATES.
        if (this.callback != null) {
            log.debug("Calling a callback function with message {}", message);
            this.callback.accept(message);
        }
    }

}

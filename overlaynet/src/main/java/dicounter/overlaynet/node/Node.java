package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;
import static dicounter.overlaynet.communication.Messages.createOverlayNetControlMessage;

import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessagePayloads;
import dicounter.overlaynet.communication.MessageType;
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

@Slf4j
@ToString(exclude = {"callback", "executorService"})
@EqualsAndHashCode(exclude = {"knownNodeAddresses", "executorService", "callback", "isRun"})
public abstract class Node {

    @NonNull
    @Getter
    private final NodeAddress nodeAddress;
    @NonNull @Getter
    private final SortedSet<NodeAddress> knownNodeAddresses = new TreeSet<>();
    @NonNull
    protected final ExecutorService executorService;

    @NonNull
    protected Consumer<Message> callback;

    private boolean isRun = false;
    @Getter @Setter
    private String gossipPayload;

    public Node(@NonNull final NodeAddress nodeAddress, @NonNull final ExecutorService executorService) {
        this.nodeAddress = nodeAddress;
        this.executorService = executorService;
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
        this.knownNodeAddresses.add(nodeAddress);
    }

    protected void addKnownAddresses(@NonNull final Collection<NodeAddress> nodeAddresses) {
        this.knownNodeAddresses.addAll(nodeAddresses);
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
        if (this.callback != null) {
            log.debug("Calling a callback function with message {}", message);
            this.callback.accept(message);
        }
        if (message.getType() == MessageType.JOIN_NODE || message.getType() == MessageType.RESPONSE_JOIN_NODE) {
            final String payload = message.getPayload();
            try {
                final SortedSet<NodeAddress> knownAddressesByAnotherNode = MessagePayloads.readKnownAddressesPayload(payload);
                addKnownAddresses(knownAddressesByAnotherNode);
                log.debug("Processes {} message {}. Updated known addresses: {}", message.getType(), message, getKnownNodeAddresses());
            } catch (final Exception e) {
                throw logError(new IllegalArgumentException("Received JOIN_NODE with empty payload. Message: " + message, e));
            }
        }
    }

}

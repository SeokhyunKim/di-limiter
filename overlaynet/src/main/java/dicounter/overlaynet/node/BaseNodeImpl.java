package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;
import static dicounter.overlaynet.utils.Messages.createMessage;

import com.fasterxml.jackson.core.type.TypeReference;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.utils.ObjectMappers;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ToString(exclude = {"callback"})
@EqualsAndHashCode(exclude = {"knownNodeAddresses", "callback", "isRun"})
public abstract class BaseNodeImpl implements Node {

    @NonNull
    @Getter
    private final NodeAddress nodeAddress;
    @NonNull @Getter
    private final SortedSet<NodeAddress> knownNodeAddresses = new TreeSet<>();

    @NonNull
    protected Consumer<Message> callback;

    private boolean isRun = false;

    public BaseNodeImpl(@NonNull final NodeAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
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

    protected void addKnownAddress(@NonNull final NodeAddress nodeAddress) {
        this.knownNodeAddresses.add(nodeAddress);
    }

    protected void addKnownAddresses(@NonNull final Collection<NodeAddress> nodeAddresses) {
        this.knownNodeAddresses.addAll(nodeAddresses);
    }

    protected boolean isMessageGettingResponse(@NonNull final Message message) {
        return message.getType() == MessageType.JOIN_NODE;
    }

    Optional<Message> createResponseMessage(@NonNull final Message message) {
        if (message.getType() == MessageType.JOIN_NODE) {
            final Message responseMessage = createMessage(MessageType.RESPONSE_JOIN_NODE, this);
            return Optional.of(responseMessage);
        }
        return Optional.empty();
    }

    void processReceivedMessage(@NonNull final Message message) {
        if (this.callback != null) {
            log.debug("Calling a callback function with message {}", message);
            this.callback.accept(message);
        }
        if (message.getType() == MessageType.JOIN_NODE || message.getType() == MessageType.RESPONSE_JOIN_NODE) {
            final String payLoad = message.getPayload();
            if (StringUtils.isEmpty(payLoad)) {
                throw logError(new IllegalArgumentException("Received JOIN_NODE with empty payload. Message: " + message));
            } else {
                final SortedSet<NodeAddress> knownAddressesByAnotherNode =
                        ObjectMappers.readValue(payLoad, new TypeReference<SortedSet<NodeAddress>>() {});
                addKnownAddresses(knownAddressesByAnotherNode);
                log.debug("Processes {} message {}. Updated known addresses: {}", message.getType(), message, getKnownNodeAddresses());
            }
        }
    }

}

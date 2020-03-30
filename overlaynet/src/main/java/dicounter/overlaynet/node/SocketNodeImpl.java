package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;
import static dicounter.overlaynet.utils.Messages.createMessage;

import com.fasterxml.jackson.core.type.TypeReference;
import dicounter.overlaynet.communication.socket.ExchangeMessage;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.exception.NetworkException;
import dicounter.overlaynet.utils.ObjectMappers;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@EqualsAndHashCode
public class SocketNodeImpl implements Node {

    @NonNull @Getter
    private final NodeAddress nodeAddress;
    @NonNull @Getter
    private final SortedSet<NodeAddress> knownNodeAddresses = new TreeSet<>();

    @NonNull
    private Consumer<Message> callback;
    @NonNull
    private final ExecutorService executorService;

    private boolean isRun = false;

    public SocketNodeImpl(@NonNull final NodeAddress nodeAddress, @NonNull final ExecutorService executorService) {
        this.nodeAddress = nodeAddress;
        this.knownNodeAddresses.add(this.nodeAddress);
        this.executorService = executorService;
        log.info("Node created with the address {}", nodeAddress);
    }

    public void sendMessage(@NonNull final NodeAddress nodeAddress, @NonNull final Message message) {
        message.setSender(this.nodeAddress);
        message.setReceiver(nodeAddress);
        if (isMessageGettingResponse(message)) {
            final ExchangeMessage exchangeMessage = ExchangeMessage.create(nodeAddress);
            exchangeMessage.sendMessage(message).receiveMessage(callback);
            final List<Message> receivedMessages = exchangeMessage.getReceivedMessages();
            for (final Message receivedMessage : receivedMessages) {
                processReceivedMessage(receivedMessage);
            }
            exchangeMessage.close();
            ExchangeMessage.create(nodeAddress).sendMessage(message).receiveMessage(callback).close();
        } else {
            ExchangeMessage.create(nodeAddress).sendMessage(message).close();
        }
    }

    private boolean isMessageGettingResponse(@NonNull final Message message) {
        return message.getType() == MessageType.JOIN_NODE;
    }

    public void setMessageCallback(@NonNull final Consumer<Message> callback) {
        this.callback = callback;
    }

    public void startMessageListening() {
        isRun = true;
        executorService.submit(() -> {
            try {
                final ServerSocket serverSocket = new ServerSocket(nodeAddress.getPort());
                log.info("Starting server socket at node {}", nodeAddress);
                while (isRun) {
                    final Socket socket = serverSocket.accept();
                    executorService.submit(() -> {
                        final ExchangeMessage exchangeMessage = ExchangeMessage.create(socket);
                        exchangeMessage.receiveMessage(callback);
                        final List<Message> messages = exchangeMessage.getReceivedMessages();
                        for (final Message message : messages) {
                            log.debug("Processing a message: {}", message);
                            processReceivedMessage(message);
                            final Optional<Message> responseOpt = createResponseMessage(message);
                            responseOpt.ifPresent(exchangeMessage::sendMessage);
                        }
                        exchangeMessage.close();
                    });
                }
            } catch (final Throwable e) {
                throw logError(new NetworkException("Caught an exception while creating a server socket at node " + nodeAddress, e));
            }
        });
        log.info("Stopping server socket at node {}", nodeAddress);
    }

    public void stopMessageListening() {
        isRun = false;
    }

    public boolean isListeningMessage() {
        return isRun;
    }

    private void processReceivedMessage(@NonNull final Message message) {
        if (message.getType() == MessageType.JOIN_NODE || message.getType() == MessageType.RESPONSE_JOIN_NODE) {
            final String payLoad = message.getPayload();
            if (StringUtils.isEmpty(payLoad)) {
                throw logError(new IllegalArgumentException("Received JOIN_NODE with empty payload. Message: " + message));
            } else {
                final SortedSet<NodeAddress> knownAddressesByAnotherNode =
                        ObjectMappers.readValue(payLoad, new TypeReference<SortedSet<NodeAddress>>() {});
                this.knownNodeAddresses.addAll(knownAddressesByAnotherNode);
                log.debug("Processes {} message {}. Updated known addresses: {}", message.getType(), message, this.knownNodeAddresses);
            }
        }
    }

    private Optional<Message> createResponseMessage(@NonNull final Message message) {
        if (message.getType() == MessageType.JOIN_NODE) {
            final Message responseMessage = createMessage(MessageType.RESPONSE_JOIN_NODE, this);
            return Optional.of(responseMessage);
        }
        return Optional.empty();
    }
}

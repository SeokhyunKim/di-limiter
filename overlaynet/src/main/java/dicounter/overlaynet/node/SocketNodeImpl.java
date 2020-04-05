package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;

import dicounter.overlaynet.communication.socket.ExchangeMessage;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.exception.NetworkException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(callSuper = true, exclude = {"executorService"})
@EqualsAndHashCode(callSuper = true, exclude = {"executorService"})
public class SocketNodeImpl extends BaseNodeImpl {

    @NonNull
    private final ExecutorService executorService;

    private ServerSocket serverSocket;

    public SocketNodeImpl(@NonNull final NodeAddress nodeAddress, @NonNull final ExecutorService executorService) {
        super(nodeAddress);
        addKnownAddress(nodeAddress);
        this.executorService = executorService;
        log.info("Node created with the address {}", nodeAddress);
    }

    @Override
    public void sendMessage(@NonNull final NodeAddress nodeAddress, @NonNull final Message message) {
        message.setSender(getNodeAddress());
        message.setReceiver(nodeAddress);
        log.debug("Send a message {}", message);
        if (isMessageGettingResponse(message)) {
            final ExchangeMessage exchangeMessage = ExchangeMessage.create(nodeAddress);
            exchangeMessage.sendMessage(message).receiveMessage();
            final List<Message> receivedMessages = exchangeMessage.getReceivedMessages();
            for (final Message receivedMessage : receivedMessages) {
                processReceivedMessage(receivedMessage);
            }
            exchangeMessage.close();
        } else {
            ExchangeMessage.create(nodeAddress).sendMessage(message).close();
        }
        log.debug("Sending a message is done. Message: {}", message);
    }

    @Override
    public void startMessageListening() {
        super.startMessageListening();
        try {
            this.serverSocket = new ServerSocket(getNodeAddress().getPort());
        } catch (final IOException e) {
            throw logError(new NetworkException("Failed to create ServerSocket at " + getNodeAddress(), e));
        }
        executorService.submit(() -> {
            try {
                log.info("Starting server socket at node {}", getNodeAddress());
                while (isListeningMessage()) {
                    final Socket socket = serverSocket.accept();
                    executorService.submit(() -> {
                        final ExchangeMessage exchangeMessage = ExchangeMessage.create(socket);
                        exchangeMessage.receiveMessage();
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
                log.info("Stop message listening at {}", getNodeAddress());
            } catch (final SocketException e) {
                if (isListeningMessage()) {
                    throw logError(new NetworkException("Caught a SocketException in message listening loop at " + getNodeAddress(), e));
                } else {
                    log.info("ServerSocket is closed and caught SocketException at {}. Exiting message listening loop.", getNodeAddress());
                }
            } catch (final Throwable e) {
                throw logError(new NetworkException("Caught an exception while creating a server socket at node " + getNodeAddress(), e));
            }
        });
        log.info("Stopping server socket at node {}", getNodeAddress());
    }

    @Override
    public void stopMessageListening() {
        super.stopMessageListening();
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (final IOException e) {
                log.error("Caught an exception while closing serverSocket at {}", getNodeAddress(), e);
            }
        }
    }
}

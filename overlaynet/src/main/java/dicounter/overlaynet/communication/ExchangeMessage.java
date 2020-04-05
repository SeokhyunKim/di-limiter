package dicounter.overlaynet.communication;

import dicounter.overlaynet.exception.NetworkException;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.Exceptions;
import dicounter.overlaynet.utils.ObjectMappers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ExchangeMessage {

    @Nullable
    private final NodeAddress nodeAddress;
    @NonNull
    private final Socket socket;
    @NonNull
    private final DataOutputStream dataOutputStream;
    @NonNull
    private final DataInputStream dataInputStream;
    @NonNull
    private final List<Message> receivedMessages = new ArrayList<>();

    private ExchangeMessage(@Nullable final NodeAddress nodeAddress, @NonNull final Socket socket) {
        this.nodeAddress = nodeAddress;
        this.socket = socket;
        try {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (final IOException e) {
            throw Exceptions.logError(new NetworkException("Caught an exception while creating DataOutputStream or DataInputStream " +
                                                      "using address, " + nodeAddress, e));
        }
    }

    public ExchangeMessage sendMessage(@NonNull final Message message) {
        try {
            log.debug("Sending a message: {}", message);
            dataOutputStream.writeUTF(ObjectMappers.writeValueAsString(message));
            return this;
        } catch (final IOException e) {
            throw Exceptions.logError(new NetworkException("Caught an exception while sending message. Address: " + getNodeAddressString() +
                                                      ", Message: "  + message, e));
        }
    }

    public ExchangeMessage receiveMessage() {
        try {
            final String receivedString = dataInputStream.readUTF();
            final Message message = ObjectMappers.readValue(receivedString, Message.class);
            receivedMessages.add(message);
            return this;
        } catch (final IOException e) {
            throw Exceptions.logError(new NetworkException("Caught an exception while receiving a message", e));
        }
    }

    public void close() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            receivedMessages.clear();
        } catch (final IOException e) {
            throw Exceptions.logError(new NetworkException("Caught an exception while closing input/output streams and socket. " +
                                                      "Address: " + getNodeAddressString(), e));
        }
    }

    private String getNodeAddressString() {
        if (nodeAddress != null) {
            return nodeAddress.toString();
        }
        return socket.getInetAddress().toString();
    }

    public static ExchangeMessage create(@NonNull final NodeAddress nodeAddress) {
        final Socket socket;
        try {
            final InetAddress address = InetAddress.getByName(nodeAddress.getIpAddress());
            socket = new Socket(address, nodeAddress.getPort());
        } catch (final UnknownHostException e) {
            throw Exceptions.logError(new NetworkException("Unknown host: " + nodeAddress, e));
        } catch (final IOException e) {
            throw Exceptions.logError(new NetworkException("Caught an exception while creating a socket to " + nodeAddress, e));
        }
        return new ExchangeMessage(nodeAddress, socket);
    }

    public static ExchangeMessage create(@NonNull final Socket socket) {
        return new ExchangeMessage(null, socket);
    }
}

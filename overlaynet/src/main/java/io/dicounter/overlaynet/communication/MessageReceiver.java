package io.dicounter.overlaynet.communication;

import io.dicounter.overlaynet.exceptions.NetworkException;
import io.dicounter.overlaynet.node.Node;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MessageReceiver {

    @NonNull
    private final ExecutorService executorService;
    @NonNull
    private final int port;
    @NonNull
    private final Node parentNode;

    private boolean isRun = false;

    public void start() {
        isRun = true;
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            log.info("Starting server socket at node {} with port {}", parentNode, port);
            while (isRun) {
                final Socket socket = serverSocket.accept();
                final MessageHandler messageHandler = new MessageHandler(socket);
                executorService.submit(messageHandler::receiveMessage);
            }
        } catch (final IOException e) {
            throw new NetworkException("Caught an exception while creating a server socket with port " + port, e);
        }
        log.info("Stopping server socket at node {} with port {}", parentNode, port);
    }

    public void stop() {
        isRun = false;
    }
}

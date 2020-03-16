package io.dicounter.overlaynet.communication;

import java.net.Socket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MessageHandler {
    @NonNull
    private final Socket socket;

    public void receiveMessage() {

    }

    public void sendMessage(@NonNull final Message message) {

    }
}

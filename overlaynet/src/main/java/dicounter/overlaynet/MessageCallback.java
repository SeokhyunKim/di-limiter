package dicounter.overlaynet;

import dicounter.overlaynet.communication.Message;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MessageCallback {
    private List<Message> received = new ArrayList<>();
    public void callback(final Message msg) {
        received.add(msg);
        log.info("New message added to MessageCallback {}. Current received messages: {}", this, received);
    }
}

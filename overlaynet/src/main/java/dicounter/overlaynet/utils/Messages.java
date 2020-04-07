package dicounter.overlaynet.utils;

import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.communication.MessageType;
import dicounter.overlaynet.node.Node;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Messages {

    public static Message createOverlayNetControlMessage(@NonNull final MessageType messageType,
                                                         @Nullable final Node node) {
        if (messageType == MessageType.JOIN_NODE || messageType == MessageType.RESPONSE_JOIN_NODE) {
            Validate.isTrue(node != null, "Null node is given for " + messageType);
            return Message.builder()
                          .type(messageType)
                          .payload(ObjectMappers.writeValueAsString(node.getKnownNodeAddresses()))
                          .build();
        } else if (messageType == MessageType.PING) {
            return Message.PING;
        }
        throw new RuntimeException("Not supported message type: " + messageType);
    }

    public static Message createPayloadTransmissionMessage(@NonNull final String payload) {
        return Message.builder()
                      .type(MessageType.PAYLOAD_TRANSMISSION)
                      .payload(payload)
                      .build();
    }

    public static Message createExceptionMessage(@NonNull final String message) {
        return Message.builder()
                      .type(MessageType.EXCEPTION)
                      .payload(message)
                      .build();
    }
}

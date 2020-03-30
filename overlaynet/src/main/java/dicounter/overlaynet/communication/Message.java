package dicounter.overlaynet.communication;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dicounter.overlaynet.node.NodeAddress;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
@EqualsAndHashCode
@JsonDeserialize(builder = Message.MessageBuilder.class)
public class Message {

    public static Message PING = Message.builder()
                                        .type(MessageType.PING)
                                        .build();

    @Nullable @Setter
    private NodeAddress sender;
    @Nullable @Setter
    private NodeAddress receiver;
    @NonNull
    private final MessageType type;
    @Nullable
    private final String payload;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class MessageBuilder {
    }
}

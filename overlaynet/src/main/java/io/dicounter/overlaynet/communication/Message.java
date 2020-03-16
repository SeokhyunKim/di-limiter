package io.dicounter.overlaynet.communication;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
@JsonDeserialize(builder = Message.MessageBuilder.class)
public class Message {
    @NonNull
    private final MessageType type;
    @Nullable
    private final String payload;

    public String toString() {
        if (payload != null) {
            return "[" + type + " : " + payload + "]";
        }
        return "[" + type + "]";
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class MessageBuilder {
    }
}

package dicounter.message;

import dicounter.overlaynet.utils.ObjectMappers;
import java.util.UUID;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class DicounterMessageTest {

    @Test
    public void serialization_deserialization_DetectMessage() {
        DetectMessage detectMessage = DetectMessage.builder()
                                                   .countingTaskId(UUID.randomUUID())
                                                   .localThresholdValue(10)
                                                   .roundNo(1)
                                                   .build();
        String serialized = ObjectMappers.writeValueAsString(detectMessage);
        System.out.println(serialized);
        DicounterMessage dicounterMessage = ObjectMappers.readValue(serialized, DicounterMessage.class);
        DetectMessage deserialized = (DetectMessage)dicounterMessage;
        Assertions.assertEquals(detectMessage, deserialized);
    }

    @Test
    public void serialization_deserialization_LeaderDetectMessage() {
        LeaderDetectMessage leaderDetectMessage = LeaderDetectMessage.builder()
                                                                     .countingTaskId(UUID.randomUUID())
                                                                     .id(UUID.randomUUID())
                                                                     .localThresholdValue(10)
                                                                     .roundNo(1)
                                                                     .build();
        String serialized = ObjectMappers.writeValueAsString(leaderDetectMessage);
        System.out.println(serialized);
        DicounterMessage dicounterMessage = ObjectMappers.readValue(serialized, DicounterMessage.class);
        LeaderDetectMessage deserialized = (LeaderDetectMessage)dicounterMessage;
        Assertions.assertEquals(leaderDetectMessage, deserialized);
    }
}

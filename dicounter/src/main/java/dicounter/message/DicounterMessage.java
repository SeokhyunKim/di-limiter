package dicounter.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = DetectMessage.class, name = "DETECT"),
               @JsonSubTypes.Type(value = LeaderDetectMessage.class, name = "LEADER_DETECT"),
              })
public interface DicounterMessage {

    DicounterMessageType getType();
}
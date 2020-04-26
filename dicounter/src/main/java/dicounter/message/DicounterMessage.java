package dicounter.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = DetectMessage.class, name = "DETECT"),
               @JsonSubTypes.Type(value = LeaderDetectMessage.class, name = "LEADER_DETECT"),
               @JsonSubTypes.Type(value = LeaderAnnounceMessage.class, name = "LEADER_ANNOUNCE"),
               @JsonSubTypes.Type(value = AggregateTriggersRequest.class, name = "AGGREGATE_TRIGGERS_REQUEST"),
               @JsonSubTypes.Type(value = AggregateTriggersResponse.class, name = "AGGREGATE_TRIGGERS_RESPONSE"),
              })
public interface DicounterMessage {

    DicounterMessageType getType();
}
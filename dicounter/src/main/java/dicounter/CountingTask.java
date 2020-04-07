package dicounter;

import java.util.UUID;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class CountingTask {

    private final UUID id;
    private final long notificationThreshold;
    private final long numParticipatingNodes;
}

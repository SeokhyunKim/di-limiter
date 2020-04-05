package dicounter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Dicounter {

    public enum COMMUNICATION_TYPE {
        SOCKET
    }

    private final Map<UUID, Trigger> registeredTriggers = new HashMap<>();

//    public void initialize(Set<>)
//
//    public void createDistributedCountingNetwork()

    public UUID registerNewTrigger(final Consumer<Long> countingCallback) {
        final Trigger trigger = Trigger.create(countingCallback);
        registeredTriggers.put(trigger.getTriggerId(), trigger);
        return trigger.getTriggerId();
    }

    public void putTrigger(final UUID triggerId) {

    }
}

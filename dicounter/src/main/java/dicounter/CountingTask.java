package dicounter;

import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

@Slf4j
@ToString
@EqualsAndHashCode
public class CountingTask {

    @Getter
    @EqualsAndHashCode.Include
    private final UUID id;

    @Getter
    @EqualsAndHashCode.Exclude
    private final long notificationThresholdForTriggers;

    @Nullable
    @Setter
    @EqualsAndHashCode.Exclude
    private Consumer<CountingTask> notificationCallback;

    // for now, this is assumed to be fixed which is an unrealistic scenario. Churning of nodes will be considered before a stable release.
    @Setter @Getter
    @EqualsAndHashCode.Exclude
    private long numParticipatingNodes;

    @Getter
    @EqualsAndHashCode.Exclude
    private long locallyObservedTriggers;

    @Getter
    @EqualsAndHashCode.Exclude
    private long locallyObservedDetectMessages;

    @Getter
    @EqualsAndHashCode.Exclude
    private long unreportedLocallyObservedDetectMessages;

    @Getter
    @EqualsAndHashCode.Exclude
    private long locallyObservedLeaderDetectMessages;

    @Getter
    @EqualsAndHashCode.Exclude
    private long unreportedLocallyObservedLeaderDetectMessages;

    @Setter @Getter
    @EqualsAndHashCode.Exclude
    private long aggregatedTriggersOfCurrentRound;

    @EqualsAndHashCode.Exclude
    private Stack<Long> remainingTriggersToObserve = new Stack<>();

    public CountingTask(final long notificationThresholdForTriggers, @NonNull final Consumer<CountingTask> notificationCallback) {
        this(UUID.randomUUID(), notificationThresholdForTriggers, notificationCallback);
    }

    public CountingTask(@NonNull final UUID id, final long notificationThresholdForTriggers,
                        @Nullable final Consumer<CountingTask> notificationCallback) {
        this.id = id;
        this.notificationThresholdForTriggers = notificationThresholdForTriggers;
        this.notificationCallback = notificationCallback;
    }

    public void startCounting() {
        remainingTriggersToObserve.clear();
        remainingTriggersToObserve.add(this.notificationThresholdForTriggers);
    }

    public void startNewRound(final long realObservedEventsSoFar) {
        remainingTriggersToObserve.add(this.notificationThresholdForTriggers - realObservedEventsSoFar);
    }

    public int getCurrentRound() {
        return remainingTriggersToObserve.size();
    }

    public long getRemainingTriggersToObserveInCurrentRound() {
        Validate.isTrue(!remainingTriggersToObserve.isEmpty(), "Not started event counting yet. Call startCounting() first");
        return remainingTriggersToObserve.peek();
    }

    public double getDetectMessageGenerationProbability() {
        Validate.isTrue(!remainingTriggersToObserve.isEmpty(), "Not started event counting yet. Call startCounting() first");
        final long currentThreshold = remainingTriggersToObserve.peek();
        return (double)numParticipatingNodes / currentThreshold;
    }

    public long getDetectMessageLocalThreshold(final int numLeaderNodes) {
        return (long) Math.floor(numParticipatingNodes / (2.0 * numLeaderNodes));
    }

    public void increaseLocallyObservedTriggers() {
        ++locallyObservedTriggers;
    }

    public void increaseLocallyObservedDetectMessages() {
        ++unreportedLocallyObservedDetectMessages;
        ++locallyObservedDetectMessages;
    }

    public void decreaseUnreportedLocallyObservedDetectMessages(final long n) {
        unreportedLocallyObservedDetectMessages = Math.max(unreportedLocallyObservedDetectMessages - n, 0);
    }

    public void increaseLocallyObservedLeaderDetectMessages() {
        ++unreportedLocallyObservedLeaderDetectMessages;
        ++locallyObservedLeaderDetectMessages;
    }

    public void decreaseUnreportedLocallyObservedLeaderDetectMessages(final long n) {
        unreportedLocallyObservedLeaderDetectMessages = Math.max(unreportedLocallyObservedLeaderDetectMessages - n, 0);
    }

    public boolean areAllTheTriggersObserved() {
        return aggregatedTriggersOfCurrentRound >= notificationThresholdForTriggers;
    }

    public void notifyCountingThresholdObserved() {
        notificationCallback.accept(this);
    }
}

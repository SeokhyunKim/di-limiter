package dicounter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class CountingTaskTest {

    @Test
    public void getDetectMessageLocalThreshold_producesThresholdsCorrectly() {
        CountingTask countingTask = new CountingTask(1000L, ct -> {});
        // floor(15 / (2 * 10)) = 0
        countingTask.setNumParticipatingNodes(15L);
        Assertions.assertEquals(countingTask.getDetectMessageLocalThreshold(10), 0L);
        // floor(20 / (2 * 10)) = 1
        countingTask.setNumParticipatingNodes(20L);
        Assertions.assertEquals(countingTask.getDetectMessageLocalThreshold(10), 1L);
        // floor(25 / (2 * 10)) = 1
        countingTask.setNumParticipatingNodes(25L);
        Assertions.assertEquals(countingTask.getDetectMessageLocalThreshold(10), 1L);
        // floor(45 / (2 * 10)) = 2
        countingTask.setNumParticipatingNodes(45L);
        Assertions.assertEquals(countingTask.getDetectMessageLocalThreshold(10), 2L);
    }
}

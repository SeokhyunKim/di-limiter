package dicounter.utils;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class ProbabilitiesTest {

    @Test
    public void binomialTrial_producesReasonableDistribution() {
        int trueCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (Probabilities.binomialTrial(0.9)) {
                ++trueCount;
            }
        }
        Assertions.assertTrue(trueCount > 800);
    }

    @Test
    public void randomBetween_producesRandomNumbersCorrectly() {
        int total = 0;
        for (int i = 0; i < 1000; i++) {
            int n = Probabilities.randomBetween(11, 20);
            total += n;
            Assertions.assertTrue(11 <= n && n < 20);
        }
        int avg = total / 1000;
        Assertions.assertTrue(14 <= avg && avg <= 16);
    }
}

package dicounter.utils;

import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Probabilities {

    private static Random random = new Random();

    public static boolean binomialTrial(final double p) {
        return random.nextDouble() < p;
    }

    public static int randomBetween(int min, int max) {
        return min + (int)(random.nextDouble() * (max - min));
    }

}

package io.dilimiter;

import static java.lang.Thread.sleep;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class DistributedLimiterTest {

    @Test
    public void test_basicClusterFormation() throws InterruptedException {
        class LimiterWithPort implements Runnable {
            private final int port;
            public LimiterWithPort(final int p) {
                this.port = p;
            }
            @Override
            public void run() {
                final DistributedLimiter limiter = DistributedLimiter.create(Config.builder().port(port).build());
                while (limiter.getNumUpMembers() < 3) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while running...");
                    }
                }
                limiter.logMemberInfosString();
            }
        }
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(new LimiterWithPort(2551));
        executor.execute(new LimiterWithPort(2552));
        executor.execute(new LimiterWithPort(0));
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}

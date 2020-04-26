package dicounter;

import com.google.common.collect.Sets;
import dicounter.overlaynet.communication.CommunicationType;
import dicounter.overlaynet.node.NodeAddress;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class DicounterTest {

    @Test
    public void countingTest_10000Messages_100Nodes() throws InterruptedException {
        Set<Dicounter> dicounters = new HashSet<>();
        Dicounter seed = new Dicounter(NodeAddress.builder()
                                                       .ipAddress("127.0.0.1")
                                                       .port(3000)
                                                       .build(),
                                            CommunicationType.HTTP, Sets.newHashSet(), 2, 5);

        for (int i = 1; i < 10000; i++) {
            Dicounter dicounter = new Dicounter(NodeAddress.builder()
                                                           .ipAddress("127.0.0.1")
                                                           .port(3000 + i)
                                                           .build(),
                                                CommunicationType.HTTP, Sets.newHashSet(seed.getNodeAddress()), 2, 5);
            dicounters.add(dicounter);
            if (dicounter.getKnownNodeAddresses().size() < i + 1) {
                Thread.sleep(100L);
            }
        }
    }
}

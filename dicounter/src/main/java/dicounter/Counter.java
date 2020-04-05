package dicounter;

import dicounter.overlaynet.node.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Counter {
    private final Node node;
    private boolean isLeader;


}

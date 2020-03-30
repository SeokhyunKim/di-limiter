package dicounter.overlaynet.node;

import dicounter.overlaynet.communication.Message;
import java.util.SortedSet;
import java.util.function.Consumer;

public interface Node {

    NodeAddress getNodeAddress();

    SortedSet<NodeAddress> getKnownNodeAddresses();

    void sendMessage(NodeAddress nodeAddress, Message message);

    void setMessageCallback(Consumer<Message> callback);
}

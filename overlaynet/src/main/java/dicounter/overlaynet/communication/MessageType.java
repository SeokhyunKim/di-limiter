package dicounter.overlaynet.communication;

public enum MessageType {
    PING,
    PING_RESPONSE,
    JOIN_NODE,
    RESPONSE_JOIN_NODE,
    PAYLOAD_TRANSMISSION,
    GOSSIP,
    EXCEPTION,
}

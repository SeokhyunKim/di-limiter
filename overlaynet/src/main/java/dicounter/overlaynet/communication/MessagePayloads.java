package dicounter.overlaynet.communication;

import static dicounter.overlaynet.utils.Exceptions.logError;

import com.fasterxml.jackson.core.type.TypeReference;
import dicounter.overlaynet.node.NodeAddress;
import dicounter.overlaynet.utils.ObjectMappers;
import java.util.SortedSet;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class MessagePayloads {

    public static String createKnownAddressesPayload(@NonNull final SortedSet<NodeAddress> knownAddresses) {
        return ObjectMappers.writeValueAsString(knownAddresses);
    }

    public static SortedSet<NodeAddress> readKnownAddressesPayload(@NonNull final String payload) {
        if (StringUtils.isEmpty(payload)) {
            throw logError(new IllegalArgumentException("Empty payload"));
        } else {
            return ObjectMappers.readValue(payload, new TypeReference<SortedSet<NodeAddress>>() {});
        }
    }

}

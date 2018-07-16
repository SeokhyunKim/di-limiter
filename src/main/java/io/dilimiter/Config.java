package io.dilimiter;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Config {

    private final String hostName;
    private final Integer port;
    private final Set<String> seedNodes;
    private final String actorSystemName;

}

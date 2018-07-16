package io.dilimiter.cluster;

import akka.cluster.Member;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class MemberInfo {

    private final String host;
    private final Integer port;
    private final String protocol;
    private final String system;
    private final String hostPort;
    private final String status;

    public static MemberInfo fromAkkaMember(Member member) {
        final String host = Optional.ofNullable(member.address().host().get()).orElse("");
        final Integer port = (Integer)Optional.ofNullable(member.address().port().get()).orElse(new Integer(0));
        final String protocol = Optional.ofNullable(member.address().protocol()).orElse("");
        final String system = Optional.ofNullable(member.address().system()).orElse("");
        final String hostPort = member.address().hostPort();
        final String status = member.status().toString();
        return MemberInfo.builder()
                .host(host)
                .port(port)
                .protocol(protocol)
                .system(system)
                .hostPort(hostPort)
                .status(status)
                .build();
    }
}

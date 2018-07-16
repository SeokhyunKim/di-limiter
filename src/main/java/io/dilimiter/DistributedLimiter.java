package io.dilimiter;

import akka.actor.ActorSystem;
import io.dilimiter.cluster.ClusterInfo;
import io.dilimiter.cluster.ClusteringActor;
import io.dilimiter.cluster.MemberInfo;
import io.dilimiter.utils.AkkaConfigBuilder;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class DistributedLimiter {

    @NonNull
    private final ActorSystem system;
    @NonNull
    private final String hostPort;
    @NonNull
    private final ClusterInfo clusterInfo;

    public int getNumUpMembers() {
        return clusterInfo.getNumMembers();
    }

    public Set<MemberInfo> getMemberInfos() {
        return clusterInfo.getMemberInfos();
    }

    public void logMemberInfosString() {
        final Set<MemberInfo> memberInfos = getMemberInfos();
        final StringBuilder sb = new StringBuilder();
        memberInfos.forEach(m -> sb.append(m.toString()).append("\n"));
        log.info("\nClusterInfo: {}\n{}", hostPort, memberInfos.toString());
    }

    public static DistributedLimiter
    create(final Config config) {
        AkkaConfigBuilder akkaConfigBuilder = new AkkaConfigBuilder();
        com.typesafe.config.Config akkaConfig = akkaConfigBuilder.create(config);
        log.info("\nNew ActorSystem config:\n{}", akkaConfig.toString());

        final ActorSystem system = ActorSystem.create(akkaConfigBuilder.getLastActorSystemName().get(), akkaConfig);
        final ClusterInfo clusterInfo = new ClusterInfo();
        system.actorOf(ClusteringActor.props(clusterInfo), "clusteringActor");

        final String hostPort = akkaConfigBuilder.getLastHostPort().get();

        return DistributedLimiter.builder()
                .system(system)
                .hostPort(hostPort)
                .clusterInfo(clusterInfo)
                .build();
    }

}

package io.dilimiter.cluster;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import scala.collection.JavaConverters;

public class ClusteringActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cluster cluster = Cluster.get(getContext().getSystem());
    private final ClusterInfo clusterInfo;

    public static Props props(@NonNull final ClusterInfo clusterInfo) {
        return Props.create(ClusteringActor.class, () -> new ClusteringActor(clusterInfo));
    }

    public ClusteringActor(@NonNull final ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MemberUp.class, mUp -> {
                    log.info("Member is up: {}", mUp.member());
                    updateClusterInfo();
                })
                .match(UnreachableMember.class, mUnreachable -> {
                    log.info("Member detected as unreachable: {}", mUnreachable.member());
                    updateClusterInfo();
                })
                .match(MemberRemoved.class, mRemoved -> {
                    log.info("Member is removed: {}", mRemoved.member());
                    updateClusterInfo();
                })
                .match(MemberEvent.class, msg -> {})
                .build();
    }

    private void updateClusterInfo() {
        final Collection<Member> members = JavaConverters.asJavaCollection(cluster.state().members());
        final Set<Member> upMembers = new HashSet<>();
        for (final Member m : members) {
            if (m.status() == MemberStatus.up()) {
                upMembers.add(m);
            }
        }
        log.info("Updating cluster info: {}", upMembers.toString());
        this.clusterInfo.setUpMembers(upMembers);
    }
}
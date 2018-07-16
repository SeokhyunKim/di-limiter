package io.dilimiter.cluster;

import akka.cluster.Member;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class ClusterInfo {

    private Set<Member> upMembers = new HashSet<>();

    public int getNumMembers() {
        return upMembers.size();
    }

    public Set<MemberInfo> getMemberInfos() {
        return upMembers.stream()
                .map(MemberInfo::fromAkkaMember)
                .collect(Collectors.toSet());
    }

}

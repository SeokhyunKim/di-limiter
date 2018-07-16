package io.dilimiter.utils;

import static lombok.AccessLevel.PRIVATE;

import io.dilimiter.cluster.ClusterInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Instances {

    private static final ClusterInfo clusterInfo = new ClusterInfo();

    public static ClusterInfo clusterInfo() {
        return clusterInfo;
    }
}

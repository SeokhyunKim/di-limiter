package io.dilimiter.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class AkkaConfigBuilder {

    private final static String DEFAULT_ACTOR_SYSTEM_NAME = "di-limiter";

    private Config lastConfig;
    private String lastActorSystemName;

    public Config create(io.dilimiter.Config config) {
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(config.getHostName())) {
            sb.append(buildHostnameConfig(config.getHostName()));
        }
        if (config.getPort() != null) {
            sb.append(buildPortConfig(Integer.toString(config.getPort())));
        }
        this.lastActorSystemName = Optional.ofNullable(config.getActorSystemName()).orElse(DEFAULT_ACTOR_SYSTEM_NAME);
        if (CollectionUtils.isNotEmpty(config.getSeedNodes())) {
            sb.append(buildSeedNodesConfig(lastActorSystemName, config.getSeedNodes()));
        } else {
            System.setProperty("DEFAULT_ACTOR_SYSTEM_NAME", DEFAULT_ACTOR_SYSTEM_NAME);
        }
        this.lastConfig = ConfigFactory.parseString(sb.toString()).withFallback(ConfigFactory.load());
        return this.lastConfig;
    }

    public Optional<Config> getLastConfig() {
        return Optional.ofNullable(lastConfig);
    }

    public Optional<String> getLastActorSystemName() {
        return Optional.ofNullable(lastActorSystemName);
    }

    public Optional<String> getLastHostPort() {
        if (lastConfig == null) {
            return Optional.empty();
        }
        final String host = this.lastConfig.getString("akka.remote.netty.tcp.hostname");
        final String port = this.lastConfig.getString("akka.remote.netty.tcp.port");
        return Optional.of(host + ":" + port);
    }

    private String buildHostnameConfig(@NonNull final String hostName) {
        return "akka.remote.netty.tcp.hostname=" + hostName + "\n" +
                       "akka.remote.artery.canonical.hostname=" + hostName + "\n";
    }

    private String buildPortConfig(@NonNull final String port) {
        return "akka.remote.netty.tcp.port=" + port + "\n" +
                       "akka.remote.artery.canonical.port=" + port + "\n";
    }

    private String buildSeedNodesConfig(@NonNull final String actorSystemName, @NonNull final Set<String> seedNodes) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> seedItr = seedNodes.iterator();
        while (seedItr.hasNext()) {
            sb.append("\"akka://").append(actorSystemName).append("@").append(seedItr.next()).append("\"");
            if (seedItr.hasNext()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        return "cluster.seed-nodes = [\n" + sb.toString() + "]" + "\n";
    }

}

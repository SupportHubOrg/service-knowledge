package com.supporthub.knowledge.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "application.kafka.topics")
public record KafkaTopicProvisioningProperties(
        TopicDefaults defaults,
        Map<String, String> owned
) {

    public record TopicDefaults(
            int partitions,
            int replicas,
            int minInSyncReplicas
    ) {
    }
}

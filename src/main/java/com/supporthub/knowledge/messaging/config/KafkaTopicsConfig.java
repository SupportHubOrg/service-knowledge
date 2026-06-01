package com.supporthub.knowledge.messaging.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {

    private final KafkaTopicProvisioningProperties properties;

    @Bean
    KafkaAdmin.NewTopics knowledgeTopics() {
        var defaults = properties.defaults();
        return new KafkaAdmin.NewTopics(properties.owned().values().stream()
                .map(topic -> TopicBuilder.name(topic)
                        .partitions(defaults.partitions())
                        .replicas(defaults.replicas())
                        .configs(Map.of(
                                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                                String.valueOf(defaults.minInSyncReplicas())
                        ))
                        .build())
                .toArray(NewTopic[]::new));
    }
}

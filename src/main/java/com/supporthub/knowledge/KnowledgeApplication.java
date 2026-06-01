package com.supporthub.knowledge;

import com.supporthub.knowledge.messaging.config.KafkaDltProperties;
import com.supporthub.knowledge.messaging.config.KafkaTopicProvisioningProperties;
import com.supporthub.knowledge.messaging.outbox.core.config.OutboxProperties;
import com.supporthub.knowledge.messaging.outbox.knowledge.KnowledgeOutboxRoutingProperties;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableConfigurationProperties({
        KafkaDltProperties.class,
        KafkaTopicProvisioningProperties.class,
        OutboxProperties.class,
        KnowledgeOutboxRoutingProperties.class
})
public class KnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}

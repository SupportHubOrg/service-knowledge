package com.supporthub.knowledge;

import com.supporthub.knowledge.messaging.config.KafkaDltProperties;
import com.supporthub.knowledge.messaging.config.KafkaTopicProvisioningProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        KafkaDltProperties.class,
        KafkaTopicProvisioningProperties.class
})
public class KnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}

package com.supporthub.knowledge;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class KnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}

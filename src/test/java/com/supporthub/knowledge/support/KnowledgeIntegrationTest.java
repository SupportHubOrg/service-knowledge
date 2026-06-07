package com.supporthub.knowledge.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers(disabledWithoutDocker = true)
@Import(KnowledgeIntegrationTestSecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://knowledge-integration-test-issuer",
        "spring.application.name=knowledge-management-service",
        "application.security.audience=knowledge-management-service",
        "application.outbox.batch-size=2",
        "application.outbox.poll-delay-ms=600000",
        "application.outbox.lock-at-most-for=PT30S",
        "application.outbox.lock-at-least-for=PT0S",
        "application.outbox.batch-send-timeout=PT10S",
        "application.outbox.retry-backoff=PT1S",
        "application.outbox.max-retry-backoff=PT5S",
        "application.outbox.stuck-timeout=PT1M",
        "application.draft-generation.batch-size=50",
        "application.draft-generation.poll-delay-ms=600000",
        "application.draft-generation.lock-at-most-for=PT30S",
        "application.draft-generation.lock-at-least-for=PT0S",
        "application.kafka.topics.defaults.partitions=3",
        "application.kafka.topics.defaults.replicas=1",
        "application.kafka.topics.defaults.min-in-sync-replicas=1"
})
public @interface KnowledgeIntegrationTest {
}

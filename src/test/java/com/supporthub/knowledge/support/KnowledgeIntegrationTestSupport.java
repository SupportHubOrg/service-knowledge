package com.supporthub.knowledge.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@KnowledgeIntegrationTest
public abstract class KnowledgeIntegrationTestSupport {

    protected static final String KNOWLEDGE_EVENTS_TOPIC = "knowledge-management-events-it";
    protected static final String KNOWLEDGE_EVENTS_DLT_TOPIC = "knowledge-base-events-dlt-it";
    protected static final String TICKET_EVENTS_TOPIC = "ticket-management-events-it";
    protected static final String ORGANIZATION_ACCESS_EVENTS_TOPIC = "organization-access-events-it";

    // Singleton containers shared across all integration test classes. Started once and left running
    // (reaped by Ryuk at JVM exit) so the cached Spring context always points at live containers,
    // even when a class triggers @DirtiesContext.
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine")
    )
            .withDatabaseName("knowledge")
            .withUsername("knowledge")
            .withPassword("knowledge");

    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @DynamicPropertySource
    static void registerIntegrationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("application.kafka.consumer.ticket-management-events", () -> TICKET_EVENTS_TOPIC);
        registry.add("application.kafka.consumer.organization-access-events", () -> ORGANIZATION_ACCESS_EVENTS_TOPIC);
        registry.add("application.kafka.producer.knowledge-management-events", () -> KNOWLEDGE_EVENTS_TOPIC);
        registry.add("application.kafka.dlt.name", () -> KNOWLEDGE_EVENTS_DLT_TOPIC);
        registry.add("application.kafka.topics.owned.knowledge-management-events", () -> KNOWLEDGE_EVENTS_TOPIC);
        registry.add("application.kafka.topics.owned.knowledge-base-events-dlt", () -> KNOWLEDGE_EVENTS_DLT_TOPIC);
        registry.add("application.outbox.routing.knowledge.event-topics.knowledge-article-draft-saved",
                () -> KNOWLEDGE_EVENTS_TOPIC);
        registry.add("application.outbox.routing.knowledge.event-topics.knowledge-article-draft-save-failed",
                () -> KNOWLEDGE_EVENTS_TOPIC);
        registry.add("application.outbox.routing.knowledge.event-topics.knowledge-article-published",
                () -> KNOWLEDGE_EVENTS_TOPIC);
        registry.add("application.outbox.routing.knowledge.event-topics.knowledge-article-rejected",
                () -> KNOWLEDGE_EVENTS_TOPIC);
    }

    protected static String kafkaBootstrapServers() {
        return KAFKA.getBootstrapServers();
    }
}

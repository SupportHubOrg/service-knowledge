package com.supporthub.knowledge.messaging.outbox.core.serialization;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventSerializerTest {

    private final OutboxEventSerializer serializer = new OutboxEventSerializer(new ObjectMapper());

    @Test
    void shouldStoreFullEnvelopeAsRawJsonAndCopyRouteAndMetadata() {
        var eventId = UUID.fromString("6cf547c2-8b4c-4d92-944a-79e4f1d801c9");
        var articleId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var event = new IntegrationEventEnvelope(
                eventId,
                KnowledgeArticlePublishedPayload.EVENT_TYPE,
                1,
                Instant.parse("2026-06-01T12:00:00Z"),
                "knowledge-management-service",
                new KnowledgeArticlePublishedPayload(
                        articleId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Article title",
                        "Article content",
                        List.of(UUID.randomUUID())
                )
        );
        var route = new OutboxEventRoute("knowledge-management-events", "ticket-key");

        var eventRecord = serializer.serialize(event, route);

        assertThat(eventRecord.route()).isEqualTo(route);
        assertThat(eventRecord.eventId()).isEqualTo(eventId);
        assertThat(eventRecord.eventType()).isEqualTo("KnowledgeArticlePublished");
        assertThat(eventRecord.payload())
                .contains("\"metadata\":{\"eventId\":\"6cf547c2-8b4c-4d92-944a-79e4f1d801c9\"")
                .contains("\"eventType\":\"KnowledgeArticlePublished\"")
                .contains("\"version\":1")
                .contains("\"timestamp\":\"2026-06-01T12:00:00Z\"")
                .contains("\"source\":\"knowledge-management-service\"")
                .contains("\"payload\":{\"articleId\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\"");
    }
}

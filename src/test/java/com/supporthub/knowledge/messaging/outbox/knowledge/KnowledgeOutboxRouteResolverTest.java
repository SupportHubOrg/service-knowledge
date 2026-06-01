package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeOutboxRouteResolverTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldRouteByEventType() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of(
                KnowledgeArticlePublishedPayload.EVENT_TYPE, "knowledge-management-events"
        )));

        var route = resolver.resolve("ticket-1", IntegrationEventEnvelope.of(
                KnowledgeArticlePublishedPayload.EVENT_TYPE,
                "knowledge-management-service",
                publishedPayload(),
                CLOCK
        ));

        assertThat(route.topic()).isEqualTo("knowledge-management-events");
        assertThat(route.messageKey()).isEqualTo("ticket-1");
    }

    @Test
    void shouldRejectUnknownEventType() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of()));
        var event = publishedEvent();

        assertThatThrownBy(() -> resolver.resolve("ticket-1", event))
                .isInstanceOf(KnowledgeOutboxRoutingException.class)
                .hasMessage("No outbox topic configured for KnowledgeArticlePublished");
    }

    @Test
    void shouldRejectBlankMessageKey() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of(
                KnowledgeArticlePublishedPayload.EVENT_TYPE, "knowledge-management-events"
        )));
        var event = publishedEvent();

        assertThatThrownBy(() -> resolver.resolve(" ", event))
                .isInstanceOf(KnowledgeOutboxRoutingException.class)
                .hasMessage("Message key is required for KnowledgeArticlePublished");
    }

    private IntegrationEventEnvelope publishedEvent() {
        return IntegrationEventEnvelope.of(
                KnowledgeArticlePublishedPayload.EVENT_TYPE,
                "knowledge-management-service",
                publishedPayload(),
                CLOCK
        );
    }

    private KnowledgeArticlePublishedPayload publishedPayload() {
        return new KnowledgeArticlePublishedPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Article title",
                "Article content",
                List.of(UUID.randomUUID())
        );
    }
}

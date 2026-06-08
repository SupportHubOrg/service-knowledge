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
    private static final String ARTICLE_PUBLISHED_EVENT = "KnowledgeArticlePublished";
    private static final String ARTICLE_PUBLISHED_CONTRACT_KEY = "knowledge-article-published";

    @Test
    void shouldRouteByContractKey() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of(
                ARTICLE_PUBLISHED_CONTRACT_KEY, "knowledge-management-events"
        )));

        var route = resolver.resolve("ticket-1", ARTICLE_PUBLISHED_CONTRACT_KEY, publishedEvent());

        assertThat(route.topic()).isEqualTo("knowledge-management-events");
        assertThat(route.messageKey()).isEqualTo("ticket-1");
    }

    @Test
    void shouldRejectUnknownContractKey() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of()));

        assertThatThrownBy(() -> resolver.resolve("ticket-1", ARTICLE_PUBLISHED_CONTRACT_KEY, publishedEvent()))
                .isInstanceOf(KnowledgeOutboxRoutingException.class)
                .hasMessage("No outbox topic configured for knowledge-article-published");
    }

    @Test
    void shouldRejectBlankMessageKey() {
        var resolver = new KnowledgeOutboxRouteResolver(new KnowledgeOutboxRoutingProperties(Map.of(
                ARTICLE_PUBLISHED_CONTRACT_KEY, "knowledge-management-events"
        )));

        assertThatThrownBy(() -> resolver.resolve(" ", ARTICLE_PUBLISHED_CONTRACT_KEY, publishedEvent()))
                .isInstanceOf(KnowledgeOutboxRoutingException.class)
                .hasMessage("Message key is required for KnowledgeArticlePublished");
    }

    private IntegrationEventEnvelope publishedEvent() {
        return IntegrationEventEnvelope.of(
                ARTICLE_PUBLISHED_EVENT,
                1,
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

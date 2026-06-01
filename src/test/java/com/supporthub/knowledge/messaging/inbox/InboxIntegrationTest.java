package com.supporthub.knowledge.messaging.inbox;

import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.knowledge.messaging.inbox.core.processing.InboxMessageProcessor;
import com.supporthub.knowledge.support.KnowledgeIntegrationTestSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InboxIntegrationTest extends KnowledgeIntegrationTestSupport {

    private final InboxMessageProcessor inboxMessageProcessor;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    InboxIntegrationTest(InboxMessageProcessor inboxMessageProcessor, JdbcTemplate jdbcTemplate) {
        this.inboxMessageProcessor = inboxMessageProcessor;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from inbox_events");
        jdbcTemplate.update("delete from outbox_events");
        jdbcTemplate.update("delete from knowledge_article_tags");
        jdbcTemplate.update("delete from knowledge_articles");
        jdbcTemplate.update("delete from knowledge_article_drafts");
    }

    @Test
    void shouldStoreInboxMetadataAndSkipDuplicateEventId() {
        var eventId = UUID.fromString("6cf547c2-8b4c-4d92-944a-79e4f1d801c9");
        var ticketId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var firstRecord = new ConsumerRecord<>(
                TICKET_EVENTS_TOPIC,
                0,
                1L,
                ticketId.toString(),
                envelope(eventId, ticketId)
        );
        var duplicateRecord = new ConsumerRecord<>(
                TICKET_EVENTS_TOPIC,
                0,
                2L,
                ticketId.toString(),
                envelope(eventId, ticketId)
        );

        inboxMessageProcessor.process(InboxConsumers.TICKET_MANAGEMENT, firstRecord);
        inboxMessageProcessor.process(InboxConsumers.TICKET_MANAGEMENT, duplicateRecord);

        var inboxEvent = jdbcTemplate.queryForMap("""
                select consumer_name, event_type, source, topic, message_key
                from inbox_events
                """);

        assertThat(inboxEvent)
                .containsEntry("consumer_name", InboxConsumers.TICKET_MANAGEMENT)
                .containsEntry("event_type", "KnowledgeArticleProposed")
                .containsEntry("source", "ticket-management-service")
                .containsEntry("topic", TICKET_EVENTS_TOPIC)
                .containsEntry("message_key", ticketId.toString());
        assertThat(inboxEventCount()).isEqualTo(1);
        assertThat(draftCount()).isEqualTo(1);
    }

    private Long inboxEventCount() {
        return jdbcTemplate.queryForObject("select count(*) from inbox_events", Long.class);
    }

    private Long draftCount() {
        return jdbcTemplate.queryForObject("select count(*) from knowledge_article_drafts", Long.class);
    }

    private String envelope(UUID eventId, UUID ticketId) {
        return """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "KnowledgeArticleProposed",
                    "version": 1,
                    "timestamp": "2026-06-01T12:00:00Z",
                    "source": "ticket-management-service"
                  },
                  "payload": {
                    "ticketId": "%s",
                    "organizationId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    "proposerId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
                    "proposedSubject": "Plan zajec issue",
                    "tags": ["dddddddd-dddd-dddd-dddd-dddddddddddd"]
                  }
                }
                """.formatted(eventId, ticketId);
    }
}

package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.messaging.error.NonRetryableKafkaProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TicketManagementEventConsumerTest {

    private final KnowledgeDraftService draftService = mock(KnowledgeDraftService.class);
    private final TicketManagementEventConsumer consumer =
            new TicketManagementEventConsumer(new ObjectMapper(), draftService);

    @Test
    void shouldConsumeRawJsonWithoutJavaTypeHeaders() {
        var rawJson = """
                {
                  "eventId": "6cf547c2-8b4c-4d92-944a-79e4f1d801c9",
                  "eventType": "KnowledgeArticleProposed",
                  "eventVersion": 1,
                  "occurredAt": "2026-06-01T12:00:00Z",
                  "source": "ticket-management-service",
                  "payload": {
                    "ticketId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    "organizationId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    "proposerId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
                    "proposedSubject": "Plan zajec issue",
                    "tags": ["dddddddd-dddd-dddd-dddd-dddddddddddd"]
                  }
                }
                """;

        consumer.consume(new ConsumerRecord<>("ticket-management-events", 0, 1L, "key", rawJson));

        verify(draftService).createDraftFromTicketProposal(argThat(payload ->
                payload.proposedSubject().equals("Plan zajec issue")
                        && payload.tags().size() == 1
        ));
    }

    @Test
    void shouldRejectUnknownEventTypeAsNonRetryable() {
        var rawJson = """
                {
                  "eventType": "TicketCreated",
                  "payload": {}
                }
                """;

        assertThatThrownBy(() -> consumer.consume(new ConsumerRecord<>("ticket-management-events", 0, 1L, "key", rawJson)))
                .isInstanceOf(NonRetryableKafkaProcessingException.class);
    }
}

package com.supporthub.knowledge.messaging.inbox.core.processing;

import com.supporthub.knowledge.messaging.error.NonRetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.knowledge.messaging.inbox.core.handler.InboxHandlerKey;
import com.supporthub.knowledge.messaging.inbox.core.handler.InboxHandlerMethod;
import com.supporthub.knowledge.messaging.inbox.core.handler.InboxHandlerRegistry;
import com.supporthub.knowledge.messaging.inbox.core.persistence.InboxEventStore;
import com.supporthub.knowledge.messaging.inbox.core.serialization.InboxEventEnvelopeReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboxMessageProcessorTest {

    private static final String CONSUMER = "knowledge-management.ticket-management";

    private final InboxEventStore eventStore = mock(InboxEventStore.class);
    private final InboxHandlerRegistry registry = new InboxHandlerRegistry();
    private final TestHandler handler = new TestHandler();
    private final InboxMessageProcessor processor = new InboxMessageProcessor(
            new InboxEventEnvelopeReader(new ObjectMapper()),
            registry,
            eventStore
    );

    @Test
    void shouldMapPayloadAndInvokeRegisteredHandler() throws Exception {
        registerHandler(true);
        when(eventStore.insertIfNew(any())).thenReturn(true);

        processor.process(CONSUMER, record(envelope("KnowledgeArticleProposed")));

        assertThat(handler.payload.proposedSubject()).isEqualTo("Plan zajec issue");
        assertThat(handler.context.eventType()).isEqualTo("KnowledgeArticleProposed");
        verify(eventStore).insertIfNew(any());
    }

    @Test
    void shouldSkipDuplicateEventBeforeInvokingHandler() throws Exception {
        registerHandler(false);
        when(eventStore.insertIfNew(any())).thenReturn(false);

        processor.process(CONSUMER, record(envelope("KnowledgeArticleProposed")));

        assertThat(handler.payload).isNull();
        verify(eventStore).insertIfNew(any());
    }

    @Test
    void shouldRejectUnknownEventTypeAsNonRetryable() {
        when(eventStore.insertIfNew(any())).thenReturn(true);

        assertThatThrownBy(() -> processor.process(CONSUMER, record(envelope("TicketCreated"))))
                .isInstanceOf(NonRetryableKafkaProcessingException.class);
    }

    @Test
    void shouldRejectMalformedEnvelopeAsNonRetryable() {
        assertThatThrownBy(() -> processor.process(CONSUMER, record("{\"payload\": {}}")))
                .isInstanceOf(NonRetryableKafkaProcessingException.class);

        verify(eventStore, never()).insertIfNew(any());
    }

    private void registerHandler(boolean withContext) throws Exception {
        Method method = withContext
                ? TestHandler.class.getDeclaredMethod(
                "handleWithContext",
                KnowledgeArticleProposedPayload.class,
                InboxEventContext.class
        )
                : TestHandler.class.getDeclaredMethod("handle", KnowledgeArticleProposedPayload.class);
        registry.register(new InboxHandlerMethod(
                new InboxHandlerKey(CONSUMER, "KnowledgeArticleProposed", 1),
                KnowledgeArticleProposedPayload.class,
                handler,
                method,
                withContext
        ));
    }

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("ticket-management-events", 0, 1L, "key", value);
    }

    private String envelope(String eventType) {
        return """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "%s",
                    "version": 1,
                    "timestamp": "2026-06-01T12:00:00Z",
                    "source": "ticket-management-service"
                  },
                  "payload": {
                    "ticketId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    "organizationId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    "proposerId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
                    "proposedSubject": "Plan zajec issue",
                    "tags": ["dddddddd-dddd-dddd-dddd-dddddddddddd"]
                  }
                }
                """.formatted(UUID.randomUUID(), eventType);
    }

    private static class TestHandler {

        private KnowledgeArticleProposedPayload payload;
        private InboxEventContext context;

        void handle(KnowledgeArticleProposedPayload payload) {
            this.payload = payload;
        }

        void handleWithContext(KnowledgeArticleProposedPayload payload, InboxEventContext context) {
            this.payload = payload;
            this.context = context;
        }
    }
}

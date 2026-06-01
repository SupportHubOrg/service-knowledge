package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.knowledge.messaging.inbox.core.processing.InboxMessageProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TicketManagementEventConsumerTest {

    private final InboxMessageProcessor inboxMessageProcessor = mock(InboxMessageProcessor.class);
    private final TicketManagementEventConsumer consumer = new TicketManagementEventConsumer(inboxMessageProcessor);

    @Test
    void shouldDelegateTicketManagementRecordToInboxProcessor() {
        var consumerRecord = new ConsumerRecord<>("ticket-management-events", 0, 1L, "key", "{}");

        consumer.consume(consumerRecord);

        verify(inboxMessageProcessor).process(InboxConsumers.TICKET_MANAGEMENT, consumerRecord);
    }
}

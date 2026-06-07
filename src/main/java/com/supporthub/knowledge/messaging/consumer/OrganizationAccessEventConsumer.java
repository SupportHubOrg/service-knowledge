package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.knowledge.messaging.inbox.core.processing.InboxMessageProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter for organization-access events. Parsing, idempotency, handler dispatch, and business
 * transaction ownership live in {@link InboxMessageProcessor}.
 */
@Component
@RequiredArgsConstructor
public class OrganizationAccessEventConsumer {

    private final InboxMessageProcessor inboxMessageProcessor;

    @KafkaListener(
            topics = "${application.kafka.consumer.organization-access-events}",
            containerFactory = "inboxKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        inboxMessageProcessor.process(InboxConsumers.ORGANIZATION_ACCESS, consumerRecord);
    }
}

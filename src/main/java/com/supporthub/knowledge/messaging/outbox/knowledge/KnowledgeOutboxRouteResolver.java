package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxRouteResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Routes knowledge events by event type. The caller still supplies the message key so all events for
 * one aggregate, usually one source ticket, keep their order on one Kafka partition.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeOutboxRouteResolver implements OutboxRouteResolver {

    private final KnowledgeOutboxRoutingProperties properties;

    @Override
    public OutboxEventRoute resolve(String messageKey, IntegrationEventEnvelope event) {
        if (messageKey == null || messageKey.isBlank()) {
            throw new KnowledgeOutboxRoutingException("Message key is required for " + event.eventType());
        }

        var topic = properties.eventTopics().get(event.eventType());
        if (topic == null || topic.isBlank()) {
            throw new KnowledgeOutboxRoutingException("No outbox topic configured for " + event.eventType());
        }

        return new OutboxEventRoute(topic, messageKey);
    }
}

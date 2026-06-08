package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxRouteResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Routes knowledge events by their stable contract key (the {@code @IntegrationEvent} value), not the
 * wire eventType, so an env override of the eventType cannot desynchronize the routing table. The
 * caller still supplies the message key so all events for one aggregate, usually one source ticket,
 * keep their order on one Kafka partition.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeOutboxRouteResolver implements OutboxRouteResolver {

    private final KnowledgeOutboxRoutingProperties properties;

    @Override
    public OutboxEventRoute resolve(String messageKey, String contractKey, IntegrationEventEnvelope event) {
        if (messageKey == null || messageKey.isBlank()) {
            throw new KnowledgeOutboxRoutingException("Message key is required for " + event.eventType());
        }

        var topic = properties.eventTopics().get(contractKey);
        if (topic == null || topic.isBlank()) {
            throw new KnowledgeOutboxRoutingException("No outbox topic configured for " + contractKey);
        }

        return new OutboxEventRoute(topic, messageKey);
    }
}

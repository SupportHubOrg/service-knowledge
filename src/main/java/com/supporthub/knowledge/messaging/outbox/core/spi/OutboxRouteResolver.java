package com.supporthub.knowledge.messaging.outbox.core.spi;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;

/** SPI a service implements to choose the destination topic and message key for an event. */
public interface OutboxRouteResolver {

    /**
     * @param messageKey  Kafka partition key for ordering
     * @param contractKey stable {@code @IntegrationEvent} key (config-driven, not the wire eventType)
     * @param event       the envelope being published
     */
    OutboxEventRoute resolve(String messageKey, String contractKey, IntegrationEventEnvelope event);
}

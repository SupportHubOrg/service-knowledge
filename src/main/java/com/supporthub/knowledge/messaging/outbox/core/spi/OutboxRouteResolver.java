package com.supporthub.knowledge.messaging.outbox.core.spi;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRoute;

/** SPI a service implements to choose the destination topic and message key for an event. */
public interface OutboxRouteResolver {

    OutboxEventRoute resolve(String messageKey, IntegrationEventEnvelope event);
}

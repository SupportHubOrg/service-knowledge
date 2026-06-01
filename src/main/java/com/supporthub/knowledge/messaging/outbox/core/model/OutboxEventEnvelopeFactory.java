package com.supporthub.knowledge.messaging.outbox.core.model;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.event.IntegrationEventRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class OutboxEventEnvelopeFactory {

    private final String source;
    private final Clock clock;
    private final IntegrationEventRegistry integrationEventRegistry;

    public OutboxEventEnvelopeFactory(@Value("${spring.application.name}") String source,
                                      Clock clock,
                                      IntegrationEventRegistry integrationEventRegistry) {
        this.source = source;
        this.clock = clock;
        this.integrationEventRegistry = integrationEventRegistry;
    }

    public IntegrationEventEnvelope create(Object payload) {
        var contract = integrationEventRegistry.get(payload.getClass());
        return IntegrationEventEnvelope.of(contract.eventType(), contract.version(), source, payload, clock);
    }
}

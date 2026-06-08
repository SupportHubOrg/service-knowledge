package com.supporthub.knowledge.messaging.outbox.core.model;

import com.supporthub.knowledge.messaging.IntegrationEventEnvelope;
import com.supporthub.knowledge.messaging.event.registry.IntegrationEventContract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class OutboxEventEnvelopeFactory {

    private final String source;
    private final Clock clock;

    public OutboxEventEnvelopeFactory(@Value("${spring.application.name}") String source, Clock clock) {
        this.source = source;
        this.clock = clock;
    }

    public IntegrationEventEnvelope create(IntegrationEventContract contract, Object payload) {
        return IntegrationEventEnvelope.of(contract.eventType(), contract.version(), source, payload, clock);
    }
}

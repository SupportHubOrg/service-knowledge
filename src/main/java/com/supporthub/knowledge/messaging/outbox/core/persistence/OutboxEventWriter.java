package com.supporthub.knowledge.messaging.outbox.core.persistence;

import com.supporthub.knowledge.messaging.event.IntegrationEventPayload;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventEnvelopeFactory;
import com.supporthub.knowledge.messaging.outbox.core.serialization.OutboxEventSerializer;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxRouteResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Write-side outbox entry point used inside business transactions.
 *
 * <p>It resolves the route, serializes the envelope, and stores a {@code PENDING} event row.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private final OutboxRouteResolver routeResolver;
    private final OutboxEventEnvelopeFactory envelopeFactory;
    private final OutboxEventSerializer serializer;
    private final OutboxEventRepository repository;
    private final Clock clock;

    @Transactional
    public void append(String messageKey, IntegrationEventPayload payload) {
        var event = envelopeFactory.create(payload);
        var route = routeResolver.resolve(messageKey, event);
        var serializedEvent = serializer.serialize(event, route);
        repository.save(OutboxEventEntity.pending(serializedEvent, clock.instant()));
    }
}

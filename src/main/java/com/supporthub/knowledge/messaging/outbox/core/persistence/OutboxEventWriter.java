package com.supporthub.knowledge.messaging.outbox.core.persistence;
import com.supporthub.knowledge.messaging.event.registry.IntegrationEventRegistry;
import com.supporthub.knowledge.messaging.outbox.core.model.MessageKeyExtractor;
import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventEnvelopeFactory;
import com.supporthub.knowledge.messaging.outbox.core.serialization.OutboxEventSerializer;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxRouteResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-side outbox entry point used inside business transactions.
 *
 * <p>It derives the message key from the payload's {@code @MessageKey} component, resolves the route
 * by the stable contract key, serializes the envelope, and stores a {@code PENDING} event row.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private final IntegrationEventRegistry integrationEventRegistry;
    private final OutboxRouteResolver routeResolver;
    private final OutboxEventEnvelopeFactory envelopeFactory;
    private final MessageKeyExtractor messageKeyExtractor;
    private final OutboxEventSerializer serializer;
    private final OutboxEventRepository repository;

    @Transactional
    public void append(Object payload) {
        var contract = integrationEventRegistry.get(payload.getClass());
        var event = envelopeFactory.create(contract, payload);
        var messageKey = messageKeyExtractor.extract(payload);
        var route = routeResolver.resolve(messageKey, contract.key(), event);
        var serializedEvent = serializer.serialize(event, route);
        repository.save(OutboxEventEntity.pending(serializedEvent));
    }
}

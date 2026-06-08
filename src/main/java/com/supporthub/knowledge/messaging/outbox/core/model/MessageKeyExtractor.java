package com.supporthub.knowledge.messaging.outbox.core.model;

import com.supporthub.knowledge.messaging.event.annotation.MessageKey;
import org.springframework.stereotype.Component;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the Kafka message key from an event payload by reading its single {@link MessageKey}
 * record component. Every aggregate event carries its own key, so ordering on one partition is kept
 * without the caller having to supply the key separately. Resolved accessors are cached per type.
 */
@Component
public class MessageKeyExtractor {

    private final Map<Class<?>, RecordComponent> keyComponentsByType = new ConcurrentHashMap<>();

    public String extract(Object payload) {
        var keyComponent = keyComponentsByType.computeIfAbsent(payload.getClass(), this::resolveKeyComponent);
        var value = read(keyComponent, payload);
        if (value == null) {
            throw new MessageKeyResolutionException(
                    "@MessageKey '%s' is null on %s".formatted(keyComponent.getName(), payload.getClass().getName())
            );
        }
        return value.toString();
    }

    private RecordComponent resolveKeyComponent(Class<?> payloadType) {
        if (!payloadType.isRecord()) {
            throw new MessageKeyResolutionException("Event payload must be a record: " + payloadType.getName());
        }
        var keyComponents = Arrays.stream(payloadType.getRecordComponents())
                .filter(component -> component.isAnnotationPresent(MessageKey.class))
                .toList();
        if (keyComponents.size() != 1) {
            throw new MessageKeyResolutionException(
                    "Exactly one @MessageKey component is required on %s, found %d".formatted(
                            payloadType.getName(), keyComponents.size()
                    )
            );
        }
        return keyComponents.get(0);
    }

    private Object read(RecordComponent keyComponent, Object payload) {
        try {
            return keyComponent.getAccessor().invoke(payload);
        } catch (ReflectiveOperationException exception) {
            throw new MessageKeyResolutionException(
                    "Failed to read @MessageKey from " + payload.getClass().getName(), exception
            );
        }
    }
}

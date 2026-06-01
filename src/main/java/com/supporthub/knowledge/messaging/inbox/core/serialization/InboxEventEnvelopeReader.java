package com.supporthub.knowledge.messaging.inbox.core.serialization;

import com.supporthub.knowledge.messaging.inbox.core.error.InboxMessageFormatException;
import com.supporthub.knowledge.messaging.inbox.core.model.InboxEventEnvelope;
import com.supporthub.knowledge.messaging.inbox.core.model.InboxEventMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InboxEventEnvelopeReader {

    private final ObjectMapper objectMapper;

    public InboxEventEnvelope read(String rawJson) {
        try {
            var root = objectMapper.readTree(rawJson);
            var metadata = root.path("metadata");
            if (!metadata.isObject()) {
                throw new InboxMessageFormatException("Kafka event metadata is missing");
            }

            var payload = root.path("payload");
            if (payload.isMissingNode() || payload.isNull()) {
                throw new InboxMessageFormatException("Kafka event payload is missing");
            }

            return new InboxEventEnvelope(
                    new InboxEventMetadata(
                            uuid(metadata, "eventId"),
                            requiredText(metadata, "eventType"),
                            requiredInt(metadata, "version"),
                            instant(metadata, "timestamp"),
                            optionalText(metadata, "source")
                    ),
                    payload
            );
        } catch (JacksonException exception) {
            throw new InboxMessageFormatException("Malformed Kafka event JSON", exception);
        } catch (IllegalArgumentException exception) {
            throw new InboxMessageFormatException("Malformed Kafka event metadata", exception);
        }
    }

    public <T> T payloadAs(JsonNode payload, Class<T> payloadType) {
        try {
            return objectMapper.treeToValue(payload, payloadType);
        } catch (JacksonException exception) {
            throw new InboxMessageFormatException("Malformed Kafka event payload", exception);
        }
    }

    private UUID uuid(JsonNode metadata, String fieldName) {
        return UUID.fromString(requiredText(metadata, fieldName));
    }

    private Instant instant(JsonNode metadata, String fieldName) {
        return Instant.parse(requiredText(metadata, fieldName));
    }

    private int requiredInt(JsonNode metadata, String fieldName) {
        var value = metadata.path(fieldName);
        if (!value.isNumber()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.intValue();
    }

    private String requiredText(JsonNode metadata, String fieldName) {
        var value = optionalText(metadata, fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    private String optionalText(JsonNode metadata, String fieldName) {
        var value = metadata.path(fieldName);
        return value.isString() ? value.asString() : null;
    }
}

package com.supporthub.knowledge.shared.identity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Single sanctioned conversion between the value object and its raw wire/column form. The static
 * {@code toRaw}/{@code toPlainText} is the one exit point (used by integration events); the JPA
 * {@link jakarta.persistence.AttributeConverter} methods delegate to it, so the unwrap lives once.
 */
@Converter(autoApply = true)
public class TicketIdConverter implements AttributeConverter<TicketId, UUID> {

    /** Sanctioned exit point for crossing into integration events / external wire formats. */
    public static UUID toRaw(TicketId id) {
        if (id == null) {
            return null;
        }
        return id.unwrap();
    }

    @Override
    public UUID convertToDatabaseColumn(TicketId attribute) {
        return toRaw(attribute);
    }

    @Override
    public TicketId convertToEntityAttribute(UUID dbData) {
        if (dbData == null) {
            return null;
        }
        return TicketId.of(dbData);
    }
}

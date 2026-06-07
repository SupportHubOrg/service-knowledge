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
public class OrganizationIdConverter implements AttributeConverter<OrganizationId, UUID> {

    /** Sanctioned exit point for crossing into integration events / external wire formats. */
    public static UUID toRaw(OrganizationId id) {
        if (id == null) {
            return null;
        }
        return id.unwrap();
    }

    @Override
    public UUID convertToDatabaseColumn(OrganizationId attribute) {
        return toRaw(attribute);
    }

    @Override
    public OrganizationId convertToEntityAttribute(UUID dbData) {
        if (dbData == null) {
            return null;
        }
        return OrganizationId.of(dbData);
    }
}

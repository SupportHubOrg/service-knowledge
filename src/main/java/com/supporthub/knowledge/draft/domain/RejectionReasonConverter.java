package com.supporthub.knowledge.draft.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Single sanctioned conversion between the value object and its raw wire/column form. The static
 * {@code toRaw}/{@code toPlainText} is the one exit point (used by integration events); the JPA
 * {@link jakarta.persistence.AttributeConverter} methods delegate to it, so the unwrap lives once.
 */
@Converter(autoApply = true)
public class RejectionReasonConverter implements AttributeConverter<RejectionReason, String> {

    /** Sanctioned exit point for crossing into integration events / generation prompts. */
    public static String toPlainText(RejectionReason value) {
        if (value == null) {
            return null;
        }
        return value.unwrap();
    }

    @Override
    public String convertToDatabaseColumn(RejectionReason attribute) {
        return toPlainText(attribute);
    }

    @Override
    public RejectionReason convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return RejectionReason.of(dbData);
    }
}

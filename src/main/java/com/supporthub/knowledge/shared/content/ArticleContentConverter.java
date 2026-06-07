package com.supporthub.knowledge.shared.content;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Single sanctioned conversion between the value object and its raw wire/column form. The static
 * {@code toRaw}/{@code toPlainText} is the one exit point (used by integration events); the JPA
 * {@link jakarta.persistence.AttributeConverter} methods delegate to it, so the unwrap lives once.
 */
@Converter(autoApply = true)
public class ArticleContentConverter implements AttributeConverter<ArticleContent, String> {

    /** Sanctioned exit point for crossing into integration events / generation prompts. */
    public static String toPlainText(ArticleContent value) {
        if (value == null) {
            return null;
        }
        return value.unwrap();
    }

    @Override
    public String convertToDatabaseColumn(ArticleContent attribute) {
        return toPlainText(attribute);
    }

    @Override
    public ArticleContent convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ArticleContent.of(dbData);
    }
}

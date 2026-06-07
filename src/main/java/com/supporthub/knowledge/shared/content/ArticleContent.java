package com.supporthub.knowledge.shared.content;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ArticleContent {

    private final String value;

    private ArticleContent(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException("Article content must not be blank");
        }
        this.value = value.trim();
    }

    public static ArticleContent of(String value) {
        return new ArticleContent(value);
    }

    String unwrap() {
        return value;
    }

    public boolean sameAs(ArticleContent other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ArticleContent[length=" + value.length() + "]";
    }
}

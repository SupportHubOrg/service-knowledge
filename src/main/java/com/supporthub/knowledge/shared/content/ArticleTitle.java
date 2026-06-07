package com.supporthub.knowledge.shared.content;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ArticleTitle {

    private final String value;

    private ArticleTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException("Article title must not be blank");
        }
        this.value = value.trim();
    }

    public static ArticleTitle of(String value) {
        return new ArticleTitle(value);
    }

    String unwrap() {
        return value;
    }

    public boolean sameAs(ArticleTitle other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ArticleTitle[" + value + "]";
    }
}

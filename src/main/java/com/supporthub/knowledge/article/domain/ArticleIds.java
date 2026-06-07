package com.supporthub.knowledge.article.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link ArticleId} to its raw wire/JSON form. {@code ArticleId} itself is
 * an {@code @EmbeddedId}, so no JPA {@code AttributeConverter} is needed for persistence.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArticleIds {

    public static UUID toRaw(ArticleId id) {
        return id == null ? null : id.unwrap();
    }
}

package com.supporthub.knowledge.article.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a published knowledge article. Mapped as an {@code @EmbeddedId} so the domain works
 * with the value object while the database stores the raw UUID column.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class ArticleId {

    @Column(name = "id")
    private UUID value;


    private ArticleId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid article id");
        }
        this.value = value;
    }

    public static ArticleId of(UUID value) {
        return new ArticleId(value);
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID());
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(ArticleId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ArticleId[" + value + "]";
    }
}

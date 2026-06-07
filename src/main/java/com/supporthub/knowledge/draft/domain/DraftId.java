package com.supporthub.knowledge.draft.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a knowledge draft. Mapped as an {@code @EmbeddedId} so the domain works with the value
 * object while the database stores the raw UUID column.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // for JPA
public final class DraftId {

    @Column(name = "id")
    private UUID value;


    private DraftId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid draft id");
        }
        this.value = value;
    }

    public static DraftId of(UUID value) {
        return new DraftId(value);
    }

    public static DraftId generate() {
        return new DraftId(UUID.randomUUID());
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(DraftId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "DraftId[" + value + "]";
    }
}

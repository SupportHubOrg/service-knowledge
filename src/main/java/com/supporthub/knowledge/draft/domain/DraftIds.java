package com.supporthub.knowledge.draft.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link DraftId} to its raw wire/JSON form. {@code DraftId} itself is an
 * {@code @EmbeddedId}, so no JPA {@code AttributeConverter} is needed for persistence.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DraftIds {

    public static UUID toRaw(DraftId id) {
        return id == null ? null : id.unwrap();
    }
}

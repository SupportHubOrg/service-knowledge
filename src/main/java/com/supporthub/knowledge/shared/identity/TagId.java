package com.supporthub.knowledge.shared.identity;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class TagId {

    private final UUID value;

    private TagId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid tag id");
        }
        this.value = value;
    }

    public static TagId of(UUID value) {
        return new TagId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(TagId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "TagId[" + value + "]";
    }
}

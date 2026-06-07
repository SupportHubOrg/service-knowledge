package com.supporthub.knowledge.shared.identity;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid user id");
        }
        this.value = value;
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(UserId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "UserId[" + value + "]";
    }
}

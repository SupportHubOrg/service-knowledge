package com.supporthub.knowledge.shared.identity;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class OrganizationId {

    private final UUID value;

    private OrganizationId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid organization id");
        }
        this.value = value;
    }

    public static OrganizationId of(UUID value) {
        return new OrganizationId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(OrganizationId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "OrganizationId[" + value + "]";
    }
}

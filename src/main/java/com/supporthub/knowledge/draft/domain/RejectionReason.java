package com.supporthub.knowledge.draft.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class RejectionReason {

    private final String value;

    private RejectionReason(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException("Rejection reason must not be blank");
        }
        this.value = value.trim();
    }

    public static RejectionReason of(String value) {
        return new RejectionReason(value);
    }

    String unwrap() {
        return value;
    }

    public boolean sameAs(RejectionReason other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "RejectionReason[length=" + value.length() + "]";
    }
}

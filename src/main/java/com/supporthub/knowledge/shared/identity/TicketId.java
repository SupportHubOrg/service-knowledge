package com.supporthub.knowledge.shared.identity;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class TicketId {

    private final UUID value;

    private TicketId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid ticket id");
        }
        this.value = value;
    }

    public static TicketId of(UUID value) {
        return new TicketId(value);
    }

    public static TicketId generate() {
        return new TicketId(UUID.randomUUID());
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(TicketId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "TicketId[" + value + "]";
    }
}

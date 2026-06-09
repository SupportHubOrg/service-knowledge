package com.supporthub.knowledge.ticket.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;

/** Role of the author of a ticket conversation message. */
public enum SenderType {
    EMPLOYEE,
    OPERATOR,
    AI;

    /** Parses the wire representation used by ticket-management events (e.g. {@code "Employee"}). */
    public static SenderType fromWireValue(String wireValue) {
        if (wireValue == null) {
            throw new BusinessValidationException("Sender type must not be null");
        }
        String normalized = wireValue.trim().toUpperCase();
        if (normalized.equals("LEVEL_1_OPERATOR") || normalized.equals("LEVEL_2_OPERATOR")) {
            return OPERATOR;
        }
        try {
            return SenderType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new BusinessValidationException("Unknown sender type: " + wireValue);
        }
    }

    /** Renders the wire representation expected by outbound integration events. */
    public String toWireValue() {
        return switch (this) {
            case EMPLOYEE -> "Employee";
            case OPERATOR -> "Operator";
            case AI -> "AI";
        };
    }
}

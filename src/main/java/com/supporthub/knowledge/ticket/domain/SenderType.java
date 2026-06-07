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
        try {
            return SenderType.valueOf(wireValue.trim().toUpperCase());
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

package com.supporthub.knowledge.ticket.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class MessageBody {

    private final String value;

    private MessageBody(String value) {
        if (value == null) {
            throw new BusinessValidationException("Message body must not be null");
        }
        this.value = value;
    }

    public static MessageBody of(String value) {
        return new MessageBody(value);
    }

    String unwrap() {
        return value;
    }

    public boolean sameAs(MessageBody other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "MessageBody[length=" + value.length() + "]";
    }
}

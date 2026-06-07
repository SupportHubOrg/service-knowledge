package com.supporthub.knowledge.shared.identity;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Identity of a projected ticket conversation message. Mapped as an {@code @EmbeddedId} so the domain
 * works with the value object while the database stores the raw UUID column.
 */
@Embeddable
@EqualsAndHashCode
public final class MessageId {

    @Column(name = "message_id")
    private UUID value;

    private MessageId() {
        // for JPA
    }

    private MessageId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid message id");
        }
        this.value = value;
    }

    public static MessageId of(UUID value) {
        return new MessageId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(MessageId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "MessageId[" + value + "]";
    }
}

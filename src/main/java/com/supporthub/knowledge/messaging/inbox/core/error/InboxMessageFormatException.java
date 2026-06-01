package com.supporthub.knowledge.messaging.inbox.core.error;

public class InboxMessageFormatException extends InboxException {

    public InboxMessageFormatException(String message) {
        super(message);
    }

    public InboxMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

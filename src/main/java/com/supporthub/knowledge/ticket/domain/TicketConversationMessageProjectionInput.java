package com.supporthub.knowledge.ticket.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class TicketConversationMessageProjectionInput {

    private final UUID messageId;
    private final UUID ticketId;
    private final UUID organizationId;
    private final UUID senderId;
    private final String senderType;
    private final String message;

    @Builder
    private TicketConversationMessageProjectionInput(UUID messageId,
                                                     UUID ticketId,
                                                     UUID organizationId,
                                                     UUID senderId,
                                                     String senderType,
                                                     String message) {
        this.messageId = messageId;
        this.ticketId = ticketId;
        this.organizationId = organizationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.message = message;
    }
}

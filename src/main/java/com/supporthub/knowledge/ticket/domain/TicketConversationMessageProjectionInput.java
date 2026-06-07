package com.supporthub.knowledge.ticket.domain;

import com.supporthub.knowledge.shared.identity.MessageId;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class TicketConversationMessageProjectionInput {

    private final MessageId messageId;
    private final TicketId ticketId;
    private final OrganizationId organizationId;
    private final UserId senderId;
    private final SenderType senderType;
    private final MessageBody message;

    @Builder
    private TicketConversationMessageProjectionInput(MessageId messageId,
                                                     TicketId ticketId,
                                                     OrganizationId organizationId,
                                                     UserId senderId,
                                                     SenderType senderType,
                                                     MessageBody message) {
        this.messageId = messageId;
        this.ticketId = ticketId;
        this.organizationId = organizationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.message = message;
    }
}

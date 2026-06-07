package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.cleanup.KnowledgeCleanupService;
import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.messaging.event.TicketDeletedPayload;
import com.supporthub.knowledge.messaging.event.TicketMessageAddedPayload;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.TICKET_MANAGEMENT)
@RequiredArgsConstructor
public class TicketManagementInboxHandlers {

    private final KnowledgeDraftService draftService;
    private final TicketConversationProjection ticketConversationProjection;
    private final KnowledgeCleanupService cleanupService;

    @InboxHandler
    void onTicketMessageAdded(TicketMessageAddedPayload payload) {
        ticketConversationProjection.project(payload);
    }

    @InboxHandler
    void onKnowledgeArticleProposed(KnowledgeArticleProposedPayload payload) {
        draftService.createDraftFromTicketProposal(payload);
    }

    @InboxHandler
    void onTicketDeleted(TicketDeletedPayload payload) {
        cleanupService.onTicketDeleted(payload.ticketId());
    }
}

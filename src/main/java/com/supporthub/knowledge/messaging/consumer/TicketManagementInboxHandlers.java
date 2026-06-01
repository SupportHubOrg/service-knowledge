package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.TICKET_MANAGEMENT)
@RequiredArgsConstructor
public class TicketManagementInboxHandlers {

    private final KnowledgeDraftService draftService;

    @InboxHandler
    void onKnowledgeArticleProposed(KnowledgeArticleProposedPayload payload) {
        draftService.createDraftFromTicketProposal(payload);
    }
}

package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;

import java.util.List;
import java.util.UUID;

/**
 * Inbound payload of the {@code KnowledgeArticleProposed} event from the ticket-management service:
 * an operator proposes turning a resolved ticket into a knowledge article. Consumed by
 * {@link com.supporthub.knowledge.messaging.consumer.TicketManagementEventConsumer} to create a
 * draft.
 *
 * <p>Per the canonical contract this event carries an {@code operatorId} (the operator linked to the
 * ticket), not an organization id. The organization is resolved separately from the local ticket
 * conversation projection ({@code TicketMessageAdded}).
 *
 * @param ticketId        the resolved ticket the proposal originates from (drives idempotency)
 * @param operatorId      operator linked to the source ticket
 * @param proposerId      operator who proposed the article
 * @param proposedSubject suggested title for the draft
 * @param tags            tag ids to attach to the draft
 */
@IntegrationEvent("knowledge-article-proposed")
public record KnowledgeArticleProposedPayload(
        UUID ticketId,
        UUID operatorId,
        UUID proposerId,
        String proposedSubject,
        List<UUID> tags
) {
}

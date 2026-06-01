package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

/**
 * Inbound payload of the {@code KnowledgeArticleProposed} event from the ticket-management service:
 * an operator proposes turning a resolved ticket into a knowledge article. Consumed by
 * {@link com.supporthub.knowledge.messaging.consumer.TicketManagementEventConsumer} to create a
 * draft.
 *
 * @param ticketId        the resolved ticket the proposal originates from (drives idempotency)
 * @param organizationId  organization the resulting draft/article belongs to
 * @param proposerId      operator who proposed the article
 * @param proposedSubject suggested title for the draft
 * @param tags            tag ids to attach to the draft
 */
@IntegrationEvent("knowledge-article-proposed")
public record KnowledgeArticleProposedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID proposerId,
        String proposedSubject,
        List<UUID> tags
) {
}

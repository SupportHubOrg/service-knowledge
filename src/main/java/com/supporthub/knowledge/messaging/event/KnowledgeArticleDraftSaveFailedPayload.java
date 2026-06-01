package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Outbound payload of {@code KnowledgeArticleDraftSaveFailed}: a ticket proposal could not be
 * turned into a draft. Acts as a compensating signal back to the ticket-management side.
 */
public record KnowledgeArticleDraftSaveFailedPayload(
        UUID sourceTicketId,
        UUID organizationId,
        String failureCode,
        String failureMessage
) {
}

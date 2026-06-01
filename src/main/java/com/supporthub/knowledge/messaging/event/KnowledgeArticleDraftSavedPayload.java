package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

/** Outbound payload of {@code KnowledgeArticleDraftSaved}: a draft was created for a ticket. */
public record KnowledgeArticleDraftSavedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        String title,
        List<UUID> tags
) {
}

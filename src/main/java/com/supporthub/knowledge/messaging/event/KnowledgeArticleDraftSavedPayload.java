package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("knowledge-article-draft-saved")
public record KnowledgeArticleDraftSavedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        String title,
        List<UUID> tags
) {
}

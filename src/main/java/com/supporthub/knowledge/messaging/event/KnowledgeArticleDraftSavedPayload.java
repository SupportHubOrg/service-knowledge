package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;
import com.supporthub.knowledge.messaging.event.annotation.MessageKey;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("knowledge-article-draft-saved")
public record KnowledgeArticleDraftSavedPayload(
        UUID draftId,
        @MessageKey UUID sourceTicketId,
        UUID organizationId,
        String title,
        List<UUID> tags
) {
}

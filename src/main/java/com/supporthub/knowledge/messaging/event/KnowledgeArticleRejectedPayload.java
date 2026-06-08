package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;
import com.supporthub.knowledge.messaging.event.annotation.MessageKey;

import java.util.UUID;

@IntegrationEvent("knowledge-article-rejected")
public record KnowledgeArticleRejectedPayload(
        UUID draftId,
        @MessageKey UUID sourceTicketId,
        UUID organizationId,
        UUID rejectedById,
        String rejectionReason
) {
}

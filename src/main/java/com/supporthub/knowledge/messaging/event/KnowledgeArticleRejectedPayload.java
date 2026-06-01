package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

@IntegrationEvent("knowledge-article-rejected")
public record KnowledgeArticleRejectedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID rejectedById,
        String rejectionReason
) {
}

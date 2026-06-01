package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

@IntegrationEvent("knowledge-article-draft-save-failed")
public record KnowledgeArticleDraftSaveFailedPayload(
        UUID sourceTicketId,
        UUID organizationId,
        String failureCode,
        String failureMessage
) {
}

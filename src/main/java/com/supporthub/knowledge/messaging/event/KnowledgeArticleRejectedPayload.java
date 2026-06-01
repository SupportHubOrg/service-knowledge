package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

public record KnowledgeArticleRejectedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID rejectedById,
        String rejectionReason
) implements IntegrationEventPayload {

    public static final String EVENT_TYPE = "KnowledgeArticleRejected";

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}

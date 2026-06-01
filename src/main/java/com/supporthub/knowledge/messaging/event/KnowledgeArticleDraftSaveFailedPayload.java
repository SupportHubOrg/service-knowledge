package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

public record KnowledgeArticleDraftSaveFailedPayload(
        UUID sourceTicketId,
        UUID organizationId,
        String failureCode,
        String failureMessage
) implements IntegrationEventPayload {

    public static final String EVENT_TYPE = "KnowledgeArticleDraftSaveFailed";

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}

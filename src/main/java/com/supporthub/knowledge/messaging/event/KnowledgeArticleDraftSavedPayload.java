package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

public record KnowledgeArticleDraftSavedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        String title,
        List<UUID> tags
) implements IntegrationEventPayload {

    public static final String EVENT_TYPE = "KnowledgeArticleDraftSaved";

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}

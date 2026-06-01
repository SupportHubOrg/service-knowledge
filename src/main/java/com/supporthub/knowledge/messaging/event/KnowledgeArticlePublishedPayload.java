package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

public record KnowledgeArticlePublishedPayload(
        UUID articleId,
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID publishedById,
        String title,
        String articleContent,
        List<UUID> tags
) implements IntegrationEventPayload {

    public static final String EVENT_TYPE = "KnowledgeArticlePublished";

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}

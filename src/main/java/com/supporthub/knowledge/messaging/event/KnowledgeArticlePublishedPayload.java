package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("knowledge-article-published")
public record KnowledgeArticlePublishedPayload(
        UUID articleId,
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID publishedById,
        String title,
        String articleContent,
        List<UUID> tags
) {
}

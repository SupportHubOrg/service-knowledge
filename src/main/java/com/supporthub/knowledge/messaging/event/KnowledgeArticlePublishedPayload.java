package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;
import com.supporthub.knowledge.messaging.event.annotation.MessageKey;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("knowledge-article-published")
public record KnowledgeArticlePublishedPayload(
        UUID articleId,
        UUID draftId,
        @MessageKey UUID sourceTicketId,
        UUID organizationId,
        UUID publishedById,
        String title,
        String articleContent,
        List<UUID> tags
) {
}

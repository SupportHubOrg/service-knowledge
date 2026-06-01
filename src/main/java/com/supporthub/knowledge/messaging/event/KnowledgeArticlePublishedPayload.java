package com.supporthub.knowledge.messaging.event;

import java.util.List;
import java.util.UUID;

/**
 * Outbound payload of {@code KnowledgeArticlePublished}: a draft became a published article.
 * Carries the full title/content so subscribers (e.g. AI triage, search indexing) can use the
 * article without calling back.
 */
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

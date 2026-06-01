package com.supporthub.knowledge.messaging;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;

import java.util.UUID;

/**
 * Outbound domain events emitted by this service. These form the public contract other services
 * (notably ticket-management) subscribe to. The Kafka implementation is
 * {@link com.supporthub.knowledge.messaging.producer.KafkaKnowledgeEventPublisher}.
 */
public interface KnowledgeEventPublisher {

    /** Emitted when a new draft is persisted from a ticket proposal. */
    void publishKnowledgeArticleDraftSaved(KnowledgeArticleDraftEntity draft);

    /** Emitted when a ticket proposal could not be turned into a draft (compensating signal). */
    void publishKnowledgeArticleDraftSaveFailed(UUID sourceTicketId,
                                                UUID organizationId,
                                                String failureCode,
                                                String failureMessage);

    /** Emitted when a draft is published as an article; carries the full article content. */
    void publishKnowledgeArticlePublished(KnowledgeArticleEntity article);

    /** Emitted when a manager rejects a draft; carries the rejection reason. */
    void publishKnowledgeArticleRejected(KnowledgeArticleDraftEntity draft, UUID rejectedById);
}

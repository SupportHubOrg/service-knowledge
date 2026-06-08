package com.supporthub.knowledge.messaging;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;

/**
 * Outbound integration events emitted by this service. These form the public contract other
 * services subscribe to. The implementation stores events in the transactional outbox; Kafka
 * delivery is handled asynchronously by the outbox publisher.
 */
public interface KnowledgeEventPublisher {

    /** Emitted when a new draft is persisted from a ticket proposal. */
    void publishKnowledgeArticleDraftSaved(KnowledgeArticleDraftEntity draft);

    /** Emitted when a ticket proposal could not be turned into a draft (compensating signal). */
    void publishKnowledgeArticleDraftSaveFailed(TicketId sourceTicketId,
                                                OrganizationId organizationId,
                                                DraftSaveFailureCode failureCode,
                                                String failureMessage);

    /** Emitted when a draft is published as an article; carries the full article content. */
    void publishKnowledgeArticlePublished(KnowledgeArticleEntity article);

    /** Emitted when a manager rejects a draft; carries the rejection reason. */
    void publishKnowledgeArticleRejected(KnowledgeArticleDraftEntity draft, UserId rejectedById);
}

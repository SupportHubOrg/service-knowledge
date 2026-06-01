package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Domain-facing implementation of {@link KnowledgeEventPublisher}. It records events into the
 * outbox; delivery to Kafka is handled later by the outbox publisher.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeOutboxEventRecorder implements KnowledgeEventPublisher {

    private final OutboxEventWriter eventWriter;
    private final KnowledgeOutboxPayloadFactory payloadFactory;

    @Override
    public void publishKnowledgeArticleDraftSaved(KnowledgeArticleDraftEntity draft) {
        eventWriter.append(draft.getSourceTicketId().toString(), payloadFactory.draftSaved(draft));
    }

    @Override
    public void publishKnowledgeArticleDraftSaveFailed(UUID sourceTicketId,
                                                       UUID organizationId,
                                                       String failureCode,
                                                       String failureMessage) {
        eventWriter.append(
                sourceTicketId.toString(),
                payloadFactory.draftSaveFailed(sourceTicketId, organizationId, failureCode, failureMessage)
        );
    }

    @Override
    public void publishKnowledgeArticlePublished(KnowledgeArticleEntity article) {
        eventWriter.append(article.getSourceTicketId().toString(), payloadFactory.articlePublished(article));
    }

    @Override
    public void publishKnowledgeArticleRejected(KnowledgeArticleDraftEntity draft, UUID rejectedById) {
        eventWriter.append(draft.getSourceTicketId().toString(), payloadFactory.articleRejected(draft, rejectedById));
    }
}

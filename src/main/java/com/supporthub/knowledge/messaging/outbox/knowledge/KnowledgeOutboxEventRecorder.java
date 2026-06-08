package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventWriter;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-facing implementation of {@link KnowledgeEventPublisher}. It records events into the
 * outbox; delivery to Kafka is handled later by the outbox publisher. The Kafka message key is
 * derived from each payload's {@code @MessageKey} component by the outbox writer.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeOutboxEventRecorder implements KnowledgeEventPublisher {

    private final OutboxEventWriter eventWriter;
    private final KnowledgeOutboxPayloadFactory payloadFactory;

    @Override
    public void publishKnowledgeArticleDraftSaved(KnowledgeArticleDraftEntity draft) {
        eventWriter.append(payloadFactory.draftSaved(draft));
    }

    @Override
    public void publishKnowledgeArticleDraftSaveFailed(TicketId sourceTicketId,
                                                       OrganizationId organizationId,
                                                       DraftSaveFailureCode failureCode,
                                                       String failureMessage) {
        eventWriter.append(payloadFactory.draftSaveFailed(sourceTicketId, organizationId, failureCode, failureMessage));
    }

    @Override
    public void publishKnowledgeArticlePublished(KnowledgeArticleEntity article) {
        eventWriter.append(payloadFactory.articlePublished(article));
    }

    @Override
    public void publishKnowledgeArticleRejected(KnowledgeArticleDraftEntity draft, UserId rejectedById) {
        eventWriter.append(payloadFactory.articleRejected(draft, rejectedById));
    }
}

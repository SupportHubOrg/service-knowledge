package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSaveFailedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSavedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleRejectedPayload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KnowledgeOutboxPayloadFactory {

    public KnowledgeArticleDraftSavedPayload draftSaved(KnowledgeArticleDraftEntity draft) {
        return new KnowledgeArticleDraftSavedPayload(
                draft.getId(),
                draft.getSourceTicketId(),
                draft.getOrganizationId(),
                draft.getTitle(),
                tagIds(draft.getTags())
        );
    }

    public KnowledgeArticleDraftSaveFailedPayload draftSaveFailed(UUID sourceTicketId,
                                                                  UUID organizationId,
                                                                  String failureCode,
                                                                  String failureMessage) {
        return new KnowledgeArticleDraftSaveFailedPayload(
                sourceTicketId,
                organizationId,
                failureCode,
                failureMessage
        );
    }

    public KnowledgeArticlePublishedPayload articlePublished(KnowledgeArticleEntity article) {
        return new KnowledgeArticlePublishedPayload(
                article.getId(),
                article.getDraft().getId(),
                article.getSourceTicketId(),
                article.getOrganizationId(),
                article.getPublishedById(),
                article.getTitle(),
                article.getContent(),
                tagIds(article.getTags())
        );
    }

    public KnowledgeArticleRejectedPayload articleRejected(KnowledgeArticleDraftEntity draft, UUID rejectedById) {
        return new KnowledgeArticleRejectedPayload(
                draft.getId(),
                draft.getSourceTicketId(),
                draft.getOrganizationId(),
                rejectedById,
                draft.getRejectionReason()
        );
    }

    private List<UUID> tagIds(List<KnowledgeArticleTagEntity> tags) {
        return tags.stream().map(KnowledgeArticleTagEntity::getTagId).toList();
    }
}

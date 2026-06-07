package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.domain.ArticleIds;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.domain.DraftIds;
import com.supporthub.knowledge.draft.domain.RejectionReasonConverter;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSaveFailedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSavedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleRejectedPayload;
import com.supporthub.knowledge.shared.content.ArticleContentConverter;
import com.supporthub.knowledge.shared.content.ArticleTitleConverter;
import com.supporthub.knowledge.shared.identity.OrganizationIdConverter;
import com.supporthub.knowledge.shared.identity.TagIdConverter;
import com.supporthub.knowledge.shared.identity.TicketIdConverter;
import com.supporthub.knowledge.shared.identity.UserIdConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KnowledgeOutboxPayloadFactory {

    public KnowledgeArticleDraftSavedPayload draftSaved(KnowledgeArticleDraftEntity draft) {
        return new KnowledgeArticleDraftSavedPayload(
                DraftIds.toRaw(draft.getId()),
                TicketIdConverter.toRaw(draft.getSourceTicketId()),
                OrganizationIdConverter.toRaw(draft.getOrganizationId()),
                ArticleTitleConverter.toPlainText(draft.getTitle()),
                tagIds(draft.getTags())
        );
    }

    public KnowledgeArticleDraftSaveFailedPayload draftSaveFailed(UUID sourceTicketId,
                                                                  UUID organizationId,
                                                                  DraftSaveFailureCode failureCode,
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
                ArticleIds.toRaw(article.getId()),
                DraftIds.toRaw(article.getDraft().getId()),
                TicketIdConverter.toRaw(article.getSourceTicketId()),
                OrganizationIdConverter.toRaw(article.getOrganizationId()),
                UserIdConverter.toRaw(article.getPublishedById()),
                ArticleTitleConverter.toPlainText(article.getTitle()),
                ArticleContentConverter.toPlainText(article.getContent()),
                tagIds(article.getTags())
        );
    }

    public KnowledgeArticleRejectedPayload articleRejected(KnowledgeArticleDraftEntity draft, UUID rejectedById) {
        String rejectionReason;
        if (draft.getRejectionReason() == null) {
            rejectionReason = null;
        } else {
            rejectionReason = RejectionReasonConverter.toPlainText(draft.getRejectionReason());
        }
        return new KnowledgeArticleRejectedPayload(
                DraftIds.toRaw(draft.getId()),
                TicketIdConverter.toRaw(draft.getSourceTicketId()),
                OrganizationIdConverter.toRaw(draft.getOrganizationId()),
                rejectedById,
                rejectionReason
        );
    }

    private List<UUID> tagIds(List<KnowledgeArticleTagEntity> tags) {
        return tags.stream().map(KnowledgeArticleTagEntity::getTagId).map(TagIdConverter::toRaw).toList();
    }
}

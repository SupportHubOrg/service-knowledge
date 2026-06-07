package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.api.ArticleResponse;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.api.DraftResponse;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface KnowledgeMapper {

    @Mapping(target = "tags", expression = "java(tagIds(draft.getTags()))")
    DraftResponse toDraftResponse(KnowledgeArticleDraftEntity draft);

    @Mapping(target = "draftId", source = "draft.id")
    @Mapping(target = "tags", expression = "java(tagIds(article.getTags()))")
    ArticleResponse toArticleResponse(KnowledgeArticleEntity article);

    @Mapping(target = "sourceTicketId", source = "payload.ticketId")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "proposerId", source = "payload.proposerId")
    @Mapping(target = "title", source = "payload.proposedSubject")
    @Mapping(target = "tagIds", source = "payload.tags")
    PendingDraftProposal toPendingDraftProposal(KnowledgeArticleProposedPayload payload, UUID organizationId);

    default List<UUID> tagIds(List<KnowledgeArticleTagEntity> tags) {
        return tags.stream()
                .map(KnowledgeArticleTagEntity::getTagId)
                .toList();
    }
}

package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.api.ArticleResponse;
import com.supporthub.knowledge.article.domain.ArticleIds;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.api.DraftResponse;
import com.supporthub.knowledge.draft.domain.DraftIds;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.domain.RejectionReason;
import com.supporthub.knowledge.draft.domain.RejectionReasonConverter;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleContentConverter;
import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.shared.content.ArticleTitleConverter;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.OrganizationIdConverter;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TagIdConverter;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.TicketIdConverter;
import com.supporthub.knowledge.shared.identity.UserId;
import com.supporthub.knowledge.shared.identity.UserIdConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {ArticleTitle.class, OrganizationId.class, TicketId.class, UserId.class,
                ArticleIds.class, ArticleContentConverter.class, ArticleTitleConverter.class,
                DraftIds.class, OrganizationIdConverter.class, TicketIdConverter.class, UserIdConverter.class})
public interface KnowledgeMapper {

    @Mapping(target = "id", expression = "java(DraftIds.toRaw(draft.getId()))")
    @Mapping(target = "sourceTicketId", expression = "java(TicketIdConverter.toRaw(draft.getSourceTicketId()))")
    @Mapping(target = "organizationId", expression = "java(OrganizationIdConverter.toRaw(draft.getOrganizationId()))")
    @Mapping(target = "proposerId", expression = "java(UserIdConverter.toRaw(draft.getProposerId()))")
    @Mapping(target = "title", expression = "java(ArticleTitleConverter.toPlainText(draft.getTitle()))")
    @Mapping(target = "content", expression = "java(contentToPlainText(draft.getContent()))")
    @Mapping(target = "rejectionReason", expression = "java(rejectionReasonToPlainText(draft.getRejectionReason()))")
    @Mapping(target = "tags", expression = "java(tagIds(draft.getTags()))")
    DraftResponse toDraftResponse(KnowledgeArticleDraftEntity draft);

    @Mapping(target = "id", expression = "java(ArticleIds.toRaw(article.getId()))")
    @Mapping(target = "draftId", expression = "java(DraftIds.toRaw(article.getDraft().getId()))")
    @Mapping(target = "sourceTicketId", expression = "java(TicketIdConverter.toRaw(article.getSourceTicketId()))")
    @Mapping(target = "organizationId", expression = "java(OrganizationIdConverter.toRaw(article.getOrganizationId()))")
    @Mapping(target = "title", expression = "java(ArticleTitleConverter.toPlainText(article.getTitle()))")
    @Mapping(target = "content", expression = "java(ArticleContentConverter.toPlainText(article.getContent()))")
    @Mapping(target = "publishedById", expression = "java(UserIdConverter.toRaw(article.getPublishedById()))")
    @Mapping(target = "tags", expression = "java(tagIds(article.getTags()))")
    ArticleResponse toArticleResponse(KnowledgeArticleEntity article);

    @Mapping(target = "sourceTicketId", expression = "java(TicketId.of(payload.ticketId()))")
    @Mapping(target = "organizationId", expression = "java(OrganizationId.of(organizationId))")
    @Mapping(target = "proposerId", expression = "java(UserId.of(payload.proposerId()))")
    @Mapping(target = "title", expression = "java(ArticleTitle.of(payload.proposedSubject()))")
    @Mapping(target = "tagIds", expression = "java(tagIdsFromRaw(payload.tags()))")
    PendingDraftProposal toPendingDraftProposal(KnowledgeArticleProposedPayload payload, UUID organizationId);

    default String contentToPlainText(ArticleContent content) {
        if (content == null) {
            return null;
        }
        return ArticleContentConverter.toPlainText(content);
    }

    default String rejectionReasonToPlainText(RejectionReason rejectionReason) {
        if (rejectionReason == null) {
            return null;
        }
        return RejectionReasonConverter.toPlainText(rejectionReason);
    }

    default List<UUID> tagIds(List<KnowledgeArticleTagEntity> tags) {
        return tags.stream()
                .map(KnowledgeArticleTagEntity::getTagId)
                .map(TagIdConverter::toRaw)
                .toList();
    }

    default List<TagId> tagIdsFromRaw(List<UUID> rawIds) {
        return rawIds.stream().map(TagId::of).toList();
    }
}

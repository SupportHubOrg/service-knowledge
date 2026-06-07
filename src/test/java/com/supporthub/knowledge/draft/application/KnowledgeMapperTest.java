package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.DraftIds;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TagIdConverter;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeMapperTest {

    private final KnowledgeMapper mapper = Mappers.getMapper(KnowledgeMapper.class);

    @Test
    void shouldMapDraftTags() {
        var tagId = TagId.of(UUID.randomUUID());
        var draft = pendingGenerationDraft("Draft title", List.of(tagId));

        var response = mapper.toDraftResponse(draft);

        assertThat(response.tags()).containsExactly(TagIdConverter.toRaw(tagId));
        assertThat(response.title()).isEqualTo("Draft title");
    }

    @Test
    void shouldMapPublishedArticle() {
        var draft = pendingGenerationDraft("Draft title", List.of(TagId.of(UUID.randomUUID())));
        draft.completeGeneration(ArticleContent.of("Generated content"), Instant.parse("2026-06-01T11:30:00Z"));
        draft.publish(ArticleTitle.of("Article title"), ArticleContent.of("Article content"), Instant.parse("2026-06-01T12:00:00Z"));
        var article = KnowledgeArticleEntity.publishFromDraft(
                draft,
                UserId.of(UUID.randomUUID()),
                Instant.parse("2026-06-01T12:01:00Z")
        );

        var response = mapper.toArticleResponse(article);

        assertThat(response.draftId()).isEqualTo(DraftIds.toRaw(draft.getId()));
        assertThat(response.title()).isEqualTo("Article title");
        assertThat(response.tags()).hasSize(1);
    }

    @Test
    void shouldMapProposalPayloadToPendingDraftProposal() {
        var organizationId = UUID.randomUUID();
        var ticketId = UUID.randomUUID();
        var proposerId = UUID.randomUUID();
        var tagId = UUID.randomUUID();
        var payload = new KnowledgeArticleProposedPayload(
                ticketId,
                UUID.randomUUID(),
                proposerId,
                "Draft title",
                List.of(tagId)
        );

        var proposal = mapper.toPendingDraftProposal(payload, organizationId);

        assertThat(proposal.getSourceTicketId()).isEqualTo(TicketId.of(ticketId));
        assertThat(proposal.getOrganizationId()).isEqualTo(OrganizationId.of(organizationId));
        assertThat(proposal.getProposerId()).isEqualTo(UserId.of(proposerId));
        assertThat(proposal.getTitle()).isEqualTo(ArticleTitle.of("Draft title"));
        assertThat(proposal.getTagIds()).containsExactly(TagId.of(tagId));
    }

    private KnowledgeArticleDraftEntity pendingGenerationDraft(String title, List<TagId> tagIds) {
        return KnowledgeArticleDraftEntity.pendingGeneration(
                DraftId.generate(),
                PendingDraftProposal.builder()
                        .sourceTicketId(TicketId.of(UUID.randomUUID()))
                        .organizationId(OrganizationId.of(UUID.randomUUID()))
                        .proposerId(UserId.of(UUID.randomUUID()))
                        .title(ArticleTitle.of(title))
                        .tagIds(tagIds)
                        .build()
        );
    }
}

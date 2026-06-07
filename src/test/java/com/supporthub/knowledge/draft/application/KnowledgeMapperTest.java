package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
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
        var tagId = UUID.randomUUID();
        var draft = pendingGenerationDraft("Draft title", List.of(tagId));

        var response = mapper.toDraftResponse(draft);

        assertThat(response.tags()).containsExactly(tagId);
        assertThat(response.title()).isEqualTo("Draft title");
    }

    @Test
    void shouldMapPublishedArticle() {
        var draft = pendingGenerationDraft("Draft title", List.of(UUID.randomUUID()));
        draft.completeGeneration("Generated content", Instant.parse("2026-06-01T11:30:00Z"));
        draft.publish("Article title", "Article content", Instant.parse("2026-06-01T12:00:00Z"));
        var article = KnowledgeArticleEntity.publishFromDraft(
                draft,
                UUID.randomUUID(),
                Instant.parse("2026-06-01T12:01:00Z")
        );

        var response = mapper.toArticleResponse(article);

        assertThat(response.draftId()).isEqualTo(draft.getId());
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

        assertThat(proposal.getSourceTicketId()).isEqualTo(ticketId);
        assertThat(proposal.getOrganizationId()).isEqualTo(organizationId);
        assertThat(proposal.getProposerId()).isEqualTo(proposerId);
        assertThat(proposal.getTitle()).isEqualTo("Draft title");
        assertThat(proposal.getTagIds()).containsExactly(tagId);
    }

    private KnowledgeArticleDraftEntity pendingGenerationDraft(String title, List<UUID> tagIds) {
        return KnowledgeArticleDraftEntity.pendingGeneration(
                UUID.randomUUID(),
                PendingDraftProposal.builder()
                        .sourceTicketId(UUID.randomUUID())
                        .organizationId(UUID.randomUUID())
                        .proposerId(UUID.randomUUID())
                        .title(title)
                        .tagIds(tagIds)
                        .build()
        );
    }
}

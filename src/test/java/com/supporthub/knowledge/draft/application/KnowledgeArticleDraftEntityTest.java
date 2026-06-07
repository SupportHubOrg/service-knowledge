package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.IllegalDraftStateException;
import com.supporthub.knowledge.draft.domain.RejectionReason;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeArticleDraftEntityTest {

    @Test
    void shouldStartInPendingGeneration() {
        var draft = pendingGenerationDraft();

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PENDING_GENERATION);
        assertThat(draft.getContent()).isNull();
    }

    @Test
    void shouldCompleteGenerationToPendingReview() {
        var draft = pendingGenerationDraft();
        var generationCompletedAt = Instant.parse("2026-06-01T11:30:00Z");

        draft.completeGeneration(ArticleContent.of("Generated body"), generationCompletedAt);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PENDING_REVIEW);
        assertThat(draft.getContent()).isEqualTo(ArticleContent.of("Generated body"));
        assertThat(draft.getUpdatedAt()).isEqualTo(generationCompletedAt);
    }

    @Test
    void shouldFailGeneration() {
        var draft = pendingGenerationDraft();
        var generationFailedAt = Instant.parse("2026-06-01T11:30:00Z");

        draft.failGeneration(generationFailedAt);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.GENERATION_FAILED);
        assertThat(draft.getUpdatedAt()).isEqualTo(generationFailedAt);
    }

    @Test
    void shouldRejectCompletingGenerationTwice() {
        var draft = pendingGenerationDraft();
        draft.completeGeneration(ArticleContent.of("Generated body"), Instant.parse("2026-06-01T11:30:00Z"));
        var secondGenerationCompletedAt = Instant.parse("2026-06-01T11:31:00Z");
        var content = ArticleContent.of("Again");

        assertThatThrownBy(() -> draft.completeGeneration(content, secondGenerationCompletedAt))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    @Test
    void shouldPublishPendingReviewDraft() {
        var draft = pendingReviewDraft();
        var publishedAt = Instant.parse("2026-06-01T12:00:00Z");

        draft.publish(ArticleTitle.of("Published title"), ArticleContent.of("Published content"), publishedAt);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PUBLISHED);
        assertThat(draft.getTitle()).isEqualTo(ArticleTitle.of("Published title"));
        assertThat(draft.getContent()).isEqualTo(ArticleContent.of("Published content"));
        assertThat(draft.getUpdatedAt()).isEqualTo(publishedAt);
    }

    @Test
    void shouldRejectPendingReviewDraft() {
        var draft = pendingReviewDraft();
        var rejectedAt = Instant.parse("2026-06-01T12:00:00Z");

        draft.reject(RejectionReason.of("Duplicate article"), rejectedAt);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.REJECTED);
        assertThat(draft.getRejectionReason()).isEqualTo(RejectionReason.of("Duplicate article"));
        assertThat(draft.getUpdatedAt()).isEqualTo(rejectedAt);
    }

    @Test
    void shouldRejectPublishingRejectedDraft() {
        var draft = pendingReviewDraft();
        draft.reject(RejectionReason.of("Duplicate article"), Instant.parse("2026-06-01T12:00:00Z"));
        var publishedAt = Instant.parse("2026-06-01T12:01:00Z");

        var title = ArticleTitle.of("Title");
        var content = ArticleContent.of("Content");

        assertThatThrownBy(() -> draft.publish(title, content, publishedAt))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    @Test
    void shouldRejectPublishingDraftStillGenerating() {
        var draft = pendingGenerationDraft();
        var publishedAt = Instant.parse("2026-06-01T12:01:00Z");

        var title = ArticleTitle.of("Title");
        var content = ArticleContent.of("Content");

        assertThatThrownBy(() -> draft.publish(title, content, publishedAt))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    private KnowledgeArticleDraftEntity pendingReviewDraft() {
        var draft = pendingGenerationDraft();
        draft.completeGeneration(ArticleContent.of("Generated content"), Instant.parse("2026-06-01T11:30:00Z"));
        return draft;
    }

    private KnowledgeArticleDraftEntity pendingGenerationDraft() {
        return KnowledgeArticleDraftEntity.pendingGeneration(
                DraftId.generate(),
                PendingDraftProposal.builder()
                        .sourceTicketId(TicketId.generate())
                        .organizationId(OrganizationId.of(UUID.randomUUID()))
                        .proposerId(UserId.of(UUID.randomUUID()))
                        .title(ArticleTitle.of("Draft title"))
                        .tagIds(List.of(TagId.of(UUID.randomUUID()), TagId.of(UUID.randomUUID())))
                        .build()
        );
    }
}

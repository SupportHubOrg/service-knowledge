package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.persistence.IllegalDraftStateException;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
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
        var now = Instant.parse("2026-06-01T11:30:00Z");

        draft.completeGeneration("Generated body", now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PENDING_REVIEW);
        assertThat(draft.getContent()).isEqualTo("Generated body");
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldFailGeneration() {
        var draft = pendingGenerationDraft();
        var now = Instant.parse("2026-06-01T11:30:00Z");

        draft.failGeneration(now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.GENERATION_FAILED);
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectCompletingGenerationTwice() {
        var draft = pendingGenerationDraft();
        draft.completeGeneration("Generated body", Instant.parse("2026-06-01T11:30:00Z"));
        var now = Instant.parse("2026-06-01T11:31:00Z");

        assertThatThrownBy(() -> draft.completeGeneration("Again", now))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    @Test
    void shouldPublishPendingReviewDraft() {
        var draft = pendingReviewDraft();
        var now = Instant.parse("2026-06-01T12:00:00Z");

        draft.publish("Published title", "Published content", now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PUBLISHED);
        assertThat(draft.getTitle()).isEqualTo("Published title");
        assertThat(draft.getContent()).isEqualTo("Published content");
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectPendingReviewDraft() {
        var draft = pendingReviewDraft();
        var now = Instant.parse("2026-06-01T12:00:00Z");

        draft.reject("Duplicate article", now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.REJECTED);
        assertThat(draft.getRejectionReason()).isEqualTo("Duplicate article");
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectPublishingRejectedDraft() {
        var draft = pendingReviewDraft();
        draft.reject("Duplicate article", Instant.parse("2026-06-01T12:00:00Z"));
        var now = Instant.parse("2026-06-01T12:01:00Z");

        assertThatThrownBy(() -> draft.publish("Title", "Content", now))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    @Test
    void shouldRejectPublishingDraftStillGenerating() {
        var draft = pendingGenerationDraft();
        var now = Instant.parse("2026-06-01T12:01:00Z");

        assertThatThrownBy(() -> draft.publish("Title", "Content", now))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    private KnowledgeArticleDraftEntity pendingReviewDraft() {
        var draft = pendingGenerationDraft();
        draft.completeGeneration("Generated content", Instant.parse("2026-06-01T11:30:00Z"));
        return draft;
    }

    private KnowledgeArticleDraftEntity pendingGenerationDraft() {
        return KnowledgeArticleDraftEntity.pendingGeneration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Draft title",
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                Instant.parse("2026-06-01T11:00:00Z")
        );
    }
}

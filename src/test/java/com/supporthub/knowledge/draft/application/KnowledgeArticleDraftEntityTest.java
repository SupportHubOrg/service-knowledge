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
    void shouldPublishPendingDraft() {
        var draft = draft();
        var now = Instant.parse("2026-06-01T12:00:00Z");

        draft.publish("Published title", "Published content", now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PUBLISHED);
        assertThat(draft.getTitle()).isEqualTo("Published title");
        assertThat(draft.getContent()).isEqualTo("Published content");
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectPendingDraft() {
        var draft = draft();
        var now = Instant.parse("2026-06-01T12:00:00Z");

        draft.reject("Duplicate article", now);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.REJECTED);
        assertThat(draft.getRejectionReason()).isEqualTo("Duplicate article");
        assertThat(draft.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldRejectPublishingRejectedDraft() {
        var draft = draft();
        draft.reject("Duplicate article", Instant.parse("2026-06-01T12:00:00Z"));

        assertThatThrownBy(() -> draft.publish("Title", "Content", Instant.parse("2026-06-01T12:01:00Z")))
                .isInstanceOf(IllegalDraftStateException.class);
    }

    private KnowledgeArticleDraftEntity draft() {
        return KnowledgeArticleDraftEntity.pending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Draft title",
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                Instant.parse("2026-06-01T11:00:00Z")
        );
    }
}

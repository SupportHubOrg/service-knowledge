package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
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
        var draft = KnowledgeArticleDraftEntity.pendingGeneration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Draft title",
                List.of(tagId),
                Instant.parse("2026-06-01T11:00:00Z")
        );

        var response = mapper.toDraftResponse(draft);

        assertThat(response.tags()).containsExactly(tagId);
        assertThat(response.title()).isEqualTo("Draft title");
    }

    @Test
    void shouldMapPublishedArticle() {
        var draft = KnowledgeArticleDraftEntity.pendingGeneration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Draft title",
                List.of(UUID.randomUUID()),
                Instant.parse("2026-06-01T11:00:00Z")
        );
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
}

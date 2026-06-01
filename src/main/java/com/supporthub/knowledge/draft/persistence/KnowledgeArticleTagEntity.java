package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_article_tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleTagEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id")
    private KnowledgeArticleDraftEntity draft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private KnowledgeArticleEntity article;

    @Column(nullable = false, name = "tag_id")
    private UUID tagId;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    public static KnowledgeArticleTagEntity forDraft(KnowledgeArticleDraftEntity draft, UUID tagId, Instant now) {
        return KnowledgeArticleTagEntity.builder()
                .id(UUID.randomUUID())
                .draft(draft)
                .tagId(tagId)
                .createdAt(now)
                .build();
    }

    public static KnowledgeArticleTagEntity forArticle(KnowledgeArticleEntity article, UUID tagId, Instant now) {
        return KnowledgeArticleTagEntity.builder()
                .id(UUID.randomUUID())
                .article(article)
                .tagId(tagId)
                .createdAt(now)
                .build();
    }
}

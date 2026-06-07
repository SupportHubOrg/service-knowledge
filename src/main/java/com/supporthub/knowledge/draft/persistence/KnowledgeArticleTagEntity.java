package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.shared.identity.TagId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_article_tags")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private TagId tagId;

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    public static KnowledgeArticleTagEntity forDraft(KnowledgeArticleDraftEntity draft, TagId tagId) {
        return KnowledgeArticleTagEntity.builder()
                .id(UUID.randomUUID())
                .draft(draft)
                .tagId(tagId)
                .build();
    }

    public static KnowledgeArticleTagEntity forArticle(KnowledgeArticleEntity article, TagId tagId) {
        return KnowledgeArticleTagEntity.builder()
                .id(UUID.randomUUID())
                .article(article)
                .tagId(tagId)
                .build();
    }
}

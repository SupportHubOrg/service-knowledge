package com.supporthub.knowledge.article.persistence;

import com.supporthub.knowledge.article.domain.ArticleStatus;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleTagEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A published knowledge article — the immutable, read-only result of accepting a draft.
 *
 * <p>Always created from a {@link KnowledgeArticleDraftEntity} via {@link #publishFromDraft}, never
 * directly. Keeps a back-reference to its originating draft and a snapshot copy of the draft's
 * title/content/tags taken at publish time, so later draft changes (there shouldn't be any, since
 * publish is terminal) cannot affect the published article.
 */
@Entity
@Table(name = "knowledge_articles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private KnowledgeArticleDraftEntity draft;

    @Column(nullable = false, name = "source_ticket_id")
    private UUID sourceTicketId;

    @Column(nullable = false, name = "organization_id")
    private UUID organizationId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status;

    @Column(nullable = false, name = "published_by_id")
    private UUID publishedById;

    @Column(nullable = false, name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KnowledgeArticleTagEntity> tags = new ArrayList<>();

    /**
     * Builds a published article by snapshotting the given draft and copying its tags.
     *
     * @param publishedById the manager who approved the publication
     */
    public static KnowledgeArticleEntity publishFromDraft(KnowledgeArticleDraftEntity draft,
                                                          UUID publishedById,
                                                          Instant now) {
        var article = KnowledgeArticleEntity.builder()
                .id(UUID.randomUUID())
                .draft(draft)
                .sourceTicketId(draft.getSourceTicketId())
                .organizationId(draft.getOrganizationId())
                .title(draft.getTitle())
                .content(draft.getContent())
                .status(ArticleStatus.PUBLISHED)
                .publishedById(publishedById)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        draft.getTags().stream()
                .map(tag -> KnowledgeArticleTagEntity.forArticle(article, tag.getTagId(), now))
                .forEach(article.tags::add);
        return article;
    }
}

package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
 * Aggregate root for a proposed knowledge article awaiting review.
 *
 * <p>State machine: a draft is born {@code PENDING_REVIEW} and from there can only move to
 * {@code PUBLISHED} (manager accepted it) or {@code REJECTED} (manager declined it). Both are
 * terminal. The transition methods {@link #publish} and {@link #reject} enforce this and throw
 * {@link IllegalDraftStateException} when called on a draft that is no longer pending, protecting
 * against double-publish / publish-after-reject races.
 *
 * <p>{@code source_ticket_id} is unique (one draft per originating ticket) and {@link Version}
 * provides optimistic locking for concurrent manager actions. Tags are owned by the draft and
 * cascade-managed via {@link #replaceTags}.
 */
@Entity
@Table(name = "knowledge_article_drafts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleDraftEntity {

    @Id
    private UUID id;

    @Column(nullable = false, name = "source_ticket_id")
    private UUID sourceTicketId;

    @Column(nullable = false, name = "organization_id")
    private UUID organizationId;

    @Column(nullable = false, name = "proposer_id")
    private UUID proposerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KnowledgeArticleTagEntity> tags = new ArrayList<>();

    /** Factory for a brand-new draft in {@code PENDING_REVIEW}, with its tags attached. */
    public static KnowledgeArticleDraftEntity pending(UUID sourceTicketId,
                                                      UUID organizationId,
                                                      UUID proposerId,
                                                      String title,
                                                      List<UUID> tagIds,
                                                      Instant now) {
        var draft = KnowledgeArticleDraftEntity.builder()
                .id(UUID.randomUUID())
                .sourceTicketId(sourceTicketId)
                .organizationId(organizationId)
                .proposerId(proposerId)
                .title(title)
                .status(DraftStatus.PENDING_REVIEW)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
        draft.replaceTags(tagIds, now);
        return draft;
    }

    /**
     * Marks the draft as published with the final title/content.
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_REVIEW}
     */
    public void publish(String title, String content, Instant now) {
        if (status != DraftStatus.PENDING_REVIEW) {
            throw new IllegalDraftStateException(id, status, "publish");
        }
        this.title = title;
        this.content = content;
        this.status = DraftStatus.PUBLISHED;
        this.updatedAt = now;
    }

    /**
     * Marks the draft as rejected with the manager's reason.
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_REVIEW}
     */
    public void reject(String reason, Instant now) {
        if (status != DraftStatus.PENDING_REVIEW) {
            throw new IllegalDraftStateException(id, status, "reject");
        }
        this.status = DraftStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = now;
    }

    private void replaceTags(List<UUID> tagIds, Instant now) {
        tags.clear();
        tagIds.stream()
                .distinct()
                .map(tagId -> KnowledgeArticleTagEntity.forDraft(this, tagId, now))
                .forEach(tags::add);
    }
}

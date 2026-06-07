package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.domain.RejectionReason;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for a proposed knowledge article awaiting review.
 *
 * <p>State machine: a draft is born {@code PENDING_GENERATION} (its body is generated asynchronously
 * from the ticket conversation). From there it moves to {@code PENDING_REVIEW} once content is ready
 * ({@link #completeGeneration}) or to {@code GENERATION_FAILED} if generation could not produce a
 * body ({@link #failGeneration}). A {@code PENDING_REVIEW} draft can then only move to
 * {@code PUBLISHED} (manager accepted it) or {@code REJECTED} (manager declined it); both terminal.
 * The transition methods enforce the legal source state and throw {@link IllegalDraftStateException}
 * otherwise, protecting against double-publish / publish-after-reject and double-generation races.
 *
 * <p>{@code source_ticket_id} is unique (one draft per originating ticket) and {@link Version}
 * provides optimistic locking for concurrent manager actions. Tags are owned by the draft and
 * cascade-managed via {@link #replaceTags}.
 */
@Entity
@Table(name = "knowledge_article_drafts")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeArticleDraftEntity {

    @EmbeddedId
    private DraftId id;

    @Column(nullable = false, name = "source_ticket_id")
    private TicketId sourceTicketId;

    @Column(nullable = false, name = "organization_id")
    private OrganizationId organizationId;

    @Column(nullable = false, name = "proposer_id")
    private UserId proposerId;

    @Column(nullable = false)
    private ArticleTitle title;

    @Column(columnDefinition = "TEXT")
    private ArticleContent content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private RejectionReason rejectionReason;

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KnowledgeArticleTagEntity> tags = new ArrayList<>();

    /**
     * Factory for a brand-new draft in {@code PENDING_GENERATION}, with its tags attached. The body
     * is filled in later by the asynchronous generation step via {@link #completeGeneration}.
     */
    public static KnowledgeArticleDraftEntity pendingGeneration(DraftId draftId, PendingDraftProposal proposal) {
        var draft = KnowledgeArticleDraftEntity.builder()
                .id(draftId)
                .sourceTicketId(proposal.getSourceTicketId())
                .organizationId(proposal.getOrganizationId())
                .proposerId(proposal.getProposerId())
                .title(proposal.getTitle())
                .status(DraftStatus.PENDING_GENERATION)
                .build();
        draft.replaceTags(proposal.getTagIds());
        return draft;
    }

    /**
     * Attaches the generated body and moves the draft to {@code PENDING_REVIEW}.
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_GENERATION}
     */
    public void completeGeneration(ArticleContent content, Instant generationCompletedAt) {
        if (status != DraftStatus.PENDING_GENERATION) {
            throw new IllegalDraftStateException(id, status, "completeGeneration");
        }
        this.content = content;
        this.status = DraftStatus.PENDING_REVIEW;
        this.updatedAt = generationCompletedAt;
    }

    /**
     * Marks generation as failed (no body could be produced).
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_GENERATION}
     */
    public void failGeneration(Instant generationFailedAt) {
        if (status != DraftStatus.PENDING_GENERATION) {
            throw new IllegalDraftStateException(id, status, "failGeneration");
        }
        this.status = DraftStatus.GENERATION_FAILED;
        this.updatedAt = generationFailedAt;
    }

    /**
     * Marks the draft as published with the final title/content.
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_REVIEW}
     */
    public void publish(ArticleTitle title, ArticleContent content, Instant publishedAt) {
        if (status != DraftStatus.PENDING_REVIEW) {
            throw new IllegalDraftStateException(id, status, "publish");
        }
        this.title = title;
        this.content = content;
        this.status = DraftStatus.PUBLISHED;
        this.updatedAt = publishedAt;
    }

    /**
     * Marks the draft as rejected with the manager's reason.
     *
     * @throws IllegalDraftStateException if the draft is not {@code PENDING_REVIEW}
     */
    public void reject(RejectionReason reason, Instant rejectedAt) {
        if (status != DraftStatus.PENDING_REVIEW) {
            throw new IllegalDraftStateException(id, status, "reject");
        }
        this.status = DraftStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = rejectedAt;
    }

    private void replaceTags(List<TagId> tagIds) {
        tags.clear();
        tagIds.stream()
                .distinct()
                .map(tagId -> KnowledgeArticleTagEntity.forDraft(this, tagId))
                .forEach(tags::add);
    }

}

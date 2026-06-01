package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.article.api.ArticleResponse;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleRepository;
import com.supporthub.knowledge.draft.api.DraftResponse;
import com.supporthub.knowledge.draft.api.PublishDraftRequest;
import com.supporthub.knowledge.draft.api.RejectDraftRequest;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftRepository;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.security.CurrentUser;
import com.supporthub.knowledge.security.KnowledgeAccessPolicy;
import com.supporthub.knowledge.shared.error.BusinessValidationException;
import com.supporthub.knowledge.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Application service that drives the lifecycle of a knowledge article draft.
 *
 * <p>The lifecycle is: an operator proposes an article from a resolved ticket (delivered as a
 * Kafka event), an organization manager reviews it, and then either publishes it as a
 * {@link com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity} or rejects it.
 * The draft state machine ({@code PENDING_REVIEW -> PUBLISHED / REJECTED}) and its invariants live
 * in {@link KnowledgeArticleDraftEntity}; this service only orchestrates it, enforces access rules
 * via {@link KnowledgeAccessPolicy}, and emits domain events via {@link KnowledgeEventPublisher}.
 *
 * <p>All write methods run in a transaction so that the entity change and the resulting event are
 * committed atomically (the publisher only sends the event after commit).
 */
@Service
@RequiredArgsConstructor
public class KnowledgeDraftService {

    private final KnowledgeArticleDraftRepository draftRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeMapper mapper;
    private final KnowledgeAccessPolicy accessPolicy;
    private final KnowledgeEventPublisher eventPublisher;
    private final Clock clock;

    /**
     * Creates a draft from a {@code KnowledgeArticleProposed} event coming from the ticket service.
     *
     * <p>Idempotent: a {@code source_ticket_id} has a unique constraint, so if a draft already
     * exists for the ticket (e.g. the event is redelivered) the existing draft is returned and no
     * duplicate is created. Only the first proposal for a ticket emits a {@code DraftSaved} event.
     */
    @Transactional
    public DraftResponse createDraftFromTicketProposal(KnowledgeArticleProposedPayload payload) {
        return draftRepository.findWithTagsBySourceTicketId(payload.ticketId())
                .map(mapper::toDraftResponse)
                .orElseGet(() -> createNewDraft(payload));
    }

    @Transactional(readOnly = true)
    public List<DraftResponse> listDrafts(CurrentUser user, DraftStatus status) {
        accessPolicy.requireOrganizationScope(user);
        List<KnowledgeArticleDraftEntity> drafts;
        if (user.isSupportHubAdministrator() && user.organizationId() == null) {
            drafts = status == null
                    ? draftRepository.findAllByOrderByCreatedAtDesc()
                    : draftRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            drafts = status == null
                    ? draftRepository.findByOrganizationIdOrderByCreatedAtDesc(user.organizationId())
                    : draftRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(user.organizationId(), status);
        }
        return drafts.stream().map(mapper::toDraftResponse).toList();
    }

    @Transactional(readOnly = true)
    public DraftResponse getDraft(CurrentUser user, UUID draftId) {
        var draft = draftRepository.findWithTagsById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge draft", draftId));
        accessPolicy.requireDraftManagement(user, draft.getOrganizationId());
        return mapper.toDraftResponse(draft);
    }

    /**
     * Publishes a draft as a knowledge article.
     *
     * <p>The manager may override the title; if the request title is blank the draft title is kept.
     * Content is mandatory and trimmed. The draft is moved to {@code PUBLISHED} (which fails if it
     * is not in {@code PENDING_REVIEW}) and a new {@link KnowledgeArticleEntity} is created from it,
     * followed by a {@code KnowledgeArticlePublished} event.
     */
    @Transactional
    public ArticleResponse publishDraft(CurrentUser user, UUID draftId, PublishDraftRequest request) {
        var draft = draftRepository.findWithTagsById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge draft", draftId));
        accessPolicy.requireDraftManagement(user, draft.getOrganizationId());

        var title = request.title() == null || request.title().isBlank() ? draft.getTitle() : request.title().trim();
        var content = request.content().trim();
        if (content.isBlank()) {
            throw new BusinessValidationException("Article content is required for publication");
        }

        var now = clock.instant();
        draft.publish(title, content, now);
        var article = KnowledgeArticleEntity.publishFromDraft(draft, user.userId(), now);
        articleRepository.save(article);
        eventPublisher.publishKnowledgeArticlePublished(article);
        return mapper.toArticleResponse(article);
    }

    /**
     * Rejects a pending draft with a mandatory reason and emits a {@code KnowledgeArticleRejected}
     * event. Fails if the draft is not in {@code PENDING_REVIEW}.
     */
    @Transactional
    public DraftResponse rejectDraft(CurrentUser user, UUID draftId, RejectDraftRequest request) {
        var draft = draftRepository.findWithTagsById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge draft", draftId));
        accessPolicy.requireDraftManagement(user, draft.getOrganizationId());
        draft.reject(request.rejectionReason().trim(), clock.instant());
        eventPublisher.publishKnowledgeArticleRejected(draft, user.userId());
        return mapper.toDraftResponse(draft);
    }

    private DraftResponse createNewDraft(KnowledgeArticleProposedPayload payload) {
        var draft = KnowledgeArticleDraftEntity.pending(
                payload.ticketId(),
                payload.organizationId(),
                payload.proposerId(),
                payload.proposedSubject(),
                payload.tags(),
                clock.instant()
        );
        var saved = draftRepository.save(draft);
        eventPublisher.publishKnowledgeArticleDraftSaved(saved);
        return mapper.toDraftResponse(saved);
    }
}

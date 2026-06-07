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
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.security.CurrentUser;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
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
 * <p>All write methods run in a transaction so that the entity change and the outbox event are
 * committed atomically. Kafka delivery happens later through the outbox publisher.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeDraftService {

    private static final String KNOWLEDGE_DRAFT_RESOURCE = "Knowledge draft";

    private final KnowledgeArticleDraftRepository draftRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeMapper mapper;
    private final KnowledgeArticleDraftFactory draftFactory;
    private final KnowledgeAccessPolicy accessPolicy;
    private final KnowledgeEventPublisher eventPublisher;
    private final TicketConversationProjection ticketConversationProjection;
    private final Clock clock;

    /**
     * Phase 1 of turning a {@code KnowledgeArticleProposed} event into a draft: persist a draft in
     * {@code PENDING_GENERATION}. The body is produced asynchronously later (see the generation
     * flow), keeping any external AI call out of the inbox transaction.
     *
     * <p>The proposal event carries no organization, so it is resolved from the local ticket
     * conversation projection. If the ticket is unknown locally the proposal is invalid and a
     * {@code DraftSaveFailed} compensation is emitted instead of creating a draft.
     *
     * <p>Idempotent: {@code source_ticket_id} is unique, so a redelivered proposal for an existing
     * draft is a no-op.
     */
    @Transactional
    public void createDraftFromTicketProposal(KnowledgeArticleProposedPayload payload) {
        if (draftRepository.findWithTagsBySourceTicketId(payload.ticketId()).isPresent()) {
            return;
        }

        var organizationId = ticketConversationProjection.resolveOrganizationId(payload.ticketId()).orElse(null);
        if (organizationId == null) {
            eventPublisher.publishKnowledgeArticleDraftSaveFailed(
                    payload.ticketId(),
                    null,
                    DraftSaveFailureCode.INVALID_ARTICLE_PROPOSAL,
                    "No ticket conversation is known for ticket " + payload.ticketId()
            );
            return;
        }

        var proposal = mapper.toPendingDraftProposal(payload, organizationId);
        var draft = draftFactory.createPendingGeneration(proposal);
        draftRepository.save(draft);
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
                .orElseThrow(() -> new ResourceNotFoundException(KNOWLEDGE_DRAFT_RESOURCE, draftId));
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
                .orElseThrow(() -> new ResourceNotFoundException(KNOWLEDGE_DRAFT_RESOURCE, draftId));
        accessPolicy.requireDraftManagement(user, draft.getOrganizationId());

        var title = request.title() == null || request.title().isBlank() ? draft.getTitle() : request.title().trim();
        var content = resolvePublicationContent(request, draft);

        var publishedAt = clock.instant();
        draft.publish(title, content, publishedAt);
        var article = KnowledgeArticleEntity.publishFromDraft(draft, user.userId(), publishedAt);
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
                .orElseThrow(() -> new ResourceNotFoundException(KNOWLEDGE_DRAFT_RESOURCE, draftId));
        accessPolicy.requireDraftManagement(user, draft.getOrganizationId());
        var rejectedAt = clock.instant();
        draft.reject(request.rejectionReason().trim(), rejectedAt);
        eventPublisher.publishKnowledgeArticleRejected(draft, user.userId());
        return mapper.toDraftResponse(draft);
    }

    /**
     * Chooses the body to publish: the manager's edited content when provided, otherwise the
     * AI-generated draft body. At least one must be non-blank.
     */
    private String resolvePublicationContent(PublishDraftRequest request, KnowledgeArticleDraftEntity draft) {
        if (request.content() != null && !request.content().isBlank()) {
            return request.content().trim();
        }
        var draftContent = draft.getContent();
        if (draftContent == null || draftContent.isBlank()) {
            throw new BusinessValidationException("Article content is required for publication");
        }
        return draftContent.trim();
    }
}

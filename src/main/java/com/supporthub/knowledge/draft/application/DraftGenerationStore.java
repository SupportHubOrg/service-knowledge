package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.DraftIds;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftRepository;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleTitleConverter;
import com.supporthub.knowledge.shared.identity.OrganizationIdConverter;
import com.supporthub.knowledge.shared.identity.TicketIdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Transactional persistence boundary for the two-phase draft generation flow. Each method is a short
 * transaction; the external AI call happens between {@link #claim} and {@link #completeGeneration} /
 * {@link #failGeneration}, never inside a transaction. The transactional outbox writes for
 * {@code DraftSaved} / {@code DraftSaveFailed} commit together with the draft state transition.
 */
@Component
@RequiredArgsConstructor
public class DraftGenerationStore {

    private final KnowledgeArticleDraftRepository draftRepository;
    private final KnowledgeEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<DraftId> findPendingGenerationIds(int batchSize) {
        return draftRepository.findIdsByStatus(DraftStatus.PENDING_GENERATION, PageRequest.of(0, batchSize));
    }

    @Transactional(readOnly = true)
    public Optional<DraftGenerationInput> claim(DraftId draftId) {
        return draftRepository.findById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .map(draft -> new DraftGenerationInput(
                        DraftIds.toRaw(draft.getId()),
                        TicketIdConverter.toRaw(draft.getSourceTicketId()),
                        OrganizationIdConverter.toRaw(draft.getOrganizationId()),
                        ArticleTitleConverter.toPlainText(draft.getTitle())
                ));
    }

    @Transactional
    public void completeGeneration(DraftId draftId, String content) {
        draftRepository.findWithTagsById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .ifPresent(draft -> {
                    var generationCompletedAt = clock.instant();
                    draft.completeGeneration(ArticleContent.of(content), generationCompletedAt);
                    eventPublisher.publishKnowledgeArticleDraftSaved(draft);
                });
    }

    @Transactional
    public void failGeneration(DraftId draftId, DraftSaveFailureCode failureCode, String failureMessage) {
        draftRepository.findById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .ifPresent(draft -> {
                    var generationFailedAt = clock.instant();
                    draft.failGeneration(generationFailedAt);
                    eventPublisher.publishKnowledgeArticleDraftSaveFailed(
                            draft.getSourceTicketId(),
                            draft.getOrganizationId(),
                            failureCode,
                            failureMessage
                    );
                });
    }
}

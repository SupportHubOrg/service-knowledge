package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftRepository;
import com.supporthub.knowledge.messaging.KnowledgeEventPublisher;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<UUID> findPendingGenerationIds(int batchSize) {
        return draftRepository.findIdsByStatus(DraftStatus.PENDING_GENERATION, PageRequest.of(0, batchSize));
    }

    @Transactional(readOnly = true)
    public Optional<DraftGenerationInput> claim(UUID draftId) {
        return draftRepository.findById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .map(draft -> new DraftGenerationInput(
                        draft.getId(),
                        draft.getSourceTicketId(),
                        draft.getOrganizationId(),
                        draft.getTitle()
                ));
    }

    @Transactional
    public void completeGeneration(UUID draftId, String content) {
        draftRepository.findWithTagsById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .ifPresent(draft -> {
                    draft.completeGeneration(content, clock.instant());
                    eventPublisher.publishKnowledgeArticleDraftSaved(draft);
                });
    }

    @Transactional
    public void failGeneration(UUID draftId, DraftSaveFailureCode failureCode, String failureMessage) {
        draftRepository.findById(draftId)
                .filter(draft -> draft.getStatus() == DraftStatus.PENDING_GENERATION)
                .ifPresent(draft -> {
                    draft.failGeneration(clock.instant());
                    eventPublisher.publishKnowledgeArticleDraftSaveFailed(
                            draft.getSourceTicketId(),
                            draft.getOrganizationId(),
                            failureCode,
                            failureMessage
                    );
                });
    }
}

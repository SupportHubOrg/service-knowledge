package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.ai.ArticleDraftContentGenerator;
import com.supporthub.knowledge.ai.ArticleGenerationContext;
import com.supporthub.knowledge.ai.ConversationMessage;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Phase 2 of article creation: turns {@code PENDING_GENERATION} drafts into reviewable ones by
 * generating their body from the ticket conversation.
 *
 * <p>Orchestration only - it reads the draft snapshot and conversation through transactional
 * collaborators, calls the {@link ArticleDraftContentGenerator} <b>outside any transaction</b> (it
 * is an external integration that may do network I/O), then persists the outcome in a short
 * transaction via {@link DraftGenerationStore}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DraftContentGenerationService {

    private final DraftGenerationStore generationStore;
    private final TicketConversationProjection ticketConversationProjection;
    private final ArticleDraftContentGenerator contentGenerator;
    private final DraftGenerationProperties properties;

    public void generatePendingDrafts() {
        var draftIds = generationStore.findPendingGenerationIds(properties.batchSize());
        for (var draftId : draftIds) {
            generateOne(draftId);
        }
    }

    private void generateOne(UUID draftId) {
        var input = generationStore.claim(draftId).orElse(null);
        if (input == null) {
            return;
        }

        try {
            var content = contentGenerator.generate(toContext(input));
            generationStore.completeGeneration(draftId, content.content());
            log.info("Generated knowledge draft body. draftId={}, ticketId={}", draftId, input.sourceTicketId());
        } catch (RuntimeException exception) {
            log.warn("Knowledge draft body generation failed. draftId={}, ticketId={}",
                    draftId, input.sourceTicketId(), exception);
            generationStore.failGeneration(
                    draftId,
                    DraftSaveFailureCode.AI_CONTENT_PREPARATION_FAILED,
                    exception.getMessage()
            );
        }
    }

    private ArticleGenerationContext toContext(DraftGenerationInput input) {
        List<ConversationMessage> conversation = ticketConversationProjection.conversation(input.sourceTicketId())
                .stream()
                .map(message -> new ConversationMessage(message.senderType(), message.message()))
                .toList();
        return new ArticleGenerationContext(
                input.sourceTicketId(),
                input.organizationId(),
                input.proposedSubject(),
                conversation
        );
    }
}

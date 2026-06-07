package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.ai.ArticleDraftContentGenerator;
import com.supporthub.knowledge.ai.ArticleGenerationContext;
import com.supporthub.knowledge.ai.ArticleGenerationException;
import com.supporthub.knowledge.ai.GeneratedArticleContent;
import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.messaging.event.DraftSaveFailureCode;
import com.supporthub.knowledge.ticket.application.TicketConversationMessage;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DraftContentGenerationServiceTest {

    private final DraftGenerationStore store = mock(DraftGenerationStore.class);
    private final TicketConversationProjection projection = mock(TicketConversationProjection.class);
    private final ArticleDraftContentGenerator generator = mock(ArticleDraftContentGenerator.class);
    private final DraftGenerationProperties properties =
            new DraftGenerationProperties(10, 1000, Duration.ofMinutes(2), Duration.ZERO);

    private final DraftContentGenerationService service =
            new DraftContentGenerationService(store, projection, generator, properties);

    @Test
    void shouldGenerateAndCompleteDraft() {
        var draftId = DraftId.of(UUID.randomUUID());
        var ticketRawId = UUID.randomUUID();
        var input = new DraftGenerationInput(UUID.randomUUID(), ticketRawId, UUID.randomUUID(), "Reset VPN token");
        when(store.findPendingGenerationIds(properties.batchSize())).thenReturn(List.of(draftId));
        when(store.claim(draftId)).thenReturn(Optional.of(input));
        when(projection.conversation(ticketRawId))
                .thenReturn(List.of(new TicketConversationMessage("Operator", "Reset it.")));
        when(generator.generate(any(ArticleGenerationContext.class)))
                .thenReturn(new GeneratedArticleContent("Generated body"));

        service.generatePendingDrafts();

        verify(store).completeGeneration(draftId, "Generated body");
    }

    @Test
    void shouldFailDraftWhenGeneratorThrows() {
        var draftId = DraftId.of(UUID.randomUUID());
        var ticketRawId = UUID.randomUUID();
        var input = new DraftGenerationInput(UUID.randomUUID(), ticketRawId, UUID.randomUUID(), "Reset VPN token");
        when(store.findPendingGenerationIds(properties.batchSize())).thenReturn(List.of(draftId));
        when(store.claim(draftId)).thenReturn(Optional.of(input));
        when(projection.conversation(ticketRawId)).thenReturn(List.of());
        when(generator.generate(any(ArticleGenerationContext.class)))
                .thenThrow(new ArticleGenerationException("no content"));

        service.generatePendingDrafts();

        verify(store).failGeneration(draftId, DraftSaveFailureCode.AI_CONTENT_PREPARATION_FAILED, "no content");
    }

    @Test
    void shouldSkipDraftThatIsNoLongerClaimable() {
        var draftId = DraftId.of(UUID.randomUUID());
        when(store.findPendingGenerationIds(properties.batchSize())).thenReturn(List.of(draftId));
        when(store.claim(draftId)).thenReturn(Optional.empty());

        service.generatePendingDrafts();

        verify(store).findPendingGenerationIds(properties.batchSize());
        verify(store).claim(draftId);
        verifyNoMoreInteractions(store);
    }
}

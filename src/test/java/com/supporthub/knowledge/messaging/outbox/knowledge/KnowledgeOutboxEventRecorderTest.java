package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventWriter;
import com.supporthub.knowledge.shared.content.ArticleContent;
import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeOutboxEventRecorderTest {

    private final OutboxEventWriter eventWriter = mock(OutboxEventWriter.class);
    private final KnowledgeOutboxPayloadFactory payloadFactory = new KnowledgeOutboxPayloadFactory();
    private final KnowledgeOutboxEventRecorder recorder = new KnowledgeOutboxEventRecorder(eventWriter, payloadFactory);

    @Test
    void shouldAppendPublishedArticleEventKeyedBySourceTicket() {
        var sourceTicketId = UUID.randomUUID();
        var draft = KnowledgeArticleDraftEntity.pendingGeneration(
                DraftId.of(UUID.randomUUID()),
                PendingDraftProposal.builder()
                        .sourceTicketId(TicketId.of(sourceTicketId))
                        .organizationId(OrganizationId.of(UUID.randomUUID()))
                        .proposerId(UserId.of(UUID.randomUUID()))
                        .title(ArticleTitle.of("Draft title"))
                        .tagIds(List.of(TagId.of(UUID.randomUUID())))
                        .build()
        );
        draft.completeGeneration(ArticleContent.of("Generated content"), Instant.parse("2026-06-01T11:30:00Z"));
        draft.publish(ArticleTitle.of("Article title"), ArticleContent.of("Article content"),
                Instant.parse("2026-06-01T12:00:00Z"));
        var article = KnowledgeArticleEntity.publishFromDraft(
                draft,
                UserId.of(UUID.randomUUID()),
                Instant.parse("2026-06-01T12:01:00Z")
        );

        recorder.publishKnowledgeArticlePublished(article);

        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventWriter).append(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(KnowledgeArticlePublishedPayload.class);
        assertThat(((KnowledgeArticlePublishedPayload) eventCaptor.getValue()).sourceTicketId())
                .isEqualTo(sourceTicketId);
    }
}

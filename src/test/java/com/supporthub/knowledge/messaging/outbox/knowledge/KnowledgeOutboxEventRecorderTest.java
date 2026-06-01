package com.supporthub.knowledge.messaging.outbox.knowledge;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.messaging.event.IntegrationEventPayload;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeOutboxEventRecorderTest {

    private final OutboxEventWriter eventWriter = mock(OutboxEventWriter.class);
    private final KnowledgeOutboxPayloadFactory payloadFactory = new KnowledgeOutboxPayloadFactory();
    private final KnowledgeOutboxEventRecorder recorder = new KnowledgeOutboxEventRecorder(eventWriter, payloadFactory);

    @Test
    void shouldAppendPublishedArticleEventKeyedBySourceTicket() {
        var sourceTicketId = UUID.randomUUID();
        var draft = KnowledgeArticleDraftEntity.pending(
                sourceTicketId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Draft title",
                List.of(UUID.randomUUID()),
                Instant.parse("2026-06-01T11:00:00Z")
        );
        draft.publish("Article title", "Article content", Instant.parse("2026-06-01T12:00:00Z"));
        var article = KnowledgeArticleEntity.publishFromDraft(
                draft,
                UUID.randomUUID(),
                Instant.parse("2026-06-01T12:01:00Z")
        );

        recorder.publishKnowledgeArticlePublished(article);

        var eventCaptor = ArgumentCaptor.forClass(IntegrationEventPayload.class);
        verify(eventWriter).append(eq(sourceTicketId.toString()), eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo("KnowledgeArticlePublished");
    }
}

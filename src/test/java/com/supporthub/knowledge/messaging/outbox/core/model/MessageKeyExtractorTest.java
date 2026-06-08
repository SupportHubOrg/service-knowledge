package com.supporthub.knowledge.messaging.outbox.core.model;

import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.event.annotation.MessageKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageKeyExtractorTest {

    private final MessageKeyExtractor extractor = new MessageKeyExtractor();

    @Test
    void shouldExtractAnnotatedComponentValueAsKey() {
        var sourceTicketId = UUID.randomUUID();
        var payload = new KnowledgeArticlePublishedPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                sourceTicketId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Article title",
                "Article content",
                List.of(UUID.randomUUID())
        );

        assertThat(extractor.extract(payload)).isEqualTo(sourceTicketId.toString());
    }

    @Test
    void shouldRejectPayloadWithoutMessageKey() {
        assertThatThrownBy(() -> extractor.extract(new NoKeyPayload(UUID.randomUUID())))
                .isInstanceOf(MessageKeyResolutionException.class)
                .hasMessageContaining("Exactly one @MessageKey");
    }

    @Test
    void shouldRejectNullKeyValue() {
        assertThatThrownBy(() -> extractor.extract(new NullableKeyPayload(null)))
                .isInstanceOf(MessageKeyResolutionException.class)
                .hasMessageContaining("is null");
    }

    private record NoKeyPayload(UUID id) {
    }

    private record NullableKeyPayload(@MessageKey UUID key) {
    }
}

package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;
import com.supporthub.knowledge.messaging.event.annotation.MessageKey;

import java.util.UUID;

@IntegrationEvent("knowledge-article-draft-save-failed")
public record KnowledgeArticleDraftSaveFailedPayload(
        @MessageKey UUID sourceTicketId,
        UUID organizationId,
        DraftSaveFailureCode failureCode,
        String failureMessage
) {
}

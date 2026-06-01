package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/** Outbound payload of {@code KnowledgeArticleRejected}: a manager declined a draft, with reason. */
public record KnowledgeArticleRejectedPayload(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID rejectedById,
        String rejectionReason
) {
}

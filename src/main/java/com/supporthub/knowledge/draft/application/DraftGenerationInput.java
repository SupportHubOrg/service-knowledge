package com.supporthub.knowledge.draft.application;

import java.util.UUID;

/**
 * Snapshot of a draft awaiting body generation, read inside a short transaction and then used to
 * build the generation context outside any transaction.
 */
public record DraftGenerationInput(
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        String proposedSubject
) {
}

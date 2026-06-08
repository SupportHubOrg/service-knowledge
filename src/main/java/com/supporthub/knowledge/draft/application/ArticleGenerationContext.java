package com.supporthub.knowledge.draft.application;

import java.util.List;
import java.util.UUID;

/**
 * Everything the generator needs to turn a resolved ticket into a knowledge article body.
 *
 * @param ticketId        source ticket
 * @param organizationId  owning organization
 * @param proposedSubject operator-proposed title, usable as a hint
 * @param conversation    the ticket conversation, ordered chronologically
 */
public record ArticleGenerationContext(
        UUID ticketId,
        UUID organizationId,
        String proposedSubject,
        List<ConversationMessage> conversation
) {
}

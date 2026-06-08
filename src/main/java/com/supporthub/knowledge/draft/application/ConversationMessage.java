package com.supporthub.knowledge.draft.application;

/**
 * One message of a ticket conversation, as input to the article content generator.
 *
 * @param senderType role of the author (e.g. {@code Employee}, {@code Operator}, {@code AI})
 * @param message    message body
 */
public record ConversationMessage(
        String senderType,
        String message
) {
}

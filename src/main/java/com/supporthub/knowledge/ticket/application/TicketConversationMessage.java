package com.supporthub.knowledge.ticket.application;

/**
 * Read-model view of one ticket conversation message, used as input for article generation.
 *
 * @param senderType role of the author (e.g. {@code Employee}, {@code Operator}, {@code AI})
 * @param message    message body
 */
public record TicketConversationMessage(
        String senderType,
        String message
) {
}

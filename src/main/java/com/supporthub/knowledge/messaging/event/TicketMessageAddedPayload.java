package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;

import java.util.UUID;

/**
 * Inbound payload of the {@code TicketMessageAdded} event from the ticket-management service: a new
 * message (from an employee, operator or AI) was added to a ticket conversation. Consumed to build
 * the local ticket conversation projection
 * ({@link com.supporthub.knowledge.ticket.persistence.TicketConversationMessageEntity}), which later
 * supplies both the organization of a proposed article and the raw text for article generation.
 *
 * @param ticketId       the ticket the message belongs to
 * @param organizationId organization the ticket belongs to (not present in {@code KnowledgeArticleProposed})
 * @param messageId      unique message id (drives idempotency)
 * @param senderId       author of the message
 * @param senderType     role of the author, e.g. {@code Employee}, {@code Operator}, {@code AI}
 * @param message        message body
 */
@IntegrationEvent("ticket-message-added")
public record TicketMessageAddedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID messageId,
        UUID senderId,
        String senderType,
        String message
) {
}

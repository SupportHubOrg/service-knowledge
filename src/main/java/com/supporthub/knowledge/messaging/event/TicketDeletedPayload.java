package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Inbound payload of the {@code TicketDeleted} event from the ticket-management service. Consumed to
 * clean up any knowledge data derived from the ticket (draft, conversation projection) and to
 * archive the published article, if any.
 *
 * @param ticketId       the deleted ticket
 * @param organizationId organization the ticket belonged to
 * @param deletedById    actor who deleted the ticket
 * @param actorType      role of the actor (e.g. {@code Operator})
 */
@IntegrationEvent("ticket-deleted")
public record TicketDeletedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID deletedById,
        String actorType
) {
}

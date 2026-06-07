package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Inbound payload of the {@code OrganizationCreated} event from organization-access. The event
 * establishes the first organization manager, so knowledge can authorize manager-only actions
 * without trusting business roles from JWT claims.
 */
@IntegrationEvent("organization-created")
public record OrganizationCreatedPayload(
        UUID organizationId,
        String organizationName,
        String tier,
        UUID managerId
) {
}

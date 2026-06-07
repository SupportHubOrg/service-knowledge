package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Inbound payload of the {@code UserRemovedFromOrganization} event from organization-access.
 * Consumed to revoke local knowledge access for a removed user.
 */
@IntegrationEvent("user-removed-from-organization")
public record UserRemovedFromOrganizationPayload(
        UUID organizationId,
        UUID userId,
        String userEmail
) {
}

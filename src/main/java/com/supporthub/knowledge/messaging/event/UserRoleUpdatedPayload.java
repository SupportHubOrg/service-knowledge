package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Inbound payload of the {@code UserRoleUpdated} event from organization-access. Consumed to keep
 * the local authorization projection aligned with the organization-access source of truth.
 */
@IntegrationEvent("user-role-updated")
public record UserRoleUpdatedPayload(
        UUID organizationId,
        UUID userId,
        String oldRole,
        String newRole
) {
}

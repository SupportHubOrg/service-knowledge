package com.supporthub.knowledge.messaging.event;

import java.util.UUID;

/**
 * Inbound payload of the {@code OrganizationDeleted} event from the organization-access service.
 * Consumed to clean up all knowledge data belonging to the organization.
 *
 * @param organizationId   the deleted organization
 * @param organizationName name of the deleted organization
 */
@IntegrationEvent("organization-deleted")
public record OrganizationDeletedPayload(
        UUID organizationId,
        String organizationName
) {
}

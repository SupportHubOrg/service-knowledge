package com.supporthub.knowledge.security;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of the authenticated caller for the duration of one request, derived from the
 * JWT by {@link CurrentUserResolver}. Used by {@link KnowledgeAccessPolicy} to make authorization
 * decisions. {@code organizationId} may be {@code null} for cross-organization admins.
 */
public record CurrentUser(
        UUID userId,
        UUID organizationId,
        Set<SupportHubRole> roles
) {

    /** True for SupportHub administrators and internal SYSTEM callers (treated as super-users). */
    public boolean isSupportHubAdministrator() {
        return roles.contains(SupportHubRole.SUPPORT_HUB_ADMINISTRATOR)
                || roles.contains(SupportHubRole.SYSTEM);
    }

    public boolean isOrganizationManager() {
        return roles.contains(SupportHubRole.ORGANIZATION_MANAGER);
    }

    /** True when this user is scoped to the given organization. */
    public boolean belongsTo(UUID organizationId) {
        return this.organizationId != null && this.organizationId.equals(organizationId);
    }
}

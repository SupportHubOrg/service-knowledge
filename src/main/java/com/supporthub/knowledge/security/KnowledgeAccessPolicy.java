package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationId;
import org.springframework.stereotype.Component;

/**
 * Single place that holds the authorization rules for knowledge resources. Each method throws
 * {@link AccessDeniedException} when the rule is not satisfied; returning normally means "allowed".
 *
 * <p>Rules in plain language:
 * <ul>
 *   <li><b>Manage drafts</b> (review/publish/reject): a SupportHub administrator (any org), or an
 *       organization manager acting within their own organization.</li>
 *   <li><b>Read articles</b>: a SupportHub administrator, or any user belonging to the article's
 *       organization.</li>
 *   <li><b>Organization scope</b>: every non-admin caller must carry an organization id in their
 *       token; admins may operate without one (cross-organization view).</li>
 * </ul>
 */
@Component
public class KnowledgeAccessPolicy {

    /** Allows SupportHub admins, or organization managers operating on their own organization. */
    public void requireDraftManagement(CurrentUser user, OrganizationId organizationId) {
        if (user.isSupportHubAdministrator()) {
            return;
        }
        if (user.isOrganizationManager() && user.belongsTo(organizationId)) {
            return;
        }
        throw new AccessDeniedException("User cannot manage knowledge drafts for organization " + organizationId);
    }

    /** Allows SupportHub admins, or any member of the article's organization. */
    public void requireArticleRead(CurrentUser user, OrganizationId organizationId) {
        if (user.isSupportHubAdministrator() || user.belongsTo(organizationId)) {
            return;
        }
        throw new AccessDeniedException("User cannot read knowledge articles for organization " + organizationId);
    }

    /** Ensures a non-admin caller has an organization id; admins are exempt (may span orgs). */
    public void requireOrganizationScope(CurrentUser user) {
        if (user.organizationId() == null && !user.isSupportHubAdministrator()) {
            throw new AccessDeniedException("User token does not contain organization scope");
        }
    }
}

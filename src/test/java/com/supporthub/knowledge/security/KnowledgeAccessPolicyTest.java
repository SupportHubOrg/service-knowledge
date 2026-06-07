package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationId;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeAccessPolicyTest {

    private final KnowledgeAccessPolicy policy = new KnowledgeAccessPolicy();

    @Test
    void shouldAllowOrganizationManagerForOwnOrganization() {
        var organizationId = UUID.randomUUID();
        var user = new CurrentUser(UUID.randomUUID(), organizationId, Set.of(SupportHubRole.ORGANIZATION_MANAGER));

        assertThatNoException().isThrownBy(() -> policy.requireDraftManagement(user, OrganizationId.of(organizationId)));
    }

    @Test
    void shouldRejectOrganizationManagerFromAnotherOrganization() {
        var user = new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Set.of(SupportHubRole.ORGANIZATION_MANAGER));

        OrganizationId organizationId = OrganizationId.of(UUID.randomUUID());

        assertThatThrownBy(() -> policy.requireDraftManagement(user, organizationId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowAdministratorAcrossOrganizations() {
        var user = new CurrentUser(UUID.randomUUID(), null, Set.of(SupportHubRole.SUPPORT_HUB_ADMINISTRATOR));

        assertThatNoException().isThrownBy(() -> policy.requireDraftManagement(user, OrganizationId.of(UUID.randomUUID())));
    }
}

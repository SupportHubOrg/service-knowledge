package com.supporthub.knowledge.security;

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

        assertThatNoException().isThrownBy(() -> policy.requireDraftManagement(user, organizationId));
    }

    @Test
    void shouldRejectOrganizationManagerFromAnotherOrganization() {
        var user = new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), Set.of(SupportHubRole.ORGANIZATION_MANAGER));

        assertThatThrownBy(() -> policy.requireDraftManagement(user, UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowAdministratorAcrossOrganizations() {
        var user = new CurrentUser(UUID.randomUUID(), null, Set.of(SupportHubRole.SUPPORT_HUB_ADMINISTRATOR));

        assertThatNoException().isThrownBy(() -> policy.requireDraftManagement(user, UUID.randomUUID()));
    }
}

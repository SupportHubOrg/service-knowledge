package com.supporthub.knowledge.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupportHubRoleTest {

    @Test
    void shouldReadCurrentAndDocumentedRoleNames() {
        assertThat(SupportHubRole.fromWireValue("LEVEL_1_OPERATOR")).isEqualTo(SupportHubRole.LEVEL_1_OPERATOR);
        assertThat(SupportHubRole.fromWireValue("OperatorLevel1")).isEqualTo(SupportHubRole.LEVEL_1_OPERATOR);
        assertThat(SupportHubRole.fromWireValue("OrganizationManager")).isEqualTo(SupportHubRole.ORGANIZATION_MANAGER);
        assertThat(SupportHubRole.fromWireValue("ROLE_SUPPORT_HUB_ADMINISTRATOR"))
                .isEqualTo(SupportHubRole.SUPPORT_HUB_ADMINISTRATOR);
    }
}

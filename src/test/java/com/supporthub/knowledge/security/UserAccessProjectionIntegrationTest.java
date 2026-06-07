package com.supporthub.knowledge.security;

import com.supporthub.knowledge.support.KnowledgeIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccessProjectionIntegrationTest extends KnowledgeIntegrationTestSupport {

    private final UserAccessProjection projection;
    private final CurrentUserResolver currentUserResolver;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    UserAccessProjectionIntegrationTest(UserAccessProjection projection,
                                        CurrentUserResolver currentUserResolver,
                                        JdbcTemplate jdbcTemplate) {
        this.projection = projection;
        this.currentUserResolver = currentUserResolver;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from knowledge_user_access");
    }

    @Test
    void shouldResolveCurrentUserFromLocalProjectionInsteadOfJwtBusinessClaims() {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var maliciousOrganizationId = UUID.randomUUID();
        projection.upsert(userId, organizationId, "ORGANIZATION_MANAGER");

        var currentUser = currentUserResolver.from(jwt(userId)
                .claim("organization_id", maliciousOrganizationId.toString())
                .claim("roles", List.of("SUPPORT_HUB_ADMINISTRATOR"))
                .build());

        assertThat(currentUser.userId()).isEqualTo(userId);
        assertThat(currentUser.organizationId()).isEqualTo(organizationId);
        assertThat(currentUser.roles()).containsExactly(SupportHubRole.ORGANIZATION_MANAGER);
    }

    @Test
    void shouldRejectAuthenticatedUserWithoutLocalAccessProjection() {
        var userId = UUID.randomUUID();

        var jwt = jwt(userId).build();

        assertThatThrownBy(() -> currentUserResolver.from(jwt))
                .isInstanceOf(UnknownUserAccessException.class)
                .hasMessage("User access is not known by knowledge service: " + userId);
    }

    @Test
    void shouldRemoveUsersByOrganization() {
        var organizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        projection.upsert(userId, organizationId, "Employee");
        projection.upsert(otherUserId, otherOrganizationId, "Employee");

        projection.removeOrganization(organizationId);

        assertThat(projection.find(userId)).isEmpty();
        assertThat(projection.find(otherUserId)).isPresent();
    }

    private Jwt.Builder jwt(UUID userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
    }
}

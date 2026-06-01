package com.supporthub.knowledge.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Translates a validated {@link Jwt} into the service's own {@link CurrentUser} view.
 *
 * <p>Organization id is read from either {@code organization_id} or {@code organizationId} (first
 * non-blank wins); absent means {@code null} (typically a cross-org admin).
 *
 * <p>Roles are gathered from three possible locations to stay compatible with both flat tokens and
 * Keycloak: a flat {@code roles} claim, Keycloak {@code realm_access.roles}, and Keycloak
 * {@code resource_access.*.roles}. Each raw value is normalized (trimmed, {@code -}/space to
 * {@code _}, upper-cased, {@code ROLE_} prefix stripped) and mapped to {@link SupportHubRole};
 * unknown values are silently dropped so foreign roles don't break resolution.
 */
@Component
public class CurrentUserResolver {

    private static final String ROLES_CLAIM = "roles";

    /** Builds the {@link CurrentUser} from the token subject, organization claim and roles. */
    public CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                organizationId(jwt),
                roles(jwt)
        );
    }

    private UUID organizationId(Jwt jwt) {
        var organizationId = firstStringClaim(jwt);
        return organizationId == null || organizationId.isBlank() ? null : UUID.fromString(organizationId);
    }

    private String firstStringClaim(Jwt jwt) {
        for (String name : new String[]{"organization_id", "organizationId"}) {
            var value = jwt.getClaimAsString(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Set<SupportHubRole> roles(Jwt jwt) {
        var roles = new LinkedHashSet<SupportHubRole>();
        addStringRoles(roles, jwt.getClaimAsStringList(ROLES_CLAIM));
        addKeycloakRealmRoles(roles, jwt);
        addKeycloakResourceRoles(roles, jwt);
        return roles;
    }

    private void addKeycloakRealmRoles(Set<SupportHubRole> roles, Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return;
        }
        var rawRoles = realmAccess.get(ROLES_CLAIM);
        if (rawRoles instanceof Collection<?> collection) {
            addStringRoles(roles, collection.stream().map(String::valueOf).toList());
        }
    }

    private void addKeycloakResourceRoles(Set<SupportHubRole> roles, Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return;
        }
        for (Object clientAccess : resourceAccess.values()) {
            if (clientAccess instanceof Map<?, ?> clientMap) {
                var rawRoles = clientMap.get(ROLES_CLAIM);
                if (rawRoles instanceof Collection<?> collection) {
                    addStringRoles(roles, collection.stream().map(String::valueOf).toList());
                }
            }
        }
    }

    private void addStringRoles(Set<SupportHubRole> roles, Collection<String> rawRoles) {
        if (rawRoles == null) {
            return;
        }
        rawRoles.stream()
                .map(this::normalize)
                .map(this::roleOrNull)
                .filter(Objects::nonNull)
                .forEach(roles::add);
    }

    private String normalize(String role) {
        return role.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase()
                .replace("ROLE_", "");
    }

    private SupportHubRole roleOrNull(String role) {
        try {
            return SupportHubRole.valueOf(role);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

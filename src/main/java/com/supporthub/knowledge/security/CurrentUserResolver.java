package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationIdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Translates a validated {@link Jwt} into the service's own {@link CurrentUser} view.
 *
 * <p>The JWT is treated as identity only: the subject identifies the authenticated user. Business
 * access (organization and role) is resolved from the local projection fed by organization-access
 * events, matching the ownership model used by the .NET services.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserAccessProjection userAccessProjection;

    /** Builds the {@link CurrentUser} from the token subject and local access projection. */
    public CurrentUser from(Jwt jwt) {
        var userId = UUID.fromString(jwt.getSubject());
        var access = userAccessProjection.find(userId)
                .orElseThrow(() -> new UnknownUserAccessException(userId));
        return new CurrentUser(
                userId,
                OrganizationIdConverter.toRaw(access.getOrganizationId()),
                Set.of(access.getRole())
        );
    }
}

package com.supporthub.knowledge.messaging.event;

import com.supporthub.knowledge.messaging.event.annotation.IntegrationEvent;

import java.util.UUID;

/**
 * Inbound payload of the {@code UserInvitationAccepted} event from organization-access. Consumed to
 * build the local user access projection used by REST authorization.
 */
@IntegrationEvent("user-invitation-accepted")
public record UserInvitationAcceptedPayload(
        UUID organizationId,
        UUID userId,
        String userEmail,
        String role
) {
}

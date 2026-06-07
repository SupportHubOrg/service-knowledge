package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.cleanup.KnowledgeCleanupService;
import com.supporthub.knowledge.messaging.event.OrganizationCreatedPayload;
import com.supporthub.knowledge.messaging.event.OrganizationDeletedPayload;
import com.supporthub.knowledge.messaging.event.UserInvitationAcceptedPayload;
import com.supporthub.knowledge.messaging.event.UserRemovedFromOrganizationPayload;
import com.supporthub.knowledge.messaging.event.UserRoleUpdatedPayload;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.knowledge.security.SupportHubRole;
import com.supporthub.knowledge.security.UserAccessProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.ORGANIZATION_ACCESS)
@RequiredArgsConstructor
public class OrganizationAccessInboxHandlers {

    private final KnowledgeCleanupService cleanupService;
    private final UserAccessProjection userAccessProjection;

    @InboxHandler
    void onOrganizationCreated(OrganizationCreatedPayload payload) {
        userAccessProjection.upsert(payload.managerId(), payload.organizationId(), SupportHubRole.ORGANIZATION_MANAGER);
    }

    @InboxHandler
    void onUserInvitationAccepted(UserInvitationAcceptedPayload payload) {
        userAccessProjection.upsert(payload.userId(), payload.organizationId(), payload.role());
    }

    @InboxHandler
    void onUserRoleUpdated(UserRoleUpdatedPayload payload) {
        userAccessProjection.upsert(payload.userId(), payload.organizationId(), payload.newRole());
    }

    @InboxHandler
    void onUserRemovedFromOrganization(UserRemovedFromOrganizationPayload payload) {
        userAccessProjection.removeUser(payload.userId());
    }

    @InboxHandler
    void onOrganizationDeleted(OrganizationDeletedPayload payload) {
        userAccessProjection.removeOrganization(payload.organizationId());
        cleanupService.onOrganizationDeleted(payload.organizationId());
    }
}

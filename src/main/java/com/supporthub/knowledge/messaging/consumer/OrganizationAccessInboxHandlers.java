package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.cleanup.KnowledgeCleanupService;
import com.supporthub.knowledge.messaging.event.OrganizationDeletedPayload;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxConsumers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.ORGANIZATION_ACCESS)
@RequiredArgsConstructor
public class OrganizationAccessInboxHandlers {

    private final KnowledgeCleanupService cleanupService;

    @InboxHandler
    void onOrganizationDeleted(OrganizationDeletedPayload payload) {
        cleanupService.onOrganizationDeleted(payload.organizationId());
    }
}

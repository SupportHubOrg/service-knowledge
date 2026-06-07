package com.supporthub.knowledge.messaging.inbox.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InboxConsumers {

    public static final String TICKET_MANAGEMENT = "knowledge-management.ticket-management";
    public static final String ORGANIZATION_ACCESS = "knowledge-management.organization-access";

}

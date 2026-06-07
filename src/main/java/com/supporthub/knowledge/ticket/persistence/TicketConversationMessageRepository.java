package com.supporthub.knowledge.ticket.persistence;

import com.supporthub.knowledge.shared.identity.MessageId;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketConversationMessageRepository extends JpaRepository<TicketConversationMessageEntity, MessageId> {

    boolean existsByMessageId(MessageId messageId);

    List<TicketConversationMessageEntity> findByTicketIdOrderByCreatedAtAsc(TicketId ticketId);

    void deleteByTicketId(TicketId ticketId);

    void deleteByOrganizationId(OrganizationId organizationId);
}

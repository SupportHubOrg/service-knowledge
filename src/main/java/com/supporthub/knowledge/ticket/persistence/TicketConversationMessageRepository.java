package com.supporthub.knowledge.ticket.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketConversationMessageRepository extends JpaRepository<TicketConversationMessageEntity, UUID> {

    boolean existsByMessageId(UUID messageId);

    List<TicketConversationMessageEntity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    void deleteByTicketId(UUID ticketId);

    void deleteByOrganizationId(UUID organizationId);
}

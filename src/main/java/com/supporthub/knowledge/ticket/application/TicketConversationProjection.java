package com.supporthub.knowledge.ticket.application;

import com.supporthub.knowledge.messaging.event.TicketMessageAddedPayload;
import com.supporthub.knowledge.ticket.persistence.TicketConversationMessageEntity;
import com.supporthub.knowledge.ticket.persistence.TicketConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Maintains and reads the local ticket conversation projection fed by {@code TicketMessageAdded}
 * events. This is the single owner of conversation read/write access; it lets the proposal flow
 * resolve a ticket's organization and lets the generation flow read the raw conversation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketConversationProjection {

    private final TicketConversationMessageRepository repository;
    private final Clock clock;

    /** Idempotently records a conversation message; redelivered messages (same id) are skipped. */
    @Transactional
    public void project(TicketMessageAddedPayload payload) {
        if (repository.existsByMessageId(payload.messageId())) {
            log.debug("Skipping already projected ticket message. messageId={}, ticketId={}",
                    payload.messageId(), payload.ticketId());
            return;
        }
        repository.save(TicketConversationMessageEntity.create(
                payload.messageId(),
                payload.ticketId(),
                payload.organizationId(),
                payload.senderId(),
                payload.senderType(),
                payload.message(),
                clock.instant()
        ));
    }

    /** Resolves the organization of a ticket from its projected messages, if any are known. */
    @Transactional(readOnly = true)
    public Optional<UUID> resolveOrganizationId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .findFirst()
                .map(TicketConversationMessageEntity::getOrganizationId);
    }

    /** Returns the ticket conversation ordered chronologically. */
    @Transactional(readOnly = true)
    public List<TicketConversationMessage> conversation(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(message -> new TicketConversationMessage(message.getSenderType(), message.getMessage()))
                .toList();
    }

    /** Drops all projected messages for a ticket (cleanup on ticket deletion). */
    @Transactional
    public void deleteByTicket(UUID ticketId) {
        repository.deleteByTicketId(ticketId);
    }

    /** Drops all projected messages for an organization (cleanup on organization deletion). */
    @Transactional
    public void deleteByOrganization(UUID organizationId) {
        repository.deleteByOrganizationId(organizationId);
    }
}

package com.supporthub.knowledge.ticket.persistence;

import com.supporthub.knowledge.shared.identity.MessageId;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import com.supporthub.knowledge.ticket.domain.MessageBody;
import com.supporthub.knowledge.ticket.domain.SenderType;
import com.supporthub.knowledge.ticket.domain.TicketConversationMessageProjectionInput;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Local projection of a single ticket conversation message, built from {@code TicketMessageAdded}
 * events published by the ticket-management service.
 *
 * <p>This read model serves two needs that the {@code KnowledgeArticleProposed} event cannot satisfy
 * on its own: it resolves the {@code organizationId} of a ticket (the proposal event only carries an
 * operator id), and it provides the raw conversation that the article-content generator turns into a
 * draft body.
 *
 * <p>Keyed by {@code message_id} so redelivered events are naturally de-duplicated.
 */
@Entity
@Table(name = "ticket_conversation_message")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TicketConversationMessageEntity {

    @EmbeddedId
    private MessageId messageId;

    @Column(nullable = false, name = "ticket_id")
    private TicketId ticketId;

    @Column(nullable = false, name = "organization_id")
    private OrganizationId organizationId;

    @Column(nullable = false, name = "sender_id")
    private UserId senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "sender_type")
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private MessageBody message;

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    public static TicketConversationMessageEntity from(TicketConversationMessageProjectionInput input) {
        return TicketConversationMessageEntity.builder()
                .messageId(input.getMessageId())
                .ticketId(input.getTicketId())
                .organizationId(input.getOrganizationId())
                .senderId(input.getSenderId())
                .senderType(input.getSenderType())
                .message(input.getMessage())
                .build();
    }
}

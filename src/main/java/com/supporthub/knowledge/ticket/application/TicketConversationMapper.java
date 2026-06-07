package com.supporthub.knowledge.ticket.application;

import com.supporthub.knowledge.messaging.event.TicketMessageAddedPayload;
import com.supporthub.knowledge.shared.identity.MessageId;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import com.supporthub.knowledge.ticket.domain.MessageBody;
import com.supporthub.knowledge.ticket.domain.SenderType;
import com.supporthub.knowledge.ticket.domain.TicketConversationMessageProjectionInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {MessageBody.class, MessageId.class, OrganizationId.class, SenderType.class, TicketId.class, UserId.class})
public interface TicketConversationMapper {

    @Mapping(target = "messageId", expression = "java(MessageId.of(payload.messageId()))")
    @Mapping(target = "ticketId", expression = "java(TicketId.of(payload.ticketId()))")
    @Mapping(target = "organizationId", expression = "java(OrganizationId.of(payload.organizationId()))")
    @Mapping(target = "senderId", expression = "java(UserId.of(payload.senderId()))")
    @Mapping(target = "senderType", expression = "java(SenderType.fromWireValue(payload.senderType()))")
    @Mapping(target = "message", expression = "java(MessageBody.of(payload.message()))")
    TicketConversationMessageProjectionInput toProjectionInput(TicketMessageAddedPayload payload);
}

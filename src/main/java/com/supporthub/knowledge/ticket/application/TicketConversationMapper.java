package com.supporthub.knowledge.ticket.application;

import com.supporthub.knowledge.messaging.event.TicketMessageAddedPayload;
import com.supporthub.knowledge.ticket.domain.TicketConversationMessageProjectionInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TicketConversationMapper {

    TicketConversationMessageProjectionInput toProjectionInput(TicketMessageAddedPayload payload);
}

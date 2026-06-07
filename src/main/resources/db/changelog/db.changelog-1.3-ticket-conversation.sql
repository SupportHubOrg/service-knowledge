--liquibase formatted sql

--changeset vlladevk:1-create-ticket-conversation-message
create table ticket_conversation_message (
    message_id uuid primary key,
    ticket_id uuid not null,
    organization_id uuid not null,
    sender_id uuid not null,
    sender_type varchar(32) not null,
    message text not null,
    created_at timestamptz not null
);

create index ix_ticket_conversation_message_ticket
    on ticket_conversation_message(ticket_id, created_at);

create index ix_ticket_conversation_message_organization
    on ticket_conversation_message(organization_id);

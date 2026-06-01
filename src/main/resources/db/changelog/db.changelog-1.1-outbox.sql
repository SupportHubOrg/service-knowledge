--liquibase formatted sql

--changeset vlladevk:4-create-outbox-events
create table outbox_events (
    id uuid primary key,
    sequence_id bigserial not null,
    event_id uuid not null,
    event_type varchar(128) not null,
    event_version integer not null,
    source varchar(128) not null,
    topic varchar(255) not null,
    message_key varchar(255) not null,
    payload text not null,
    status varchar(32) not null,
    attempts integer not null,
    next_attempt_at timestamptz,
    locked_at timestamptz,
    published_at timestamptz,
    last_error text,
    traceparent varchar(255),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null
);

create unique index ux_outbox_events_event_id
    on outbox_events(event_id);

create unique index ux_outbox_events_sequence_id
    on outbox_events(sequence_id);

create index ix_outbox_events_pending_claim
    on outbox_events(sequence_id)
    where status = 'PENDING';

create index ix_outbox_events_stuck_in_progress
    on outbox_events(locked_at)
    where status = 'IN_PROGRESS';

--changeset vlladevk:5-create-shedlock
create table shedlock (
    name varchar(64) not null primary key,
    lock_until timestamptz not null,
    locked_at timestamptz not null,
    locked_by varchar(255) not null
);

--changeset vlladevk:6-outbox-headofline-index
create index ix_outbox_events_headofline
    on outbox_events(topic, message_key, sequence_id)
    where status in ('PENDING', 'IN_PROGRESS');

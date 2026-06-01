--liquibase formatted sql

--changeset vlladevk:7-create-inbox-events
create table inbox_events (
    id uuid primary key,
    consumer_name varchar(128) not null,
    event_id uuid not null,
    event_type varchar(128) not null,
    event_version integer not null,
    source varchar(128),
    topic varchar(255) not null,
    kafka_partition integer not null,
    kafka_offset bigint not null,
    message_key varchar(255),
    processed_at timestamptz not null
);

create unique index ux_inbox_events_consumer_event
    on inbox_events(consumer_name, event_id);

create unique index ux_inbox_events_consumer_record
    on inbox_events(consumer_name, topic, kafka_partition, kafka_offset);

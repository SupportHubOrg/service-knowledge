--liquibase formatted sql

--changeset vlladevk:1-create-knowledge-user-access
create table knowledge_user_access (
    user_id uuid primary key,
    organization_id uuid,
    role varchar(64) not null,
    updated_at timestamptz not null
);

create index ix_knowledge_user_access_organization
    on knowledge_user_access(organization_id);

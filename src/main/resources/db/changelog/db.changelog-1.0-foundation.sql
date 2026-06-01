--liquibase formatted sql

--changeset vlladevk:1-create-knowledge-drafts
create table knowledge_article_drafts (
    id uuid primary key,
    source_ticket_id uuid not null,
    organization_id uuid not null,
    proposer_id uuid not null,
    title varchar(255) not null,
    content text,
    status varchar(32) not null,
    rejection_reason text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null
);

create unique index ux_knowledge_article_drafts_source_ticket
    on knowledge_article_drafts(source_ticket_id);

create index ix_knowledge_article_drafts_organization_status
    on knowledge_article_drafts(organization_id, status);

--changeset vlladevk:2-create-knowledge-articles
create table knowledge_articles (
    id uuid primary key,
    draft_id uuid not null unique references knowledge_article_drafts(id),
    source_ticket_id uuid not null,
    organization_id uuid not null,
    title varchar(255) not null,
    content text not null,
    status varchar(32) not null,
    published_by_id uuid not null,
    published_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null
);

create unique index ux_knowledge_articles_source_ticket
    on knowledge_articles(source_ticket_id);

create index ix_knowledge_articles_organization_status
    on knowledge_articles(organization_id, status);

--changeset vlladevk:3-create-knowledge-article-tags
create table knowledge_article_tags (
    id uuid primary key,
    draft_id uuid references knowledge_article_drafts(id) on delete cascade,
    article_id uuid references knowledge_articles(id) on delete cascade,
    tag_id uuid not null,
    created_at timestamptz not null,
    constraint ck_knowledge_article_tags_owner check (
        (draft_id is not null and article_id is null)
        or (draft_id is null and article_id is not null)
    )
);

create index ix_knowledge_article_tags_draft
    on knowledge_article_tags(draft_id);

create index ix_knowledge_article_tags_article
    on knowledge_article_tags(article_id);

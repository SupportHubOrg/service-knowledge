--liquibase formatted sql

--changeset vlladevk:1-knowledge-articles-fts-index
create index ix_knowledge_articles_fts
    on knowledge_articles
    using gin (to_tsvector('simple', title || ' ' || content));

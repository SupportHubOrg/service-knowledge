package com.supporthub.knowledge.messaging.event;

/**
 * Canonical reasons a ticket proposal could not be turned into a knowledge draft, carried by
 * {@link KnowledgeArticleDraftSaveFailedPayload}. Values match the documented contract.
 */
public enum DraftSaveFailureCode {

    /** The proposal data is incomplete or refers to an unknown ticket. */
    INVALID_ARTICLE_PROPOSAL,

    /** The AI step could not prepare the article body from the ticket conversation. */
    AI_CONTENT_PREPARATION_FAILED,

    /** Any other technical error while saving or preparing the article. */
    KNOWLEDGE_TECHNICAL_ERROR
}

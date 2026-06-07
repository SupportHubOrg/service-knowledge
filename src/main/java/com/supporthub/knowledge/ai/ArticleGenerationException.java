package com.supporthub.knowledge.ai;

/**
 * Thrown by an {@link ArticleDraftContentGenerator} when it cannot produce an article body (e.g. the
 * AI provider is unavailable or returns an unusable result). Treated by the generation flow as a
 * recoverable, content-preparation failure.
 */
public class ArticleGenerationException extends RuntimeException {

    public ArticleGenerationException(String message) {
        super(message);
    }

    public ArticleGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

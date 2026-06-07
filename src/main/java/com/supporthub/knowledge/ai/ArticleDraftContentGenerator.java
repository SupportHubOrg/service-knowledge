package com.supporthub.knowledge.ai;

/**
 * Port for turning a ticket conversation into a knowledge article body. The current implementation is
 * a deterministic stub; a real LLM-backed adapter (Anthropic) will replace it without changing
 * callers.
 *
 * <p>This is an external-integration boundary: implementations may perform network calls and must be
 * invoked outside the inbox/business transaction.
 */
public interface ArticleDraftContentGenerator {

    /**
     * @throws ArticleGenerationException if no usable body could be produced
     */
    GeneratedArticleContent generate(ArticleGenerationContext context);
}

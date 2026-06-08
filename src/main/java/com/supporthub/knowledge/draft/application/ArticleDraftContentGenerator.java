package com.supporthub.knowledge.draft.application;

/**
 * Port for turning a ticket conversation into a knowledge article body.
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

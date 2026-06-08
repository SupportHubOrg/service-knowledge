package com.supporthub.knowledge.draft.application;

/**
 * Result of article generation: the ready-to-review article body.
 *
 * @param content the generated article body
 */
public record GeneratedArticleContent(
        String content
) {
}

package com.supporthub.knowledge.ai;

/**
 * Result of article generation: the ready-to-review article body.
 *
 * @param content the generated article body
 */
public record GeneratedArticleContent(
        String content
) {
}

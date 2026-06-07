package com.supporthub.knowledge.ai;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.stream.Collectors;

/**
 * Placeholder generator that deterministically assembles a draft body from the ticket conversation,
 * so the end-to-end business process works and is testable before the real model is wired.
 *
 * <p>Keep this class as a local fallback/test double when no LLM-backed adapter is configured.
 */
@Component
@ConditionalOnProperty(prefix = "application.ai.article-generation", name = "provider", havingValue = "stub",
        matchIfMissing = true)
public class StubArticleDraftContentGenerator implements ArticleDraftContentGenerator {

    @Override
    public GeneratedArticleContent generate(ArticleGenerationContext context) {
        if (context.conversation().isEmpty()) {
            throw new ArticleGenerationException(
                    "No conversation available to generate an article for ticket " + context.ticketId());
        }

        var transcript = context.conversation().stream()
                .map(message -> "- %s: %s".formatted(message.senderType(), message.message()))
                .collect(Collectors.joining("\n"));

        var content = """
                %s

                Draft generated automatically from the resolved ticket conversation and pending manager review.

                Conversation summary:
                %s
                """.formatted(context.proposedSubject(), transcript);

        return new GeneratedArticleContent(content);
    }
}

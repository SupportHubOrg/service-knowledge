package com.supporthub.knowledge.ai.article.infrastructure;

import com.supporthub.knowledge.ai.common.SafeAiLoggingAdvisor;
import com.supporthub.knowledge.draft.application.ArticleDraftContentGenerator;
import com.supporthub.knowledge.draft.application.ArticleGenerationContext;
import com.supporthub.knowledge.draft.application.ArticleGenerationException;
import com.supporthub.knowledge.draft.application.GeneratedArticleContent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.ai.article-generation", name = "provider", havingValue = "openai")
public class SpringAiArticleDraftContentGenerator implements ArticleDraftContentGenerator {

    private static final String OPERATION = "knowledge-draft-generation";

    private static final String SYSTEM_PROMPT = """
            You are a knowledge base article writer for a customer support platform.

            Your task is to transform a resolved support ticket conversation into a clear, practical knowledge base article.

            Rules:
            - Use only facts present in the provided ticket conversation.
            - Do not invent causes, steps, product behavior, commands, URLs, credentials, or configuration values.
            - Do not include passwords, tokens, API keys, private keys, authorization headers, connection strings, or any other secrets.
            - If the input contains redacted values such as [REDACTED_PASSWORD] or [REDACTED_TOKEN], keep them redacted and do not try to reconstruct them.
            - Do not include personal data unless it is essential to the solution.
            - Do not mention internal ticket IDs, user IDs, organization IDs, message IDs, or implementation details of the support system.
            - Write for future operators and AI assistants who need to solve the same problem quickly.
            - Keep the article concise, actionable, and solution-focused.
            - If the conversation does not contain enough information to write a useful article, return exactly:
            INSUFFICIENT_INFORMATION
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Create a knowledge base article from the following resolved support ticket.

            Proposed article title:
            {proposedSubject}

            Ticket conversation:
            {conversation}

            Required article format:

            # {proposedSubject}

            ## Problem
            Describe the user-visible problem in 1-3 sentences.

            ## Cause
            Explain the confirmed cause if it is present in the conversation.
            If the cause is not clearly known, write: "The exact cause was not confirmed."

            ## Solution
            Describe the final working solution.

            ## Steps
            List practical steps that can be followed next time.
            Use numbered steps only if there are actual steps in the conversation.

            ## Notes
            Add short warnings, prerequisites, or follow-up notes if they are present in the conversation.
            If there are no useful notes, omit this section.

            Output only the article content. Do not add explanations outside the article.
            """;

    private final ChatClient articleDraftChatClient;
    private final ArticleGenerationProperties properties;
    private final ArticleContentValidator articleContentValidator;

    @Override
    public GeneratedArticleContent generate(ArticleGenerationContext context) {
        if (context.conversation().isEmpty()) {
            throw new ArticleGenerationException(
                    "No conversation available to generate an article for ticket " + context.ticketId());
        }

        var content = articleDraftChatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(user -> user.text(USER_PROMPT_TEMPLATE)
                        .param("proposedSubject", context.proposedSubject())
                        .param("conversation", conversation(context)))
                .options(chatOptions())
                .advisors(advisor -> advisor.params(advisorContext(context)))
                .call()
                .content();

        return new GeneratedArticleContent(articleContentValidator.requireUsableArticle(content));
    }

    private OpenAiSdkChatOptions chatOptions() {
        return OpenAiSdkChatOptions.builder()
                .model(properties.model())
                .maxCompletionTokens(properties.maxOutputTokens())
                .store(false)
                .build();
    }

    private Map<String, Object> advisorContext(ArticleGenerationContext context) {
        return Map.of(
                SafeAiLoggingAdvisor.CONTEXT_OPERATION, OPERATION,
                SafeAiLoggingAdvisor.CONTEXT_TICKET_ID, context.ticketId(),
                SafeAiLoggingAdvisor.CONTEXT_ORGANIZATION_ID, context.organizationId(),
                SafeAiLoggingAdvisor.CONTEXT_MODEL, properties.model()
        );
    }

    private String conversation(ArticleGenerationContext context) {
        return context.conversation().stream()
                .map(message -> "%s: %s".formatted(message.senderType(), message.message()))
                .collect(Collectors.joining("\n"));
    }
}

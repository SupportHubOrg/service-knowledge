package com.supporthub.knowledge.ai.common;

import com.supporthub.knowledge.ai.article.infrastructure.ArticleGenerationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

@RequiredArgsConstructor
@Slf4j
public class SafeAiLoggingAdvisor implements CallAdvisor {

    public static final String CONTEXT_OPERATION = "operation";
    public static final String CONTEXT_TICKET_ID = "ticketId";
    public static final String CONTEXT_ORGANIZATION_ID = "organizationId";
    public static final String CONTEXT_MODEL = "model";

    private static final int ORDER = 20;

    private final ArticleGenerationProperties properties;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        if (!properties.safeLoggingEnabled()) {
            return callAdvisorChain.nextCall(chatClientRequest);
        }
        var start = System.nanoTime();
        var context = chatClientRequest.context();
        try {
            log.info("Calling AI model. operation={}, ticketId={}, organizationId={}, model={}, promptChars={}",
                    context.get(CONTEXT_OPERATION),
                    context.get(CONTEXT_TICKET_ID),
                    context.get(CONTEXT_ORGANIZATION_ID),
                    context.get(CONTEXT_MODEL),
                    context.get(CredentialScrubbingAdvisor.REDACTED_INPUT_LENGTH));
            var response = callAdvisorChain.nextCall(chatClientRequest);
            log.info("AI model call completed. operation={}, ticketId={}, organizationId={}, model={}, durationMs={}",
                    context.get(CONTEXT_OPERATION),
                    context.get(CONTEXT_TICKET_ID),
                    context.get(CONTEXT_ORGANIZATION_ID),
                    context.get(CONTEXT_MODEL),
                    elapsedMillis(start));
            return response;
        } catch (RuntimeException exception) {
            log.warn("AI model call failed. operation={}, ticketId={}, organizationId={}, model={}, durationMs={}, errorType={}",
                    context.get(CONTEXT_OPERATION),
                    context.get(CONTEXT_TICKET_ID),
                    context.get(CONTEXT_ORGANIZATION_ID),
                    context.get(CONTEXT_MODEL),
                    elapsedMillis(start),
                    exception.getClass().getSimpleName());
            throw exception;
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}

package com.supporthub.knowledge.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

@RequiredArgsConstructor
public class CredentialScrubbingAdvisor implements BaseAdvisor {

    static final String REDACTED_INPUT_LENGTH = "redactedInputLength";

    private static final int ORDER = 10;

    private final SecretSanitizer secretSanitizer;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        var sanitizedMessages = chatClientRequest.prompt().getInstructions().stream()
                .map(this::sanitizeMessage)
                .toList();
        var sanitizedPrompt = new Prompt(sanitizedMessages, chatClientRequest.prompt().getOptions());
        return chatClientRequest.mutate()
                .prompt(sanitizedPrompt)
                .context(REDACTED_INPUT_LENGTH, sanitizedPrompt.getContents().length())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private Message sanitizeMessage(Message message) {
        var sanitizedText = secretSanitizer.sanitize(message.getText());
        if (message instanceof UserMessage userMessage) {
            return userMessage.mutate().text(sanitizedText).build();
        }
        if (message instanceof SystemMessage systemMessage) {
            return systemMessage.mutate().text(sanitizedText).build();
        }
        if (message instanceof AssistantMessage assistantMessage) {
            return AssistantMessage.builder()
                    .content(sanitizedText)
                    .properties(assistantMessage.getMetadata())
                    .toolCalls(assistantMessage.getToolCalls())
                    .media(assistantMessage.getMedia())
                    .build();
        }
        return message;
    }
}

package com.supporthub.knowledge.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "application.ai.article-generation", name = "provider", havingValue = "openai")
class ArticleGenerationConfig {

    @Bean
    OpenAiSdkChatModel articleDraftChatModel(ArticleGenerationProperties properties,
                                             @Value("${spring.ai.openai-sdk.api-key:}") String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ArticleGenerationException("OPENAI_API_KEY is required when article generation provider is openai");
        }
        return OpenAiSdkChatModel.builder()
                .options(OpenAiSdkChatOptions.builder()
                        .apiKey(apiKey)
                        .model(properties.model())
                        .store(false)
                        .build())
                .build();
    }

    @Bean
    ChatClient articleDraftChatClient(OpenAiSdkChatModel articleDraftChatModel,
                                      SecretSanitizer secretSanitizer,
                                      ArticleGenerationProperties properties) {
        return ChatClient.builder(articleDraftChatModel)
                .defaultAdvisors(advisors(secretSanitizer, properties))
                .build();
    }

    private List<Advisor> advisors(SecretSanitizer secretSanitizer, ArticleGenerationProperties properties) {
        var advisors = new ArrayList<Advisor>();
        advisors.add(new CredentialScrubbingAdvisor(secretSanitizer));
        advisors.add(new SafeAiLoggingAdvisor(properties));
        if (properties.safeGuardEnabled()) {
            advisors.add(SafeGuardAdvisor.builder()
                    .sensitiveWords(List.of("-----BEGIN PRIVATE KEY-----", "Authorization: Bearer"))
                    .failureResponse(properties.insufficientInformationMarker())
                    .order(30)
                    .build());
        }
        return advisors;
    }
}

package com.supporthub.knowledge.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleContentValidator {

    private final ArticleGenerationProperties properties;
    private final SecretSanitizer secretSanitizer;

    public String requireUsableArticle(String content) {
        var trimmed = requirePresentArticle(content);
        if (trimmed.equals(properties.insufficientInformationMarker())) {
            throw new ArticleGenerationException("Ticket conversation does not contain enough information");
        }
        if (trimmed.length() < properties.minimumContentLength()) {
            throw new ArticleGenerationException("AI article draft is too short");
        }
        if (trimmed.length() > properties.maximumContentLength()) {
            throw new ArticleGenerationException("AI article draft is too long");
        }
        if (secretSanitizer.containsPotentialSecret(trimmed)) {
            throw new ArticleGenerationException("AI article draft contains potential credentials");
        }
        return trimmed;
    }

    private String requirePresentArticle(String content) {
        if (content == null || content.isBlank()) {
            throw new ArticleGenerationException("AI returned an empty article draft");
        }
        return content.trim();
    }
}

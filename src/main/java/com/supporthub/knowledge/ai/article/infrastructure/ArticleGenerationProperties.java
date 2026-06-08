package com.supporthub.knowledge.ai.article.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.ai.article-generation")
public record ArticleGenerationProperties(
        String provider,
        String model,
        Integer maxOutputTokens,
        Integer minimumContentLength,
        Integer maximumContentLength,
        String insufficientInformationMarker,
        boolean safeLoggingEnabled,
        boolean safeGuardEnabled
) {
}

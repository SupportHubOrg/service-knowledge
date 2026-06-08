package com.supporthub.knowledge.ai.article.infrastructure;

import com.supporthub.knowledge.draft.application.ArticleGenerationException;

import com.supporthub.knowledge.ai.common.SecretSanitizer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleContentValidatorTest {

    private final ArticleGenerationProperties properties = new ArticleGenerationProperties(
            "stub",
            "gpt-5.4-mini",
            1800,
            20,
            1000,
            "INSUFFICIENT_INFORMATION",
            true,
            false
    );
    private final ArticleContentValidator validator = new ArticleContentValidator(properties, new SecretSanitizer());

    @Test
    void shouldAcceptUsableContent() {
        var content = "# Reset VPN token\n\n## Problem\nVPN access failed.\n\n## Solution\nReset the token in the portal.";

        assertThat(validator.requireUsableArticle(content)).isEqualTo(content);
    }

    @Test
    void shouldRejectInsufficientInformationMarker() {
        assertThatThrownBy(() -> validator.requireUsableArticle("INSUFFICIENT_INFORMATION"))
                .isInstanceOf(ArticleGenerationException.class);
    }

    @Test
    void shouldRejectPotentialCredentials() {
        assertThatThrownBy(() -> validator.requireUsableArticle(
                "# Token fix\n\nUse password=summer2026 to reset the VPN token."))
                .isInstanceOf(ArticleGenerationException.class);
    }
}

package com.supporthub.knowledge.ai.article.infrastructure;

import com.supporthub.knowledge.draft.application.ConversationMessage;

import com.supporthub.knowledge.draft.application.ArticleGenerationException;

import com.supporthub.knowledge.draft.application.ArticleGenerationContext;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubArticleDraftContentGeneratorTest {

    private final StubArticleDraftContentGenerator generator = new StubArticleDraftContentGenerator();

    @Test
    void shouldAssembleBodyFromConversation() {
        var context = new ArticleGenerationContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Reset VPN token",
                List.of(
                        new ConversationMessage("Employee", "I cannot log in to the VPN."),
                        new ConversationMessage("Operator", "Reset the token from the portal.")
                )
        );

        var content = generator.generate(context).content();

        assertThat(content)
                .contains("Reset VPN token")
                .contains("Employee: I cannot log in to the VPN.")
                .contains("Operator: Reset the token from the portal.");
    }

    @Test
    void shouldFailWhenConversationIsEmpty() {
        var context = new ArticleGenerationContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Reset VPN token",
                List.of()
        );

        assertThatThrownBy(() -> generator.generate(context))
                .isInstanceOf(ArticleGenerationException.class);
    }
}

package com.supporthub.knowledge.messaging.event;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationEventRegistryTest {

    @Test
    void shouldBuildContractsForAnnotatedPayloadsOnStartup() {
        var registry = new IntegrationEventRegistry(properties(defaultContracts()));

        registry.initialize();

        var contract = registry.get(KnowledgeArticlePublishedPayload.class);
        assertThat(contract.key()).isEqualTo("knowledge-article-published");
        assertThat(contract.eventType()).isEqualTo("KnowledgeArticlePublished");
        assertThat(contract.version()).isEqualTo(1);
    }

    @Test
    void shouldRejectMissingContractOnStartup() {
        var contracts = defaultContracts();
        contracts.remove("knowledge-article-published");
        var registry = new IntegrationEventRegistry(properties(contracts));

        assertThatThrownBy(registry::initialize)
                .isInstanceOf(IntegrationEventConfigurationException.class)
                .hasMessage("Integration event contract is not configured: knowledge-article-published");
    }

    @Test
    void shouldRejectPayloadOutsideStartupCatalog() {
        var registry = new IntegrationEventRegistry(properties(defaultContracts()));
        registry.initialize();

        assertThatThrownBy(() -> registry.get(String.class))
                .isInstanceOf(IntegrationEventConfigurationException.class)
                .hasMessage("Integration event payload is not registered: java.lang.String");
    }

    private IntegrationEventProperties properties(
            Map<String, IntegrationEventProperties.ContractProperties> contracts
    ) {
        return new IntegrationEventProperties(contracts);
    }

    private Map<String, IntegrationEventProperties.ContractProperties> defaultContracts() {
        return new java.util.HashMap<>(Map.ofEntries(
                Map.entry("knowledge-article-proposed", contract("KnowledgeArticleProposed")),
                Map.entry("ticket-message-added", contract("TicketMessageAdded")),
                Map.entry("ticket-deleted", contract("TicketDeleted")),
                Map.entry("organization-deleted", contract("OrganizationDeleted")),
                Map.entry("knowledge-article-draft-saved", contract("KnowledgeArticleDraftSaved")),
                Map.entry("knowledge-article-draft-save-failed", contract("KnowledgeArticleDraftSaveFailed")),
                Map.entry("knowledge-article-published", contract("KnowledgeArticlePublished")),
                Map.entry("knowledge-article-rejected", contract("KnowledgeArticleRejected"))
        ));
    }

    private IntegrationEventProperties.ContractProperties contract(String eventType) {
        return new IntegrationEventProperties.ContractProperties(eventType, 1);
    }
}

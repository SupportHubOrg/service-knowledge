package com.supporthub.knowledge.article;

import com.supporthub.knowledge.article.application.KnowledgeArticleService;
import com.supporthub.knowledge.cleanup.KnowledgeCleanupService;
import com.supporthub.knowledge.draft.api.PublishDraftRequest;
import com.supporthub.knowledge.draft.application.DraftContentGenerationService;
import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftRepository;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.messaging.event.TicketMessageAddedPayload;
import com.supporthub.knowledge.security.CurrentUser;
import com.supporthub.knowledge.security.SupportHubRole;
import com.supporthub.knowledge.support.KnowledgeIntegrationTestSupport;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeArticleSearchAndCleanupIntegrationTest extends KnowledgeIntegrationTestSupport {

    private final KnowledgeDraftService draftService;
    private final DraftContentGenerationService generationService;
    private final TicketConversationProjection ticketConversationProjection;
    private final KnowledgeArticleService articleService;
    private final KnowledgeCleanupService cleanupService;
    private final KnowledgeArticleDraftRepository draftRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    KnowledgeArticleSearchAndCleanupIntegrationTest(KnowledgeDraftService draftService,
                                                    DraftContentGenerationService generationService,
                                                    TicketConversationProjection ticketConversationProjection,
                                                    KnowledgeArticleService articleService,
                                                    KnowledgeCleanupService cleanupService,
                                                    KnowledgeArticleDraftRepository draftRepository,
                                                    JdbcTemplate jdbcTemplate) {
        this.draftService = draftService;
        this.generationService = generationService;
        this.ticketConversationProjection = ticketConversationProjection;
        this.articleService = articleService;
        this.cleanupService = cleanupService;
        this.draftRepository = draftRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from outbox_events");
        jdbcTemplate.update("delete from knowledge_article_tags");
        jdbcTemplate.update("delete from knowledge_articles");
        jdbcTemplate.update("delete from knowledge_article_drafts");
        jdbcTemplate.update("delete from ticket_conversation_message");
    }

    @Test
    void shouldFindPublishedArticleByFullTextThenArchiveOnTicketDeletion() {
        var organizationId = UUID.randomUUID();
        var ticketId = UUID.randomUUID();
        var manager = new CurrentUser(UUID.randomUUID(), organizationId, Set.of(SupportHubRole.ORGANIZATION_MANAGER));
        publishArticle(ticketId, organizationId, "Reset VPN token",
                "Open the portal and reset the VPN token.");

        assertThat(articleService.searchPublishedArticles(manager, "vpn token")).hasSize(1);
        assertThat(articleService.searchPublishedArticles(manager, "mailbox")).isEmpty();

        cleanupService.onTicketDeleted(ticketId);

        // Published article is archived (hidden from search); its backing draft is retained.
        assertThat(articleService.searchPublishedArticles(manager, "vpn token")).isEmpty();
    }

    @Test
    void shouldDeleteUnpublishedDraftAndProjectionOnTicketDeletion() {
        var organizationId = UUID.randomUUID();
        var ticketId = UUID.randomUUID();
        createPendingReviewDraft(ticketId, organizationId);
        assertThat(draftRepository.findWithTagsBySourceTicketId(ticketId)).isPresent();

        cleanupService.onTicketDeleted(ticketId);

        assertThat(draftRepository.findWithTagsBySourceTicketId(ticketId)).isEmpty();
        assertThat(ticketConversationProjection.conversation(ticketId)).isEmpty();
    }

    @Test
    void shouldScopeSearchToOrganization() {
        var organizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();
        publishArticle(UUID.randomUUID(), organizationId, "Reset VPN token", "Reset the VPN token here.");
        var otherManager = new CurrentUser(UUID.randomUUID(), otherOrganizationId,
                Set.of(SupportHubRole.ORGANIZATION_MANAGER));

        assertThat(articleService.searchPublishedArticles(otherManager, "vpn token")).isEmpty();
    }

    @Test
    void shouldArchiveArticlesOnOrganizationDeletion() {
        var organizationId = UUID.randomUUID();
        var manager = new CurrentUser(UUID.randomUUID(), organizationId, Set.of(SupportHubRole.ORGANIZATION_MANAGER));
        publishArticle(UUID.randomUUID(), organizationId, "Reset VPN token", "Reset the VPN token here.");

        cleanupService.onOrganizationDeleted(organizationId);

        assertThat(articleService.searchPublishedArticles(manager, "vpn token")).isEmpty();
    }

    private void createPendingReviewDraft(UUID ticketId, UUID organizationId) {
        ticketConversationProjection.project(new TicketMessageAddedPayload(
                ticketId,
                organizationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Operator",
                "Resolution for ticket"
        ));
        draftService.createDraftFromTicketProposal(new KnowledgeArticleProposedPayload(
                ticketId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Reset VPN token",
                List.of(UUID.randomUUID())
        ));
        generationService.generatePendingDrafts();
    }

    private void publishArticle(UUID ticketId, UUID organizationId, String subject, String content) {
        ticketConversationProjection.project(new TicketMessageAddedPayload(
                ticketId,
                organizationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Operator",
                "Resolution for ticket"
        ));
        draftService.createDraftFromTicketProposal(new KnowledgeArticleProposedPayload(
                ticketId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                subject,
                List.of(UUID.randomUUID())
        ));
        generationService.generatePendingDrafts();
        var draftId = draftRepository.findWithTagsBySourceTicketId(ticketId).orElseThrow().getId();
        var publisher = new CurrentUser(UUID.randomUUID(), organizationId, Set.of(SupportHubRole.ORGANIZATION_MANAGER));
        draftService.publishDraft(publisher, draftId, new PublishDraftRequest(subject, content));
    }
}

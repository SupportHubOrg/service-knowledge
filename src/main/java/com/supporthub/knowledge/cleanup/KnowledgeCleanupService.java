package com.supporthub.knowledge.cleanup;

import com.supporthub.knowledge.article.persistence.KnowledgeArticleRepository;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftRepository;
import com.supporthub.knowledge.ticket.application.TicketConversationProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Removes knowledge data when its source ticket or organization is deleted upstream.
 *
 * <p>Published articles are archived (kept for history/audit, hidden from reads/search); unpublished
 * drafts and the ticket conversation projection are deleted. Published drafts are retained because a
 * published article references them.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeCleanupService {

    private final KnowledgeArticleDraftRepository draftRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final TicketConversationProjection ticketConversationProjection;
    private final Clock clock;

    @Transactional
    public void onTicketDeleted(UUID ticketId) {
        var now = clock.instant();
        articleRepository.findBySourceTicketId(ticketId).ifPresent(article -> article.archive(now));
        draftRepository.findWithTagsBySourceTicketId(ticketId)
                .filter(KnowledgeCleanupService::isDeletable)
                .ifPresent(draftRepository::delete);
        ticketConversationProjection.deleteByTicket(ticketId);
    }

    @Transactional
    public void onOrganizationDeleted(UUID organizationId) {
        var now = clock.instant();
        articleRepository.findByOrganizationIdAndStatusOrderByPublishedAtDesc(
                        organizationId, com.supporthub.knowledge.article.domain.ArticleStatus.PUBLISHED)
                .forEach(article -> article.archive(now));
        var deletableDrafts = draftRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .filter(KnowledgeCleanupService::isDeletable)
                .toList();
        draftRepository.deleteAll(deletableDrafts);
        ticketConversationProjection.deleteByOrganization(organizationId);
    }

    private static boolean isDeletable(KnowledgeArticleDraftEntity draft) {
        return draft.getStatus() != DraftStatus.PUBLISHED;
    }
}

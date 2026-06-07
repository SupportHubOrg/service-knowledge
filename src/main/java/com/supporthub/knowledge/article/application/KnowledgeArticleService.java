package com.supporthub.knowledge.article.application;

import com.supporthub.knowledge.article.api.ArticleResponse;
import com.supporthub.knowledge.article.domain.ArticleStatus;
import com.supporthub.knowledge.article.persistence.KnowledgeArticleRepository;
import com.supporthub.knowledge.draft.application.KnowledgeMapper;
import com.supporthub.knowledge.security.CurrentUser;
import com.supporthub.knowledge.security.KnowledgeAccessPolicy;
import com.supporthub.knowledge.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeMapper mapper;
    private final KnowledgeAccessPolicy accessPolicy;

    @Transactional(readOnly = true)
    public List<ArticleResponse> listPublishedArticles(CurrentUser user) {
        accessPolicy.requireOrganizationScope(user);
        if (user.isSupportHubAdministrator() && user.organizationId() == null) {
            return articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED).stream()
                    .map(mapper::toArticleResponse)
                    .toList();
        }
        return articleRepository.findByOrganizationIdAndStatusOrderByPublishedAtDesc(
                        user.organizationId(),
                        ArticleStatus.PUBLISHED
                ).stream()
                .map(mapper::toArticleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> searchPublishedArticles(CurrentUser user, String query) {
        accessPolicy.requireOrganizationScope(user);
        var status = ArticleStatus.PUBLISHED.name();
        var matches = user.isSupportHubAdministrator() && user.organizationId() == null
                ? articleRepository.search(status, query)
                : articleRepository.searchByOrganization(user.organizationId(), status, query);
        return matches.stream().map(mapper::toArticleResponse).toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse getPublishedArticle(CurrentUser user, UUID articleId) {
        var article = articleRepository.findWithTagsById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article", articleId));
        accessPolicy.requireArticleRead(user, article.getOrganizationId());
        return mapper.toArticleResponse(article);
    }
}

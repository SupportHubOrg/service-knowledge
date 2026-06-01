package com.supporthub.knowledge.article.persistence;

import com.supporthub.knowledge.article.domain.ArticleStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticleEntity, UUID> {

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleEntity> findWithTagsById(UUID id);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleEntity> findByOrganizationIdAndStatusOrderByPublishedAtDesc(UUID organizationId,
                                                                                     ArticleStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleEntity> findByStatusOrderByPublishedAtDesc(ArticleStatus status);
}

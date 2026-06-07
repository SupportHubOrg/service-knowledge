package com.supporthub.knowledge.article.persistence;

import com.supporthub.knowledge.article.domain.ArticleId;
import com.supporthub.knowledge.article.domain.ArticleStatus;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticleEntity, ArticleId> {

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleEntity> findWithTagsById(ArticleId id);

    Optional<KnowledgeArticleEntity> findBySourceTicketId(TicketId sourceTicketId);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleEntity> findByOrganizationIdAndStatusOrderByPublishedAtDesc(OrganizationId organizationId,
                                                                                     ArticleStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleEntity> findByStatusOrderByPublishedAtDesc(ArticleStatus status);

    /**
     * Full-text search over title+content within an organization. Native because it relies on
     * PostgreSQL text search ({@code to_tsvector}/{@code websearch_to_tsquery}); kept localized here.
     *
     * <p>{@link jakarta.persistence.AttributeConverter}s are not applied to native query parameters,
     * so this boundary deliberately takes the raw organization id; callers extract it explicitly.
     */
    @Query(value = """
            select a.* from knowledge_articles a
            where a.status = :status
              and a.organization_id = :organizationId
              and to_tsvector('simple', a.title || ' ' || a.content)
                  @@ websearch_to_tsquery('simple', :query)
            order by a.published_at desc
            """, nativeQuery = true)
    List<KnowledgeArticleEntity> searchByOrganization(@Param("organizationId") UUID organizationId,
                                                      @Param("status") String status,
                                                      @Param("query") String query);

    /** Full-text search across all organizations (administrator scope). */
    @Query(value = """
            select a.* from knowledge_articles a
            where a.status = :status
              and to_tsvector('simple', a.title || ' ' || a.content)
                  @@ websearch_to_tsquery('simple', :query)
            order by a.published_at desc
            """, nativeQuery = true)
    List<KnowledgeArticleEntity> search(@Param("status") String status,
                                        @Param("query") String query);
}

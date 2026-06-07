package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeArticleDraftRepository extends JpaRepository<KnowledgeArticleDraftEntity, UUID> {

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleDraftEntity> findWithTagsById(UUID id);

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleDraftEntity> findWithTagsBySourceTicketId(UUID sourceTicketId);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByOrganizationIdAndStatusOrderByCreatedAtDesc(UUID organizationId,
                                                                                        DraftStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByStatusOrderByCreatedAtDesc(DraftStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findAllByOrderByCreatedAtDesc();

    @Query("select d.id from KnowledgeArticleDraftEntity d where d.status = :status order by d.createdAt asc")
    List<UUID> findIdsByStatus(DraftStatus status, Pageable pageable);
}

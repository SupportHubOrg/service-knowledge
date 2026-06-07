package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TicketId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KnowledgeArticleDraftRepository extends JpaRepository<KnowledgeArticleDraftEntity, DraftId> {

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleDraftEntity> findWithTagsById(DraftId id);

    @EntityGraph(attributePaths = "tags")
    Optional<KnowledgeArticleDraftEntity> findWithTagsBySourceTicketId(TicketId sourceTicketId);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByOrganizationIdOrderByCreatedAtDesc(OrganizationId organizationId);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByOrganizationIdAndStatusOrderByCreatedAtDesc(OrganizationId organizationId,
                                                                                        DraftStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findByStatusOrderByCreatedAtDesc(DraftStatus status);

    @EntityGraph(attributePaths = "tags")
    List<KnowledgeArticleDraftEntity> findAllByOrderByCreatedAtDesc();

    @Query("select d.id from KnowledgeArticleDraftEntity d where d.status = :status order by d.createdAt asc")
    List<DraftId> findIdsByStatus(DraftStatus status, Pageable pageable);
}

package com.supporthub.knowledge.draft.application;

import com.supporthub.knowledge.draft.domain.PendingDraftProposal;
import com.supporthub.knowledge.draft.persistence.KnowledgeArticleDraftEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KnowledgeArticleDraftFactory {

    public KnowledgeArticleDraftEntity createPendingGeneration(PendingDraftProposal proposal) {
        return KnowledgeArticleDraftEntity.pendingGeneration(UUID.randomUUID(), proposal);
    }
}

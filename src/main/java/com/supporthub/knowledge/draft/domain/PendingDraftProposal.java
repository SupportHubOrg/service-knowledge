package com.supporthub.knowledge.draft.domain;

import com.supporthub.knowledge.shared.content.ArticleTitle;
import com.supporthub.knowledge.shared.error.BusinessValidationException;
import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.TagId;
import com.supporthub.knowledge.shared.identity.TicketId;
import com.supporthub.knowledge.shared.identity.UserId;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public final class PendingDraftProposal {

    private final TicketId sourceTicketId;
    private final OrganizationId organizationId;
    private final UserId proposerId;
    private final ArticleTitle title;
    private final List<TagId> tagIds;

    @Builder
    private PendingDraftProposal(TicketId sourceTicketId,
                                 OrganizationId organizationId,
                                 UserId proposerId,
                                 ArticleTitle title,
                                 List<TagId> tagIds) {
        this.sourceTicketId = require(sourceTicketId, "sourceTicketId");
        this.organizationId = require(organizationId, "organizationId");
        this.proposerId = require(proposerId, "proposerId");
        this.title = require(title, "title");
        this.tagIds = copyTagIds(tagIds);
    }

    private static List<TagId> copyTagIds(List<TagId> tagIds) {
        require(tagIds, "tagIds");
        if (tagIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessValidationException("Draft proposal tag ids must not contain null values");
        }
        return List.copyOf(tagIds);
    }

    private static <T> T require(T value, String fieldName) {
        if (value == null) {
            throw new BusinessValidationException("Draft proposal " + fieldName + " must not be null");
        }
        return value;
    }
}

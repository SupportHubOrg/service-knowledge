package com.supporthub.knowledge.draft.domain;

import com.supporthub.knowledge.shared.error.BusinessValidationException;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class PendingDraftProposal {

    private final UUID sourceTicketId;
    private final UUID organizationId;
    private final UUID proposerId;
    private final String title;
    private final List<UUID> tagIds;

    @Builder
    private PendingDraftProposal(UUID sourceTicketId,
                                 UUID organizationId,
                                 UUID proposerId,
                                 String title,
                                 List<UUID> tagIds) {
        this.sourceTicketId = require(sourceTicketId, "sourceTicketId");
        this.organizationId = require(organizationId, "organizationId");
        this.proposerId = require(proposerId, "proposerId");
        this.title = requireTitle(title);
        this.tagIds = copyTagIds(tagIds);
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessValidationException("Draft proposal title must not be blank");
        }
        return title.trim();
    }

    private static List<UUID> copyTagIds(List<UUID> tagIds) {
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

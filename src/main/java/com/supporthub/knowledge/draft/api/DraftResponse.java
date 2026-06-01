package com.supporthub.knowledge.draft.api;

import com.supporthub.knowledge.draft.domain.DraftStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A proposed knowledge article awaiting manager review.")
public record DraftResponse(

        @Schema(description = "Unique draft identifier.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Resolved ticket this draft was proposed from. Unique per draft.",
                example = "a1b2c3d4-0000-0000-0000-000000000001")
        UUID sourceTicketId,

        @Schema(description = "Organization the draft belongs to.",
                example = "a1b2c3d4-0000-0000-0000-0000000000ff")
        UUID organizationId,

        @Schema(description = "Operator who proposed the article.",
                example = "a1b2c3d4-0000-0000-0000-0000000000aa")
        UUID proposerId,

        @Schema(description = "Proposed title of the article.", example = "How to reset a VPN token")
        String title,

        @Schema(description = "Draft body. May be null until the manager fills it in at publish time.")
        String content,

        @Schema(description = "Current review status.", example = "PENDING_REVIEW")
        DraftStatus status,

        @Schema(description = "Reason provided by the manager when the draft was rejected; null otherwise.",
                example = "Duplicates an existing article")
        String rejectionReason,

        @Schema(description = "Tag identifiers attached to the draft.")
        List<UUID> tags,

        @Schema(description = "When the draft was created.")
        Instant createdAt,

        @Schema(description = "When the draft was last modified (review, publish or reject).")
        Instant updatedAt
) {
}

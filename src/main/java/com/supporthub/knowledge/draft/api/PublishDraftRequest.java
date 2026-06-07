package com.supporthub.knowledge.draft.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to publish a draft as a knowledge article.")
public record PublishDraftRequest(

        @Schema(description = "Optional final title. If blank, the draft's current title is kept.",
                example = "How to reset a VPN token", maxLength = 255)
        @Size(max = 255)
        String title,

        @Schema(description = "Optional final article body. If blank, the AI-generated draft body is "
                + "published as-is; at least one of the two must be present.",
                example = "1. Open the portal... 2. Click 'Reset token'...", maxLength = 20000)
        @Size(max = 20000)
        String content
) {
}

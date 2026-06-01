package com.supporthub.knowledge.draft.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to reject a draft.")
public record RejectDraftRequest(

        @Schema(description = "Mandatory reason for the rejection, shown to the operator.",
                example = "Duplicates an existing article",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 2000)
        @NotBlank
        @Size(max = 2000)
        String rejectionReason
) {
}

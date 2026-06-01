package com.supporthub.knowledge.draft.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Result of publishing a draft: the id of the newly created article.")
public record ArticlePublishedResponse(

        @Schema(description = "Identifier of the published knowledge article.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID articleId
) {
}

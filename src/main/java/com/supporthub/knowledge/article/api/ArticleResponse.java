package com.supporthub.knowledge.article.api;

import com.supporthub.knowledge.article.domain.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A published knowledge article (read-only result of accepting a draft).")
public record ArticleResponse(

        @Schema(description = "Unique article identifier.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Draft this article was published from.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID draftId,

        @Schema(description = "Resolved ticket the article originates from.",
                example = "a1b2c3d4-0000-0000-0000-000000000001")
        UUID sourceTicketId,

        @Schema(description = "Organization the article belongs to.",
                example = "a1b2c3d4-0000-0000-0000-0000000000ff")
        UUID organizationId,

        @Schema(description = "Article title.", example = "How to reset a VPN token")
        String title,

        @Schema(description = "Full article body.")
        String content,

        @Schema(description = "Lifecycle status of the article.", example = "PUBLISHED")
        ArticleStatus status,

        @Schema(description = "Manager who published the article.",
                example = "a1b2c3d4-0000-0000-0000-0000000000bb")
        UUID publishedById,

        @Schema(description = "When the article was published.")
        Instant publishedAt,

        @Schema(description = "Tag identifiers attached to the article.")
        List<UUID> tags
) {
}

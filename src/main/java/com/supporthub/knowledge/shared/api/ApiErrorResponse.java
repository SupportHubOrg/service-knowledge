package com.supporthub.knowledge.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard error body returned for failed requests.")
public record ApiErrorResponse(

        @Schema(description = "When the error occurred.")
        Instant timestamp,

        @Schema(description = "HTTP status code.", example = "404")
        int status,

        @Schema(description = "HTTP status reason phrase.", example = "Not Found")
        String error,

        @Schema(description = "Human-readable error message.", example = "Knowledge draft not found")
        String message,

        @Schema(description = "Request path that produced the error.", example = "/api/v1/knowledge/drafts/{id}")
        String path
) {
}

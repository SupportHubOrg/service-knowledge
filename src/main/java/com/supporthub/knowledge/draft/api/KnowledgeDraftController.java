package com.supporthub.knowledge.draft.api;

import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.draft.domain.DraftStatus;
import com.supporthub.knowledge.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge/drafts")
@RequiredArgsConstructor
@Tag(name = "Knowledge Drafts",
        description = "Review, publish and reject proposed knowledge articles. "
                + "Restricted to organization managers (own organization) and SupportHub administrators.")
public class KnowledgeDraftController {

    private final KnowledgeDraftService draftService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    @Operation(summary = "List drafts",
            description = "Lists drafts visible to the caller, newest first. Non-admins see only "
                    + "their organization's drafts; admins without an organization see all drafts. "
                    + "Optionally filtered by status.")
    public List<DraftResponse> listDrafts(
            @Parameter(description = "Optional status filter.", example = "PENDING_REVIEW")
            @RequestParam(required = false) DraftStatus status,
            JwtAuthenticationToken authentication) {
        return draftService.listDrafts(currentUserResolver.from(jwt(authentication)), status);
    }

    @GetMapping("/{draftId}")
    @Operation(summary = "Get a draft by id")
    @ApiResponse(responseCode = "200", description = "Draft found")
    @ApiResponse(responseCode = "403", description = "Caller may not manage drafts for this organization")
    @ApiResponse(responseCode = "404", description = "Draft not found")
    public DraftResponse getDraft(
            @Parameter(description = "Draft identifier.") @PathVariable UUID draftId,
            JwtAuthenticationToken authentication) {
        return draftService.getDraft(currentUserResolver.from(jwt(authentication)), draftId);
    }

    @PostMapping("/{draftId}/publish")
    @Operation(summary = "Publish a draft",
            description = "Publishes a PENDING_REVIEW draft as a knowledge article and returns the "
                    + "new article id. The optional title overrides the draft title; content is required.")
    @ApiResponse(responseCode = "200", description = "Draft published")
    @ApiResponse(responseCode = "400", description = "Invalid payload or draft is not pending review")
    @ApiResponse(responseCode = "403", description = "Caller may not manage drafts for this organization")
    @ApiResponse(responseCode = "404", description = "Draft not found")
    public ArticlePublishedResponse publishDraft(@PathVariable UUID draftId,
                                                 @Valid @RequestBody PublishDraftRequest request,
                                                 JwtAuthenticationToken authentication) {
        var article = draftService.publishDraft(currentUserResolver.from(jwt(authentication)), draftId, request);
        return new ArticlePublishedResponse(article.id());
    }

    @PostMapping("/{draftId}/reject")
    @Operation(summary = "Reject a draft",
            description = "Rejects a PENDING_REVIEW draft with a mandatory reason.")
    @ApiResponse(responseCode = "200", description = "Draft rejected")
    @ApiResponse(responseCode = "400", description = "Invalid payload or draft is not pending review")
    @ApiResponse(responseCode = "403", description = "Caller may not manage drafts for this organization")
    @ApiResponse(responseCode = "404", description = "Draft not found")
    public DraftResponse rejectDraft(@PathVariable UUID draftId,
                                     @Valid @RequestBody RejectDraftRequest request,
                                     JwtAuthenticationToken authentication) {
        return draftService.rejectDraft(currentUserResolver.from(jwt(authentication)), draftId, request);
    }

    private Jwt jwt(JwtAuthenticationToken authentication) {
        return authentication.getToken();
    }
}

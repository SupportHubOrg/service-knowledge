package com.supporthub.knowledge.article.api;

import com.supporthub.knowledge.article.application.KnowledgeArticleService;
import com.supporthub.knowledge.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge/articles")
@RequiredArgsConstructor
@Validated
@Tag(name = "Knowledge Articles",
        description = "Read-only access to published knowledge articles, scoped to the caller's organization.")
public class KnowledgeArticleController {

    private final KnowledgeArticleService articleService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    @Operation(summary = "List published articles",
            description = "Lists published articles, newest first. Non-admins see only their "
                    + "organization's articles; admins without an organization see all.")
    public List<ArticleResponse> listPublishedArticles(JwtAuthenticationToken authentication) {
        return articleService.listPublishedArticles(currentUserResolver.from(jwt(authentication)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search published articles",
            description = "Full-text search over article title and body. Non-admins search only their "
                    + "organization's articles; admins without an organization search all.")
    public List<ArticleResponse> searchPublishedArticles(
            @Parameter(description = "Search query.", example = "vpn token reset")
            @RequestParam("q") @NotBlank String query,
            JwtAuthenticationToken authentication) {
        return articleService.searchPublishedArticles(currentUserResolver.from(jwt(authentication)), query);
    }

    @GetMapping("/{articleId}")
    @Operation(summary = "Get a published article by id")
    @ApiResponse(responseCode = "200", description = "Article found")
    @ApiResponse(responseCode = "403", description = "Caller may not read articles for this organization")
    @ApiResponse(responseCode = "404", description = "Article not found")
    public ArticleResponse getPublishedArticle(
            @Parameter(description = "Article identifier.") @PathVariable UUID articleId,
            JwtAuthenticationToken authentication) {
        return articleService.getPublishedArticle(currentUserResolver.from(jwt(authentication)), articleId);
    }

    private Jwt jwt(JwtAuthenticationToken authentication) {
        return authentication.getToken();
    }
}

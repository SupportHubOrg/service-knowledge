package com.supporthub.knowledge.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Exposes interactive API docs at {@code /swagger-ui.html} and the raw spec at
 * {@code /v3/api-docs}. Declares a single bearer-JWT security scheme applied to the whole API, so
 * the "Authorize" button in Swagger UI lets you paste an access token and call the secured
 * endpoints. The token is the same OAuth2 JWT the service validates as a resource server.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    OpenAPI knowledgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Knowledge Management Service API")
                        .description("""
                                Manages the organization knowledge base built from resolved tickets.

                                Workflow: an operator proposes an article from a resolved ticket \
                                (delivered asynchronously over Kafka, which creates a draft), an \
                                organization manager reviews the draft and either publishes it as a \
                                knowledge article or rejects it. Published articles are read-only \
                                and scoped to the organization.

                                All endpoints require a valid OAuth2 access token (bearer JWT).""")
                        .version("v1")
                        .contact(new Contact().name("Support Hub").email("support@supporthub.com"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("OAuth2 access token issued by the identity provider (Keycloak realm).")));
    }
}

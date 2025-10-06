package com.nviaud.pricing.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.Scopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI configuration with OAuth2 Authorization Code flow via Keycloak.
 * Only enabled when springdoc.swagger-ui.enabled=true (default: true for dev, false for prod)
 */
@Configuration
@ConditionalOnProperty(
    name = ["springdoc.swagger-ui.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class SwaggerConfig {

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuerUri: String

    @Bean
    fun customOpenAPI(): OpenAPI {
        val authorizationUrl = "$issuerUri/protocol/openid-connect/auth"
        val tokenUrl = "$issuerUri/protocol/openid-connect/token"

        val authorizationCodeFlow = OAuthFlow()
            .authorizationUrl(authorizationUrl)
            .tokenUrl(tokenUrl)
            .scopes(
                Scopes()
                    .addString("openid", "OpenID Connect scope")
                    .addString("profile", "User profile information")
                    .addString("email", "User email address")
            )

        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .flows(OAuthFlows().authorizationCode(authorizationCodeFlow))

        return OpenAPI()
            .info(
                Info()
                    .title("Product Analytics API")
                    .version("1.0.0")
                    .description("API for product pricing analytics and insights")
            )
            .components(
                Components().addSecuritySchemes("oauth2", securityScheme)
            )
            .addSecurityItem(
                SecurityRequirement().addList("oauth2", listOf("openid", "profile", "email"))
            )
    }
}

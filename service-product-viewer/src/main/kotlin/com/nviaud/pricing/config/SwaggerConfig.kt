package com.nviaud.pricing.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@Suppress("unused")
class SwaggerConfig(private val oauth2ClientConfig :OAuth2ClientProperties) {

    @Bean
    fun customOpenAPI(): OpenAPI {

        val clientName = "keycloak"

        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.OPENIDCONNECT)
            .openIdConnectUrl(
                oauth2ClientConfig
                    .provider[clientName]
                    !!.issuerUri
                    .plus("/.well-known/openid-configuration")
            )

        return OpenAPI()
            .info(
                Info()
                    .title("Pricing API")
                    .version("1.0.0")
                    .description("API documentation for Pricing API.")
            ).components(
                Components().addSecuritySchemes("openid", securityScheme)
            ).addSecurityItem(
                SecurityRequirement().addList("openid")
            )

    }
}

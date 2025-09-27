package com.nviaud.pricing.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@Suppress("unused")
class SwaggerConfig() {

    @Bean
    fun customOpenAPI(): OpenAPI {

        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.OPENIDCONNECT)
            .openIdConnectUrl(
                "http://localhost:8081/realms/pricing/.well-known/openid-configuration"
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

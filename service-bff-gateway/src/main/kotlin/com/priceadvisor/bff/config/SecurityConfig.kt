package com.priceadvisor.bff.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler

/**
 * Security configuration for stateful authentication using OAuth2 / OIDC.
 * This is a reactive (WebFlux) configuration for Spring Cloud Gateway.
 */
@Configuration
@EnableWebFluxSecurity
@Suppress("unused")
class SecurityConfig(private val clientRegistrationRepository: ReactiveClientRegistrationRepository) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .authorizeExchange { auth ->
                auth
                    .pathMatchers("/static/**", "/templates/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2Login {  }
            .logout { logout ->
                logout.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }
        return http.build()
    }

    private fun oidcLogoutSuccessHandler(): ServerLogoutSuccessHandler {
        val oidcLogoutSuccessHandler =
            OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository)

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/")

        return oidcLogoutSuccessHandler
    }
}
package com.priceadvisor.bff.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

/**
 * Security configuration for stateful authentication using OAuth2 / OIDC.
 */
@Configuration
@Suppress("unused")
class SecurityConfig(private val clientRegistrationRepository: ClientRegistrationRepository) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/static/**", "/templates/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login {  }
            .sessionManagement { session ->
                session
                    .maximumSessions(1)
            }
            .logout { logout ->
                logout
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
            }
        return http.build()
    }

    private fun oidcLogoutSuccessHandler(): LogoutSuccessHandler {
        val oidcLogoutSuccessHandler =
            OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository)

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/")

        return oidcLogoutSuccessHandler
    }
}
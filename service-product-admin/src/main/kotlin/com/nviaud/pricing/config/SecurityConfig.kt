package com.nviaud.pricing.config

import com.nviaud.pricing.api.annotations.Public
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Security configuration for stateless authentication using JWT tokens.
 */
@Configuration
@Suppress("unused")
class SecurityConfig @Autowired constructor(
    private val handlerMappings: List<RequestMappingInfoHandlerMapping>
) {

    private var publicPatterns: Array<String> ? = null

    /**
     * Configure the security filter chain.
     * Public endpoints are accessible without authentication, while all other endpoints require ADMIN role.
     * JWT is used for stateless authentication.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/", "/static/**", "/templates/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers(*publicPatterns!!).permitAll()
                    .anyRequest().hasRole("ADMIN")
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }  // JWT only, stateless
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Stateless session management

        return http.build()
    }

    /**
     * Initialize public URL patterns by scanning for @Public annotations on controller methods.
     */
    @PostConstruct
    fun init() {
        this.publicPatterns = handlerMappings
            .flatMap { mapping ->
                mapping.handlerMethods.entries
            }
            .filter { (_, handler) ->
                handler is HandlerMethod && handler.hasMethodAnnotation(Public::class.java)
            }
            .flatMap { (info, _) ->
                val patterns = mutableListOf<String>()
                info.patternsCondition?.patterns?.forEach { pattern ->
                    patterns.add(pattern.toString())
                }
                info.pathPatternsCondition?.patterns?.forEach { pattern ->
                    patterns.add(pattern.toString())
                }
                patterns
            }
            .toSet()
            .toTypedArray()
    }

}

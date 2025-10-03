package com.nviaud.pricing.config

import com.nviaud.pricing.api.security.QuotationPermissionEvaluator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler

/**
 * Configuration to register custom PermissionEvaluator for method security.
 */
@Configuration
class MethodSecurityConfig(
    private val quotationPermissionEvaluator: QuotationPermissionEvaluator
) {

    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(quotationPermissionEvaluator)
        return expressionHandler
    }
}

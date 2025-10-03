package com.nviaud.pricing.api.security

import com.nviaud.pricing.repositories.QuotationRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.io.Serializable

/**
 * Custom permission evaluator for Quotation entities.
 * Checks if the authenticated user owns the quotation or has admin role.
 */
@Component
class QuotationPermissionEvaluator(
    private val quotationRepository: QuotationRepository
) : PermissionEvaluator {

    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        if (targetType != "Quotation") return false

        // Check if user has admin role
        if (authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            return true
        }

        // Extract user ID from Keycloak JWT token
        val jwt = authentication.principal as? Jwt ?: return false
        val userId = jwt.getClaimAsString("sub") ?: return false

        // Convert targetId to Long
        val quotationId = when (targetId) {
            is Long -> targetId
            is Int -> targetId.toLong()
            is String -> targetId.toLongOrNull() ?: return false
            else -> return false
        }

        // Efficient existence check - no full entity fetch
        return quotationRepository.existsByIdAndUserId(quotationId, userId)
    }

    override fun hasPermission(
        authentication: Authentication,
        targetDomainObject: Any?,
        permission: Any?
    ): Boolean {
        // Not used in this implementation
        return false
    }
}

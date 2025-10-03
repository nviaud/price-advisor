package com.nviaud.pricing.events

/**
 * Event topics for pricing domain.
 *
 * Versioning: All topics are suffixed with a version (e.g., .v1).
 * Increment the version when breaking changes are introduced to the event payload or semantics.
 */
object Events {
    /**
     * Published when a quotation is submitted by a user.
     * Payload: QuotationSubmitted
     * Version: v1
     */
    const val QUOTATION_SUBMITTED_V1 = "pricing-quotation-submitted.v1"

    /**
     * Published when a product is aggregated.
     * Payload: ProductAggregate
     * Version: v1
     */
    const val PRODUCT_AGGREGATED_V1 = "pricing-product-aggregated.v1"

    /**
     * Published when a product is created.
     * Payload: ProductCreated
     * Version: v1
     */
    const val PRODUCT_CREATED_V1 = "pricing-product-created.v1"

    /**
     * Published when a product is updated.
     * Payload: ProductUpdated
     * Version: v1
     */
    const val PRODUCT_UPDATED_V1 = "pricing-product-updated.v1"

    /**
     * Published when a quotation status is updated.
     * Payload: QuotationUpdated
     * Version: v1
     */
    const val QUOTATION_UPDATED_V1 = "pricing-quotation-updated.v1"
}
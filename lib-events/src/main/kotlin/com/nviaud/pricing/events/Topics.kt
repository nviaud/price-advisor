package com.nviaud.pricing.events

/**
 * Event topics for pricing domain.
 *
 * Versioning: All topics are suffixed with a version (e.g., .v1).
 * Increment the version when breaking changes are introduced to the event payload or semantics.
 */
object Topics {

    /**
     * Published when a quotation is submitted by a user.
     * Payload: QuotationSubmitted
     * Version: v1
     */
    const val QUOTATION_SUBMITTED_V1 = "pricing-quotation-submitted.v1"

    /**
     * Published when quotation parsing (AI extraction) is completed successfully.
     * The quotation is now in WAITING_VALIDATION status.
     * Payload: QuotationParsingCompleted
     * Version: v1
     */
    const val QUOTATION_PARSING_COMPLETED_V1 = "pricing-quotation-parsing-completed.v1"

    /**
     * Published when quotation parsing (AI extraction) fails.
     * The quotation is marked as ERROR status.
     * Payload: QuotationParsingFailed
     * Version: v1
     */
    const val QUOTATION_PARSING_FAILED_V1 = "pricing-quotation-parsing-failed.v1"

    /**
     * Published when a quotation is validated by the user or admin.
     * Payload: QuotationValidated
     * Version: v1
     */
    const val QUOTATION_VALIDATED_V1 = "pricing-quotation-validated.v1"

    /**
     * Published when a quotation is rejected by the user or admin.
     * Payload: QuotationRejected
     * Version: v1
     */
    const val QUOTATION_REJECTED_V1 = "pricing-quotation-rejected.v1"

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

}
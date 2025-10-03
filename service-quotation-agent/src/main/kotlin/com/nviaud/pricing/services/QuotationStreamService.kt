package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.events.v1.QuotationUpdated
import com.nviaud.pricing.repositories.QuotationRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Service to handle streaming quotation updates via SSE.
 * Listens to QuotationUpdated events and broadcasts them to subscribed clients.
 */
@Service
class QuotationStreamService(
    private val quotationRepository: QuotationRepository
) {

    private val logger = LoggerFactory.getLogger(QuotationStreamService::class.java)

    // Map of quotationId to Sinks for broadcasting updates
    private val quotationSinks = ConcurrentHashMap<Long, Sinks.Many<Quotation>>()

    /**
     * Listen to QuotationUpdated events from the message broker.
     */
    @Bean
    fun onQuotationUpdated(): Consumer<QuotationUpdated> = Consumer { event ->
        logger.info("Received QuotationUpdated event: $event")

        val quotationId = event.quotationId.toLong()
        val sink = quotationSinks[quotationId]

        if (sink != null) {
            // Fetch the full quotation and emit to subscribers
            val quotation = quotationRepository.findById(quotationId).orElse(null)
            if (quotation != null) {
                sink.tryEmitNext(quotation)
                logger.debug("Emitted quotation update to stream for quotationId: {}", quotationId)

                // Complete the stream if quotation reached a terminal state
                if (quotation.status in listOf(
                        com.nviaud.pricing.entities.QuotationStatus.VALIDATED,
                        com.nviaud.pricing.entities.QuotationStatus.REJECTED,
                        com.nviaud.pricing.entities.QuotationStatus.ERROR
                    )
                ) {
                    sink.tryEmitComplete()
                    quotationSinks.remove(quotationId)
                    logger.debug("Completed and removed stream for quotationId: {}", quotationId)
                }
            }
        }
    }

    /**
     * Create a reactive stream for a specific quotation.
     * Returns a Flux that emits quotation updates as they occur.
     */
    fun streamQuotationUpdates(quotationId: Long): Flux<Quotation> {
        // Create or get existing sink for this quotation
        val sink = quotationSinks.computeIfAbsent(quotationId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }

        // Emit initial state immediately
        val initialQuotation = quotationRepository.findById(quotationId).orElseThrow {
            NoSuchElementException("Quotation with id $quotationId not found")
        }

        return Flux.concat(
            Flux.just(initialQuotation),
            sink.asFlux()
        ).doOnCancel {
            logger.debug("Client disconnected from stream for quotationId: {}", quotationId)
            // Check if there are no more subscribers and clean up
            if (sink.currentSubscriberCount() != 0) {
                quotationSinks.remove(quotationId)
            }
        }
    }
}

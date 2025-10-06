package com.nviaud.pricing.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.jackson.JsonFormat
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Publishes events from the transactional outbox to the message broker.
 * Runs periodically to ensure at-least-once delivery.
 */
@Component
class OutboxEventPublisher(
    private val outboxRepository: OutboxEventRepository,
    private val streamBridge: StreamBridge,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Publishes unpublished events from the outbox.
     * Runs every second.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publishOutboxEvents() {
        val unpublished = outboxRepository.findUnpublishedEvents(100)

        if (unpublished.isEmpty()) {
            return
        }

        logger.debug("Publishing {} outbox events", unpublished.size)

        unpublished.forEach { event ->
            try {
                // Deserialize CloudEvent from JSON
                val cloudEvent = JsonFormat().deserialize(event.payload.toByteArray())

                // Send to message broker (event type is the destination)
                val message = MessageBuilder
                    .withPayload(cloudEvent)
                    .build()

                val sent = streamBridge.send(event.eventType, message)

                if (sent) {
                    event.published = true
                    event.publishedAt = Instant.now()
                    event.publishAttempts++
                    outboxRepository.save(event)
                    logger.debug(
                        "Published event: type={}, aggregateId={}, correlationId={}",
                        event.eventType,
                        event.aggregateId,
                        event.correlationId
                    )
                } else {
                    event.publishAttempts++
                    event.lastError = "StreamBridge.send() returned false"
                    outboxRepository.save(event)
                    logger.error(
                        "Failed to publish event: type={}, aggregateId={}, attempt={}",
                        event.eventType,
                        event.aggregateId,
                        event.publishAttempts
                    )
                }
            } catch (e: Exception) {
                event.publishAttempts++
                event.lastError = e.message
                outboxRepository.save(event)
                logger.error(
                    "Error publishing event: type={}, aggregateId={}, attempt={}",
                    event.eventType,
                    event.aggregateId,
                    event.publishAttempts,
                    e
                )
            }
        }
    }

    /**
     * Cleans up old published events.
     * Runs every hour, keeps events for 7 days.
     *
     * Uses ShedLock to ensure only ONE instance executes this cleanup
     * across multiple service instances in production.
     *
     * - lockAtMostFor: Maximum lock duration (50 minutes) - if instance crashes, lock expires
     * - lockAtLeastFor: Minimum lock duration (10 minutes) - prevents too frequent execution
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    @SchedulerLock(
        name = "OutboxEventPublisher_cleanupOldEvents",
        lockAtMostFor = "50m",
        lockAtLeastFor = "10m"
    )
    @Transactional
    fun cleanupOldEvents() {
        val retentionDays = 7L
        val before = Instant.now().minus(retentionDays, ChronoUnit.DAYS)
        val deleted = outboxRepository.deletePublishedEventsBefore(before)

        if (deleted > 0) {
            logger.info("Cleaned up {} published events older than {} days", deleted, retentionDays)
        }
    }

}

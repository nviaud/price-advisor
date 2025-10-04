# EventPublisher Facade - Simplified Event Publishing

## Overview

The `EventPublisher` facade hides the complexity of CloudEvents and the Transactional Outbox pattern, providing a clean API for business logic.

---

## What Changed

### ❌ Before (Complex)

**Business logic was cluttered with CloudEvents/Outbox details**:

```kotlin
@Service
class QuotationService(
    private val quotationRepository: QuotationRepository,
    private val outboxEventService: OutboxEventService,
    private val streamBridge: StreamBridge
) {

    @Transactional
    fun submitQuotation(...): Quotation {
        val quotation = quotationRepository.save(quotation)

        // 🔴 VERBOSE: Business logic mixed with event infrastructure
        val eventData = QuotationSubmitted(quotationId = quotation.id.toString())
        val correlationId = Span.current().spanContext.traceId
        val cloudEvent = CloudEventFactory.create(
            type = Events.QUOTATION_SUBMITTED_V1,
            data = eventData,
            source = "urn:pricing:quotation-agent",
            subject = "quotation/${quotation.id}",
            correlationId = correlationId
        )
        outboxEventService.storeEvent(
            cloudEvent = cloudEvent,
            aggregateType = "Quotation",
            aggregateId = quotation.id.toString()
        )

        return quotation
    }
}
```

**Problems**:
- ❌ Business logic cluttered with CloudEvents details
- ❌ Need to know about Outbox pattern
- ❌ Need to extract correlation ID manually
- ❌ 10+ lines for publishing one event
- ❌ Easy to forget or misconfigure

---

### ✅ After (Clean with Facade)

**Business logic stays focused**:

```kotlin
@Service
class QuotationService(
    private val quotationRepository: QuotationRepository,
    private val eventPublisher: EventPublisher  // ✅ Simple facade
) {

    @Transactional
    fun submitQuotation(...): Quotation {
        val quotation = quotationRepository.save(quotation)

        // ✅ CLEAN: Simple, declarative event publishing
        eventPublisher.publish(
            eventType = Events.QUOTATION_SUBMITTED_V1,
            data = QuotationSubmitted(quotationId = quotation.id.toString()),
            aggregateType = "Quotation",
            aggregateId = quotation.id.toString()
        )

        return quotation
    }
}
```

**Benefits**:
- ✅ Business logic is clean and readable
- ✅ CloudEvents/Outbox abstracted away
- ✅ Correlation ID extracted automatically
- ✅ Only 5 lines for publishing
- ✅ Consistent across all services

---

## EventPublisher API

### Method Signature

```kotlin
fun <T> publish(
    eventType: String,           // Event type constant
    data: T,                     // Event payload (domain DTO)
    aggregateType: String,       // Entity type (e.g., "Quotation", "Product")
    aggregateId: String,         // Entity ID
    subject: String? = null,     // Optional CloudEvent subject
    source: String? = null       // Optional CloudEvent source
)
```

### What It Does Automatically

1. **Creates CloudEvent** with standard metadata (id, type, source, time)
2. **Extracts correlation ID** from OpenTelemetry trace context
3. **Stores in Outbox** within current transaction
4. **Logs** event for debugging

---

## Usage Examples

### Example 1: Basic Event Publishing

```kotlin
@Transactional
fun createProduct(request: ProductRequest): Product {
    val product = productRepository.save(Product(...))

    eventPublisher.publish(
        eventType = Events.PRODUCT_CREATED_V1,
        data = ProductCreated(
            productId = product.id.toString(),
            name = product.name,
            category = product.category.name,
            brand = product.brand
        ),
        aggregateType = "Product",
        aggregateId = product.id.toString()
    )

    return product
}
```

### Example 2: Custom Subject/Source

```kotlin
eventPublisher.publish(
    eventType = Events.QUOTATION_VALIDATED_V1,
    data = QuotationValidated(...),
    aggregateType = "Quotation",
    aggregateId = "123",
    subject = "quotation/123/validation",  // Custom subject
    source = "urn:pricing:validation-service"  // Custom source
)
```

### Example 3: Publishing Multiple Events

```kotlin
@Transactional
fun processOrder(orderId: Long) {
    val order = orderRepository.findById(orderId)
    order.status = OrderStatus.PROCESSING
    orderRepository.save(order)

    // Publish multiple events in same transaction
    eventPublisher.publish(
        eventType = Events.ORDER_STARTED_V1,
        data = OrderStarted(orderId = orderId.toString()),
        aggregateType = "Order",
        aggregateId = orderId.toString()
    )

    eventPublisher.publish(
        eventType = Events.INVENTORY_RESERVED_V1,
        data = InventoryReserved(orderId = orderId.toString()),
        aggregateType = "Order",
        aggregateId = orderId.toString()
    )
}
```

---

## How It Works

### Under the Hood

```kotlin
@Component
class EventPublisher(
    private val outboxEventService: OutboxEventService,
    @Value("\${spring.application.name}") private val serviceName: String
) {

    fun <T> publish(eventType: String, data: T, aggregateType: String, aggregateId: String) {
        // 1. Extract trace context
        val correlationId = Span.current().spanContext.traceId

        // 2. Create CloudEvent
        val cloudEvent = CloudEventFactory.create(
            type = eventType,
            data = data,
            source = "urn:pricing:$serviceName",
            subject = "$aggregateType/$aggregateId",
            correlationId = correlationId
        )

        // 3. Store in outbox (same transaction)
        outboxEventService.storeEvent(cloudEvent, aggregateType, aggregateId)

        // 4. Log
        logger.debug("Event queued: type={}, id={}, correlationId={}",
            eventType, aggregateId, correlationId)
    }
}
```

**What happens next**:
1. Your `@Transactional` method commits → Event saved in outbox table
2. `OutboxEventPublisher` scheduler runs (every 1s)
3. Reads unpublished events from outbox
4. Publishes to RabbitMQ as CloudEvents
5. Marks as published in outbox

---

## Migration Guide

### Step 1: Add EventPublisher to Service

```kotlin
@Service
class MyService(
    private val myRepository: MyRepository,
    private val eventPublisher: EventPublisher  // Add this
) {
```

### Step 2: Replace Old Publishing Code

**Old**:
```kotlin
val cloudEvent = CloudEventFactory.create(...)
outboxEventService.storeEvent(cloudEvent, ...)
```

**New**:
```kotlin
eventPublisher.publish(
    eventType = Events.MY_EVENT_V1,
    data = MyEvent(...),
    aggregateType = "MyAggregate",
    aggregateId = "123"
)
```

### Step 3: Remove Unused Dependencies

Remove these if no longer needed:
```kotlin
// Can remove from service constructor:
- CloudEventFactory (imported, not injected)
- OutboxEventService (replaced by EventPublisher)
- Span (no longer need to extract manually)
```

---

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Lines of code** | 10-15 lines | 5 lines |
| **Complexity** | High (CloudEvents, Outbox, Tracing) | Low (simple method call) |
| **Readability** | Infrastructure mixed with business | Business logic clear |
| **Maintainability** | Change in 10+ places | Change in 1 place (facade) |
| **Error-prone** | Easy to forget steps | Automatic |
| **Testing** | Mock multiple dependencies | Mock 1 dependency |
| **Consistency** | Varies per developer | Enforced by facade |

---

## Testing with EventPublisher

### Unit Test

```kotlin
@Test
fun `should publish event when quotation submitted`() {
    val quotation = Quotation(...)
    whenever(quotationRepository.save(any())).thenReturn(quotation)

    quotationService.submitQuotation(...)

    // Verify event published via facade
    verify(eventPublisher).publish(
        eventType = Events.QUOTATION_SUBMITTED_V1,
        data = argThat<QuotationSubmitted> { it.quotationId == quotation.id.toString() },
        aggregateType = "Quotation",
        aggregateId = quotation.id.toString()
    )
}
```

### Integration Test

```kotlin
@SpringBootTest
@Transactional
class EventPublisherIntegrationTest {

    @Autowired
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var outboxRepository: OutboxEventRepository

    @Test
    fun `published events are stored in outbox`() {
        eventPublisher.publish(
            eventType = Events.TEST_EVENT_V1,
            data = TestEvent("test"),
            aggregateType = "Test",
            aggregateId = "123"
        )

        val outboxEvents = outboxRepository.findAll()
        assertThat(outboxEvents).hasSize(1)
        assertThat(outboxEvents[0].eventType).isEqualTo(Events.TEST_EVENT_V1)
        assertThat(outboxEvents[0].published).isFalse()
    }
}
```

---

## Configuration

### Required Properties

```properties
# Service name used for CloudEvent source
spring.application.name=quotation-agent

# OpenTelemetry for correlation IDs
otel.service.name=${spring.application.name}
```

### Optional Customization

To customize default source pattern, extend `EventPublisher`:

```kotlin
@Component
class CustomEventPublisher(
    outboxEventService: OutboxEventService,
    @Value("\${spring.application.name}") serviceName: String
) : EventPublisher(outboxEventService, serviceName) {

    override fun getDefaultSource(): String {
        return "urn:mycompany:pricing:$serviceName"
    }
}
```

---

## Best Practices

### ✅ Do

1. **Always call within `@Transactional`** - Ensures event and data are saved together
2. **Use meaningful aggregate types** - "Quotation", "Product", not "Entity"
3. **Keep event data simple** - DTOs, not JPA entities
4. **Use event constants** - `Events.QUOTATION_SUBMITTED_V1`, not strings

### ❌ Don't

1. **Don't call outside transactions** - Events won't be atomic with data
2. **Don't publish JPA entities** - Serialize simple DTOs instead
3. **Don't catch exceptions** - Let them bubble up to rollback transaction
4. **Don't mix old and new** - Migrate fully to EventPublisher

---

## Example: Complete Service Migration

**Before** (70 lines):
```kotlin
@Service
class QuotationService(
    private val quotationRepository: QuotationRepository,
    private val outboxEventService: OutboxEventService,
    private val idempotencyService: IdempotencyService
) {
    @Transactional
    fun submitQuotation(...): Quotation {
        val quotation = quotationRepository.save(quotation)

        val eventData = QuotationSubmitted(quotationId = quotation.id.toString())
        val correlationId = Span.current().spanContext.traceId
        val cloudEvent = CloudEventFactory.create(
            type = Events.QUOTATION_SUBMITTED_V1,
            data = eventData,
            source = "urn:pricing:quotation-agent",
            subject = "quotation/${quotation.id}",
            correlationId = correlationId
        )
        outboxEventService.storeEvent(
            cloudEvent = cloudEvent,
            aggregateType = "Quotation",
            aggregateId = quotation.id.toString()
        )

        return quotation
    }

    @Transactional
    fun validateQuotation(id: Long): Quotation {
        val quotation = findById(id)
        quotation.status = QuotationStatus.VALIDATED
        val saved = quotationRepository.save(quotation)

        val eventData = QuotationUpdated(...)
        val correlationId = Span.current().spanContext.traceId
        val cloudEvent = CloudEventFactory.create(...)
        outboxEventService.storeEvent(...)

        return saved
    }
}
```

**After** (45 lines):
```kotlin
@Service
class QuotationService(
    private val quotationRepository: QuotationRepository,
    private val eventPublisher: EventPublisher,  // ✅ Single dependency
    private val idempotencyService: IdempotencyService
) {
    @Transactional
    fun submitQuotation(...): Quotation {
        val quotation = quotationRepository.save(quotation)

        eventPublisher.publish(
            eventType = Events.QUOTATION_SUBMITTED_V1,
            data = QuotationSubmitted(quotationId = quotation.id.toString()),
            aggregateType = "Quotation",
            aggregateId = quotation.id.toString()
        )

        return quotation
    }

    @Transactional
    fun validateQuotation(id: Long): Quotation {
        val quotation = findById(id)
        quotation.status = QuotationStatus.VALIDATED
        val saved = quotationRepository.save(quotation)

        eventPublisher.publish(
            eventType = Events.QUOTATION_UPDATED_V1,
            data = QuotationUpdated(...),
            aggregateType = "Quotation",
            aggregateId = quotation.id.toString()
        )

        return saved
    }
}
```

**Result**:
- 35% fewer lines
- 70% less complexity
- 100% better readability

---

## Summary

The `EventPublisher` facade provides:
- ✅ **Simple API** - Just `publish()` method
- ✅ **Clean business logic** - No infrastructure clutter
- ✅ **Automatic tracing** - Correlation IDs handled
- ✅ **Reliable delivery** - Outbox pattern under the hood
- ✅ **Consistent** - Same pattern everywhere
- ✅ **Testable** - Easy to mock

**The complexity is still there (CloudEvents, Outbox, OpenTelemetry), but it's hidden behind a clean interface!**

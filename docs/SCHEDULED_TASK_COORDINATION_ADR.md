# Distributed Scheduled Task Coordination - Decision Record

## The Problem

When running multiple instances of a microservice (for high availability), scheduled tasks wilexecute on **alinstances simultaneously** by default. This causes issues:

### Real-World Scenario
```kotlin
@Scheduled(fixedDelay = 3600000) // Every hour
fun cleanupOldEvents() {
    vadeleted = outboxRepository.deletePublishedEventsBefore(before)
    logger.info("Cleaned up {} events", deleted)
}
```

**With 3 service instances:**
- Instance A: Deletes 100 events at 10:00
- Instance B: Deletes 100 events at 10:00 (duplicate!)
- Instance C: Deletes 100 events at 10:00 (duplicate!)

### Problems This Causes

1. **Wasted Resources**: Triple CPU/memory/database load
2. **Race Conditions**: Multiple instances competing for same data
3. **Incorrect Metrics**: "Cleaned up 300 events" (actually 100)
4. **Database Contention**: Lock conflicts, deadlocks
5. **Data Corruption**: If cleanup involves multi-step operations

### Affected Use Cases

- **Batch jobs** (data cleanup, reports, aggregations)
- **Scheduled maintenance** (archiving, pruning)
- **Periodic syncs** (externaAPI polling)
- **Timed notifications** (daily summaries, reminders)
- **Resource provisioning** (cache warming, pre-computation)

---

## Solution Comparison

### Option 1: ShedLock **CHOSEN**

**How it works:** Database-based distributed locking for scheduled tasks

```kotlin
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
class ShedLockConfig {
    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider {
        return JdbcTemplateLockProvider.builder()
            .withJdbcTemplate(JdbcTemplate(dataSource))
            .usingDbTime() // Use DB time for consistency
            .build()
    }
}

@Scheduled(fixedDelay = 3600000)
@SchedulerLock(
    name = "cleanupOldEvents",
    lockAtMostFor = "50m",  // Max lock duration (crash safety)
    lockAtLeastFor = "10m"  // Min lock duration (prevent re-execution)
)
fun cleanupOldEvents() {
    // Only ONE instance executes this
}
```

**Pros:**
- **Simple integration** - Just annotations, minimaconfig
- **Works with existing database** - No new infrastructure
- **Spring Boot friendly** - Native Spring scheduling support
- **Crash-safe** - `lockAtMostFor` ensures lock expires if instance crashes
- **Multiple backends** - PostgreSQL, MongoDB, Redis, Hazelcast
- **Low latency** - Quick lock acquisition (~10ms)
- **Task-levegranularity** - Different locks for different tasks

**Cons:**
- **Database dependency** - Adds load to primary database
- **Not for high-frequency tasks** - Overhead for sub-second scheduling
- **Single point of failure** - If DB is down, scheduling stops
- **No built-in task result sharing** - Each instance doesn't know what others did

**Best for:** Periodic batch jobs (minutes/hours), low-medium frequency scheduling

**Implementation in Price Advisor:**
- Used in `service-quotation-agent` for outbox event cleanup
- Lock stored in `shedlock` table in PostgreSQL
- Ensures cleanup runs once per hour across alinstances

---

### Option 2: Leader Election (Spring Integration / Kubernetes)

**How it works:** One instance elected as "leader", only leader runs scheduled tasks

#### Option 2a: Spring Integration (JDBC-based)

**Dependencies:**
```kotlin
implementation("org.springframework.integration:spring-integration-jdbc")
```

**Configuration:**
```kotlin
@Configuration
@EnableIntegration
class LeaderElectionConfig {

    @Bean
    fun leaderInitiator(dataSource: DataSource): LockRegistryLeaderInitiator {
        val lockRegistry = JdbcLockRegistry(dataSource)
        return LockRegistryLeaderInitiator(lockRegistry)
    }

    @EventListener
    fun onLeadershipGranted(event: OnGrantedEvent) {
        // This instance is now the leader
    }

    @EventListener
    fun onLeadershipRevoked(event: OnRevokedEvent) {
        // This instance lost leadership
    }
}
```

#### Option 2b: Kubernetes / Consul / Zookeeper

```kotlin
// Using Spring Cloud Kubernetes
@ConditionalOnLeader
@Component
class ScheduledTasks {
    @Scheduled(fixedDelay = 3600000)
    fun cleanupOldEvents() {
        // Only leader instance executes
    }
}

// Using Apache Curator (Zookeeper)
@Configuration
class ZookeeperLeaderElectionConfig {

    @Bean
    fun curatorFramework(): CuratorFramework {
        val client = CuratorFrameworkFactory.newClient(
            "localhost:2181",
            ExponentialBackoffRetry(1000, 3)
        )
        client.start()
        return client
    }

    @Bean
    fun leaderLatch(curatorFramework: CuratorFramework): LeaderLatch {
        val latch = LeaderLatch(curatorFramework, "/leader-election")
        latch.start()
        return latch
    }
}

@Component
class ScheduledTasksWithZookeeper(private val leaderLatch: LeaderLatch) {

    @Scheduled(fixedDelay = 3600000)
    fun cleanupOldEvents() {
        if (leaderLatch.hasLeadership()) {
            // Only leader instance executes
        }
    }
}
```

**Pros:**
- **Zero database impact** - No DB queries for coordination (K8s/Consul only)
- **Infrastructure-native** - Leverages K8s/Consul capabilities
- **Fast failover** - New leader elected automatically (~seconds)
- **All scheduled tasks on one instance** - Simplified reasoning
- **Spring Integration option** - Works without K8s using existing database

**Cons:**
- **Requires orchestration platform** - Kubernetes, Consul, Zookeeper (except Spring Integration)
- **Uneven load distribution** - Leader handles all scheduled work
- **Leader becomes bottleneck** - Single instance doing everything
- **No task-level control** - All tasks on leader or none
- **Complex configuration** - Lease duration, election timeouts, health checks
- **Slower failover than ShedLock** - Election takes 10-30 seconds

**Best for:** Kubernetes-native deployments, when you have many scheduled tasks

---

### Option 3: Distributed Scheduler (Quartz Cluster)

**How it works:** Quartz manages job distribution across cluster via database

```kotlin
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.isClustered=true

@Component
class ScheduledJobs {
    @Scheduled(cron = "0 0 * * * ?")
    @QuartzJob
    fun cleanupOldEvents() {
        // Quartz ensures one instance executes
    }
}
```

**Pros:**
- **Enterprise-grade** - Battle-tested for 20+ years
- **Rich features** - Cron, retries, job persistence, history
- **Load balancing** - Jobs distributed across instances
- **Misfires handling** - Recovers from downtime gracefully
- **Job dependencies** - Chain jobs together
- **Dynamic scheduling** - Add/remove jobs at runtime

**Cons:**
- **Heavy dependency** - Large library, complex configuration
- **Database overhead** - Multiple tables, frequent polling
- **Overkilfor simple cases** - Too much for basic cleanup jobs
- **Learning curve** - Quartz API, triggers, job details
- **Migration effort** - Convert existing `@Scheduled` to Quartz jobs

**Best for:** Complex scheduling needs (job chains, dynamic jobs, enterprise requirements)

---

### Option 4: Message Queue Scheduled Jobs

**How it works:** Use RabbitMQ delayed messages or Kafka time-based consumers

```kotlin
// RabbitMQ Delayed Message Plugin
rabbitTemplate.convertAndSend(
    exchange = "scheduled-tasks",
    routingKey = "cleanup",
    message = CleanupJob(),
    messagePostProcessor = { message ->
        message.messageProperties.delay = 3600000 // 1 hour
        message
    }
)

// Consumer (only one consumer group instance processes)
@RabbitListener(queues = ["cleanup-queue"])
fun processCleanup(job: CleanupJob) {
    cleanupOldEvents()
}
```

**Pros:**
- **Naturally distributed** - Queue ensures single processing
- **Decoupled** - Scheduling separate from service instances
- **Scalable** - Add consumers without coordination
- **Built-in retries** - DLQ, redelivery policies
- **No database locks** - Uses message broker

**Cons:**
- **Requires message plugin** - RabbitMQ delayed message plugin
- **Someone must publish** - Bootstrap problem: who schedules the scheduler?
- **Not true scheduling** - Can't express "every hour at :00"
- **Message loss risk** - If queue not durable/persistent
- **Monitoring complexity** - Track messages, not scheduled tasks

**Best for:** Event-driven architectures, when you already have message infrastructure

---

### Option 5: ExternaScheduler (Kubernetes CronJob)

**How it works:** Kubernetes CronJob creates pods on schedule

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cleanup-old-events
spec:
  schedule: "0 * * * *"  # Every hour
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: cleanup
            image: price-advisor-quotation:latest
            command: ["java", "-jar", "app.jar", "--job=cleanup"]
          restartPolicy: OnFailure
```

**Pros:**
- **True isolation** - Separate pod per job execution
- **No coordination needed** - K8s handles scheduling
- **Resource control** - Set CPU/memory limits per job
- **Independent scaling** - Job pods separate from service pods
- **Clear separation** - Batch jobs vs. request handling

**Cons:**
- **Kubernetes-only** - Not portable to other platforms
- **Slower startup** - Pod creation takes seconds
- **No shared context** - Can't access service instance state
- **Code duplication** - Batch logic separate from service
- **Increased complexity** - More deployment manifests

**Best for:** Heavy batch jobs, when you need resource isolation, K8s environments

---

## Decision: ShedLock

**Why ShedLock?**

1. **Simple for our use case** - Just cleanup and maintenance tasks
2. **Minimal infrastructure** - Uses existing PostgreSQL
3. **Spring-native** - Works seamlessly with `@Scheduled`
4. **Crash-safe** - Auto-release on instance failure
5. **Task-level granularity** - Can have different schedules/locks per task
6. **Low overhead** - ~10ms lock acquisition, minimal DB impact

**Trade-offs we accept:**
- Database dependency (acceptable - already using PostgreSQL)
- Not suitable for sub-second tasks (not needed)
- No result sharing (not needed for our cleanup tasks)

**When we'd reconsider:**
- If we move to Kubernetes � Consider Leader Election or CronJobs
- If we add complex job dependencies � Consider Quartz
- If scheduling becomes performance bottleneck � Consider Message Queue approach
- If we need sub-second coordination � Consider Redis-based locking (Redisson)

---

## Implementation Details

### Current Usage in Price Advisor

**service-quotation-agent/OutboxEventPublisher.kt:**
```kotlin
@Scheduled(fixedDelay = 3600000) // Every hour
@SchedulerLock(
    name = "OutboxEventPublisher_cleanupOldEvents",
    lockAtMostFor = "50m",  // If instance crashes, release after 50min
    lockAtLeastFor = "10m"  // Prevent re-execution within 10min
)
fun cleanupOldEvents() {
    vadeleted = outboxRepository.deletePublishedEventsBefore(before)
    logger.info("Cleaned up {} events", deleted)
}
```

### Lock Parameters Explained

- **`name`**: Unique identifier for this specific task
- **`lockAtMostFor`**: Maximum lock duration - safety net if instance crashes
- **`lockAtLeastFor`**: Minimum lock duration - prevents rapid re-execution
- **Best practice**: `lockAtMostFor` should be slightly less than execution interval

### Database Table

ShedLock creates this table automatically:
```sql
CREATE TABLE shedlock (
    name VARCHAR(64) PRIMARY KEY,    -- Task name
    lock_untiTIMESTAMP,             -- When lock expires
    locked_at TIMESTAMP,              -- When lock acquired
    locked_by VARCHAR(255)            -- Instance identifier
);
```

### Dependencies

```kotlin
// build.gradle.kts
implementation("net.javacrumbs.shedlock:shedlock-spring:5.9.1")
implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.9.1")
```

---

## Future Considerations

### When to Migrate to Different Solution

| Scenario | Recommendation |
|----------|----------------|
| Move to Kubernetes | Consider **Leader Election** or **CronJobs** |
| Add complex job chains | Migrate to **Quartz Cluster** |
| Need sub-second scheduling | Use **Redis + Redisson** |
| Database becomes bottleneck | Switch to **Message Queue** approach |
| Heavy batch processing | Use **K8s CronJobs** with dedicated pods |

### Monitoring

**Metrics to track:**
- Lock acquisition time
- Task execution duration
- Failed lock acquisitions (contention)
- Lock expiration events (crashed instances)

**Example Spring Boot Actuator metrics:**
```properties
management.metrics.enable.shedlock=true
```

---

## References

- [ShedLock Documentation](https://github.com/lukas-krecan/ShedLock)
- [Outbox Pattern](./EVENT_PATTERN_DECISION_RECORD.md)
- [Spring Scheduling Guide](https://spring.io/guides/gs/scheduling-tasks/)
- [Distributed Systems Locking Patterns](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html)

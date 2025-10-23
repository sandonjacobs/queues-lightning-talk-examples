# Queues Lightning Talk Examples

A demonstration of the new **KIP-932 Queues for Kafka** functionality using an IoT Alerts use case.

## Overview

This repository contains examples that demonstrate the new **KIP-932 Queues for Kafka** functionality. The system simulates an IoT environment where devices generate random alerts that are produced to a Kafka topic and then cooperatively consumed using the new queue-based consumption model.

## Architecture

The IoT Alerts system consists of several key components:

1. **Alert Generator** - Produces random IoT alerts to Kafka
2. **Alert Processors** - Multiple consumer instances using KIP-932 Queues
3. **Cache Manager** - Manages device-to-recipient subscriptions
4. **Coordinator** - Orchestrates the entire system

## Key Features

### 1. Random Alert Generation

The system generates realistic IoT alerts with the following characteristics:

- **25 Sample Devices**: `device-001` through `device-025`
- **8 Sample Recipients**: Various users with different notification preferences
- **4 Alert Types**: Temperature, Humidity, Pressure, and Motion
- **Randomized Content**: Each alert contains realistic sensor readings and messages

#### Alert Types and Sample Messages

```kotlin
enum class AlertType(val value: String) {
    TEMPERATURE("Temperature"),
    HUMIDITY("Humidity"), 
    PRESSURE("Pressure"),
    MOTION("Motion")
}
```

**Sample Alert Messages:**
- Temperature: "Temperature threshold exceeded: 95°F"
- Humidity: "Humidity level critical: 87%"
- Pressure: "Pressure reading abnormal: 28 PSI"
- Motion: "Motion detected in restricted area"

### 2. Device Subscription Management

The system uses a **Caffeine cache** to manage device-to-recipient subscriptions:

```kotlin
class CacheManager {
    private val deviceRecipientsCache: Cache<String, List<Recipient>> = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofHours(24))
        .recordStats()
        .build()
}
```

**Key Features:**
- Each device has 1-3 randomly assigned recipients
- Cache expires entries after 24 hours
- Supports up to 10,000 device subscriptions
- Provides cache statistics for monitoring

### 3. Kafka Production

The `AlertGenerator` produces alerts to the `iot-alerts` topic:

```kotlin
class AlertGenerator : Runnable, AutoCloseable {
    private lateinit var kafkaProducer: KafkaProducer<String, String>
    
    override fun run() {
        kafkaProducer = KafkaProducer<String, String>(
            KafkaConfigurations.producerProperties(
                bootstrapServers = "localhost:9092",
                clientId = "iot-alert-generator"
            )
        )
        
        // Generate and send alerts every 100ms for 5 minutes
        while (Clock.System.now() < stopTime) {
            val deviceId = deviceIds.random()
            val recipients = cacheManager.getRecipientsForDevice(deviceId)
            val alert = generateRandomAlert(deviceId, recipients)
            
            kafkaProducer.send(ProducerRecord(KAFKA_ALERT_TOPIC, deviceId, Json.encodeToString(alert)))
            Thread.sleep(sendIntervalMs) // 100ms interval
        }
    }
}
```

**Production Characteristics:**
- **Topic**: `iot-alerts`
- **Key**: Device ID (for partitioning)
- **Value**: JSON-serialized Alert object
- **Frequency**: Every 100ms
- **Duration**: 5 minutes of continuous generation

## KIP-932 Queues Implementation

### What is KIP-932?

KIP-932 introduces a new **queue-based consumption model** for Kafka that enables:

- **Cooperative Processing**: Multiple consumers can work on the same partition
- **Explicit Acknowledgment**: Fine-grained control over message processing
- **Load Balancing**: Better distribution of work across consumer instances
- **Fault Tolerance**: Improved handling of consumer failures

### Implementation Details

The system uses `KafkaShareConsumer` (the new consumer type) with specific configurations:

```kotlin
class AlertProcessor(val id: Int): Runnable, AutoCloseable {
    private lateinit var kafkaShareConsumer: KafkaShareConsumer<String, String>
    
    override fun run() {
        kafkaShareConsumer = KafkaShareConsumer(
            KafkaConfigurations.shareConsumerProperties("localhost:9092", SHARE_GROUP_ID)
        )
        
        kafkaShareConsumer.subscribe(listOf(IotAlertCoordinator.KAFKA_ALERT_TOPIC))
        
        while (true) {
            val records = kafkaShareConsumer.poll(Duration.ofMillis(1000))
            for (record in records) {
                try {
                    val alert = Json.decodeFromString<Alert>(record.value())
                    logger.info("Processing alert {}", alert)
                    kafkaShareConsumer.acknowledge(record, AcknowledgeType.ACCEPT)
                } catch (e: Exception) {
                    logger.error("Error processing record", e)
                    kafkaShareConsumer.acknowledge(record, AcknowledgeType.RELEASE)
                }
            }
        }
    }
}
```

### KIP-932 Configuration

The system uses specific configurations to enable the new queue functionality:

```kotlin
fun shareConsumerProperties(bootstrapServers: String, groupId: String): Properties {
    val props = consumerProperties(bootstrapServers, groupId)
    
    // Enable unstable APIs for KIP-932 features
    props.put("unstable.api.versions.enable", "true")
    
    // KIP-932 specific configuration
    props.put(ConsumerConfig.SHARE_ACKNOWLEDGEMENT_MODE_CONFIG, "explicit")
    
    // Performance tuning for queue-based consumption
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100")
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "10000")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    
    return props
}
```

### Key KIP-932 Features Demonstrated

1. **Explicit Acknowledgment**: 
   - `AcknowledgeType.ACCEPT` - Message processed successfully
   - `AcknowledgeType.RELEASE` - Message failed processing, can be retried

2. **Cooperative Consumption**:
   - 6 AlertProcessor instances running simultaneously
   - All consumers share the same `SHARE_GROUP_ID = "alert-processor"`
   - Work is distributed across all available consumers

3. **Load Balancing**:
   - Each consumer processes records independently
   - Failed messages are released back to the queue for retry
   - No single point of failure

## System Orchestration

The `IotAlertCoordinator` orchestrates the entire system:

```kotlin
class IotAlertCoordinator : Runnable {
    override fun run() {
        // 1. Populate device subscriptions
        populateDeviceSubscriptions()
        
        // 2. Start alert generator
        val alertGenerator = AlertGenerator(sampleDevices, cacheManager)
        Thread(alertGenerator).start()
        
        // 3. Start 6 alert processors
        for (i in 1..6) {
            val processor = AlertProcessor(i)
            Thread(processor).start()
        }
    }
}
```

## Data Models

### Alert Model

```kotlin
@Serializable
data class Alert(
    val deviceId: String,
    val message: String,
    val timestamp: Long,
    val alertType: AlertType,
    val recipients: List<Recipient>
)
```

### Recipient Model

```kotlin
@Serializable
data class Recipient(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val preferredMethod: Preference
)

enum class Preference(val value: String) {
    EMAIL("Email"),
    SMS("SMS"),
    PUSH_NOTIFICATION("Push Notification")
}
```

## Running the System

To run the IoT Alerts demonstration:

1. **Start Kafka** (ensure it's running on `localhost:9092`)
    ```bash
    docker compose up -d
    ```
2. **Run the application**:
   ```bash
   ./mvnw exec:java -Dexec.mainClass="dev.sandonjacobs.kafka.QueuesLightningTalkExamples"
   ```

The system will:
- Generate device subscriptions
- Start producing random alerts every 100ms for 5 minutes
- Launch 6 consumer instances using KIP-932 Queues
- Process alerts cooperatively across all consumers

## Benefits of KIP-932 Queues

1. **Improved Throughput**: Multiple consumers can process the same partition
2. **Better Fault Tolerance**: Failed messages are automatically retried
3. **Dynamic Scaling**: Consumers can be added/removed without rebalancing
4. **Fine-grained Control**: Explicit acknowledgment provides better error handling
5. **Load Distribution**: Work is automatically balanced across available consumers

## Monitoring and Observability

The system includes comprehensive logging:

- **Alert Generation**: Logs each alert sent to Kafka
- **Alert Processing**: Logs each alert received and processed by consumer instances
- **Error Handling**: Logs processing failures with detailed error information
- **Cache Statistics**: Caffeine cache provides built-in statistics

## Conclusion

This IoT Alerts use case effectively demonstrates the power and flexibility of KIP-932 Queues for Kafka. The system shows how the new queue-based consumption model enables:

- **Cooperative processing** of IoT alerts across multiple consumer instances
- **Improved reliability** through explicit acknowledgment and retry mechanisms
- **Better scalability** with dynamic consumer management
- **Enhanced fault tolerance** for mission-critical IoT applications

The implementation provides a realistic simulation of an IoT alerting system while showcasing the advanced features of Kafka's new queue functionality.

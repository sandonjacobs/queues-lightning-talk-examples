package dev.sandonjacobs.kafka.iot.generator

import dev.sandonjacobs.kafka.iot.CacheManager
import dev.sandonjacobs.kafka.iot.IotAlertCoordinator.Companion.KAFKA_ALERT_TOPIC
import dev.sandonjacobs.kafka.iot.model.Alert
import dev.sandonjacobs.kafka.iot.model.AlertType
import dev.sandonjacobs.kafka.iot.model.Recipient
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AlertGenerator(val deviceIds: Set<String>, val cacheManager: CacheManager,
                     val sendIntervalMs: Long = 100L, val howLong: Duration = 5.minutes) : Runnable, AutoCloseable {

    private val logger = LoggerFactory.getLogger(AlertGenerator::class.java)

    private lateinit var kafkaProducer: KafkaProducer<String, String>

    @OptIn(ExperimentalTime::class)
    override fun run() {

        kafkaProducer = KafkaProducer<String, String>(dev.sandonjacobs.kafka.KafkaConfigurations.producerProperties(
            bootstrapServers = "localhost:9092",
            clientId = "iot-alert-generator"
        ))

        val stopTime = calcStopTime()
        while (Clock.System.now() < stopTime) {
            // for a random deviceId
            val deviceId = deviceIds.random()
            // lookup recipients from cache by deviceId
            val recipients = cacheManager.getRecipientsForDevice(deviceId)

            // generate random alerts for that deviceId and recipients
            val alert = generateRandomAlert(deviceId, recipients)
            // send to kafka
            kafkaProducer.send(ProducerRecord(KAFKA_ALERT_TOPIC, deviceId,Json.encodeToString(alert)))
            logger.info("Alert sent to Kafka for device {} : {}", deviceId, alert)
            // wait for the configured interval
            Thread.sleep(sendIntervalMs)
        }

        close()
    }

    /**
     * Generates a single random Alert for the given device ID.
     *
     * @param deviceId The device ID to generate an alert for
     * @return A randomly generated Alert object
     */
    private fun generateRandomAlert(deviceId: String, recipients: List<Recipient>): Alert {
        val alertType = AlertType.values().random()
        val message = generateRandomMessage(alertType)
        val timestamp = System.currentTimeMillis()

        return Alert(
            deviceId = deviceId,
            message = message,
            timestamp = timestamp,
            alertType = alertType,
            recipients = recipients
        )
    }

    /**
     * Generates a random message based on the alert type.
     */
    private fun generateRandomMessage(alertType: AlertType): String {
        val messages = when (alertType) {
            AlertType.TEMPERATURE -> listOf(
                "Temperature threshold exceeded: ${Random.nextInt(80, 120)}°F",
                "Critical temperature reading: ${Random.nextInt(90, 110)}°F",
                "Temperature anomaly detected: ${Random.nextInt(75, 95)}°F"
            )

            AlertType.HUMIDITY -> listOf(
                "Humidity level critical: ${Random.nextInt(80, 100)}%",
                "High humidity detected: ${Random.nextInt(70, 95)}%",
                "Humidity threshold exceeded: ${Random.nextInt(75, 90)}%"
            )

            AlertType.PRESSURE -> listOf(
                "Pressure reading abnormal: ${Random.nextInt(25, 35)} PSI",
                "Critical pressure level: ${Random.nextInt(20, 30)} PSI",
                "Pressure threshold exceeded: ${Random.nextInt(22, 32)} PSI"
            )

            AlertType.MOTION -> listOf(
                "Motion detected in restricted area",
                "Unauthorized movement detected",
                "Motion sensor triggered"
            )
        }
        return messages.random()
    }

    override fun close() {
        kafkaProducer.close()
    }

    @OptIn(ExperimentalTime::class)
    private fun calcStopTime(): Instant {
        val startTime = Clock.System.now()
        return startTime + howLong
    }
}
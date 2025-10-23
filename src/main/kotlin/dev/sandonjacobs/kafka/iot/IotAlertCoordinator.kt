package dev.sandonjacobs.kafka.iot

import dev.sandonjacobs.kafka.iot.generator.AlertGenerator
import dev.sandonjacobs.kafka.iot.model.Preference
import dev.sandonjacobs.kafka.iot.model.Recipient
import dev.sandonjacobs.kafka.iot.processor.AlertProcessor
import kotlin.random.Random

/**
 * Coordinator for IoT alerts that manages device subscriptions and alert generation.
 * Uses Caffeine cache to store recipient subscriptions for each device ID.
 */
class IotAlertCoordinator : Runnable {

    private val logger = org.slf4j.LoggerFactory.getLogger(IotAlertCoordinator::class.java)

    companion object {
        const val KAFKA_ALERT_TOPIC = "iot-alerts"
    }

    private val sampleDevices = setOf(
        "device-001", "device-002", "device-003", "device-004", "device-005",
        "device-006", "device-007", "device-008", "device-009", "device-010",
        "device-011", "device-012", "device-013", "device-014", "device-015",
        "device-016", "device-017", "device-018", "device-019", "device-020",
        "device-021", "device-022", "device-023", "device-024", "device-025"
    )

    private val sampleRecipients = listOf(
        Recipient("John Doe", "john.doe@example.com", "+1-555-0101", Preference.EMAIL),
        Recipient("Jane Smith", "jane.smith@example.com", "+1-555-0102", Preference.SMS),
        Recipient("Bob Johnson", "bob.johnson@example.com", "+1-555-0103", Preference.PUSH_NOTIFICATION),
        Recipient("Alice Brown", "alice.brown@example.com", "+1-555-0104", Preference.EMAIL),
        Recipient("Charlie Wilson", "charlie.wilson@example.com", "+1-555-0105", Preference.SMS),
        Recipient("Diana Prince", "diana.prince@example.com", "+1-555-0106", Preference.PUSH_NOTIFICATION),
        Recipient("Eve Adams", "eve.adams@example.com", "+1-555-0107", Preference.EMAIL),
        Recipient("Frank Miller", "frank.miller@example.com", "+1-555-0108", Preference.SMS)
    )


    private val cacheManager = CacheManager()

    private fun populateDeviceSubscriptions() {
        sampleDevices.forEach { device ->
            val numberOfRecipients = Random.nextInt(1, 3) // Each device has 1 to 3 recipients
            val recipients = sampleRecipients.shuffled().take(numberOfRecipients)
            cacheManager.populateDeviceSubscription(device, recipients)
        }}

    override fun run() {
        // populate the cache with sample device subscriptions
        populateDeviceSubscriptions()
        logger.info("Generated device subscriptions and populated cache.")

        // start the alert generator
        val alertGenerator = AlertGenerator(sampleDevices, cacheManager)
        logger.info("Starting Alert Generator...")
        Thread(alertGenerator).start()

        for (i in 1..6) {
            val processor = AlertProcessor(i)
            logger.info("Starting Alert Processor instance: id = {}", i)
            Thread(processor).start()
        }
    }
}

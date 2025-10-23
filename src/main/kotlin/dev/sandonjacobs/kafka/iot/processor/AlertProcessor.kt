package dev.sandonjacobs.kafka.iot.processor

import dev.sandonjacobs.kafka.KafkaConfigurations
import dev.sandonjacobs.kafka.iot.IotAlertCoordinator
import dev.sandonjacobs.kafka.iot.model.Alert
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.AcknowledgeType
import org.apache.kafka.clients.consumer.KafkaShareConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class AlertProcessor(val id: Int): Runnable, AutoCloseable {

    private val logger: Logger = LoggerFactory.getLogger(AlertProcessor::class.java)

    companion object {
        const val SHARE_GROUP_ID = "alert-processor"
    }

    private lateinit var kafkaShareConsumer: KafkaShareConsumer<String, String>

    override fun run() {
        kafkaShareConsumer = KafkaShareConsumer(
            KafkaConfigurations.shareConsumerProperties("localhost:9092", SHARE_GROUP_ID))

        kafkaShareConsumer.subscribe(listOf(IotAlertCoordinator.KAFKA_ALERT_TOPIC))
        while (true) {
            val records = kafkaShareConsumer.poll(Duration.ofMillis(1000))
            for (record in records) {
                try {
                    logger.info("Processor {} - Received alert from topic: {}, partition {}",
                        id, record.topic(), record.partition())
                    val alert = Json.decodeFromString<Alert>(record.value())
                    logger.info("Processing alert {}", alert)
                    kafkaShareConsumer.acknowledge(record, AcknowledgeType.ACCEPT)
                } catch (e: Exception) {
                    logger.error("Processor {} - Error processing record from topic: {}, partition {}",
                        id, record.topic(), record.partition(), e)
                    kafkaShareConsumer.acknowledge(record, AcknowledgeType.RELEASE)
                }
            }
        }
    }

    override fun close() {
        kafkaShareConsumer.close()
    }
}
package dev.sandonjacobs.kafka.example1.processor

import dev.sandonjacobs.kafka.KafkaConfigurations
import dev.sandonjacobs.kafka.example1.Example1Coordinator
import dev.sandonjacobs.kafka.example1.model.CohortCustomerKey
import dev.sandonjacobs.kafka.example1.model.CohortUpdatedEvent
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.AcknowledgeType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaShareConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties
import kotlin.time.ExperimentalTime

class CohortUpdateEventProcessor {

    private val logger = LoggerFactory.getLogger(CohortUpdateEventProcessor::class.java)

    private lateinit var kafkaProducer: KafkaProducer<String, String>


    @OptIn(ExperimentalTime::class)
    fun start(consumerThreads: Int = 6) {
        logger.info("*** Starting CohortUpdateEventProcessor with {} consumer threads", consumerThreads)
        // Initialize Kafka producer and consumer here
        val producerProperties = KafkaConfigurations.producerProperties(
            bootstrapServers = "localhost:9092",
            clientId = "example1-producer"
        )
        kafkaProducer = KafkaProducer(producerProperties)

        val consumerProperties: Properties = KafkaConfigurations.shareConsumerProperties(
            bootstrapServers = "localhost:9092",
            groupId = "example1-consumer-group"
        )
        repeat(consumerThreads) {
            val kafkaShareConsumer = KafkaShareConsumer<String, String>(consumerProperties)
            kafkaShareConsumer.subscribe(listOf(Example1Coordinator.Companion.COHORT_LOAD_TOPIC))

            Thread {
                while (true) {
                    val records = kafkaShareConsumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        Thread.currentThread().threadId()
                        logger.debug("Thread {} -> Consumed message: {} from topic: {}", Thread.currentThread().threadId(), record.value(), record.topic())
                        processRecord(record)
                        kafkaShareConsumer.acknowledge(record, AcknowledgeType.ACCEPT)
                    }
                }
            }.start()
        }
    }

    fun decodeInputEvent(record: ConsumerRecord<String, String>): CohortUpdatedEvent =
        Json.decodeFromString<CohortUpdatedEvent>(record.value())

    fun stop() {
        kafkaProducer.close()
    }

    fun processRecord(record: ConsumerRecord<String, String>) {
        val inputEvent = decodeInputEvent(record)
        inputEvent.toFileProcessCommands().forEach { fileProcessCommand ->
            kafkaProducer.send(
                ProducerRecord(
                    Example1Coordinator.COHORT_FILE_PROCESS_TOPIC,
                    Json.encodeToString(
                        CohortCustomerKey(
                            customerId = fileProcessCommand.customerId,
                            cohortId = fileProcessCommand.cohortId
                        )
                    ),
                    Json.encodeToString(fileProcessCommand)
                )
            )
            logger.info("CohortUpdateEventProcessor processed message : {}", inputEvent)
        }

    }

}
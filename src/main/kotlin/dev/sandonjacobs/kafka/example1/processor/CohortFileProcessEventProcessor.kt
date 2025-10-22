package dev.sandonjacobs.kafka.example1.processor

import dev.sandonjacobs.kafka.KafkaConfigurations
import dev.sandonjacobs.kafka.example1.Example1Coordinator
import dev.sandonjacobs.kafka.example1.model.CohortEntry
import dev.sandonjacobs.kafka.example1.model.CohortFileProcessCommand
import dev.sandonjacobs.kafka.example1.model.CohortMemberKey
import dev.sandonjacobs.kafka.example1.model.MemberToCohortCommand
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.kafka.clients.consumer.AcknowledgeType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaShareConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.Duration
import java.util.Properties
import kotlin.collections.forEach

class CohortFileProcessEventProcessor {

    private val logger = LoggerFactory.getLogger(CohortFileProcessEventProcessor::class.java)

    private lateinit var kafkaProducer: KafkaProducer<String, String>

    fun start(consumerThreads: Int = 9) {

        logger.info("*** Starting CohortFileProcessEventProcessor with {} consumer threads", consumerThreads)

        // Initialize Kafka producer and consumer here
        val producerProperties = KafkaConfigurations.producerProperties(
            bootstrapServers = "localhost:9092",
            clientId = "example1-producer"
        )
        kafkaProducer = KafkaProducer(producerProperties)

        val consumerProperties: Properties = KafkaConfigurations.shareConsumerProperties(
            bootstrapServers = "localhost:9092",
            groupId = "example1-file-processor"
        )

        repeat(consumerThreads) {
            val kafkaShareConsumer = KafkaShareConsumer<String, String>(consumerProperties)
            kafkaShareConsumer.subscribe(listOf(Example1Coordinator.Companion.COHORT_FILE_PROCESS_TOPIC))

            Thread {
                while (true) {
                    val records = kafkaShareConsumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        // Process each record here
                        logger.debug("Thread -> {} Consumed message: {} from topic: {}", Thread.currentThread().threadId(), record.value(), record.topic())
                        processRecord(record)
                        kafkaShareConsumer.acknowledge(record, AcknowledgeType.ACCEPT)
                    }
                }
            }.start()
        }
    }

    /**
     * Loads cohort entries from a JSON file located at the given file path.
     *
     * @param filePath The path to the JSON file containing cohort entries.
     * @return A list of [dev.sandonjacobs.kafka.example1.model.CohortEntry] objects if the file is found and parsed successfully; otherwise, null.
     */
    fun loadCohortEntriesFromFile(filePath: String): List<CohortEntry>? {
        val inputStream = loadFile(filePath) ?: return null
        return toCohortEntries(inputStream)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun toCohortEntries(inputStream: InputStream): List<CohortEntry> {
        val json = Json { ignoreUnknownKeys = true }

        val list: List<CohortEntry> = json.decodeFromStream(inputStream)

        return list
    }

    private fun loadFile(filePath: String): InputStream? = object {}.javaClass.getResourceAsStream(filePath)

    fun processRecord(record: ConsumerRecord<String, String>) {
        // Add your processing logic here
        val fileProcessCommand = Json.decodeFromString<CohortFileProcessCommand>(record.value())
        loadCohortEntriesFromFile(fileProcessCommand.fileLocation)?.forEach { member ->
            val cohortCommand = member.cohortEntryToCohortCommand(fileProcessCommand)
            val commandJson = Json.encodeToString(MemberToCohortCommand.serializer(), cohortCommand)
            kafkaProducer.send(
                ProducerRecord(
                    Example1Coordinator.Companion.COHORT_MEMBER_COMMAND_TOPIC,
                    Json.Default.encodeToString(
                        CohortMemberKey(
                            customerId = fileProcessCommand.customerId,
                            cohortId = fileProcessCommand.cohortId,
                            memberId = member.memberId
                        )
                    ),
                    commandJson
                )
            )
            logger.info("CohortFileProcessEventProcessor processed message : {}", commandJson)
        }
    }

}
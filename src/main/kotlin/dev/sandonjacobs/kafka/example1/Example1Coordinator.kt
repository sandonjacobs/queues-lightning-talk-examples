package dev.sandonjacobs.kafka.example1

import dev.sandonjacobs.kafka.KafkaConfigurations
import dev.sandonjacobs.kafka.example1.processor.CohortFileProcessEventProcessor
import dev.sandonjacobs.kafka.example1.processor.CohortUpdateEventProcessor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.time.ExperimentalTime

/**
 * Coordinator for Example 1 Kafka operations.
 * This class will handle the coordination of Kafka producers and consumers
 * for the first example in the lightning talk.
 */
class Example1Coordinator: Runnable {

    companion object {
        const val COHORT_LOAD_TOPIC = "cohort-load"
        const val COHORT_FILE_PROCESS_TOPIC = "cohort-file-process"
        const val COHORT_MEMBER_COMMAND_TOPIC = "cohort-member-command"
    }

    @OptIn(ExperimentalTime::class)
    override fun run() {
        val cohortUpdateEventProcessor = CohortUpdateEventProcessor()
        cohortUpdateEventProcessor.start()

        val cohortFileProcessEventProcessor = CohortFileProcessEventProcessor()
        cohortFileProcessEventProcessor.start()

        val kafkaProducer = KafkaProducer<String, String>(
            KafkaConfigurations.producerProperties(
                bootstrapServers = "localhost:9092",
                clientId = "example1-coordinator-producer"
            )
        )

        loadCustomerCohorts().map { path ->
            dev.sandonjacobs.kafka.example1.model.CohortUpdatedEvent(
                customerId = path.split("/")[3],
                cohortId = path.split("/")[5],
                updatedTs = kotlin.time.Clock.System.now(),
                fileLocations = listOf(
                    "$path/file_1.json",
                    "$path/file_2.json",
                    "$path/file_3.json"
                )
            )
        }.forEach { event ->

            println("Sending ${event} to topic $COHORT_LOAD_TOPIC")

            kafkaProducer.send(
                ProducerRecord(COHORT_LOAD_TOPIC,
                    Json.encodeToString(
                        dev.sandonjacobs.kafka.example1.model.CohortCustomerKey(
                            customerId = event.customerId,
                            cohortId = event.cohortId
                        )
                    ), Json.encodeToString(event)
                )
            )
        }
    }

    fun loadCustomerCohorts(): List<String> {
        val customerCohortsFile = object {}.javaClass.getResourceAsStream("/example1/customer_cohorts.json")

        return Json.parseToJsonElement(customerCohortsFile!!.reader(Charsets.UTF_8).readText()).jsonArray.map { it ->
            val customerId = it.jsonObject["customerId"]!!.toString().replace("\"", "")
            val cohortId = it.jsonObject["cohortId"]!!.toString().replace("\"", "")
            //println("Customer ID: $customerId, Cohort ID: $cohortId") }
            "/example1/customers/$customerId/cohorts/$cohortId"
        }
    }
}

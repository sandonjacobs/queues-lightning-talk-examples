package dev.sandonjacobs.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.util.*

/**
 * Utility class for creating Kafka producer and consumer configurations.
 */
object KafkaConfigurations {

    /**
     * Creates properties for a Kafka producer.
     * 
     * @param bootstrapServers The Kafka bootstrap servers
     * @param clientId The client ID for the producer
     * @param keySerializer The serializer class for message keys
     * @param valueSerializer The serializer class for message values
     * @return Properties configured for a Kafka producer
     */
    fun producerProperties(
        bootstrapServers: String,
        clientId: String,
        keySerializer: Class<out Serializer<*>> = org.apache.kafka.common.serialization.StringSerializer::class.java,
        valueSerializer: Class<out Serializer<*>> = org.apache.kafka.common.serialization.StringSerializer::class.java
    ): Properties {
        return Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("client.id", clientId)
            put("key.serializer", keySerializer.name)
            put("value.serializer", valueSerializer.name)
        }
    }

    /**
     * Creates properties for a Kafka consumer.
     * 
     * @param bootstrapServers The Kafka bootstrap servers
     * @param groupId The consumer group ID
     * @param keyDeserializer The deserializer class for message keys
     * @param valueDeserializer The deserializer class for message values
     * @return Properties configured for a Kafka consumer
     */
    fun consumerProperties(
        bootstrapServers: String,
        groupId: String,
        keyDeserializer: Class<out Deserializer<*>> = org.apache.kafka.common.serialization.StringDeserializer::class.java,
        valueDeserializer: Class<out Deserializer<*>> = org.apache.kafka.common.serialization.StringDeserializer::class.java
    ): Properties {
        return Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("group.id", groupId)
            put("key.deserializer", keyDeserializer.name)
            put("value.deserializer", valueDeserializer.name)
        }
    }

    fun shareConsumerProperties(bootstrapServers: String,
                                groupId: String,
                                keyDeserializer: Class<out Deserializer<*>> = org.apache.kafka.common.serialization.StringDeserializer::class.java,
                                valueDeserializer: Class<out Deserializer<*>> = org.apache.kafka.common.serialization.StringDeserializer::class.java): Properties {
        val props = consumerProperties(bootstrapServers, groupId, keyDeserializer, valueDeserializer)
        // TODO: Can I read from the VERY Beginning of time for a topic?
        props.put("share.group." + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        // Enable unstable APIs to access newer features like the queue protocol
        props.put("unstable.api.versions.enable", "true")
        // KIP-932 configuration for Kafka 4.0+
        props.put(ConsumerConfig.SHARE_ACKNOWLEDGEMENT_MODE_CONFIG, "explicit")
        // Process fewer records at a time for better load balancing
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100")
        // Use shorter poll intervals
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "10000")
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")

        return props
    }
}

package dev.sandonjacobs.kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.util.Properties

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
        keySerializer: Class<out Serializer<*>>,
        valueSerializer: Class<out Serializer<*>>
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
        keyDeserializer: Class<out Deserializer<*>>,
        valueDeserializer: Class<out Deserializer<*>>
    ): Properties {
        return Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("group.id", groupId)
            put("key.deserializer", keyDeserializer.name)
            put("value.deserializer", valueDeserializer.name)
        }
    }
}

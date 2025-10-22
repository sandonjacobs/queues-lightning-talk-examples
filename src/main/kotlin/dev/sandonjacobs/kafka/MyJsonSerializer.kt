package dev.sandonjacobs.kafka

import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class MyJsonSerializer<T> : org.apache.kafka.common.serialization.Serializer<T> {

    /** Serializes the given data to a JSON byte array.
     *
     * @param topic The topic associated with the data.
     * @param data The data to serialize.
     * @return A byte array representing the serialized JSON data, or null if the input data is null.
     */
    override fun serialize(topic: String?, data: T?): ByteArray? {
        if (data == null) return null
        return Json.encodeToString(data).toByteArray(StandardCharsets.UTF_8)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}

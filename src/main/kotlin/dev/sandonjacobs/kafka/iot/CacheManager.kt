package dev.sandonjacobs.kafka.iot

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sandonjacobs.kafka.iot.model.Recipient
import java.time.Duration

class CacheManager {

    /**
     * Cache mapping device IDs to their subscribed recipients.
     * Key: deviceId (String)
     * Value: List of Recipient objects subscribed to alerts for this device
     */
    private val deviceRecipientsCache: Cache<String, List<Recipient>> = Caffeine.newBuilder()
        .maximumSize(10_000) // Maximum number of device subscriptions to cache
        .expireAfterWrite(Duration.ofHours(24)) // Expire entries after 24 hours
        .recordStats() // Enable cache statistics
        .build()

    /**
     * Populates the cache with sample device subscriptions for demonstration purposes.
     * In a real application, this would load data from a database or external service.
     */
    fun populateDeviceSubscription(deviceId: String, recipients: List<Recipient>) = deviceRecipientsCache.put(deviceId, recipients)

    /**
     * Retrieves the list of recipients subscribed to alerts for a specific device.
     *
     * @param deviceId The device ID to look up
     * @return List of recipients subscribed to this device, or empty list if none found
     */
    fun getRecipientsForDevice(deviceId: String): List<Recipient> = deviceRecipientsCache.getIfPresent(deviceId) ?: emptyList()


}
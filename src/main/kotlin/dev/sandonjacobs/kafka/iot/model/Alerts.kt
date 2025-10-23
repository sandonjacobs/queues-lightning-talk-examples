package dev.sandonjacobs.kafka.iot.model

import kotlinx.serialization.Serializable

enum class AlertType(val value: String) {
    TEMPERATURE("Temperature"),
    HUMIDITY("Humidity"),
    PRESSURE("Pressure"),
    MOTION("Motion")
}

enum class Preference(val value: String) {
    EMAIL("Email"),
    SMS("SMS"),
    PUSH_NOTIFICATION("Push Notification")
}

@Serializable
data class Recipient(val name: String, val email: String, val phoneNumber: String, val preferredMethod: Preference)

@Serializable
data class Alert(val deviceId: String, val message: String, val timestamp: Long, val alertType: AlertType, val recipients: List<Recipient>)


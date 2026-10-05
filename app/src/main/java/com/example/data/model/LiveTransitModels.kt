package com.example.data.model

data class LiveVehicle(
    val id: String,
    val routeId: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Double = 0.0,
    val currentStatus: String = "IN_TRANSIT_TO",
    val stopName: String = "",
    val speedKmh: Double = 0.0,
    val occupancyStatus: String = "FEW_SEATS_AVAILABLE",
    val updatedAtEpochSeconds: Long = System.currentTimeMillis() / 1000
)

data class LiveSchedulePrediction(
    val id: String,
    val routeId: String,
    val tripId: String,
    val stopName: String,
    val destination: String,
    val scheduledTime: String,
    val estimatedArrivalSeconds: Long,
    val delayMinutes: Double,
    val statusText: String,
    val platform: String? = null
) {
    val countdownMinutes: Long
        get() {
            val nowSec = System.currentTimeMillis() / 1000
            val diffSec = estimatedArrivalSeconds - nowSec
            return if (diffSec <= 30) 0 else (diffSec / 60)
        }

    val displayCountdown: String
        get() = when {
            countdownMinutes <= 0 -> "ARRIVING"
            countdownMinutes == 1L -> "1 min"
            else -> "$countdownMinutes mins"
        }
}

data class LiveAgencyConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val description: String,
    val sampleRoutes: List<String>
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transit_routes")
data class TransitRoute(
    @PrimaryKey val routeId: String,
    val routeName: String,
    val transportType: String = "Bus", // Bus, Metro, BRT, Light Rail, Shuttle
    val totalDistanceKm: Double = 12.5,
    val avgTravelTimeMinutes: Int = 35,
    val stopsCount: Int = 10,
    val operatingHours: String = "06:00 - 23:00",
    val activeVehicles: Int = 6,
    val colorHex: String = "#00C9E0",
    val stopsJson: String = "" // JSON list of TransitStopInfo
)

data class TransitStopInfo(
    val name: String,
    val sequence: Int,
    val xOffsetNorm: Float, // Normalized 0..1 for Canvas map visualization
    val yOffsetNorm: Float,
    val isTransferHub: Boolean = false
)

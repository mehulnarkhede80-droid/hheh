package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transit_demand_records",
    indices = [
        Index(value = ["routeId"]),
        Index(value = ["hourOfDay"]),
        Index(value = ["timestamp"])
    ]
)
data class TransitDemandRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val stopName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dayOfWeek: String = "Monday", // Monday .. Sunday
    val hourOfDay: Int = 8, // 0 .. 23
    val passengerBoardings: Int = 45,
    val passengerAlightings: Int = 20,
    val currentOccupancy: Int = 55,
    val vehicleCapacity: Int = 80,
    val delayMinutes: Double = 3.5,
    val weatherCondition: String = "Clear", // Clear, Rain, Heavy Rain, Snow, Fog
    val isPeakHour: Boolean = false,
    val efficiencyScore: Double = 82.0, // 0..100
    val notes: String = ""
) {
    val loadFactorPercent: Double
        get() = if (vehicleCapacity > 0) (currentOccupancy.toDouble() / vehicleCapacity) * 100.0 else 0.0
}

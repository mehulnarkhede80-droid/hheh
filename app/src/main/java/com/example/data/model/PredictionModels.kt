package com.example.data.model

data class RouteEfficiencyStats(
    val routeId: String,
    val routeName: String,
    val transportType: String,
    val colorHex: String,
    val totalRecords: Int,
    val avgBoardings: Double,
    val avgOccupancy: Double,
    val avgLoadFactor: Double,
    val avgDelayMinutes: Double,
    val onTimeRatePercent: Double,
    val overallEfficiencyScore: Double, // 0..100
    val efficiencyGrade: String, // A+, A, B, C, D
    val totalPassengers: Int,
    val peakHourMaxDemand: Int,
    val carbonSavedKg: Double,
    val topBottleneckStop: String,
    val congestionLevel: String // "Optimal", "Moderate", "Severe Congestion"
)

data class DemandPredictionInput(
    val routeId: String,
    val targetHour: Int = 8,
    val targetDay: String = "Monday",
    val targetWeather: String = "Clear",
    val plannedHeadwayMinutes: Int = 10,
    val plannedVehicleCapacity: Int = 80
)

data class DemandPredictionResult(
    val routeId: String,
    val routeName: String,
    val targetHour: Int,
    val targetDay: String,
    val targetWeather: String,
    val predictedBoardings: Int,
    val predictedOccupancy: Int,
    val predictedLoadFactor: Double,
    val surgeRisk: String, // "Low Risk", "Moderate Demand", "High Peak Surge", "Critical Capacity Warning"
    val surgeRiskColorHex: String,
    val recommendedVehicles: Int,
    val recommendedHeadwayMinutes: Int,
    val estimatedDelayMinutes: Double,
    val confidencePercent: Int,
    val recommendedActions: List<String>,
    val hourlyForecastCurve: List<HourlyForecastPoint>
)

data class HourlyForecastPoint(
    val hour: Int,
    val predictedPassengers: Int,
    val historicalAvgPassengers: Int,
    val isPeak: Boolean
)

data class NetworkOverviewMetrics(
    val totalActiveRoutes: Int = 0,
    val totalRecordsAnalyzed: Int = 0,
    val averageNetworkEfficiency: Double = 0.0,
    val overallOnTimeRate: Double = 0.0,
    val totalPassengersServed: Int = 0,
    val highEfficiencyRouteCount: Int = 0,
    val bottleneckRoutesCount: Int = 0,
    val peakSurgeRouteId: String = ""
)

data class TransitDatasetExport(
    val metadata: Map<String, String>,
    val routes: List<TransitRoute>,
    val records: List<TransitDemandRecord>
)

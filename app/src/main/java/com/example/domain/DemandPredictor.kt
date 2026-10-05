package com.example.domain

import com.example.data.model.DemandPredictionInput
import com.example.data.model.DemandPredictionResult
import com.example.data.model.HourlyForecastPoint
import com.example.data.model.NetworkOverviewMetrics
import com.example.data.model.RouteEfficiencyStats
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object DemandPredictor {

    /**
     * Computes efficiency stats for each route based on historical demand records
     */
    fun computeRouteEfficiency(
        routes: List<TransitRoute>,
        records: List<TransitDemandRecord>
    ): List<RouteEfficiencyStats> {
        val recordsByRoute = records.groupBy { it.routeId }

        return routes.map { route ->
            val routeRecords = recordsByRoute[route.routeId] ?: emptyList()
            if (routeRecords.isEmpty()) {
                RouteEfficiencyStats(
                    routeId = route.routeId,
                    routeName = route.routeName,
                    transportType = route.transportType,
                    colorHex = route.colorHex,
                    totalRecords = 0,
                    avgBoardings = 0.0,
                    avgOccupancy = 0.0,
                    avgLoadFactor = 0.0,
                    avgDelayMinutes = 0.0,
                    onTimeRatePercent = 100.0,
                    overallEfficiencyScore = 75.0,
                    efficiencyGrade = "B",
                    totalPassengers = 0,
                    peakHourMaxDemand = 0,
                    carbonSavedKg = 0.0,
                    topBottleneckStop = "No recorded incidents",
                    congestionLevel = "Optimal"
                )
            } else {
                val totalPassengers = routeRecords.sumOf { it.passengerBoardings }
                val avgBoardings = routeRecords.map { it.passengerBoardings }.average()
                val avgOccupancy = routeRecords.map { it.currentOccupancy }.average()
                val avgLoadFactor = routeRecords.map { it.loadFactorPercent }.average()
                val avgDelay = routeRecords.map { it.delayMinutes }.average()
                val onTimeCount = routeRecords.count { it.delayMinutes <= 5.0 }
                val onTimeRate = (onTimeCount.toDouble() / routeRecords.size) * 100.0
                val avgEfficiency = routeRecords.map { it.efficiencyScore }.average()
                val maxPeakDemand = routeRecords.maxOfOrNull { it.currentOccupancy } ?: 0

                // Carbon savings calculation: 0.12 kg CO2 saved per passenger-km compared to single occupancy car
                val carbonSaved = (totalPassengers * (route.totalDistanceKm / max(1, route.stopsCount))) * 0.12

                // Identify top bottleneck stop
                val bottleneckStop = routeRecords
                    .groupBy { it.stopName }
                    .maxByOrNull { (_, stopRecs) -> stopRecs.map { it.delayMinutes }.average() }
                    ?.key ?: "None"

                val grade = when {
                    avgEfficiency >= 90.0 -> "A+"
                    avgEfficiency >= 80.0 -> "A"
                    avgEfficiency >= 70.0 -> "B"
                    avgEfficiency >= 60.0 -> "C"
                    else -> "D"
                }

                val congestion = when {
                    avgDelay >= 8.0 || avgLoadFactor > 92.0 -> "Severe Congestion"
                    avgDelay >= 4.0 || avgLoadFactor > 75.0 -> "Moderate"
                    else -> "Optimal"
                }

                RouteEfficiencyStats(
                    routeId = route.routeId,
                    routeName = route.routeName,
                    transportType = route.transportType,
                    colorHex = route.colorHex,
                    totalRecords = routeRecords.size,
                    avgBoardings = Math.round(avgBoardings * 10.0) / 10.0,
                    avgOccupancy = Math.round(avgOccupancy * 10.0) / 10.0,
                    avgLoadFactor = Math.round(avgLoadFactor * 10.0) / 10.0,
                    avgDelayMinutes = Math.round(avgDelay * 10.0) / 10.0,
                    onTimeRatePercent = Math.round(onTimeRate * 10.0) / 10.0,
                    overallEfficiencyScore = Math.round(avgEfficiency * 10.0) / 10.0,
                    efficiencyGrade = grade,
                    totalPassengers = totalPassengers,
                    peakHourMaxDemand = maxPeakDemand,
                    carbonSavedKg = Math.round(carbonSaved * 10.0) / 10.0,
                    topBottleneckStop = bottleneckStop,
                    congestionLevel = congestion
                )
            }
        }.sortedByDescending { it.overallEfficiencyScore }
    }

    /**
     * Aggregates high-level system analytics for the Dashboard
     */
    fun computeNetworkOverview(
        routes: List<TransitRoute>,
        records: List<TransitDemandRecord>
    ): NetworkOverviewMetrics {
        if (routes.isEmpty() || records.isEmpty()) {
            return NetworkOverviewMetrics()
        }

        val effStats = computeRouteEfficiency(routes, records)
        val avgEfficiency = effStats.map { it.overallEfficiencyScore }.average()
        val avgOnTime = effStats.map { it.onTimeRatePercent }.average()
        val totalPax = records.sumOf { it.passengerBoardings }
        val highEff = effStats.count { it.overallEfficiencyScore >= 80.0 }
        val bottlenecks = effStats.count { it.congestionLevel.contains("Severe") || it.avgDelayMinutes > 6.0 }
        val peakSurgeRoute = effStats.maxByOrNull { it.peakHourMaxDemand }?.routeId ?: "N/A"

        return NetworkOverviewMetrics(
            totalActiveRoutes = routes.size,
            totalRecordsAnalyzed = records.size,
            averageNetworkEfficiency = Math.round(avgEfficiency * 10.0) / 10.0,
            overallOnTimeRate = Math.round(avgOnTime * 10.0) / 10.0,
            totalPassengersServed = totalPax,
            highEfficiencyRouteCount = highEff,
            bottleneckRoutesCount = bottlenecks,
            peakSurgeRouteId = peakSurgeRoute
        )
    }

    /**
     * Predicts transit demand and recommends optimization actions
     */
    fun predictDemand(
        input: DemandPredictionInput,
        route: TransitRoute?,
        records: List<TransitDemandRecord>
    ): DemandPredictionResult {
        val routeId = input.routeId
        val routeName = route?.routeName ?: routeId
        val routeRecords = records.filter { it.routeId == routeId }

        // Hourly diurnal baseline multiplier
        val hourFactor = when (input.targetHour) {
            in 0..4 -> 0.15
            5 -> 0.45
            6 -> 0.90
            7 -> 1.70
            8 -> 2.10 // Peak morning
            9 -> 1.60
            10 -> 1.05
            in 11..13 -> 1.25 // Lunch / midday
            in 14..15 -> 1.10
            16 -> 1.45
            17 -> 2.05 // Peak evening
            18 -> 1.95
            19 -> 1.40
            20 -> 0.95
            21 -> 0.70
            22 -> 0.45
            else -> 0.30
        }

        // Day of week factor
        val dayFactor = when (input.targetDay.lowercase()) {
            "saturday" -> 0.85
            "sunday" -> 0.65
            "friday" -> 1.15
            "monday" -> 1.10
            else -> 1.0
        }

        // Weather impact factor
        val weatherFactor = when (input.targetWeather.lowercase()) {
            "heavy rain" -> 1.30
            "rain" -> 1.18
            "snow" -> 1.25
            "fog" -> 1.05
            else -> 1.0
        }

        // Calculate baseline from historical data
        val baseBoardings = if (routeRecords.isNotEmpty()) {
            val hourMatches = routeRecords.filter { it.hourOfDay == input.targetHour }
            if (hourMatches.isNotEmpty()) {
                hourMatches.map { it.passengerBoardings }.average()
            } else {
                routeRecords.map { it.passengerBoardings }.average() * (hourFactor / 1.3)
            }
        } else {
            35.0 * hourFactor
        }

        val predictedBoardings = max(5, (baseBoardings * dayFactor * (weatherFactor * 0.9 + 0.1)).roundToInt())
        val capacity = max(20, input.plannedVehicleCapacity)
        val predictedOccupancy = min(capacity, max(8, (predictedBoardings * 1.35).roundToInt()))
        val predictedLoadFactor = Math.round(((predictedOccupancy.toDouble() / capacity) * 100.0) * 10.0) / 10.0

        val estimatedDelay = when {
            input.targetWeather.contains("Heavy") -> 11.5
            input.targetWeather.contains("Rain") -> 6.5
            input.targetHour in 7..9 || input.targetHour in 17..19 -> 4.8
            else -> 1.8
        }

        // Surge Risk
        val (surgeRisk, riskColor) = when {
            predictedLoadFactor >= 92.0 -> Pair("Critical Capacity Warning", "#EF4444")
            predictedLoadFactor >= 78.0 -> Pair("High Peak Surge", "#F59E0B")
            predictedLoadFactor >= 55.0 -> Pair("Moderate Demand", "#06B6D4")
            else -> Pair("Low Risk / Underutilized", "#10B981")
        }

        // Optimization recommendations based on research
        // Headway & fleet calculation
        val baselineVehicles = route?.activeVehicles ?: 6
        val recommendedHeadway = when {
            predictedLoadFactor >= 90.0 -> max(4, (input.plannedHeadwayMinutes * 0.6).roundToInt())
            predictedLoadFactor >= 75.0 -> max(6, (input.plannedHeadwayMinutes * 0.8).roundToInt())
            predictedLoadFactor <= 40.0 -> min(25, (input.plannedHeadwayMinutes * 1.4).roundToInt())
            else -> input.plannedHeadwayMinutes
        }

        val recommendedVehicles = when {
            predictedLoadFactor >= 90.0 -> baselineVehicles + 3
            predictedLoadFactor >= 75.0 -> baselineVehicles + 1
            predictedLoadFactor <= 35.0 -> max(2, baselineVehicles - 2)
            else -> baselineVehicles
        }

        val actions = mutableListOf<String>()
        if (predictedLoadFactor >= 90.0) {
            actions.add("⚠️ Dispatch $recommendedVehicles vehicles with tightened ${recommendedHeadway}m headway to prevent platform overflow.")
            actions.add("🚌 Introduce short-turn express shuttles between high-boarding stations.")
        } else if (predictedLoadFactor >= 75.0) {
            actions.add("⚡ Operating in peak window. Maintain ${recommendedHeadway}m headway to preserve on-time punctuality.")
        } else if (predictedLoadFactor <= 40.0) {
            actions.add("🌱 Off-peak efficiency saving: Increase headway to ${recommendedHeadway}m or downsize to micro-transit vehicles to cut fuel emissions.")
        } else {
            actions.add("✅ Fleet allocation is balanced. Current capacity matches ridership demand closely.")
        }

        if (input.targetWeather.contains("Rain")) {
            actions.add("🌧️ Inclement weather alert: Buffer schedule by +${estimatedDelay.roundToInt()} mins at signalized intersections.")
        }

        // Generate 24-hour forecast curve
        val hourlyCurve = (0..23).map { h ->
            val hFac = when (h) {
                in 0..4 -> 0.15
                5 -> 0.45
                6 -> 0.90
                7 -> 1.70
                8 -> 2.10
                9 -> 1.60
                10 -> 1.05
                in 11..13 -> 1.25
                in 14..15 -> 1.10
                16 -> 1.45
                17 -> 2.05
                18 -> 1.95
                19 -> 1.40
                20 -> 0.95
                21 -> 0.70
                22 -> 0.45
                else -> 0.30
            }
            val histAvg = routeRecords.filter { it.hourOfDay == h }.map { it.passengerBoardings }.average().let {
                if (it.isNaN()) (30 * hFac).toInt() else it.roundToInt()
            }
            val pred = (histAvg * dayFactor * (weatherFactor * 0.9 + 0.1)).roundToInt()
            val isPeak = h in 7..9 || h in 17..19
            HourlyForecastPoint(
                hour = h,
                predictedPassengers = pred,
                historicalAvgPassengers = histAvg,
                isPeak = isPeak
            )
        }

        val confidence = when {
            routeRecords.size >= 50 -> 94
            routeRecords.size >= 20 -> 86
            routeRecords.size >= 5 -> 74
            else -> 62
        }

        return DemandPredictionResult(
            routeId = routeId,
            routeName = routeName,
            targetHour = input.targetHour,
            targetDay = input.targetDay,
            targetWeather = input.targetWeather,
            predictedBoardings = predictedBoardings,
            predictedOccupancy = predictedOccupancy,
            predictedLoadFactor = predictedLoadFactor,
            surgeRisk = surgeRisk,
            surgeRiskColorHex = riskColor,
            recommendedVehicles = recommendedVehicles,
            recommendedHeadwayMinutes = recommendedHeadway,
            estimatedDelayMinutes = estimatedDelay,
            confidencePercent = confidence,
            recommendedActions = actions,
            hourlyForecastCurve = hourlyCurve
        )
    }
}

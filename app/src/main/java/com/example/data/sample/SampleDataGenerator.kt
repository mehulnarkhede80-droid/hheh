package com.example.data.sample

import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object SampleDataGenerator {

    val sampleRoutes = listOf(
        TransitRoute(
            routeId = "RT-METRO-RED",
            routeName = "Metro Red Line (City Center - Tech Valley)",
            transportType = "Metro",
            totalDistanceKm = 24.5,
            avgTravelTimeMinutes = 42,
            stopsCount = 12,
            operatingHours = "05:00 - 00:30",
            activeVehicles = 14,
            colorHex = "#EF4444",
            stopsJson = """[
                {"name":"Grand Central","sequence":1,"xOffsetNorm":0.15,"yOffsetNorm":0.35,"isTransferHub":true},
                {"name":"Financial District","sequence":2,"xOffsetNorm":0.28,"yOffsetNorm":0.40,"isTransferHub":false},
                {"name":"Civic Plaza","sequence":3,"xOffsetNorm":0.42,"yOffsetNorm":0.45,"isTransferHub":true},
                {"name":"University North","sequence":4,"xOffsetNorm":0.58,"yOffsetNorm":0.48,"isTransferHub":false},
                {"name":"Medical Complex","sequence":5,"xOffsetNorm":0.72,"yOffsetNorm":0.50,"isTransferHub":false},
                {"name":"Innovation Hub","sequence":6,"xOffsetNorm":0.85,"yOffsetNorm":0.52,"isTransferHub":true}
            ]"""
        ),
        TransitRoute(
            routeId = "RT-METRO-BLUE",
            routeName = "Metro Blue Line (Harbor Waterfront - Airport)",
            transportType = "Metro",
            totalDistanceKm = 31.0,
            avgTravelTimeMinutes = 55,
            stopsCount = 15,
            operatingHours = "05:30 - 01:00",
            activeVehicles = 16,
            colorHex = "#3B82F6",
            stopsJson = """[
                {"name":"Port Terminal","sequence":1,"xOffsetNorm":0.12,"yOffsetNorm":0.75,"isTransferHub":false},
                {"name":"Bayside Pier","sequence":2,"xOffsetNorm":0.25,"yOffsetNorm":0.68,"isTransferHub":false},
                {"name":"Civic Plaza","sequence":3,"xOffsetNorm":0.42,"yOffsetNorm":0.45,"isTransferHub":true},
                {"name":"Eastwood Mall","sequence":4,"xOffsetNorm":0.62,"yOffsetNorm":0.35,"isTransferHub":false},
                {"name":"Terminal 1 Intl","sequence":5,"xOffsetNorm":0.88,"yOffsetNorm":0.22,"isTransferHub":true}
            ]"""
        ),
        TransitRoute(
            routeId = "RT-BUS-101",
            routeName = "Crosstown BRT 101 (Downtown - West Hills)",
            transportType = "BRT",
            totalDistanceKm = 16.8,
            avgTravelTimeMinutes = 38,
            stopsCount = 14,
            operatingHours = "06:00 - 23:00",
            activeVehicles = 10,
            colorHex = "#10B981",
            stopsJson = """[
                {"name":"Grand Central","sequence":1,"xOffsetNorm":0.15,"yOffsetNorm":0.35,"isTransferHub":true},
                {"name":"Market Street","sequence":2,"xOffsetNorm":0.30,"yOffsetNorm":0.28,"isTransferHub":false},
                {"name":"Maple Gardens","sequence":3,"xOffsetNorm":0.50,"yOffsetNorm":0.22,"isTransferHub":false},
                {"name":"Hillside Transit Center","sequence":4,"xOffsetNorm":0.75,"yOffsetNorm":0.18,"isTransferHub":true}
            ]"""
        ),
        TransitRoute(
            routeId = "RT-EXP-500",
            routeName = "Silicon Corridor Express 500",
            transportType = "Bus",
            totalDistanceKm = 28.2,
            avgTravelTimeMinutes = 48,
            stopsCount = 8,
            operatingHours = "06:30 - 21:30",
            activeVehicles = 8,
            colorHex = "#A855F7",
            stopsJson = """[
                {"name":"Financial District","sequence":1,"xOffsetNorm":0.28,"yOffsetNorm":0.40,"isTransferHub":false},
                {"name":"High-Tech Parkway","sequence":2,"xOffsetNorm":0.60,"yOffsetNorm":0.62,"isTransferHub":false},
                {"name":"Innovation Hub","sequence":3,"xOffsetNorm":0.85,"yOffsetNorm":0.52,"isTransferHub":true}
            ]"""
        ),
        TransitRoute(
            routeId = "RT-SHUTTLE-12",
            routeName = "Campus & Medical District Circulator",
            transportType = "Shuttle",
            totalDistanceKm = 9.4,
            avgTravelTimeMinutes = 24,
            stopsCount = 9,
            operatingHours = "07:00 - 22:00",
            activeVehicles = 5,
            colorHex = "#F97316",
            stopsJson = """[
                {"name":"University North","sequence":1,"xOffsetNorm":0.58,"yOffsetNorm":0.48,"isTransferHub":false},
                {"name":"Student Union","sequence":2,"xOffsetNorm":0.65,"yOffsetNorm":0.56,"isTransferHub":false},
                {"name":"Medical Complex","sequence":3,"xOffsetNorm":0.72,"yOffsetNorm":0.50,"isTransferHub":false},
                {"name":"Research Park","sequence":4,"xOffsetNorm":0.68,"yOffsetNorm":0.42,"isTransferHub":false}
            ]"""
        )
    )

    fun generateMetropolitanDataset(): Pair<List<TransitRoute>, List<TransitDemandRecord>> {
        val routes = sampleRoutes
        val records = mutableListOf<TransitDemandRecord>()
        val random = Random(42)

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val weatherTypes = listOf("Clear", "Clear", "Clear", "Rain", "Clear", "Fog", "Rain")

        val baseStopsMap = mapOf(
            "RT-METRO-RED" to listOf("Grand Central", "Financial District", "Civic Plaza", "University North", "Innovation Hub"),
            "RT-METRO-BLUE" to listOf("Port Terminal", "Bayside Pier", "Civic Plaza", "Eastwood Mall", "Terminal 1 Intl"),
            "RT-BUS-101" to listOf("Grand Central", "Market Street", "Maple Gardens", "Hillside Transit Center"),
            "RT-EXP-500" to listOf("Financial District", "High-Tech Parkway", "Innovation Hub"),
            "RT-SHUTTLE-12" to listOf("University North", "Student Union", "Medical Complex")
        )

        val capacityMap = mapOf(
            "RT-METRO-RED" to 320,
            "RT-METRO-BLUE" to 320,
            "RT-BUS-101" to 85,
            "RT-EXP-500" to 60,
            "RT-SHUTTLE-12" to 40
        )

        // Generate realistic 24h curve points across weekdays and weekends
        for (route in routes) {
            val stops = baseStopsMap[route.routeId] ?: listOf("Stop 1", "Stop 2")
            val capacity = capacityMap[route.routeId] ?: 80

            for (dayIdx in days.indices) {
                val day = days[dayIdx]
                val weather = weatherTypes[dayIdx % weatherTypes.size]
                val isWeekend = day == "Saturday" || day == "Sunday"

                // Sample key hours: 6, 8, 9, 12, 14, 17, 18, 20, 22
                val hours = listOf(6, 7, 8, 9, 11, 12, 14, 16, 17, 18, 19, 21, 22)
                for (hour in hours) {
                    val isMorningPeak = hour in 7..9
                    val isEveningPeak = hour in 17..19
                    val isPeak = (isMorningPeak || isEveningPeak) && !isWeekend

                    for (stop in stops) {
                        var demandFactor = when {
                            isMorningPeak && !isWeekend -> 1.8
                            isEveningPeak && !isWeekend -> 1.75
                            isWeekend && hour in 11..16 -> 1.3
                            hour in 11..14 -> 1.1
                            hour >= 21 -> 0.4
                            else -> 0.75
                        }

                        if (weather == "Rain") demandFactor *= 1.15
                        if (stop.contains("Central") || stop.contains("Financial") || stop.contains("Plaza")) {
                            demandFactor *= 1.3
                        }

                        val maxPax = (capacity * 0.85).toInt()
                        val boardings = min(
                            capacity,
                            max(8, (capacity * 0.28 * demandFactor + random.nextInt(-5, 10)).toInt())
                        )
                        val alightings = min(
                            capacity,
                            max(5, (capacity * 0.22 * demandFactor + random.nextInt(-4, 8)).toInt())
                        )
                        val occupancy = min(
                            capacity,
                            max(12, (capacity * 0.50 * demandFactor + random.nextInt(-8, 12)).toInt())
                        )

                        var delay = when {
                            weather == "Rain" && isPeak -> random.nextDouble(6.0, 14.0)
                            isPeak -> random.nextDouble(2.0, 7.5)
                            weather == "Rain" -> random.nextDouble(3.0, 8.0)
                            else -> random.nextDouble(0.5, 3.0)
                        }
                        if (route.transportType == "Metro") delay *= 0.4 // Metro has dedicated right-of-way

                        // Calculate efficiency score (0..100)
                        // Good efficiency: high load factor (65-90%), low delay, smooth operations
                        val loadRatio = occupancy.toDouble() / capacity
                        val loadScore = when {
                            loadRatio in 0.60..0.88 -> 95.0
                            loadRatio in 0.40..0.60 -> 82.0
                            loadRatio > 0.90 -> 72.0 // Overcrowded
                            else -> 58.0 // Underutilized
                        }
                        val delayPenalty = min(35.0, delay * 3.0)
                        val weatherBonus = if (weather == "Rain" && delay < 5) 5.0 else 0.0
                        val efficiency = max(20.0, min(99.0, loadScore - delayPenalty + weatherBonus))

                        records.add(
                            TransitDemandRecord(
                                routeId = route.routeId,
                                stopName = stop,
                                timestamp = System.currentTimeMillis() - (random.nextInt(1, 14) * 86400000L) + (hour * 3600000L),
                                dayOfWeek = day,
                                hourOfDay = hour,
                                passengerBoardings = boardings,
                                passengerAlightings = alightings,
                                currentOccupancy = occupancy,
                                vehicleCapacity = capacity,
                                delayMinutes = Math.round(delay * 10.0) / 10.0,
                                weatherCondition = weather,
                                isPeakHour = isPeak,
                                efficiencyScore = Math.round(efficiency * 10.0) / 10.0,
                                notes = if (delay > 8.0) "Peak congestion slowdown at $stop" else "Normal operation"
                            )
                        )
                    }
                }
            }
        }

        return Pair(routes, records)
    }

    fun generateMonsoonCongestionDataset(): Pair<List<TransitRoute>, List<TransitDemandRecord>> {
        val (routes, baseRecords) = generateMetropolitanDataset()
        val monsoonRecords = baseRecords.map { r ->
            val monsoonDelay = r.delayMinutes * 1.8 + 4.0
            val surgeBoardings = (r.passengerBoardings * 1.25).toInt()
            val surgeOccupancy = min(r.vehicleCapacity, (r.currentOccupancy * 1.20).toInt())
            val monsoonEfficiency = max(22.0, r.efficiencyScore - 18.0)
            r.copy(
                weatherCondition = "Heavy Rain",
                delayMinutes = Math.round(monsoonDelay * 10.0) / 10.0,
                passengerBoardings = surgeBoardings,
                currentOccupancy = surgeOccupancy,
                efficiencyScore = Math.round(monsoonEfficiency * 10.0) / 10.0,
                notes = "Heavy rain surge: waterlogged corridor, +${Math.round(monsoonDelay)}m delay"
            )
        }
        return Pair(routes, monsoonRecords)
    }

    fun generateRegionalSuburbanDataset(): Pair<List<TransitRoute>, List<TransitDemandRecord>> {
        val suburbanRoutes = listOf(
            TransitRoute(
                routeId = "RT-SUB-NORTH",
                routeName = "Suburban North Park & Ride",
                transportType = "Bus",
                totalDistanceKm = 36.0,
                avgTravelTimeMinutes = 52,
                stopsCount = 6,
                operatingHours = "05:30 - 21:00",
                activeVehicles = 6,
                colorHex = "#06B6D4",
                stopsJson = """[
                    {"name":"North Suburb Hub","sequence":1,"xOffsetNorm":0.10,"yOffsetNorm":0.15,"isTransferHub":true},
                    {"name":"Parkway Junction","sequence":2,"xOffsetNorm":0.35,"yOffsetNorm":0.30,"isTransferHub":false},
                    {"name":"Expressway Gateway","sequence":3,"xOffsetNorm":0.65,"yOffsetNorm":0.50,"isTransferHub":false},
                    {"name":"Metro Exchange","sequence":4,"xOffsetNorm":0.85,"yOffsetNorm":0.75,"isTransferHub":true}
                ]"""
            ),
            TransitRoute(
                routeId = "RT-AIRPORT-FLYER",
                routeName = "Intercity Airport Direct Flyer",
                transportType = "BRT",
                totalDistanceKm = 40.5,
                avgTravelTimeMinutes = 45,
                stopsCount = 5,
                operatingHours = "04:30 - 01:30",
                activeVehicles = 8,
                colorHex = "#EC4899",
                stopsJson = """[
                    {"name":"Central Station","sequence":1,"xOffsetNorm":0.20,"yOffsetNorm":0.40,"isTransferHub":true},
                    {"name":"Convention Center","sequence":2,"xOffsetNorm":0.50,"yOffsetNorm":0.45,"isTransferHub":false},
                    {"name":"Airport Terminals","sequence":3,"xOffsetNorm":0.90,"yOffsetNorm":0.50,"isTransferHub":true}
                ]"""
            )
        )

        val records = mutableListOf<TransitDemandRecord>()
        val random = Random(99)
        for (route in suburbanRoutes) {
            val stops = if (route.routeId == "RT-SUB-NORTH") {
                listOf("North Suburb Hub", "Parkway Junction", "Expressway Gateway", "Metro Exchange")
            } else {
                listOf("Central Station", "Convention Center", "Airport Terminals")
            }
            val cap = 65
            for (hour in listOf(6, 7, 8, 9, 12, 16, 17, 18, 20)) {
                val isPeak = hour in 7..9 || hour in 17..18
                for (stop in stops) {
                    val boardings = if (isPeak) random.nextInt(35, 60) else random.nextInt(12, 30)
                    val occupancy = min(cap, (boardings * 1.15).toInt())
                    val delay = if (isPeak) random.nextDouble(4.0, 11.0) else random.nextDouble(1.0, 4.0)
                    val eff = if (isPeak) 84.0 else 76.0
                    records.add(
                        TransitDemandRecord(
                            routeId = route.routeId,
                            stopName = stop,
                            timestamp = System.currentTimeMillis() - (random.nextInt(1, 7) * 86400000L) + (hour * 3600000L),
                            dayOfWeek = "Tuesday",
                            hourOfDay = hour,
                            passengerBoardings = boardings,
                            passengerAlightings = (boardings * 0.8).toInt(),
                            currentOccupancy = occupancy,
                            vehicleCapacity = cap,
                            delayMinutes = Math.round(delay * 10.0) / 10.0,
                            weatherCondition = "Clear",
                            isPeakHour = isPeak,
                            efficiencyScore = eff,
                            notes = "Suburban commuter scheduled run"
                        )
                    )
                }
            }
        }
        return Pair(suburbanRoutes, records)
    }
}

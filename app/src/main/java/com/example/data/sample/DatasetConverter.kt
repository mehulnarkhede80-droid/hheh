package com.example.data.sample

import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import org.json.JSONArray
import org.json.JSONObject

object DatasetConverter {

    /**
     * Converts routes and demand records into formatted JSON string
     */
    fun exportToJson(routes: List<TransitRoute>, records: List<TransitDemandRecord>): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("routesCount", routes.size)
        root.put("recordsCount", records.size)

        val routesArray = JSONArray()
        for (r in routes) {
            val routeObj = JSONObject()
            routeObj.put("routeId", r.routeId)
            routeObj.put("routeName", r.routeName)
            routeObj.put("transportType", r.transportType)
            routeObj.put("totalDistanceKm", r.totalDistanceKm)
            routeObj.put("avgTravelTimeMinutes", r.avgTravelTimeMinutes)
            routeObj.put("stopsCount", r.stopsCount)
            routeObj.put("operatingHours", r.operatingHours)
            routeObj.put("activeVehicles", r.activeVehicles)
            routeObj.put("colorHex", r.colorHex)
            routeObj.put("stopsJson", r.stopsJson)
            routesArray.put(routeObj)
        }
        root.put("routes", routesArray)

        val recordsArray = JSONArray()
        for (rec in records) {
            val recObj = JSONObject()
            recObj.put("routeId", rec.routeId)
            recObj.put("stopName", rec.stopName)
            recObj.put("timestamp", rec.timestamp)
            recObj.put("dayOfWeek", rec.dayOfWeek)
            recObj.put("hourOfDay", rec.hourOfDay)
            recObj.put("passengerBoardings", rec.passengerBoardings)
            recObj.put("passengerAlightings", rec.passengerAlightings)
            recObj.put("currentOccupancy", rec.currentOccupancy)
            recObj.put("vehicleCapacity", rec.vehicleCapacity)
            recObj.put("delayMinutes", rec.delayMinutes)
            recObj.put("weatherCondition", rec.weatherCondition)
            recObj.put("isPeakHour", rec.isPeakHour)
            recObj.put("efficiencyScore", rec.efficiencyScore)
            recObj.put("notes", rec.notes)
            recordsArray.put(recObj)
        }
        root.put("records", recordsArray)

        return root.toString(2)
    }

    /**
     * Converts demand records into standard CSV format
     */
    fun exportToCsv(records: List<TransitDemandRecord>): String {
        val sb = StringBuilder()
        sb.append("routeId,stopName,timestamp,dayOfWeek,hourOfDay,passengerBoardings,passengerAlightings,currentOccupancy,vehicleCapacity,delayMinutes,weatherCondition,isPeakHour,efficiencyScore,notes\n")
        for (r in records) {
            sb.append("\"${r.routeId}\",")
            sb.append("\"${r.stopName}\",")
            sb.append("${r.timestamp},")
            sb.append("\"${r.dayOfWeek}\",")
            sb.append("${r.hourOfDay},")
            sb.append("${r.passengerBoardings},")
            sb.append("${r.passengerAlightings},")
            sb.append("${r.currentOccupancy},")
            sb.append("${r.vehicleCapacity},")
            sb.append("${r.delayMinutes},")
            sb.append("\"${r.weatherCondition}\",")
            sb.append("${r.isPeakHour},")
            sb.append("${r.efficiencyScore},")
            val cleanNotes = r.notes.replace("\"", "\"\"")
            sb.append("\"$cleanNotes\"\n")
        }
        return sb.toString()
    }

    /**
     * Parses JSON string into TransitRoute and TransitDemandRecord lists
     */
    fun parseJson(jsonStr: String): Pair<List<TransitRoute>, List<TransitDemandRecord>> {
        val root = JSONObject(jsonStr.trim())
        val routesList = mutableListOf<TransitRoute>()
        val recordsList = mutableListOf<TransitDemandRecord>()

        if (root.has("routes")) {
            val routesArray = root.getJSONArray("routes")
            for (i in 0 until routesArray.length()) {
                val r = routesArray.getJSONObject(i)
                routesList.add(
                    TransitRoute(
                        routeId = r.optString("routeId", "RT-${System.currentTimeMillis() % 1000}"),
                        routeName = r.optString("routeName", "Imported Route"),
                        transportType = r.optString("transportType", "Bus"),
                        totalDistanceKm = r.optDouble("totalDistanceKm", 15.0),
                        avgTravelTimeMinutes = r.optInt("avgTravelTimeMinutes", 35),
                        stopsCount = r.optInt("stopsCount", 10),
                        operatingHours = r.optString("operatingHours", "06:00 - 23:00"),
                        activeVehicles = r.optInt("activeVehicles", 6),
                        colorHex = r.optString("colorHex", "#00C9E0"),
                        stopsJson = r.optString("stopsJson", "")
                    )
                )
            }
        }

        if (root.has("records")) {
            val recordsArray = root.getJSONArray("records")
            for (i in 0 until recordsArray.length()) {
                val rec = recordsArray.getJSONObject(i)
                recordsList.add(
                    TransitDemandRecord(
                        routeId = rec.optString("routeId", "UNKNOWN"),
                        stopName = rec.optString("stopName", "Station"),
                        timestamp = rec.optLong("timestamp", System.currentTimeMillis()),
                        dayOfWeek = rec.optString("dayOfWeek", "Monday"),
                        hourOfDay = rec.optInt("hourOfDay", 8),
                        passengerBoardings = rec.optInt("passengerBoardings", 25),
                        passengerAlightings = rec.optInt("passengerAlightings", 15),
                        currentOccupancy = rec.optInt("currentOccupancy", 40),
                        vehicleCapacity = rec.optInt("vehicleCapacity", 80),
                        delayMinutes = rec.optDouble("delayMinutes", 2.0),
                        weatherCondition = rec.optString("weatherCondition", "Clear"),
                        isPeakHour = rec.optBoolean("isPeakHour", false),
                        efficiencyScore = rec.optDouble("efficiencyScore", 80.0),
                        notes = rec.optString("notes", "Imported record")
                    )
                )
            }
        }

        return Pair(routesList, recordsList)
    }

    /**
     * Parses CSV format of demand records
     */
    fun parseCsv(csvStr: String): List<TransitDemandRecord> {
        val lines = csvStr.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val header = lines.first().split(",").map { it.trim().removeSurrounding("\"").lowercase() }
        val routeIdIdx = header.indexOfFirst { it.contains("route") }
        val stopNameIdx = header.indexOfFirst { it.contains("stop") }
        val dayOfWeekIdx = header.indexOfFirst { it.contains("day") }
        val hourIdx = header.indexOfFirst { it.contains("hour") }
        val boardingsIdx = header.indexOfFirst { it.contains("board") || it.contains("pax") || it.contains("passenger") }
        val alightingsIdx = header.indexOfFirst { it.contains("alight") }
        val occupancyIdx = header.indexOfFirst { it.contains("occupan") || it.contains("load") }
        val capacityIdx = header.indexOfFirst { it.contains("capac") }
        val delayIdx = header.indexOfFirst { it.contains("delay") }
        val weatherIdx = header.indexOfFirst { it.contains("weather") }

        val result = mutableListOf<TransitDemandRecord>()
        for (i in 1 until lines.size) {
            val line = lines[i]
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) continue

            val routeId = cols.getOrNull(if (routeIdIdx >= 0) routeIdIdx else 0) ?: "RT-CUSTOM"
            val stopName = cols.getOrNull(if (stopNameIdx >= 0) stopNameIdx else 1) ?: "Stop A"
            val dayOfWeek = cols.getOrNull(if (dayOfWeekIdx >= 0) dayOfWeekIdx else 3) ?: "Monday"
            val hourOfDay = cols.getOrNull(if (hourIdx >= 0) hourIdx else 4)?.toIntOrNull() ?: 8
            val boardings = cols.getOrNull(if (boardingsIdx >= 0) boardingsIdx else 5)?.toIntOrNull() ?: 20
            val alightings = cols.getOrNull(if (alightingsIdx >= 0) alightingsIdx else 6)?.toIntOrNull() ?: 10
            val occupancy = cols.getOrNull(if (occupancyIdx >= 0) occupancyIdx else 7)?.toIntOrNull() ?: (boardings * 1.5).toInt()
            val capacity = cols.getOrNull(if (capacityIdx >= 0) capacityIdx else 8)?.toIntOrNull() ?: 80
            val delay = cols.getOrNull(if (delayIdx >= 0) delayIdx else 9)?.toDoubleOrNull() ?: 2.0
            val weather = cols.getOrNull(if (weatherIdx >= 0) weatherIdx else 10) ?: "Clear"

            val isPeak = hourOfDay in 7..9 || hourOfDay in 17..19
            val eff = kotlin.math.max(20.0, 95.0 - (delay * 3.0))

            result.add(
                TransitDemandRecord(
                    routeId = routeId,
                    stopName = stopName,
                    timestamp = System.currentTimeMillis() - (i * 3600000L),
                    dayOfWeek = dayOfWeek,
                    hourOfDay = hourOfDay,
                    passengerBoardings = boardings,
                    passengerAlightings = alightings,
                    currentOccupancy = occupancy,
                    vehicleCapacity = capacity,
                    delayMinutes = delay,
                    weatherCondition = weather,
                    isPeakHour = isPeak,
                    efficiencyScore = Math.round(eff * 10.0) / 10.0,
                    notes = "Imported from CSV"
                )
            )
        }
        return result
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim().removeSurrounding("\""))
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim().removeSurrounding("\""))
        return result
    }
}

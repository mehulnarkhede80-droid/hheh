package com.example.data.network

import com.example.data.model.LiveSchedulePrediction
import com.example.data.model.LiveVehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

object LiveTransitNetworkClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Default open public GTFS-RT feed (No API Key Required)
    const val DEFAULT_AGENCY_URL = "https://api-v3.mbta.dot.gov"

    /**
     * Fetch real-time active vehicles from public GTFS-RT endpoint
     */
    suspend fun fetchLiveVehicles(
        baseUrl: String = DEFAULT_AGENCY_URL,
        routeId: String? = null
    ): Result<List<LiveVehicle>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder("$baseUrl/vehicles?include=stop,route&page[limit]=40")
            if (!routeId.isNullOrBlank()) {
                val cleanRoute = routeId.removePrefix("RT-").replace("METRO-", "").replace("BUS-", "")
                urlBuilder.append("&filter[route]=$cleanRoute")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header("Accept", "application/vnd.api+json")
                .header("User-Agent", "TransitPulse-Android/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Transit API returned HTTP ${response.code}: $body"))
            }

            val vehicles = parseVehiclesJsonApi(body)
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch real-time schedule arrival predictions
     */
    suspend fun fetchLivePredictions(
        baseUrl: String = DEFAULT_AGENCY_URL,
        routeId: String? = null
    ): Result<List<LiveSchedulePrediction>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder("$baseUrl/predictions?include=stop,trip,route&sort=arrival_time&page[limit]=30")
            if (!routeId.isNullOrBlank()) {
                val cleanRoute = routeId.removePrefix("RT-").replace("METRO-", "").replace("BUS-", "")
                urlBuilder.append("&filter[route]=$cleanRoute")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header("Accept", "application/vnd.api+json")
                .header("User-Agent", "TransitPulse-Android/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Predictions API returned HTTP ${response.code}"))
            }

            val predictions = parsePredictionsJsonApi(body)
            Result.success(predictions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseVehiclesJsonApi(jsonString: String): List<LiveVehicle> {
        val root = JSONObject(jsonString)
        val dataArray = root.optJSONArray("data") ?: return emptyList()

        // Build included map for stops
        val includedStops = mutableMapOf<String, String>()
        val includedArray = root.optJSONArray("included")
        if (includedArray != null) {
            for (i in 0 until includedArray.length()) {
                val inc = includedArray.getJSONObject(i)
                if (inc.optString("type") == "stop") {
                    val stopId = inc.optString("id")
                    val attrs = inc.optJSONObject("attributes")
                    val stopName = attrs?.optString("name") ?: stopId
                    includedStops[stopId] = stopName
                }
            }
        }

        val list = mutableListOf<LiveVehicle>()
        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            val id = item.optString("id", "veh-$i")
            val attrs = item.optJSONObject("attributes") ?: JSONObject()
            val rels = item.optJSONObject("relationships")

            val routeRel = rels?.optJSONObject("route")?.optJSONObject("data")
            val rId = routeRel?.optString("id") ?: "RED"

            val stopRel = rels?.optJSONObject("stop")?.optJSONObject("data")
            val sId = stopRel?.optString("id") ?: ""
            val stopName = includedStops[sId] ?: sId

            val lat = attrs.optDouble("latitude", 42.3601)
            val lng = attrs.optDouble("longitude", -71.0589)
            val bearing = attrs.optDouble("bearing", 0.0)
            val status = attrs.optString("current_status", "IN_TRANSIT_TO")
            val label = attrs.optString("label", id)
            val speed = attrs.optDouble("speed", 0.0)
            val occupancy = attrs.optString("occupancy_status", "FEW_SEATS_AVAILABLE")

            list.add(
                LiveVehicle(
                    id = id,
                    routeId = "RT-$rId",
                    label = label,
                    latitude = lat,
                    longitude = lng,
                    bearing = bearing,
                    currentStatus = status,
                    stopName = stopName.ifBlank { "Corridor Station" },
                    speedKmh = speed * 3.6,
                    occupancyStatus = occupancy
                )
            )
        }
        return list
    }

    private fun parsePredictionsJsonApi(jsonString: String): List<LiveSchedulePrediction> {
        val root = JSONObject(jsonString)
        val dataArray = root.optJSONArray("data") ?: return emptyList()

        // Build included map for stops and trips
        val includedStops = mutableMapOf<String, String>()
        val includedTrips = mutableMapOf<String, String>()
        val includedArray = root.optJSONArray("included")
        if (includedArray != null) {
            for (i in 0 until includedArray.length()) {
                val inc = includedArray.getJSONObject(i)
                val type = inc.optString("type")
                val incId = inc.optString("id")
                val attrs = inc.optJSONObject("attributes")

                if (type == "stop") {
                    val stopName = attrs?.optString("name") ?: incId
                    includedStops[incId] = stopName
                } else if (type == "trip") {
                    val headsign = attrs?.optString("headsign") ?: "Downtown"
                    includedTrips[incId] = headsign
                }
            }
        }

        val list = mutableListOf<LiveSchedulePrediction>()
        val nowSec = System.currentTimeMillis() / 1000

        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            val id = item.optString("id", "pred-$i")
            val attrs = item.optJSONObject("attributes") ?: JSONObject()
            val rels = item.optJSONObject("relationships")

            val routeRel = rels?.optJSONObject("route")?.optJSONObject("data")
            val rId = routeRel?.optString("id") ?: "RED"

            val stopRel = rels?.optJSONObject("stop")?.optJSONObject("data")
            val sId = stopRel?.optString("id") ?: ""
            val stopName = includedStops[sId] ?: sId.ifBlank { "Station #$i" }

            val tripRel = rels?.optJSONObject("trip")?.optJSONObject("data")
            val tId = tripRel?.optString("id") ?: ""
            val destination = includedTrips[tId] ?: "Terminus"

            val arrivalTimeStr = attrs.optString("arrival_time", "")
            val departureTimeStr = attrs.optString("departure_time", "")
            val targetTimeStr = arrivalTimeStr.ifBlank { departureTimeStr }

            var arrivalEpoch = nowSec + ((i + 1) * 180) // fallback estimation
            if (targetTimeStr.isNotBlank()) {
                try {
                    val parsed = OffsetDateTime.parse(targetTimeStr)
                    arrivalEpoch = parsed.toEpochSecond()
                } catch (_: Exception) {}
            }

            val statusText = attrs.optString("status", "On time")
            val platform = attrs.optString("platform_code").takeIf { it.isNotBlank() }

            val delayMinutes = when {
                statusText.contains("min late", ignoreCase = true) -> {
                    val regex = """(\d+)\s*min""".toRegex()
                    val match = regex.find(statusText)
                    match?.groupValues?.get(1)?.toDoubleOrNull() ?: 3.0
                }
                statusText.contains("Delayed", ignoreCase = true) -> 5.0
                else -> 0.0
            }

            list.add(
                LiveSchedulePrediction(
                    id = id,
                    routeId = "RT-$rId",
                    tripId = tId,
                    stopName = stopName,
                    destination = destination,
                    scheduledTime = targetTimeStr.takeLast(14).take(5),
                    estimatedArrivalSeconds = arrivalEpoch,
                    delayMinutes = delayMinutes,
                    statusText = statusText,
                    platform = platform
                )
            )
        }
        return list
    }
}

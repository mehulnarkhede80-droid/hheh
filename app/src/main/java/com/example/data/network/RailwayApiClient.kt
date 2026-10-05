package com.example.data.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class RailwayApiEndpoint(val path: String, val title: String) {
    TRAIN_SCHEDULE("trainSchedule", "Train schedule status"),
    TRAINS_BETWEEN_STATIONS("trainBetweenStations", "Trains between stations"),
    TRAIN_LIST("trainList", "Train list"),
    STATION_LINGUISTIC_NAMES("stationLinguisticNames", "Station linguistic names"),
    CLASS_AND_QUOTA("classAndQuota", "Classes and quota")
}

object RailwayApiClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun post(
        serviceOrigin: String,
        endpoint: RailwayApiEndpoint,
        requestJson: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.RAILWAY_API_KEY
            if (apiKey.isBlank() || apiKey == "YOUR_RAILWAY_API_KEY" || apiKey == "null") {
                return@withContext Result.failure(IllegalStateException("Configure RAILWAY_API_KEY in the local .env file and rebuild the app."))
            }

            val origin = serviceOrigin.trim().trimEnd('/')
            if (!origin.startsWith("https://") && !origin.startsWith("http://")) {
                return@withContext Result.failure(IllegalArgumentException("Enter the railway API service origin, including https://."))
            }

            val payload = try {
                JSONObject(requestJson)
            } catch (e: Exception) {
                return@withContext Result.failure(IllegalArgumentException("Request body must be a valid JSON object.", e))
            }

            val request = Request.Builder()
                .url("$origin/CRISApi/ws1/nget/${endpoint.path}")
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success(responseBody.ifBlank { "The API returned an empty response." })
                } else {
                    Result.failure(Exception("Railway API returned HTTP ${response.code}: ${responseBody.take(1200)}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
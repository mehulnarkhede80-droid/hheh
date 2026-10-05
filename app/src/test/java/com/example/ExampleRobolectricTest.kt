package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.gemini.GeminiApiClient
import com.example.data.gemini.GeminiModelTier
import com.example.data.model.DemandPredictionInput
import com.example.data.sample.DatasetConverter
import com.example.data.sample.SampleDataGenerator
import com.example.domain.DemandPredictor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TransitPulse", appName)
    }

    @Test
    fun `test demand prediction and route efficiency analytics`() {
        val (routes, records) = SampleDataGenerator.generateMetropolitanDataset()
        assertTrue(routes.isNotEmpty())
        assertTrue(records.isNotEmpty())

        val stats = DemandPredictor.computeRouteEfficiency(routes, records)
        assertTrue(stats.isNotEmpty())

        val redLine = stats.find { it.routeId == "RT-METRO-RED" }
        assertNotNull(redLine)
        assertTrue(redLine!!.totalPassengers > 0)

        // Test prediction model
        val input = DemandPredictionInput(
            routeId = "RT-METRO-RED",
            targetHour = 8,
            targetDay = "Monday",
            targetWeather = "Rain"
        )
        val result = DemandPredictor.predictDemand(input, routes.first(), records)
        assertNotNull(result)
        assertTrue(result.predictedBoardings > 0)
        assertTrue(result.hourlyForecastCurve.size == 24)
    }

    @Test
    fun `test json and csv dataset conversion`() {
        val (routes, records) = SampleDataGenerator.generateRegionalSuburbanDataset()
        val json = DatasetConverter.exportToJson(routes, records)
        assertTrue(json.contains("RT-SUB-NORTH"))

        val (parsedRoutes, parsedRecords) = DatasetConverter.parseJson(json)
        assertEquals(routes.size, parsedRoutes.size)
        assertEquals(records.size, parsedRecords.size)

        val csv = DatasetConverter.exportToCsv(records)
        assertTrue(csv.contains("routeId"))

        val parsedCsvRecords = DatasetConverter.parseCsv(csv)
        assertEquals(records.size, parsedCsvRecords.size)
    }

    @Test
    fun `test gemini chatbot local free engine fallback`() = runBlocking {
        val response = GeminiApiClient.sendChatMessage(
            history = emptyList(),
            userMessage = "Optimize morning rush headway for Metro Red Line",
            modelTier = GeminiModelTier.FLASH_GENERAL
        )
        assertNotNull(response)
        assertTrue(response.text.isNotBlank())
        assertTrue(response.text.contains("Free Tier") || response.text.contains("Headway") || response.text.contains("Peak"))
    }
}

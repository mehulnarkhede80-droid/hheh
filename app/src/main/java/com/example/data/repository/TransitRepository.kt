package com.example.data.repository

import com.example.data.local.TransitDao
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import com.example.data.sample.DatasetConverter
import com.example.data.sample.SampleDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransitRepository(private val transitDao: TransitDao) {

    val allRoutes: Flow<List<TransitRoute>> = transitDao.getAllRoutes()
    val allRecords: Flow<List<TransitDemandRecord>> = transitDao.getAllDemandRecords()

    fun getRecordsForRoute(routeId: String): Flow<List<TransitDemandRecord>> {
        return transitDao.getRecordsForRoute(routeId)
    }

    suspend fun ensureInitialDataSeeded() = withContext(Dispatchers.IO) {
        val count = transitDao.getRecordCount()
        if (count == 0) {
            val (routes, records) = SampleDataGenerator.generateMetropolitanDataset()
            transitDao.insertRoutes(routes)
            transitDao.insertDemandRecords(records)
        }
    }

    suspend fun insertDemandRecord(record: TransitDemandRecord): Long = withContext(Dispatchers.IO) {
        transitDao.insertDemandRecord(record)
    }

    suspend fun insertRoute(route: TransitRoute) = withContext(Dispatchers.IO) {
        transitDao.insertRoute(route)
    }

    suspend fun deleteRecordById(id: Long) = withContext(Dispatchers.IO) {
        transitDao.deleteDemandRecordById(id)
    }

    suspend fun deleteRouteById(routeId: String) = withContext(Dispatchers.IO) {
        transitDao.deleteRecordsForRoute(routeId)
        transitDao.deleteRouteById(routeId)
    }

    suspend fun injectDataset(datasetType: String) = withContext(Dispatchers.IO) {
        val (routes, records) = when (datasetType) {
            "monsoon" -> SampleDataGenerator.generateMonsoonCongestionDataset()
            "suburban" -> SampleDataGenerator.generateRegionalSuburbanDataset()
            else -> SampleDataGenerator.generateMetropolitanDataset()
        }
        transitDao.insertRoutes(routes)
        transitDao.insertDemandRecords(records)
    }

    suspend fun resetDatabaseToDefault() = withContext(Dispatchers.IO) {
        transitDao.clearAllDemandRecords()
        transitDao.clearAllRoutes()
        val (routes, records) = SampleDataGenerator.generateMetropolitanDataset()
        transitDao.insertRoutes(routes)
        transitDao.insertDemandRecords(records)
    }

    suspend fun importJsonData(jsonString: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val (routes, records) = DatasetConverter.parseJson(jsonString)
        if (routes.isNotEmpty()) {
            transitDao.insertRoutes(routes)
        }
        if (records.isNotEmpty()) {
            transitDao.insertDemandRecords(records)
        }
        Pair(routes.size, records.size)
    }

    suspend fun importCsvData(csvString: String, targetRouteId: String? = null): Int = withContext(Dispatchers.IO) {
        val parsedRecords = DatasetConverter.parseCsv(csvString)
        val finalRecords = if (!targetRouteId.isNullOrBlank()) {
            parsedRecords.map { it.copy(routeId = targetRouteId) }
        } else {
            parsedRecords
        }
        if (finalRecords.isNotEmpty()) {
            transitDao.insertDemandRecords(finalRecords)
        }
        finalRecords.size
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val routes = transitDao.getAllRoutesSync()
        val records = transitDao.getAllRecordsSync()
        DatasetConverter.exportToJson(routes, records)
    }

    suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val records = transitDao.getAllRecordsSync()
        DatasetConverter.exportToCsv(records)
    }
}

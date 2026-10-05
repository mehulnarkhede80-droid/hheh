package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TransitDatabase
import com.example.data.model.DemandPredictionInput
import com.example.data.model.DemandPredictionResult
import com.example.data.model.NetworkOverviewMetrics
import com.example.data.model.RouteEfficiencyStats
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import com.example.data.repository.TransitRepository
import com.example.domain.DemandPredictor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransitRepository
    val routes: StateFlow<List<TransitRoute>>
    val records: StateFlow<List<TransitDemandRecord>>

    val routeEfficiencyStats: StateFlow<List<RouteEfficiencyStats>>
    val networkOverview: StateFlow<NetworkOverviewMetrics>

    private val _selectedRouteId = MutableStateFlow<String>("")
    val selectedRouteId: StateFlow<String> = _selectedRouteId.asStateFlow()

    private val _predictionInput = MutableStateFlow(DemandPredictionInput(routeId = ""))
    val predictionInput: StateFlow<DemandPredictionInput> = _predictionInput.asStateFlow()

    val predictionResult: StateFlow<DemandPredictionResult?>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val database = TransitDatabase.getDatabase(application)
        repository = TransitRepository(database.transitDao())

        routes = repository.allRoutes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        records = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        routeEfficiencyStats = combine(routes, records) { rList, recList ->
            DemandPredictor.computeRouteEfficiency(rList, recList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        networkOverview = combine(routes, records) { rList, recList ->
            DemandPredictor.computeNetworkOverview(rList, recList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkOverviewMetrics()
        )

        predictionResult = combine(routes, records, _predictionInput) { rList, recList, input ->
            if (rList.isEmpty()) {
                null
            } else {
                val targetRouteId = if (input.routeId.isNotEmpty()) input.routeId else rList.first().routeId
                val targetRoute = rList.find { it.routeId == targetRouteId } ?: rList.first()
                val effectiveInput = if (input.routeId.isEmpty()) input.copy(routeId = targetRoute.routeId) else input
                DemandPredictor.predictDemand(effectiveInput, targetRoute, recList)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Seed initial data if database is empty
        viewModelScope.launch {
            _isLoading.value = true
            repository.ensureInitialDataSeeded()
            _isLoading.value = false
        }

        // Auto select first route when routes are loaded
        viewModelScope.launch {
            routes.collect { rList ->
                if (rList.isNotEmpty() && _selectedRouteId.value.isEmpty()) {
                    val firstId = rList.first().routeId
                    _selectedRouteId.value = firstId
                    _predictionInput.value = _predictionInput.value.copy(routeId = firstId)
                }
            }
        }
    }

    fun selectRoute(routeId: String) {
        _selectedRouteId.value = routeId
        _predictionInput.value = _predictionInput.value.copy(routeId = routeId)
    }

    fun updatePredictionInput(input: DemandPredictionInput) {
        _predictionInput.value = input
        if (input.routeId.isNotEmpty() && input.routeId != _selectedRouteId.value) {
            _selectedRouteId.value = input.routeId
        }
    }

    fun addNewRecord(record: TransitDemandRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.insertDemandRecord(record)
            _isLoading.value = false
            _userMessage.value = "New demand record logged for ${record.routeId}"
        }
    }

    fun addNewRoute(route: TransitRoute) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.insertRoute(route)
            _selectedRouteId.value = route.routeId
            _predictionInput.value = _predictionInput.value.copy(routeId = route.routeId)
            _isLoading.value = false
            _userMessage.value = "Route ${route.routeId} created"
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
            _userMessage.value = "Record deleted"
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            repository.deleteRouteById(routeId)
            _userMessage.value = "Route $routeId removed"
        }
    }

    fun injectDataset(datasetType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.injectDataset(datasetType)
            _isLoading.value = false
            _userMessage.value = "Injected dataset: $datasetType"
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.resetDatabaseToDefault()
            _isLoading.value = false
            _userMessage.value = "Database reset to baseline metropolitan dataset"
        }
    }

    fun importJson(jsonText: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val (routesCount, recordsCount) = repository.importJsonData(jsonText)
                _isLoading.value = false
                _userMessage.value = "Imported $routesCount routes and $recordsCount demand records"
                onComplete(true, "Successfully imported $routesCount routes & $recordsCount records!")
            } catch (e: Exception) {
                _isLoading.value = false
                onComplete(false, "Import error: ${e.localizedMessage ?: "Invalid JSON"}")
            }
        }
    }

    fun importCsv(csvText: String, targetRouteId: String?, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val count = repository.importCsvData(csvText, targetRouteId)
                _isLoading.value = false
                _userMessage.value = "Imported $count records from CSV"
                onComplete(true, "Successfully imported $count records!")
            } catch (e: Exception) {
                _isLoading.value = false
                onComplete(false, "CSV parse error: ${e.localizedMessage ?: "Invalid CSV format"}")
            }
        }
    }

    fun exportJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataAsJson()
            onReady(json)
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportDataAsCsv()
            onReady(csv)
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}

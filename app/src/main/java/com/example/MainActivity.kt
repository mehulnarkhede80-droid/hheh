package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TransitViewModel
import com.example.ui.dialogs.AddRecordDialog
import com.example.ui.dialogs.AddRouteDialog
import com.example.ui.dialogs.ExportDatasetDialog
import com.example.ui.dialogs.ImportDatasetDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DataManagementScreen
import com.example.ui.screens.DemandPredictionScreen
import com.example.ui.screens.LiveTransitScreen
import com.example.ui.screens.RouteEfficiencyScreen
import com.example.ui.screens.TransitChatbotScreen
import com.example.ui.theme.TransitPulseTheme

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    LIVE_TRACKER("Live Radar", Icons.Filled.Radio, Icons.Outlined.Radio, "tab_live_radar"),
    EFFICIENCY("Efficiency", Icons.Filled.Speed, Icons.Outlined.Speed, "tab_efficiency"),
    PREDICTION("Predictor", Icons.Filled.AutoGraph, Icons.Outlined.AutoGraph, "tab_prediction"),
    AI_COPILOT("AI Copilot", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "tab_ai_copilot"),
    DATA_STUDIO("Data Studio", Icons.Filled.Storage, Icons.Outlined.Storage, "tab_data_studio")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TransitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TransitPulseTheme {
                TransitApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TransitApp(viewModel: TransitViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddRecordDialog by remember { mutableStateOf(false) }
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val efficiencyStats by viewModel.routeEfficiencyStats.collectAsStateWithLifecycle()
    val networkOverview by viewModel.networkOverview.collectAsStateWithLifecycle()
    val selectedRouteId by viewModel.selectedRouteId.collectAsStateWithLifecycle()
    val predictionInput by viewModel.predictionInput.collectAsStateWithLifecycle()
    val predictionResult by viewModel.predictionResult.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    // Live Transit State
    val liveVehicles by viewModel.liveVehicles.collectAsStateWithLifecycle()
    val livePredictions by viewModel.livePredictions.collectAsStateWithLifecycle()
    val isSyncingLive by viewModel.isSyncingLive.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastLiveSyncTimestamp.collectAsStateWithLifecycle()
    val liveSyncError by viewModel.liveSyncError.collectAsStateWithLifecycle()
    val liveAgencyUrl by viewModel.liveAgencyUrl.collectAsStateWithLifecycle()

    // Handle back button to return to overview tab if on secondary screens
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0B101D),
                contentColor = Color(0xFF00C9E0),
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                ScreenTab.entries.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF00C9E0),
                            indicatorColor = Color(0xFF00C9E0),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    routes = routes,
                    routeEfficiencyStats = efficiencyStats,
                    networkOverview = networkOverview,
                    selectedRouteId = selectedRouteId,
                    predictionResult = predictionResult,
                    onSelectRoute = { viewModel.selectRoute(it) },
                    onNavigateToSimulator = { currentTab = 3 },
                    onOpenAddRecord = { showAddRecordDialog = true },
                    onOpenImportDataset = { showImportDialog = true }
                )
                1 -> LiveTransitScreen(
                    routes = routes,
                    selectedRouteId = selectedRouteId,
                    liveVehicles = liveVehicles,
                    livePredictions = livePredictions,
                    isSyncing = isSyncingLive,
                    lastSyncTimestamp = lastSyncTimestamp,
                    syncError = liveSyncError,
                    agencyUrl = liveAgencyUrl,
                    onSelectRoute = { viewModel.selectRoute(it) },
                    onSyncNow = { viewModel.syncLiveData(it) },
                    onIngestToDatabase = { viewModel.ingestLiveTelemetry() },
                    onSetAgencyUrl = { viewModel.setLiveAgencyUrl(it) }
                )
                2 -> RouteEfficiencyScreen(
                    routes = routes,
                    records = records,
                    efficiencyStats = efficiencyStats,
                    selectedRouteId = selectedRouteId,
                    onSelectRoute = { viewModel.selectRoute(it) }
                )
                3 -> DemandPredictionScreen(
                    routes = routes,
                    selectedRouteId = selectedRouteId,
                    predictionInput = predictionInput,
                    predictionResult = predictionResult,
                    onSelectRoute = { viewModel.selectRoute(it) },
                    onUpdateInput = { viewModel.updatePredictionInput(it) }
                )
                4 -> TransitChatbotScreen()
                5 -> DataManagementScreen(
                    routes = routes,
                    records = records,
                    onOpenAddRecord = { showAddRecordDialog = true },
                    onOpenAddRoute = { showAddRouteDialog = true },
                    onOpenImportDataset = { showImportDialog = true },
                    onOpenExportDataset = { showExportDialog = true },
                    onDeleteRecord = { viewModel.deleteRecord(it) },
                    onResetDatabase = { viewModel.resetToDefault() }
                )
            }
        }
    }

    // Modal Dialogs
    if (showAddRecordDialog) {
        AddRecordDialog(
            routes = routes,
            selectedRouteId = selectedRouteId,
            onDismiss = { showAddRecordDialog = false },
            onAddRecord = { rec -> viewModel.addNewRecord(rec) }
        )
    }

    if (showAddRouteDialog) {
        AddRouteDialog(
            onDismiss = { showAddRouteDialog = false },
            onAddRoute = { r -> viewModel.addNewRoute(r) }
        )
    }

    if (showImportDialog) {
        ImportDatasetDialog(
            onDismiss = { showImportDialog = false },
            onInjectPrebuilt = { type -> viewModel.injectDataset(type) },
            onImportJson = { json, cb -> viewModel.importJson(json, cb) },
            onImportCsv = { csv, cb -> viewModel.importCsv(csv, selectedRouteId, cb) }
        )
    }

    if (showExportDialog) {
        ExportDatasetDialog(
            onDismiss = { showExportDialog = false },
            onExportJson = { cb -> viewModel.exportJson(cb) },
            onExportCsv = { cb -> viewModel.exportCsv(cb) }
        )
    }
}

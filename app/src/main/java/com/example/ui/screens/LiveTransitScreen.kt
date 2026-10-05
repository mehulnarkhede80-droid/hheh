package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveSchedulePrediction
import com.example.data.model.LiveVehicle
import com.example.data.model.TransitRoute
import com.example.data.network.RailwayApiEndpoint
import com.example.ui.components.RailwayApiLookupPanel
import com.example.ui.components.parseColor
import kotlinx.coroutines.delay

@Composable
fun LiveTransitScreen(
    routes: List<TransitRoute>,
    selectedRouteId: String,
    liveVehicles: List<LiveVehicle>,
    livePredictions: List<LiveSchedulePrediction>,
    isSyncing: Boolean,
    lastSyncTimestamp: Long,
    syncError: String?,
    agencyUrl: String,
    onSelectRoute: (String) -> Unit,
    onSyncNow: (String?) -> Unit,
    onIngestToDatabase: () -> Unit,
    onSetAgencyUrl: (String) -> Unit,
    railwayApiResponse: String?,
    railwayApiError: String?,
    isRailwayApiLoading: Boolean,
    onRailwayLookup: (String, RailwayApiEndpoint, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterRoute by remember { mutableStateOf<String?>(null) }
    var showAgencyDialog by remember { mutableStateOf(false) }
    var customAgencyInput by remember { mutableStateOf(agencyUrl) }
    var activeTab by remember { mutableStateOf(0) } // 0: Live Schedules, 1: Live Vehicles Radar

    // Periodic auto-sync countdown (sync every 25s if on this screen)
    LaunchedEffect(Unit) {
        while (true) {
            delay(25000)
            onSyncNow(selectedFilterRoute)
        }
    }

    if (showAgencyDialog) {
        AlertDialog(
            onDismissRequest = { showAgencyDialog = false },
            title = {
                Text(
                    text = "Configure Live GTFS-RT Agency",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Connect to any open public General Transit Feed Specification Realtime (GTFS-RT) or transit REST endpoint.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Preset Transit Feeds:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C9E0))
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D2E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                customAgencyInput = "https://api-v3.mbta.dot.gov"
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("MBTA Boston Open Live GTFS-RT Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Real trains (Red, Orange, Blue) & buses • No key required", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customAgencyInput,
                        onValueChange = { customAgencyInput = it },
                        label = { Text("Agency API Base URL", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C9E0),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        onSetAgencyUrl(customAgencyInput)
                        showAgencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9E0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply & Sync", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgencyDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Live Sync Status
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isSyncing) Color(0xFF00C9E0) else Color(0xFF10B981), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Transit Radar",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lastSyncTimestamp > 0)
                            "Synchronized with live GTFS-RT feed"
                        else
                            "Connecting to public transit telemetry...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showAgencyDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configure Feed",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = { onSyncNow(selectedFilterRoute) },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("btn_sync_live")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF00C9E0),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = Color(0xFF00C9E0),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            RailwayApiLookupPanel(
                response = railwayApiResponse,
                error = railwayApiError,
                isLoading = isRailwayApiLoading,
                onLookup = onRailwayLookup
            )
        }

        // Live Telemetry Banner with Ingest Button
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(Color(0xFF00C9E0).copy(alpha = 0.4f), Color(0xFF1E293B)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0E3A4B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = Color(0xFF00C9E0),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Real-Time Telemetry Stream",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${liveVehicles.size} active vehicles • ${livePredictions.size} scheduled departures",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "REAL-TIME SYNC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onIngestToDatabase,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9E0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_ingest_live_telemetry")
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Live Telemetry to Analytics Engine",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Error Notice Banner if Sync Failed
        if (syncError != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF220810)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = syncError,
                            fontSize = 11.sp,
                            color = Color(0xFFFFCCD5),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Corridors Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAll = selectedFilterRoute == null
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAll) Color(0xFF00C9E0) else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAll) Color(0xFF00C9E0) else Color(0xFF24334A)
                        ),
                        modifier = Modifier.clickable {
                            selectedFilterRoute = null
                            onSyncNow(null)
                        }
                    ) {
                        Text(
                            text = "All Corridors",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAll) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                items(listOf("Red", "Orange", "Blue", "Green-B", "1")) { rName ->
                    val isSel = selectedFilterRoute == rName
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) Color(0xFF00C9E0) else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) Color(0xFF00C9E0) else Color(0xFF24334A)
                        ),
                        modifier = Modifier.clickable {
                            selectedFilterRoute = rName
                            onSyncNow(rName)
                        }
                    ) {
                        Text(
                            text = "$rName Line",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Sub-Tab Switcher: Live Departure Board vs Real-Time Vehicle Positions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeTab == 0) Color(0xFF00C9E0) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (activeTab == 0) Color.Black else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Departures Board",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 0) Color.Black else Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeTab == 1) Color(0xFF00C9E0) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsSubway,
                            contentDescription = null,
                            tint = if (activeTab == 1) Color.Black else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Vehicles",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 1) Color.Black else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Content
        if (activeTab == 0) {
            // Live Predictions / Departures
            if (livePredictions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DirectionsSubway, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No live departures currently scheduled", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap 'Sync' to refresh live GTFS-RT feed.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            } else {
                items(livePredictions, key = { it.id }) { pred ->
                    PredictionItemCard(prediction = pred)
                }
            }
        } else {
            // Active Live Vehicles
            if (liveVehicles.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No active vehicles on radar", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Vehicles will report as they broadcast live GTFS-RT coordinates.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            } else {
                items(liveVehicles, key = { it.id }) { veh ->
                    LiveVehicleCard(vehicle = veh)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PredictionItemCard(prediction: LiveSchedulePrediction) {
    val isDelayed = prediction.delayMinutes > 2.0
    val statusColor = if (isDelayed) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0E1726)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF00C9E0).copy(alpha = 0.20f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C9E0))
                    ) {
                        Text(
                            text = prediction.routeId.replace("RT-", ""),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00C9E0),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "to ${prediction.destination}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = prediction.stopName,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    prediction.platform?.let { p ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• Track $p", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = prediction.displayCountdown,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = prediction.statusText,
                    fontSize = 10.sp,
                    color = if (isDelayed) Color(0xFFFCA5A5) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun LiveVehicleCard(vehicle: LiveVehicle) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0E1726)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF0E3A4B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsSubway,
                        contentDescription = null,
                        tint = Color(0xFF00C9E0),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vehicle #${vehicle.label}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = vehicle.routeId.replace("RT-", ""),
                            fontSize = 10.sp,
                            color = Color(0xFF00C9E0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${vehicle.currentStatus.replace("_", " ")} ${vehicle.stopName}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "${vehicle.speedKmh.toInt()} km/h",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5F3FC),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vehicle.occupancyStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ImportDatasetDialog(
    onDismiss: () -> Unit,
    onInjectPrebuilt: (String) -> Unit,
    onImportJson: (String, (Boolean, String) -> Unit) -> Unit,
    onImportCsv: (String, (Boolean, String) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var rawInputText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("import_dataset_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF00C9E0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inject & Import Datasets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Load synthetic or empirical transit demand data",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF00C9E0),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF00C9E0)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Prebuilt Datasets", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Custom CSV / JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Prebuilt Cards
                    PrebuiltDatasetCard(
                        title = "Metropolitan Transit Core",
                        subtitle = "5 multimodal corridors • 100+ telemetry records • Peak commuter rush",
                        icon = Icons.Default.Dataset,
                        accentColor = Color(0xFF00C9E0),
                        onClick = {
                            onInjectPrebuilt("metropolitan")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrebuiltDatasetCard(
                        title = "Extreme Monsoon Congestion",
                        subtitle = "Surge delay spikes • High subway demand shift • Severe bottlenecks",
                        icon = Icons.Default.Thunderstorm,
                        accentColor = Color(0xFFEF4444),
                        onClick = {
                            onInjectPrebuilt("monsoon")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrebuiltDatasetCard(
                        title = "Regional Suburban Commuter",
                        subtitle = "Park & Ride corridors • Intercity Express Flyer • Low off-peak",
                        icon = Icons.Default.UploadFile,
                        accentColor = Color(0xFF10B981),
                        onClick = {
                            onInjectPrebuilt("suburban")
                            onDismiss()
                        }
                    )
                } else {
                    // Custom CSV / JSON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paste CSV or JSON payload:",
                            fontSize = 12.sp,
                            color = Color(0xFFA5F3FC),
                            fontWeight = FontWeight.SemiBold
                        )

                        // Sample filler button
                        OutlinedButton(
                            onClick = {
                                rawInputText = """routeId,stopName,timestamp,dayOfWeek,hourOfDay,passengerBoardings,passengerAlightings,currentOccupancy,vehicleCapacity,delayMinutes,weatherCondition
RT-BUS-101,Grand Central,1700000000000,Monday,8,64,12,72,85,6.2,Rain
RT-BUS-101,Market Street,1700003600000,Monday,9,48,22,60,85,4.1,Rain
RT-METRO-RED,Civic Plaza,1700000000000,Tuesday,8,120,45,280,320,1.2,Clear"""
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Insert Sample CSV", fontSize = 10.sp, color = Color(0xFF00C9E0))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = {
                            rawInputText = it
                            importStatusMessage = null
                        },
                        placeholder = { Text("Paste JSON dataset or CSV text here...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C9E0),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF060911),
                            unfocusedContainerColor = Color(0xFF060911)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("raw_dataset_input")
                    )

                    importStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (rawInputText.trim().startsWith("{") || rawInputText.trim().startsWith("[")) {
                                    onImportJson(rawInputText) { success, msg ->
                                        isSuccess = success
                                        importStatusMessage = msg
                                        if (success) {
                                            rawInputText = ""
                                        }
                                    }
                                } else {
                                    onImportCsv(rawInputText) { success, msg ->
                                        isSuccess = success
                                        importStatusMessage = msg
                                        if (success) {
                                            rawInputText = ""
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9E0)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("import_raw_dataset_btn")
                        ) {
                            Text("Parse & Inject Dataset", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrebuiltDatasetCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Text("LOAD", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

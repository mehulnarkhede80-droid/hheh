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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute

@Composable
fun AddRecordDialog(
    routes: List<TransitRoute>,
    selectedRouteId: String,
    onDismiss: () -> Unit,
    onAddRecord: (TransitDemandRecord) -> Unit
) {
    var routeId by remember { mutableStateOf(if (selectedRouteId.isNotEmpty()) selectedRouteId else routes.firstOrNull()?.routeId ?: "RT-101") }
    var stopName by remember { mutableStateOf("Central Plaza") }
    var dayOfWeek by remember { mutableStateOf("Monday") }
    var hourOfDay by remember { mutableIntStateOf(8) }
    var boardingsText by remember { mutableStateOf("45") }
    var alightingsText by remember { mutableStateOf("20") }
    var vehicleCapacityText by remember { mutableStateOf("80") }
    var delayMinutes by remember { mutableFloatStateOf(2.5f) }
    var weatherCondition by remember { mutableStateOf("Clear") }
    var notesText by remember { mutableStateOf("") }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val weatherOptions = listOf("Clear", "Rain", "Heavy Rain", "Fog", "Snow")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_record_dialog")
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
                    Text(
                        text = "Log Transit Demand",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Add new telemetry record to historical dataset",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Route Selection Chips
                Text("Select Route", color = Color(0xFFA5F3FC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    routes.take(4).forEach { r ->
                        val isSel = r.routeId == routeId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF00C9E0) else Color(0xFF1E293B),
                            modifier = Modifier
                                .clickable { routeId = r.routeId }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = r.routeId.replace("RT-", ""),
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stop Name Input
                OutlinedTextField(
                    value = stopName,
                    onValueChange = { stopName = it },
                    label = { Text("Stop / Station Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00C9E0),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_stop_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Day of Week
                Text("Day of Week", color = Color(0xFFA5F3FC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { d ->
                        val isSel = d == dayOfWeek
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) Color(0xFFF59E0B) else Color(0xFF1E293B),
                            modifier = Modifier.clickable { dayOfWeek = d }
                        ) {
                            Text(
                                text = d.take(3),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hour Slider
                Text(
                    text = "Hour of Day: ${String.format("%02d", hourOfDay)}:00",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = hourOfDay.toFloat(),
                    onValueChange = { hourOfDay = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00C9E0),
                        activeTrackColor = Color(0xFF00C9E0),
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("slider_hour")
                )

                // Passenger Counts Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = boardingsText,
                        onValueChange = { boardingsText = it },
                        label = { Text("Boardings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C9E0),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_boardings")
                    )
                    OutlinedTextField(
                        value = alightingsText,
                        onValueChange = { alightingsText = it },
                        label = { Text("Alightings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C9E0),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_alightings")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vehicle Capacity & Delay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = vehicleCapacityText,
                        onValueChange = { vehicleCapacityText = it },
                        label = { Text("Vehicle Cap.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C9E0),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delay: ${delayMinutes.toInt()} min",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = delayMinutes,
                            onValueChange = { delayMinutes = it },
                            valueRange = 0f..25f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFEF4444),
                                activeTrackColor = Color(0xFFEF4444),
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weather Condition
                Text("Weather Condition", color = Color(0xFFA5F3FC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weatherOptions.forEach { w ->
                        val isSel = w == weatherCondition
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF3B82F6) else Color(0xFF1E293B),
                            modifier = Modifier.clickable { weatherCondition = w }
                        ) {
                            Text(
                                text = w,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val boardings = boardingsText.toIntOrNull() ?: 30
                        val alightings = alightingsText.toIntOrNull() ?: 15
                        val cap = vehicleCapacityText.toIntOrNull() ?: 80
                        val occupancy = (boardings * 1.2).toInt().coerceAtMost(cap)
                        val isPeak = hourOfDay in 7..9 || hourOfDay in 17..19
                        val eff = (95.0 - (delayMinutes * 2.8)).coerceIn(20.0, 99.0)

                        val newRec = TransitDemandRecord(
                            routeId = routeId,
                            stopName = stopName.ifBlank { "Main Stop" },
                            timestamp = System.currentTimeMillis(),
                            dayOfWeek = dayOfWeek,
                            hourOfDay = hourOfDay,
                            passengerBoardings = boardings,
                            passengerAlightings = alightings,
                            currentOccupancy = occupancy,
                            vehicleCapacity = cap,
                            delayMinutes = Math.round(delayMinutes * 10.0) / 10.0,
                            weatherCondition = weatherCondition,
                            isPeakHour = isPeak,
                            efficiencyScore = Math.round(eff * 10.0) / 10.0,
                            notes = notesText.ifBlank { "User logged entry" }
                        )
                        onAddRecord(newRec)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_add_record_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9E0))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Telemetry Record", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

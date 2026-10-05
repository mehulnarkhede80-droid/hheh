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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DemandPredictionInput
import com.example.data.model.DemandPredictionResult
import com.example.data.model.TransitRoute
import com.example.ui.components.HourlyDemandChart
import com.example.ui.components.parseColor

@Composable
fun DemandPredictionScreen(
    routes: List<TransitRoute>,
    selectedRouteId: String,
    predictionInput: DemandPredictionInput,
    predictionResult: DemandPredictionResult?,
    onSelectRoute: (String) -> Unit,
    onUpdateInput: (DemandPredictionInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val weatherTypes = listOf("Clear", "Rain", "Heavy Rain", "Fog", "Snow")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transit Demand Predictor",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Diurnal regression & fleet allocation simulation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0E3A4B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C9E0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00C9E0),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${predictionResult?.confidencePercent ?: 90}% Confidence",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA5F3FC)
                        )
                    }
                }
            }
        }

        // Route Selector Chips with Google Stitch Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(routes) { r ->
                    val isSel = r.routeId == selectedRouteId
                    val rColor = parseColor(r.colorHex)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) rColor.copy(alpha = 0.22f) else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) rColor else Color(0xFF24334A)
                        ),
                        modifier = Modifier
                            .clickable {
                                onSelectRoute(r.routeId)
                                onUpdateInput(predictionInput.copy(routeId = r.routeId))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(rColor, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = r.routeId,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Interactive "What-If" Scenario Controls Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                ),
                modifier = Modifier.fillMaxWidth().testTag("simulation_controls_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF0E3A4B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF00C9E0), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Scenario Simulation Parameters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Day of Week
                    Text("Day of Week", fontSize = 12.sp, color = Color(0xFFA5F3FC), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        days.forEach { d ->
                            val isSel = d == predictionInput.targetDay
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0xFF00C9E0) else Color(0xFF131D2E),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) Color(0xFF00C9E0) else Color(0xFF24334A)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateInput(predictionInput.copy(targetDay = d)) }
                            ) {
                                Text(
                                    text = d.take(3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hour of Day Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Departure Hour", fontSize = 12.sp, color = Color(0xFFA5F3FC), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${String.format(java.util.Locale.US, "%02d", predictionInput.targetHour)}:00 ${if (predictionInput.targetHour in 7..9 || predictionInput.targetHour in 17..19) "🔥 PEAK SURGE" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (predictionInput.targetHour in 7..9 || predictionInput.targetHour in 17..19) Color(0xFFF59E0B) else Color(0xFF00C9E0)
                        )
                    }

                    Slider(
                        value = predictionInput.targetHour.toFloat(),
                        onValueChange = { onUpdateInput(predictionInput.copy(targetHour = it.toInt())) },
                        valueRange = 0f..23f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00C9E0),
                            activeTrackColor = Color(0xFF00C9E0),
                            inactiveTrackColor = Color(0xFF24334A)
                        ),
                        modifier = Modifier.testTag("prediction_hour_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weather Condition Chips
                    Text("Weather Condition", fontSize = 12.sp, color = Color(0xFFA5F3FC), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weatherTypes.forEach { w ->
                            val isSel = w == predictionInput.targetWeather
                            val wColor = when (w) {
                                "Rain", "Heavy Rain" -> Color(0xFF38BDF8)
                                "Snow" -> Color(0xFFA5F3FC)
                                else -> Color(0xFF10B981)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) wColor.copy(alpha = 0.25f) else Color(0xFF131D2E),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) wColor else Color(0xFF24334A)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateInput(predictionInput.copy(targetWeather = w)) }
                            ) {
                                Text(
                                    text = w,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dispatch Headway & Capacity Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scheduled Headway: ${predictionInput.plannedHeadwayMinutes} mins", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        Text("Vehicle Cap: ${predictionInput.plannedVehicleCapacity} pax", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = predictionInput.plannedHeadwayMinutes.toFloat(),
                        onValueChange = { onUpdateInput(predictionInput.copy(plannedHeadwayMinutes = it.toInt())) },
                        valueRange = 3f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF10B981),
                            activeTrackColor = Color(0xFF10B981),
                            inactiveTrackColor = Color(0xFF24334A)
                        )
                    )
                }
            }
        }

        // Real-Time Forecast Outcome Card
        item {
            predictionResult?.let { res ->
                val riskColor = parseColor(res.surgeRiskColorHex)

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.verticalGradient(listOf(riskColor.copy(alpha = 0.75f), Color(0xFF1E293B)))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("prediction_result_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Forecast Prediction Output",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = riskColor.copy(alpha = 0.20f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, riskColor)
                            ) {
                                Text(
                                    text = res.surgeRisk.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = riskColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary 3-Box Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PredictionMetricBox(
                                label = "Expected Boardings",
                                value = "${res.predictedBoardings}",
                                unit = "passengers",
                                color = Color(0xFF00C9E0),
                                modifier = Modifier.weight(1f)
                            )
                            PredictionMetricBox(
                                label = "Vehicle Occupancy",
                                value = "${res.predictedOccupancy}",
                                unit = "pax on board",
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            PredictionMetricBox(
                                label = "Capacity Load",
                                value = "${res.predictedLoadFactor.toInt()}%",
                                unit = "utilization",
                                color = riskColor,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Fleet Sizing Recommendation Bar
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF131D2E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Fleet Sizing Recommendation", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${res.recommendedVehicles} Vehicles • Headway ${res.recommendedHeadwayMinutes}m",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.20f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "OPTIMIZED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Operational Directives
                        Text(
                            text = "Model Optimization Directives",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFA5F3FC)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        res.recommendedActions.forEach { action ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF090D16),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = action,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full 24-Hour Forecast Curve
        item {
            predictionResult?.let { res ->
                HourlyDemandChart(
                    forecastPoints = res.hourlyForecastCurve,
                    selectedHour = res.targetHour,
                    onHourSelected = { h -> onUpdateInput(predictionInput.copy(targetHour = h)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PredictionMetricBox(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131D2E),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = unit, fontSize = 9.sp, color = Color(0xFF64748B))
        }
    }
}

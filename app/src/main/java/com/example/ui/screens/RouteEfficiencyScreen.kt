package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RouteEfficiencyStats
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import com.example.ui.components.RouteEfficiencyBarChart
import com.example.ui.components.parseColor

@Composable
fun RouteEfficiencyScreen(
    routes: List<TransitRoute>,
    records: List<TransitDemandRecord>,
    efficiencyStats: List<RouteEfficiencyStats>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRoute = remember(routes, selectedRouteId) {
        routes.find { it.routeId == selectedRouteId } ?: routes.firstOrNull()
    }
    val currentStats = remember(efficiencyStats, selectedRouteId) {
        efficiencyStats.find { it.routeId == selectedRouteId } ?: efficiencyStats.firstOrNull()
    }
    val routeRecords = remember(records, selectedRouteId) {
        records.filter { it.routeId == selectedRouteId }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Route Efficiency Analytics",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Dynamic load factor optimization, bottleneck detection & schedule adherence",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Route Selector Chips with Google Stitch Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("route_chips_row")
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
                        modifier = Modifier.clickable { onSelectRoute(r.routeId) }
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

        // Selected Route Overview Card
        item {
            currentRoute?.let { route ->
                val rColor = parseColor(route.colorHex)
                val stats = currentStats

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.verticalGradient(listOf(rColor.copy(alpha = 0.65f), Color(0xFF1E293B)))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("route_detail_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = rColor.copy(alpha = 0.20f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, rColor)
                                    ) {
                                        Text(
                                            text = route.transportType.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = rColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = route.routeId,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = route.routeName,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 18.sp
                                )
                            }

                            // Grade Pill
                            stats?.let { s ->
                                val gradeColor = when (s.efficiencyGrade) {
                                    "A+", "A" -> Color(0xFF10B981)
                                    "B" -> Color(0xFF00C9E0)
                                    "C" -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = gradeColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, gradeColor)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text("GRADE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = gradeColor.copy(alpha = 0.8f))
                                        Text(s.efficiencyGrade, fontSize = 20.sp, fontWeight = FontWeight.Black, color = gradeColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Efficiency Score Progress Bar
                        stats?.let { s ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Overall Efficiency Index", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                                    Text("${s.overallEfficiencyScore.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00C9E0))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .background(Color(0xFF1E293B), RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((s.overallEfficiencyScore / 100f).toFloat().coerceIn(0f, 1f))
                                            .height(10.dp)
                                            .background(
                                                Brush.horizontalGradient(listOf(Color(0xFF00C9E0), Color(0xFF10B981))),
                                                RoundedCornerShape(5.dp)
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 4 Detailed Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RouteMetricBox(
                                label = "Load Factor",
                                value = "${stats?.avgLoadFactor?.toInt() ?: 0}%",
                                subtitle = "Capacity utilization",
                                icon = Icons.Default.Speed,
                                color = Color(0xFF00C9E0),
                                modifier = Modifier.weight(1f)
                            )
                            RouteMetricBox(
                                label = "Punctuality",
                                value = "${stats?.onTimeRatePercent?.toInt() ?: 100}%",
                                subtitle = "Avg delay ${stats?.avgDelayMinutes ?: 0.0}m",
                                icon = Icons.Default.HourglassTop,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RouteMetricBox(
                                label = "Total Volume",
                                value = "${stats?.totalPassengers ?: 0}",
                                subtitle = "Recorded boardings",
                                icon = Icons.Default.People,
                                color = Color(0xFFA855F7),
                                modifier = Modifier.weight(1f)
                            )
                            RouteMetricBox(
                                label = "CO2 Offset",
                                value = "${stats?.carbonSavedKg?.toInt() ?: 0} kg",
                                subtitle = "Emissions saved",
                                icon = Icons.Default.EnergySavingsLeaf,
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Route Specs Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF060911), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SpecItem("Distance", "${route.totalDistanceKm} km")
                            SpecItem("Trip Time", "${route.avgTravelTimeMinutes} min")
                            SpecItem("Stops", "${route.stopsCount}")
                            SpecItem("Active Fleet", "${route.activeVehicles} units")
                        }
                    }
                }
            }
        }

        // Bottleneck Alert Card
        item {
            currentStats?.let { s ->
                val hasBottleneck = s.avgDelayMinutes > 4.5 || s.congestionLevel.contains("Congestion")
                val alertColor = if (hasBottleneck) Color(0xFFEF4444) else Color(0xFF10B981)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasBottleneck) Color(0xFF220810) else Color(0xFF081C14)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(alertColor.copy(alpha = 0.8f), Color.Transparent))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bottleneck_card")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(alertColor.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasBottleneck) Icons.Default.Warning else Icons.Default.EnergySavingsLeaf,
                                contentDescription = null,
                                tint = alertColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (hasBottleneck) "Corridor Bottleneck Identified" else "Optimal Flow Corridor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (hasBottleneck)
                                    "Choke point detected at '${s.topBottleneckStop}' with ${s.avgDelayMinutes}m average delay. Telemetry recommends inserting short-turn express buses."
                                else
                                    "Corridor operations running within optimal tolerance. Vehicle loads and dwell times are balanced.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Corridor Efficiency Ranking Bar Chart
        item {
            RouteEfficiencyBarChart(
                stats = efficiencyStats,
                onSelectRoute = onSelectRoute
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RouteMetricBox(
    label: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131D2E),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Route Efficiency Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Performance optimization, bottleneck detection & load factor",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Route Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("route_chips_row")
            ) {
                items(routes) { r ->
                    val isSel = r.routeId == selectedRouteId
                    val rColor = parseColor(r.colorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) rColor.copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) rColor else Color(0xFF334155)
                        ),
                        modifier = Modifier.clickable { onSelectRoute(r.routeId) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(rColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = r.routeId,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.verticalGradient(listOf(rColor.copy(alpha = 0.6f), Color(0xFF1E293B)))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("route_detail_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = rColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = route.transportType.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = rColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = route.routeId,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = route.routeName,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            // Grade Pill
                            stats?.let { s ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("GRADE", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA7F3D0))
                                        Text(s.efficiencyGrade, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Efficiency Progress Bar
                        stats?.let { s ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Overall Efficiency Score", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    Text("${s.overallEfficiencyScore.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C9E0))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Detailed Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Route Specs Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF060911), RoundedCornerShape(10.dp))
                                .padding(10.dp),
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

        // Bottleneck Warning Card
        item {
            currentStats?.let { s ->
                val hasBottleneck = s.avgDelayMinutes > 4.5 || s.congestionLevel.contains("Congestion")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasBottleneck) Color(0xFF280B14) else Color(0xFF0A2218)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                if (hasBottleneck) Color(0xFFEF4444) else Color(0xFF10B981),
                                Color.Transparent
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bottleneck_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    (if (hasBottleneck) Color(0xFFEF4444) else Color(0xFF10B981)).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasBottleneck) Icons.Default.Warning else Icons.Default.EnergySavingsLeaf,
                                contentDescription = null,
                                tint = if (hasBottleneck) Color(0xFFEF4444) else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (hasBottleneck) "Corridor Bottleneck Alert" else "Smooth Flow Corridor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (hasBottleneck)
                                    "Critical pinch point at '${s.topBottleneckStop}' with ${s.avgDelayMinutes}m avg delay. Recommend dispatching skip-stop feeder."
                                else
                                    "Corridor operations running within optimal tolerance. Zero critical choke points detected.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Corridor Efficiency Ranking
        item {
            RouteEfficiencyBarChart(
                stats = efficiencyStats,
                onSelectRoute = onSelectRoute
            )
        }

        // Recent Telemetry Sample for Selected Route
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Station Records ($selectedRouteId)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (routeRecords.isEmpty()) {
                        Text("No telemetry records recorded for this route yet.", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        routeRecords.take(4).forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = rec.stopName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    Text(text = "${rec.dayOfWeek} ${String.format("%02d", rec.hourOfDay)}:00 • ${rec.weatherCondition}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${rec.passengerBoardings} boardings", fontWeight = FontWeight.Bold, color = Color(0xFF00C9E0), fontSize = 12.sp)
                                    Text(text = "Delay: ${rec.delayMinutes}m", color = if (rec.delayMinutes > 5) Color(0xFFEF4444) else Color(0xFF10B981), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
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
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

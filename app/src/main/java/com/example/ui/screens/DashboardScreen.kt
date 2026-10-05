package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DemandPredictionResult
import com.example.data.model.NetworkOverviewMetrics
import com.example.data.model.RouteEfficiencyStats
import com.example.data.model.TransitRoute
import com.example.ui.components.HourlyDemandChart
import com.example.ui.components.RouteEfficiencyBarChart
import com.example.ui.components.TransitNetworkMap

@Composable
fun DashboardScreen(
    routes: List<TransitRoute>,
    routeEfficiencyStats: List<RouteEfficiencyStats>,
    networkOverview: NetworkOverviewMetrics,
    selectedRouteId: String,
    predictionResult: DemandPredictionResult?,
    onSelectRoute: (String) -> Unit,
    onNavigateToSimulator: () -> Unit,
    onOpenAddRecord: () -> Unit,
    onOpenImportDataset: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Hero Section with generated banner
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(Color(0xFF00C9E0).copy(alpha = 0.5f), Color(0xFF1E293B)))
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    // Hero Image
                    Image(
                        painter = painterResource(id = R.drawable.hero_banner),
                        contentDescription = "Transit Analytics Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                    )

                    // Scrim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x77000000),
                                        Color(0xDD090D16)
                                    )
                                )
                            )
                    )

                    // Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF00C9E0).copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C9E0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF00C9E0), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DEMAND & ROUTE OPTIMIZATION", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA5F3FC))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Text(
                                    text = "SYSTEM ONLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "TransitPulse Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Dynamic Route Efficiency & Demand Forecasting Platform",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // 2. High Level Metric Cards (2x2 Grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "Network Efficiency",
                    value = "${networkOverview.averageNetworkEfficiency.toInt()}%",
                    badge = if (networkOverview.averageNetworkEfficiency >= 80) "Optimal Grade A" else "Grade B",
                    badgeColor = Color(0xFF10B981),
                    icon = Icons.Default.Speed,
                    iconColor = Color(0xFF00C9E0),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "On-Time Rate",
                    value = "${networkOverview.overallOnTimeRate.toInt()}%",
                    badge = "Punctuality",
                    badgeColor = Color(0xFF00C9E0),
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "Passengers Served",
                    value = "${networkOverview.totalPassengersServed}",
                    badge = "${networkOverview.totalRecordsAnalyzed} records",
                    badgeColor = Color(0xFFA855F7),
                    icon = Icons.Default.DirectionsBus,
                    iconColor = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Active Corridors",
                    value = "${networkOverview.totalActiveRoutes}",
                    badge = "${networkOverview.highEfficiencyRouteCount} High Eff",
                    badgeColor = Color(0xFF10B981),
                    icon = Icons.Default.Timeline,
                    iconColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Quick Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToSimulator,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9E0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("action_run_simulator")
                ) {
                    Icon(Icons.Default.AutoGraph, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Predict Demand", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenAddRecord,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("action_log_telemetry")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Data", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // 4. Interactive Transit Network Map
        item {
            TransitNetworkMap(
                routes = routes,
                selectedRouteId = selectedRouteId,
                onSelectRoute = onSelectRoute
            )
        }

        // 5. 24-Hour Demand Forecast Curve
        item {
            predictionResult?.let { res ->
                HourlyDemandChart(
                    forecastPoints = res.hourlyForecastCurve,
                    selectedHour = res.targetHour,
                    onHourSelected = {}
                )
            }
        }

        // 6. Corridor Efficiency Leaderboard
        item {
            RouteEfficiencyBarChart(
                stats = routeEfficiencyStats,
                onSelectRoute = onSelectRoute
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricSummaryCard(
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

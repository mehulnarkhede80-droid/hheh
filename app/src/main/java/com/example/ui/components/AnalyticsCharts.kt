package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecastPoint
import com.example.data.model.RouteEfficiencyStats
import kotlin.math.max

@Composable
fun HourlyDemandChart(
    forecastPoints: List<HourlyForecastPoint>,
    selectedHour: Int,
    onHourSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedPoint by remember { mutableStateOf<HourlyForecastPoint?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_demand_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "24-Hour Ridership & Demand Curve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Historical baseline vs forecasted surge",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF00C9E0), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Predicted", fontSize = 10.sp, color = Color(0xFFA5F3FC))
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF64748B), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Historical", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF060911), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                if (forecastPoints.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No demand forecast data available", color = Color(0xFF64748B))
                    }
                } else {
                    val maxVal = remember(forecastPoints) {
                        max(50, forecastPoints.maxOf { max(it.predictedPassengers, it.historicalAvgPassengers) } + 15)
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(forecastPoints) {
                                detectTapGestures { tapOffset ->
                                    val count = forecastPoints.size
                                    if (count > 0) {
                                        val stepX = size.width / (count - 1).coerceAtLeast(1)
                                        val idx = (tapOffset.x / stepX).toInt().coerceIn(0, count - 1)
                                        val pt = forecastPoints[idx]
                                        inspectedPoint = pt
                                        onHourSelected(pt.hour)
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val count = forecastPoints.size
                        val stepX = w / (count - 1).coerceAtLeast(1)
                        val bottomPad = 24f
                        val topPad = 16f
                        val chartHeight = h - bottomPad - topPad

                        // 1. Draw peak hour background highlight zones (7-9 AM, 17-19 PM)
                        val peakBrush = Brush.verticalGradient(
                            listOf(Color(0xFFF59E0B).copy(alpha = 0.15f), Color.Transparent)
                        )
                        // Morning Peak (7 to 9)
                        val mStart = 7 * stepX
                        val mEnd = 9 * stepX
                        drawRect(
                            brush = peakBrush,
                            topLeft = Offset(mStart, 0f),
                            size = Size(mEnd - mStart, h - bottomPad)
                        )
                        // Evening Peak (17 to 19)
                        val eStart = 17 * stepX
                        val eEnd = 19 * stepX
                        drawRect(
                            brush = peakBrush,
                            topLeft = Offset(eStart, 0f),
                            size = Size(eEnd - eStart, h - bottomPad)
                        )

                        // 2. Draw horizontal guide lines
                        val steps = 3
                        for (i in 0..steps) {
                            val y = topPad + chartHeight * (i.toFloat() / steps)
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        // 3. Historical Path (Dashed Slate)
                        val histPath = Path()
                        forecastPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val normY = (pt.historicalAvgPassengers.toFloat() / maxVal).coerceIn(0f, 1f)
                            val y = topPad + chartHeight * (1f - normY)
                            if (i == 0) histPath.moveTo(x, y) else histPath.lineTo(x, y)
                        }
                        drawPath(
                            path = histPath,
                            color = Color(0xFF64748B),
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )

                        // 4. Predicted Area Gradient
                        val areaPath = Path()
                        forecastPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val normY = (pt.predictedPassengers.toFloat() / maxVal).coerceIn(0f, 1f)
                            val y = topPad + chartHeight * (1f - normY)
                            if (i == 0) {
                                areaPath.moveTo(x, h - bottomPad)
                                areaPath.lineTo(x, y)
                            } else {
                                areaPath.lineTo(x, y)
                            }
                        }
                        areaPath.lineTo(w, h - bottomPad)
                        areaPath.close()

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF00C9E0).copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // 5. Predicted Line
                        val predPath = Path()
                        forecastPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val normY = (pt.predictedPassengers.toFloat() / maxVal).coerceIn(0f, 1f)
                            val y = topPad + chartHeight * (1f - normY)
                            if (i == 0) predPath.moveTo(x, y) else predPath.lineTo(x, y)
                        }
                        drawPath(
                            path = predPath,
                            color = Color(0xFF00C9E0),
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )

                        // 6. Highlight selected hour
                        val selIdx = selectedHour.coerceIn(0, count - 1)
                        val selPt = forecastPoints[selIdx]
                        val selX = selIdx * stepX
                        val selNormY = (selPt.predictedPassengers.toFloat() / maxVal).coerceIn(0f, 1f)
                        val selY = topPad + chartHeight * (1f - selNormY)

                        drawLine(
                            color = Color(0xFFF59E0B),
                            start = Offset(selX, 0f),
                            end = Offset(selX, h - bottomPad),
                            strokeWidth = 2f
                        )

                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = 7f,
                            center = Offset(selX, selY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = Offset(selX, selY)
                        )
                    }

                    // Floating hour pill
                    val pt = inspectedPoint ?: forecastPoints.getOrNull(selectedHour)
                    pt?.let {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C9E0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%02d", it.hour)}:00 → ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${it.predictedPassengers} pax",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00C9E0)
                                    )
                                    if (it.isPeak) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "🔥 PEAK",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteEfficiencyBarChart(
    stats: List<RouteEfficiencyStats>,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("route_efficiency_bar_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Corridor Efficiency Ranking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Capacity Utilization vs Schedule Adherence Index",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(14.dp))

            stats.take(5).forEach { r ->
                val barProgress = (r.overallEfficiencyScore / 100.0).toFloat().coerceIn(0f, 1f)
                val barColor = when {
                    r.overallEfficiencyScore >= 85.0 -> Color(0xFF10B981)
                    r.overallEfficiencyScore >= 70.0 -> Color(0xFF00C9E0)
                    r.overallEfficiencyScore >= 55.0 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = r.routeId,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${r.transportType})",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = barColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Grade ${r.efficiencyGrade}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${r.overallEfficiencyScore.toInt()}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barProgress)
                                .height(8.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(barColor.copy(alpha = 0.7f), barColor)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Load: ${r.avgLoadFactor.toInt()}%",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Punctuality: ${r.onTimeRatePercent.toInt()}%",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

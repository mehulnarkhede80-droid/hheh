package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.TransitRoute
import com.example.data.model.TransitStopInfo
import org.json.JSONArray
import kotlin.math.sqrt

@Composable
fun TransitNetworkMap(
    routes: List<TransitRoute>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStopName by remember { mutableStateOf<String?>(null) }
    var selectedStopInfo by remember { mutableStateOf<TransitStopInfo?>(null) }
    var selectedRouteColor by remember { mutableStateOf(Color(0xFF00C9E0)) }
    var showHeatmapMode by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "map_telemetry_anim")

    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    // Moving vehicle animation along corridors
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transit_network_map_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1322)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(Color(0xFF00C9E0).copy(alpha = 0.35f), Color(0xFF1E293B)))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF0891B2), Color(0xFF0E3A4B))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subway,
                            contentDescription = "Network Map",
                            tint = Color(0xFF00C9E0),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Transit Mesh Corridors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Live telemetry • Interactive station nodes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Heatmap & Live Mode Toggle Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (showHeatmapMode) Color(0xFF452205) else Color(0xFF064E3B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showHeatmapMode) Color(0xFFF59E0B) else Color(0xFF10B981)
                    ),
                    modifier = Modifier.clickable { showHeatmapMode = !showHeatmapMode }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (showHeatmapMode) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showHeatmapMode) "HEATMAP" else "LIVE MESH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showHeatmapMode) Color(0xFFFDE68A) else Color(0xFFA7F3D0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Map Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .background(Color(0xFF060911), RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0E1726))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                // Parse stops per route
                val parsedRoutesWithStops = remember(routes) {
                    routes.map { r ->
                        val stopList = mutableListOf<TransitStopInfo>()
                        try {
                            if (r.stopsJson.isNotBlank()) {
                                val arr = JSONArray(r.stopsJson)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    stopList.add(
                                        TransitStopInfo(
                                            name = obj.getString("name"),
                                            sequence = obj.getInt("sequence"),
                                            xOffsetNorm = obj.getDouble("xOffsetNorm").toFloat(),
                                            yOffsetNorm = obj.getDouble("yOffsetNorm").toFloat(),
                                            isTransferHub = obj.optBoolean("isTransferHub", false)
                                        )
                                    )
                                }
                            }
                        } catch (_: Exception) {
                            stopList.add(TransitStopInfo("Origin", 1, 0.2f, 0.5f, true))
                            stopList.add(TransitStopInfo("Terminus", 2, 0.8f, 0.5f, true))
                        }
                        Triple(r, stopList, parseColor(r.colorHex))
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(parsedRoutesWithStops, selectedRouteId) {
                            detectTapGestures { tapOffset ->
                                val w = size.width
                                val h = size.height

                                var nearestStop: TransitStopInfo? = null
                                var nearestRoute: TransitRoute? = null
                                var nearestColor = Color(0xFF00C9E0)
                                var minDistance = Float.MAX_VALUE

                                for ((r, stopList, c) in parsedRoutesWithStops) {
                                    for (s in stopList) {
                                        val stopX = s.xOffsetNorm * w
                                        val stopY = s.yOffsetNorm * h
                                        val dx = stopX - tapOffset.x
                                        val dy = stopY - tapOffset.y
                                        val dist = sqrt(dx * dx + dy * dy)
                                        if (dist < minDistance) {
                                            minDistance = dist
                                            nearestStop = s
                                            nearestRoute = r
                                            nearestColor = c
                                        }
                                    }
                                }

                                if (minDistance < 65f && nearestStop != null && nearestRoute != null) {
                                    selectedStopName = nearestStop.name
                                    selectedStopInfo = nearestStop
                                    selectedRouteColor = nearestColor
                                    onSelectRoute(nearestRoute.routeId)
                                } else {
                                    selectedStopName = null
                                    selectedStopInfo = null
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 1. Sleek architectural grid
                    val gridCols = 8
                    val gridRows = 6
                    for (i in 1..gridCols) {
                        val gx = canvasWidth * (i.toFloat() / (gridCols + 1))
                        drawLine(
                            color = Color(0xFF131D2E),
                            start = Offset(gx, 0f),
                            end = Offset(gx, canvasHeight),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
                        )
                    }
                    for (j in 1..gridRows) {
                        val gy = canvasHeight * (j.toFloat() / (gridRows + 1))
                        drawLine(
                            color = Color(0xFF131D2E),
                            start = Offset(0f, gy),
                            end = Offset(canvasWidth, gy),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
                        )
                    }

                    // 2. Heatmap glow if activated
                    if (showHeatmapMode) {
                        for ((_, stops, _) in parsedRoutesWithStops) {
                            for (s in stops) {
                                val hx = s.xOffsetNorm * canvasWidth
                                val hy = s.yOffsetNorm * canvasHeight
                                val heatRadius = if (s.isTransferHub) 50f else 32f
                                val heatColor = if (s.isTransferHub) Color(0xFFEF4444) else Color(0xFFF59E0B)

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(heatColor.copy(alpha = 0.40f), Color.Transparent),
                                        center = Offset(hx, hy),
                                        radius = heatRadius
                                    ),
                                    radius = heatRadius,
                                    center = Offset(hx, hy)
                                )
                            }
                        }
                    }

                    // 3. Draw route tracks
                    for ((route, stops, color) in parsedRoutesWithStops) {
                        if (stops.size < 2) continue
                        val isSelected = route.routeId == selectedRouteId

                        val path = Path()
                        val first = stops.first()
                        path.moveTo(first.xOffsetNorm * canvasWidth, first.yOffsetNorm * canvasHeight)

                        for (i in 1 until stops.size) {
                            val curr = stops[i]
                            val prev = stops[i - 1]
                            val x1 = prev.xOffsetNorm * canvasWidth
                            val y1 = prev.yOffsetNorm * canvasHeight
                            val x2 = curr.xOffsetNorm * canvasWidth
                            val y2 = curr.yOffsetNorm * canvasHeight

                            val midX = (x1 + x2) / 2
                            path.cubicTo(midX, y1, midX, y2, x2, y2)
                        }

                        // Luminous glow for selected corridor
                        if (isSelected) {
                            drawPath(
                                path = path,
                                color = color.copy(alpha = 0.28f),
                                style = Stroke(width = 22f, cap = StrokeCap.Round)
                            )
                        }

                        // Corridor Line
                        drawPath(
                            path = path,
                            color = if (isSelected) color else color.copy(alpha = 0.50f),
                            style = Stroke(
                                width = if (isSelected) 8f else 4f,
                                cap = StrokeCap.Round
                            )
                        )

                        // 4. Moving vehicles along route
                        if (stops.size >= 2) {
                            val totalSegments = stops.size - 1
                            val scaledProgress = (vehicleProgress * totalSegments)
                            val segIdx = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
                            val segT = (scaledProgress - segIdx).coerceIn(0f, 1f)

                            val p1 = stops[segIdx]
                            val p2 = stops[segIdx + 1]

                            val vx = (p1.xOffsetNorm + (p2.xOffsetNorm - p1.xOffsetNorm) * segT) * canvasWidth
                            val vy = (p1.yOffsetNorm + (p2.yOffsetNorm - p1.yOffsetNorm) * segT) * canvasHeight

                            // Vehicle beacon
                            drawCircle(
                                color = Color.White,
                                radius = 5.5f,
                                center = Offset(vx, vy)
                            )
                            drawCircle(
                                color = color,
                                radius = 3f,
                                center = Offset(vx, vy)
                            )
                        }
                    }

                    // 5. Draw Station Nodes
                    for ((route, stops, color) in parsedRoutesWithStops) {
                        val isRouteSelected = route.routeId == selectedRouteId

                        for (stop in stops) {
                            val cx = stop.xOffsetNorm * canvasWidth
                            val cy = stop.yOffsetNorm * canvasHeight
                            val isSelectedStop = selectedStopName == stop.name

                            // Pulse animation for selected or hub
                            if ((stop.isTransferHub && isRouteSelected) || isSelectedStop) {
                                drawCircle(
                                    color = color.copy(alpha = (1f - pulseProgress) * 0.7f),
                                    radius = 12f + (pulseProgress * 20f),
                                    center = Offset(cx, cy)
                                )
                            }

                            // Outer ring
                            drawCircle(
                                color = if (stop.isTransferHub) Color.White else color,
                                radius = if (isSelectedStop) 10f else if (stop.isTransferHub) 8f else 5.5f,
                                center = Offset(cx, cy)
                            )

                            // Inner core
                            drawCircle(
                                color = if (isSelectedStop) color else Color(0xFF0F172A),
                                radius = if (isSelectedStop) 5.5f else if (stop.isTransferHub) 4.5f else 2.8f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // Interactive Bottom Station Sheet
                selectedStopInfo?.let { stop ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D2E).copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedRouteColor),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(selectedRouteColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stop.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (stop.isTransferHub) "Major Multimodal Transfer Hub • Station #${stop.sequence}" else "Standard Corridor Station • Stop #${stop.sequence}",
                                        fontSize = 11.sp,
                                        color = if (stop.isTransferHub) Color(0xFFA5F3FC) else Color(0xFF94A3B8)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedStopName = null
                                        selectedStopInfo = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(14.dp))

            // Corridor Quick Selector Chips with Google Stitch Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                routes.take(4).forEach { r ->
                    val isSelected = r.routeId == selectedRouteId
                    val routeColor = parseColor(r.colorHex)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) routeColor.copy(alpha = 0.22f) else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) routeColor else Color(0xFF24334A)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectRoute(r.routeId) }
                            .testTag("route_chip_${r.routeId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(routeColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = r.routeId.replace("RT-", ""),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        Color(0xFF00C9E0)
    }
}

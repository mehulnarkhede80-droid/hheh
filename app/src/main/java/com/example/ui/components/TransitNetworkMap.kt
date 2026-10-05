package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transit_network_map_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B1322)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            imageVector = Icons.Default.Subway,
                            contentDescription = "Network Map",
                            tint = Color(0xFF00C9E0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Interactive Transit Network",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Tap stops or lines to inspect corridor telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Live Pulse Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF064E3B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE MESH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Map Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFF060911), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
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
                            // fallback default stops if parse fails
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

                                // Find nearest stop
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

                                if (minDistance < 60f && nearestStop != null && nearestRoute != null) {
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

                    // 1. Draw subtle background coordinate grid
                    val gridCols = 6
                    val gridRows = 5
                    for (i in 1..gridCols) {
                        val gx = canvasWidth * (i.toFloat() / (gridCols + 1))
                        drawLine(
                            color = Color(0xFF131D2E),
                            start = Offset(gx, 0f),
                            end = Offset(gx, canvasHeight),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                    for (j in 1..gridRows) {
                        val gy = canvasHeight * (j.toFloat() / (gridRows + 1))
                        drawLine(
                            color = Color(0xFF131D2E),
                            start = Offset(0f, gy),
                            end = Offset(canvasWidth, gy),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // 2. Draw route lines
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

                            // Smooth cubic or quadratic curve for organic metro map look
                            val midX = (x1 + x2) / 2
                            path.cubicTo(midX, y1, midX, y2, x2, y2)
                        }

                        // Outer glowing aura for selected route
                        if (isSelected) {
                            drawPath(
                                path = path,
                                color = color.copy(alpha = 0.25f),
                                style = Stroke(width = 18f, cap = StrokeCap.Round)
                            )
                        }

                        // Main route track
                        drawPath(
                            path = path,
                            color = if (isSelected) color else color.copy(alpha = 0.55f),
                            style = Stroke(
                                width = if (isSelected) 8f else 4f,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 3. Draw stops / station nodes
                    for ((route, stops, color) in parsedRoutesWithStops) {
                        val isRouteSelected = route.routeId == selectedRouteId

                        for (stop in stops) {
                            val cx = stop.xOffsetNorm * canvasWidth
                            val cy = stop.yOffsetNorm * canvasHeight
                            val isSelectedStop = selectedStopName == stop.name

                            // Pulse animation for transfer hubs or selected stop
                            if ((stop.isTransferHub && isRouteSelected) || isSelectedStop) {
                                drawCircle(
                                    color = color.copy(alpha = (1f - pulseProgress) * 0.6f),
                                    radius = 12f + (pulseProgress * 16f),
                                    center = Offset(cx, cy)
                                )
                            }

                            // Outer node ring
                            drawCircle(
                                color = if (stop.isTransferHub) Color.White else color,
                                radius = if (isSelectedStop) 9f else if (stop.isTransferHub) 7.5f else 5f,
                                center = Offset(cx, cy)
                            )

                            // Inner core
                            drawCircle(
                                color = if (isSelectedStop) color else Color(0xFF0F172A),
                                radius = if (isSelectedStop) 5f else if (stop.isTransferHub) 4f else 2.5f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // Stop tooltip overlay if inspected
                selectedStopInfo?.let { stop ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            tonalElevation = 6.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, selectedRouteColor.copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(selectedRouteColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stop.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (stop.isTransferHub) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TRANSFER HUB",
                                        color = Color(0xFFA5F3FC),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route Selector Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                routes.take(4).forEach { r ->
                    val isSelected = r.routeId == selectedRouteId
                    val routeColor = parseColor(r.colorHex)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) routeColor.copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) routeColor else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectRoute(r.routeId) }
                            .testTag("route_chip_${r.routeId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(routeColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = r.routeId.replace("RT-", ""),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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

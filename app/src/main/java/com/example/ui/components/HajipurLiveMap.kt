package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HajipurLocations
import com.example.ui.theme.VegGreenBright
import kotlin.math.roundToInt

private const val MIN_LAT = 25.5600
private const val MAX_LAT = 25.8100
private const val MIN_LNG = 85.0800
private const val MAX_LNG = 85.3400

private val MapPinRed = Color(0xFFE31837)
private val MapRoadSlate = Color(0xFFCBD5E1)
private val MapRoadBorder = Color(0xFF94A3B8)

private data class MapPoiMarker(
    val titleEn: String,
    val titleHi: String,
    val lat: Double,
    val lng: Double,
    val isSchoolOrInstitute: Boolean = false
)

private val hajipurMapPois = listOf(
    MapPoiMarker("My Crush Pizza Hajipur", "माई क्रश पिज़्ज़ा हाजीपुर", 25.68342, 85.21804),
    MapPoiMarker("R.N. College, Chauhatta", "आर.एन. कॉलेज, चौहट्टा", 25.6838, 85.2176, isSchoolOrInstitute = true),
    MapPoiMarker("Manoj villa", "मनोज विला", 25.6904, 85.2148),
    MapPoiMarker("TRW", "टीआरडब्ल्यू", 25.6890, 85.2124, isSchoolOrInstitute = true),
    MapPoiMarker("Sanjeev Kumar Das", "संजीव कुमार दास", 25.6871, 85.2152),
    MapPoiMarker("Shahi Colony", "शाही कॉलोनी", 25.6886, 85.2132),
    MapPoiMarker("Cinema Road", "सिनेमा रोड", 25.6850, 85.2044),
    MapPoiMarker("Dakbunglow Chowk", "डाकबंगला चौक", 25.6879, 85.2118),
    MapPoiMarker("Station Road", "स्टेशन रोड", 25.6858, 85.2095),
    MapPoiMarker("Dighikala East", "दिघीकला ईस्ट", 25.7045, 85.2260),
    MapPoiMarker("Zonal Office Rd", "ज़ोनल ऑफिस रोड", 25.6872, 85.2105),
    MapPoiMarker("Anjanpeer Chowk", "अंजनपीर चौक", 25.6940, 85.2080),
    MapPoiMarker("Gandhi Chowk", "गांधी चौक", 25.6836, 85.2062),
    MapPoiMarker("Ramaashish Chowk", "रामाशीष चौक", 25.6982, 85.2215)
)

/**
 * Resolves the nearest Hajipur locality/colony/street for any latitude & longitude as the user scrolls the map.
 */
fun resolveLocationAddressFromCoordinates(lat: Double, lng: Double): Pair<String, String> {
    val distFromStoreKm = HajipurLocations.calculateDistanceKm(
        HajipurLocations.KITCHEN_LAT,
        HajipurLocations.KITCHEN_LNG,
        lat,
        lng
    )
    if (!HajipurLocations.isWithinDeliveryRange(distFromStoreKm)) {
        return "Outside 10 km Range ($distFromStoreKm km)" to
            "Out of Delivery Range ($distFromStoreKm km from My Crush Pizza Hajipur • Max 10 km)"
    }
    val nearest = HajipurLocations.landmarks.minByOrNull {
        HajipurLocations.calculateDistanceKm(lat, lng, it.lat, it.lng)
    } ?: HajipurLocations.landmarks.first()
    val distMeters = (HajipurLocations.calculateDistanceKm(lat, lng, nearest.lat, nearest.lng) * 1000).roundToInt()
    return if (distMeters <= 350) {
        nearest.name to nearest.addressLine
    } else {
        "Near ${nearest.name}, Hajipur" to nearest.addressLine
    }
}

/**
 * Large Interactive 3D-Building Architectural Delivery Map for Hajipur.
 * - Supports smooth 2D scrolling/panning (`detectDragGestures`) and tap (`detectTapGestures`):
 *   As the user scrolls/drags on the map, the streets, 3D building footprints, and POI labels pan smoothly
 *   under the center pin and update the customer's delivery coordinates & street address in real time!
 * - Center dark tooltip: "Order will be delivered here • Place the pin accurately on the map" with vertical pin needle
 * - Bottom-right circular white GPS crosshair button (no external Google Maps button!)
 */
@Composable
fun HajipurLiveMap(
    customerLat: Double,
    customerLng: Double,
    deliveryLat: Double = HajipurLocations.KITCHEN_LAT,
    deliveryLng: Double = HajipurLocations.KITCHEN_LNG,
    showDeliveryRider: Boolean = false,
    riderName: String = "",
    estimatedMinutes: Int = 35,
    distanceKm: Double = 1.5,
    interactivePicker: Boolean = false,
    onLocationPicked: ((Double, Double, String) -> Unit)? = null,
    onDetectGpsClick: (() -> Unit)? = null,
    mapHeight: Dp = 320.dp,
    modifier: Modifier = Modifier
) {
    val currentOnLocationPicked by rememberUpdatedState(onLocationPicked)

    var centerLat by remember { mutableDoubleStateOf(customerLat) }
    var centerLng by remember { mutableDoubleStateOf(customerLng) }

    LaunchedEffect(customerLat, customerLng) {
        centerLat = customerLat
        centerLng = customerLng
    }

    val pulseTransition = rememberInfiniteTransition(label = "map_pin_pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 12f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    // Viewport span in degrees (zoomed-in street level so scrolling moves smoothly across Hajipur blocks)
    val latSpan = 0.0110
    val lngSpan = 0.0135

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hajipur_live_map"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .pointerInput(interactivePicker) {
                        if (interactivePicker) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                // Dragging map right moves camera West (lng decreases); dragging down moves camera North (lat increases)
                                val deltaLng = -(dragAmount.x / w) * lngSpan
                                val deltaLat = (dragAmount.y / h) * latSpan
                                val nextLat = (centerLat + deltaLat).coerceIn(MIN_LAT, MAX_LAT)
                                val nextLng = (centerLng + deltaLng).coerceIn(MIN_LNG, MAX_LNG)
                                centerLat = nextLat
                                centerLng = nextLng
                                val (_, resolvedAddress) = resolveLocationAddressFromCoordinates(nextLat, nextLng)
                                currentOnLocationPicked?.invoke(nextLat, nextLng, resolvedAddress)
                            }
                        }
                    }
                    .pointerInput(interactivePicker) {
                        if (interactivePicker) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val dxRatio = (offset.x - w * 0.5f) / w
                                val dyRatio = (offset.y - h * 0.5f) / h
                                val nextLat = (centerLat - dyRatio * latSpan).coerceIn(MIN_LAT, MAX_LAT)
                                val nextLng = (centerLng + dxRatio * lngSpan).coerceIn(MIN_LNG, MAX_LNG)
                                centerLat = nextLat
                                centerLng = nextLng
                                val (_, resolvedAddress) = resolveLocationAddressFromCoordinates(nextLat, nextLng)
                                currentOnLocationPicked?.invoke(nextLat, nextLng, resolvedAddress)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Convert world (lat, lng) into screen pixel coordinates relative to current scrolled center
                    fun worldToScreen(lat: Double, lng: Double): Offset {
                        val x = w * 0.5f + ((lng - centerLng) / lngSpan).toFloat() * w
                        val y = h * 0.5f - ((lat - centerLat) / latSpan).toFloat() * h
                        return Offset(x, y)
                    }

                    // 1. Crisp Architectural White-Gray Land Base (Matching Screenshot 2!)
                    drawRect(color = Color(0xFFF4F6F9))

                    // Continuous scrolling offset in pixels based on centerLat / centerLng
                    val panX = -((centerLng - HajipurLocations.KITCHEN_LNG) / lngSpan).toFloat() * w
                    val panY = ((centerLat - HajipurLocations.KITCHEN_LAT) / latSpan).toFloat() * h

                    // Tile modulo helper so roads and 3D buildings repeat seamlessly as you scroll in any direction!
                    fun wrapCoord(value: Float, period: Float): Float {
                        val mod = value % period
                        return if (mod < 0f) mod + period else mod
                    }

                    val tileW = w * 0.55f
                    val tileH = h * 0.55f
                    val baseOffsetX = wrapCoord(panX, tileW) - tileW
                    val baseOffsetY = wrapCoord(panY, tileH) - tileH

                    // 2. Angled Architectural Street Grid & 3D Building Footprints (Matching Screenshot 2!)
                    var tx = baseOffsetX
                    while (tx < w + tileW) {
                        var ty = baseOffsetY
                        while (ty < h + tileH) {
                            // Main diagonal-tilted neighborhood avenues
                            drawLine(
                                color = MapRoadBorder,
                                start = Offset(tx + tileW * 0.18f, ty),
                                end = Offset(tx + tileW * 0.02f, ty + tileH),
                                strokeWidth = 22f,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = MapRoadSlate,
                                start = Offset(tx + tileW * 0.18f, ty),
                                end = Offset(tx + tileW * 0.02f, ty + tileH),
                                strokeWidth = 18f,
                                cap = StrokeCap.Square
                            )

                            drawLine(
                                color = MapRoadBorder,
                                start = Offset(tx + tileW * 0.68f, ty),
                                end = Offset(tx + tileW * 0.52f, ty + tileH),
                                strokeWidth = 24f,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = MapRoadSlate,
                                start = Offset(tx + tileW * 0.68f, ty),
                                end = Offset(tx + tileW * 0.52f, ty + tileH),
                                strokeWidth = 20f,
                                cap = StrokeCap.Square
                            )

                            // Cross streets
                            drawLine(
                                color = MapRoadBorder,
                                start = Offset(tx, ty + tileH * 0.32f),
                                end = Offset(tx + tileW, ty + tileH * 0.48f),
                                strokeWidth = 18f,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = MapRoadSlate,
                                start = Offset(tx, ty + tileH * 0.32f),
                                end = Offset(tx + tileW, ty + tileH * 0.48f),
                                strokeWidth = 14f,
                                cap = StrokeCap.Square
                            )

                            drawLine(
                                color = MapRoadBorder,
                                start = Offset(tx, ty + tileH * 0.78f),
                                end = Offset(tx + tileW, ty + tileH * 0.92f),
                                strokeWidth = 16f,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = MapRoadSlate,
                                start = Offset(tx, ty + tileH * 0.78f),
                                end = Offset(tx + tileW, ty + tileH * 0.92f),
                                strokeWidth = 12f,
                                cap = StrokeCap.Square
                            )

                            // 3D Architectural Building Footprints (White-gray roof + subtle 3D drop shadow wall)
                            val buildingSpecs = listOf(
                                Triple(0.24f, 0.06f, Size(tileW * 0.16f, tileH * 0.12f)),
                                Triple(0.43f, 0.10f, Size(tileW * 0.14f, tileH * 0.15f)),
                                Triple(0.22f, 0.22f, Size(tileW * 0.13f, tileH * 0.10f)),
                                Triple(0.75f, 0.08f, Size(tileW * 0.18f, tileH * 0.14f)),
                                Triple(0.74f, 0.26f, Size(tileW * 0.15f, tileH * 0.12f)),
                                Triple(0.18f, 0.46f, Size(tileW * 0.17f, tileH * 0.14f)),
                                Triple(0.38f, 0.50f, Size(tileW * 0.14f, tileH * 0.16f)),
                                Triple(0.20f, 0.64f, Size(tileW * 0.15f, tileH * 0.11f)),
                                Triple(0.68f, 0.54f, Size(tileW * 0.18f, tileH * 0.15f)),
                                Triple(0.70f, 0.72f, Size(tileW * 0.14f, tileH * 0.12f)),
                                Triple(0.36f, 0.84f, Size(tileW * 0.16f, tileH * 0.11f))
                            )

                            buildingSpecs.forEachIndexed { idx, (bxRatio, byRatio, bSize) ->
                                val bx = tx + tileW * bxRatio
                                val by = ty + tileH * byRatio
                                val tiltDeg = if (idx % 2 == 0) 14f else -10f
                                rotate(degrees = tiltDeg, pivot = Offset(bx + bSize.width / 2f, by + bSize.height / 2f)) {
                                    // 3D Extruded Side Shadow
                                    drawRoundRect(
                                        color = Color(0xFFD5DCE6),
                                        topLeft = Offset(bx + 4f, by + 5f),
                                        size = bSize,
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )
                                    // Building Roof Top
                                    drawRoundRect(
                                        color = Color(0xFFFDFEFF),
                                        topLeft = Offset(bx, by),
                                        size = bSize,
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )
                                    // Crisp architectural roof outline
                                    drawRoundRect(
                                        color = Color(0xFFCBD5E1),
                                        topLeft = Offset(bx, by),
                                        size = bSize,
                                        cornerRadius = CornerRadius(3f, 3f),
                                        style = Stroke(width = 1.4f)
                                    )
                                }
                            }

                            ty += tileH
                        }
                        tx += tileW
                    }

                    val kitchenOffset = worldToScreen(HajipurLocations.KITCHEN_LAT, HajipurLocations.KITCHEN_LNG)
                    val customerOffset = Offset(w * 0.5f, h * 0.5f)
                    val riderOffset = worldToScreen(deliveryLat, deliveryLng)

                    // 3. Route Polyline (when tracking active order or viewing store connection)
                    if (showDeliveryRider) {
                        drawLine(
                            color = Color(0xFF0284C7).copy(alpha = 0.65f),
                            start = kitchenOffset,
                            end = riderOffset,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = MapPinRed,
                            start = riderOffset,
                            end = customerOffset,
                            strokeWidth = 7f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                        )
                    } else if (!interactivePicker) {
                        drawLine(
                            color = MapPinRed.copy(alpha = 0.75f),
                            start = kitchenOffset,
                            end = customerOffset,
                            strokeWidth = 5f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f)
                        )
                    }

                    // 4. Store Hub Marker (if visible inside current viewport)
                    if (kitchenOffset.x in -40f..(w + 40f) && kitchenOffset.y in -40f..(h + 40f)) {
                        drawCircle(
                            color = MapPinRed.copy(alpha = 0.18f),
                            radius = 20f,
                            center = kitchenOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = kitchenOffset
                        )
                        drawCircle(
                            color = MapPinRed,
                            radius = 9f,
                            center = kitchenOffset
                        )
                    }

                    // 5. Center Pin Ground Target & Vertical Needle (Matching Screenshot 2!)
                    drawCircle(
                        color = MapPinRed.copy(alpha = 0.16f),
                        radius = pulseRadius,
                        center = customerOffset
                    )
                    // Small ground shadow dot at exact center
                    drawCircle(
                        color = Color(0x66000000),
                        radius = 4.5f,
                        center = customerOffset
                    )
                    // Vertical dark needle stem rising from center point up to the floating dark callout box
                    drawLine(
                        color = Color(0xFF1F2937),
                        start = customerOffset,
                        end = Offset(customerOffset.x, customerOffset.y - 34f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )

                    // 6. Live Rider Marker (if active)
                    if (showDeliveryRider) {
                        drawCircle(
                            color = VegGreenBright.copy(alpha = 0.30f),
                            radius = pulseRadius * 1.05f,
                            center = riderOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 13f,
                            center = riderOffset
                        )
                        drawCircle(
                            color = VegGreenBright,
                            radius = 10f,
                            center = riderOffset
                        )
                    }
                }

                // 7. Bilingual Hindi/English POI Markers that scroll across the map (Manoj villa, TRW, Sanjeev Kumar Das, etc.)
                hajipurMapPois.forEach { poi ->
                    val dxRatio = ((poi.lng - centerLng) / lngSpan).toFloat()
                    val dyRatio = ((centerLat - poi.lat) / latSpan).toFloat()
                    if (dxRatio in -0.46f..0.46f && dyRatio in -0.44f..0.44f) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(
                                    x = (dxRatio * 310f).dp,
                                    y = (dyRatio * mapHeight.value * 0.85f).dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF64748B),
                                border = BorderStroke(2.dp, Color.White),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (poi.isSchoolOrInstitute) {
                                        Icon(
                                            imageVector = Icons.Filled.School,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                            Column {
                                Text(
                                    text = poi.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF475569),
                                    maxLines = 1
                                )
                                Text(
                                    text = poi.titleHi,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 8. Center Dark Floating Callout Box ("Order will be delivered here" or "NOT AVAILABLE ABOVE 10 KM")
                val liveStoreDistanceKm = HajipurLocations.calculateDistanceKm(
                    HajipurLocations.KITCHEN_LAT,
                    HajipurLocations.KITCHEN_LNG,
                    centerLat,
                    centerLng
                )
                val isPinWithin10Km = HajipurLocations.isWithinDeliveryRange(liveStoreDistanceKm)
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { IntOffset(0, -54.dp.roundToPx()) },
                    color = if (isPinWithin10Km) Color(0xFF262626) else Color(0xFFB91C1C),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPinWithin10Km) "Deliver here" else "Out of range",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (!isPinWithin10Km) {
                                "$liveStoreDistanceKm km (Max 10 km)"
                            } else if (interactivePicker) {
                                "$liveStoreDistanceKm km"
                            } else {
                                "$liveStoreDistanceKm km • ~$estimatedMinutes min"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFFE5E7EB),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // 9. Bottom-Right Circular White GPS Crosshair Button (Matching Screenshot 2!)
                if (interactivePicker && onDetectGpsClick != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .size(44.dp)
                            .shadow(5.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { onDetectGpsClick() }
                            .testTag("map_floating_locate_me_btn"),
                        color = Color.White,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.GpsFixed,
                                contentDescription = "Use Current GPS Location",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Clean White Map Footer Strip showing live scrolled location & 10 km range check
            val (liveLocalityTitle, _) = resolveLocationAddressFromCoordinates(centerLat, centerLng)
            val footerDistanceKm = HajipurLocations.calculateDistanceKm(
                HajipurLocations.KITCHEN_LAT,
                HajipurLocations.KITCHEN_LNG,
                centerLat,
                centerLng
            )
            val footerWithin10Km = HajipurLocations.isWithinDeliveryRange(footerDistanceKm)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (footerWithin10Km) Color.White else Color(0xFFFEF2F2))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Pinned Location",
                        tint = MapPinRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = liveLocalityTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (footerWithin10Km) Color(0xFF0F172A) else Color(0xFFB91C1C),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showDeliveryRider) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeliveryDining,
                            contentDescription = "Delivery Rider",
                            tint = MapPinRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = riderName.ifBlank { "Order Dispatched" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MapPinRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "Distance",
                            tint = if (footerWithin10Km) VegGreenBright else Color(0xFFB91C1C),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (footerWithin10Km) {
                                "$footerDistanceKm km"
                            } else {
                                "$footerDistanceKm km (>10 km)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (footerWithin10Km) Color(0xFF15803D) else Color(0xFFB91C1C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

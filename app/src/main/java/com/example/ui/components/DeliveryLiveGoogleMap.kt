package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mycrushpizza.`in`.BuildConfig
import com.example.data.model.HajipurLocations
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushCardSurface
import com.example.ui.theme.CrushElevatedSurface
import com.example.ui.theme.CrushMutedText
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.theme.VegGreenBright
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Checks whether a real Google Maps API key has been configured via Secrets Gradle Plugin (.env / Secrets panel).
 */
fun hasValidGoogleMapsApiKey(apiKey: String = BuildConfig.MAPS_API_KEY): Boolean {
    val trimmed = apiKey.trim()
    return trimmed.isNotBlank() &&
        !trimmed.equals("YOUR_GOOGLE_MAPS_API_KEY", ignoreCase = true) &&
        !trimmed.equals("MY_MAPS_API_KEY", ignoreCase = true) &&
        !trimmed.contains("PLACEHOLDER", ignoreCase = true)
}

/**
 * Computes a safe bounding box across store, live delivery partner, and customer delivery coordinates.
 */
fun computeLiveDeliveryMapBounds(
    storeLat: Double,
    storeLng: Double,
    deliveryLat: Double,
    deliveryLng: Double,
    customerLat: Double,
    customerLng: Double
): LatLngBounds {
    val builder = LatLngBounds.Builder()
        .include(LatLng(storeLat, storeLng))
        .include(LatLng(deliveryLat, deliveryLng))
        .include(LatLng(customerLat, customerLng))
    return builder.build()
}

/**
 * Google Maps SDK powered Live Delivery Partner Tracking Card for active orders.
 * - Streams real-time delivery partner coordinates (deliveryLat / deliveryLng) from Firestore.
 * - Plots Kitchen, Live Delivery Partner, and Customer Delivery markers with live route polylines.
 * - Smoothly follows the delivery partner or fits the full route on camera updates.
 * - Gracefully falls back with a clear notice if MAPS_API_KEY is not yet configured in AI Studio Secrets.
 */
@Composable
fun DeliveryLiveGoogleMapCard(
    order: PizzaOrder,
    storeLat: Double = HajipurLocations.KITCHEN_LAT,
    storeLng: Double = HajipurLocations.KITCHEN_LNG,
    mapHeight: Dp = 280.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasValidKey = remember { hasValidGoogleMapsApiKey() }
    val storePosition = remember(storeLat, storeLng) { LatLng(storeLat, storeLng) }
    val customerPosition = remember(order.customerLat, order.customerLng) {
        LatLng(order.customerLat, order.customerLng)
    }
    val deliveryPosition = remember(order.deliveryLat, order.deliveryLng) {
        LatLng(order.deliveryLat, order.deliveryLng)
    }

    val effectiveStatus = order.autoProgressedOrderStatus()
    val isPartnerDispatched = order.isLocationSharingActive ||
        effectiveStatus in listOf(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY)
    val partnerLabel = order.deliveryPartnerName.ifBlank {
        if (isPartnerDispatched) "Delivery Partner" else "Kitchen Preparing"
    }

    val pulseTransition = rememberInfiniteTransition(label = "live_gps_pulse")
    val liveDotAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    var followRider by remember(order.id) { mutableStateOf(true) }
    var fitRouteTrigger by remember(order.id) { mutableIntStateOf(0) }
    var isMapLoaded by remember(order.id) { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (isPartnerDispatched) deliveryPosition else customerPosition,
            14.5f
        )
    }

    // Automatically animate Google Map camera as live delivery partner GPS coordinates update in Firestore
    LaunchedEffect(
        isMapLoaded,
        order.deliveryLat,
        order.deliveryLng,
        order.customerLat,
        order.customerLng,
        followRider,
        fitRouteTrigger
    ) {
        if (!hasValidKey || !isMapLoaded) return@LaunchedEffect
        try {
            if (followRider && isPartnerDispatched && fitRouteTrigger == 0) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(deliveryPosition, 15.5f),
                    durationMs = 700
                )
            } else {
                val bounds = computeLiveDeliveryMapBounds(
                    storeLat = storeLat,
                    storeLng = storeLng,
                    deliveryLat = order.deliveryLat,
                    deliveryLng = order.deliveryLng,
                    customerLat = order.customerLat,
                    customerLng = order.customerLng
                )
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, 96),
                    durationMs = 700
                )
            }
        } catch (_: Exception) {
            // Safe fallback if map viewport is momentarily resizing
            try {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(deliveryPosition, 14.5f))
            } catch (_: Exception) {
                // Ignore transient camera move errors
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_maps_live_delivery_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            if (isPartnerDispatched) Color(0xFF059669).copy(alpha = 0.45f) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Live Telemetry Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isPartnerDispatched) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.DeliveryDining,
                                contentDescription = "Live Delivery Partner Location",
                                tint = if (isPartnerDispatched) Color(0xFF059669) else Color(0xFFE31837),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Live Delivery Map",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isPartnerDispatched) {
                                "$partnerLabel • ${order.distanceKm} km away"
                            } else {
                                "RN College Chautha Road → Your Location (${order.distanceKm} km)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = if (isPartnerDispatched) Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .alpha(if (isPartnerDispatched) liveDotAlpha else 1f)
                                .clip(CircleShape)
                                .background(if (isPartnerDispatched) Color(0xFF059669) else Color(0xFFD97706))
                        )
                        Text(
                            text = if (isPartnerDispatched) "LIVE" else "KITCHEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPartnerDispatched) Color(0xFF047857) else Color(0xFFB45309),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Google Maps SDK Canvas (or graceful fallback if MAPS_API_KEY is not yet configured in Secrets panel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .testTag("google_map_container")
            ) {
                if (hasValidKey) {
                    val storeMarkerState = remember(order.id) { MarkerState(position = storePosition) }
                    val riderMarkerState = remember(order.id) { MarkerState(position = deliveryPosition) }
                    val customerMarkerState = remember(order.id) { MarkerState(position = customerPosition) }
                    LaunchedEffect(storePosition) { storeMarkerState.position = storePosition }
                    LaunchedEffect(deliveryPosition) { riderMarkerState.position = deliveryPosition }
                    LaunchedEffect(customerPosition) { customerMarkerState.position = customerPosition }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("google_maps_sdk_view"),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            mapType = MapType.NORMAL,
                            isTrafficEnabled = isPartnerDispatched
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            compassEnabled = true,
                            mapToolbarEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        onMapLoaded = { isMapLoaded = true }
                    ) {
                        // 1. Restaurant Kitchen Marker
                        Marker(
                            state = storeMarkerState,
                            title = "My Crush Pizza – Hajipur",
                            snippet = "Chauhatta RN College Road Kitchen",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )

                        // 2. Live Delivery Partner Marker (Updates dynamically as Firestore streams GPS)
                        Marker(
                            state = riderMarkerState,
                            title = partnerLabel,
                            snippet = buildString {
                                if (order.deliveryPartnerVehicle.isNotBlank()) {
                                    append("${order.deliveryPartnerVehicle} • ")
                                }
                                append("${order.distanceKm} km away • ETA ${order.estimatedMinutes} mins")
                            },
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )

                        // 3. Customer Delivery Destination Marker
                        Marker(
                            state = customerMarkerState,
                            title = "Delivery Destination (${order.customerName.ifBlank { "You" }})",
                            snippet = order.deliveryAddress,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )

                        // 4. Completed Leg: Store -> Delivery Partner
                        if (isPartnerDispatched) {
                            Polyline(
                                points = listOf(storePosition, deliveryPosition),
                                color = Color(0xFF059669),
                                width = 10f
                            )
                        }

                        // 5. Remaining Route: Delivery Partner -> Customer
                        Polyline(
                            points = listOf(deliveryPosition, customerPosition),
                            color = Color(0xFFE31837),
                            width = 11f,
                            pattern = listOf(Dash(24f), Gap(12f))
                        )
                    }

                    // Floating Camera Controls (Follow Rider / Fit Route)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                followRider = true
                                fitRouteTrigger = 0
                            },
                            color = if (followRider) CrushRed else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.testTag("map_follow_rider_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = "Center on Delivery Partner",
                                    tint = if (followRider) Color.White else CrushBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Rider",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (followRider) Color.White else CrushBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                followRider = false
                                fitRouteTrigger += 1
                            },
                            color = if (!followRider) CrushYellow else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.testTag("map_fit_route_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Route,
                                    contentDescription = "Fit Full Delivery Route",
                                    tint = CrushBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Route",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrushBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                } else {
                    // Clean live Hajipur delivery map view
                    HajipurLiveMap(
                        customerLat = order.customerLat,
                        customerLng = order.customerLng,
                        deliveryLat = order.deliveryLat,
                        deliveryLng = order.deliveryLng,
                        showDeliveryRider = true,
                        riderName = partnerLabel,
                        estimatedMinutes = order.estimatedMinutes,
                        distanceKm = order.distanceKm,
                        interactivePicker = false,
                        mapHeight = mapHeight
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Bottom Delivery Partner Info & Quick Call Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partnerLabel + if (order.deliveryPartnerVehicle.isNotBlank()) " • ${order.deliveryPartnerVehicle}" else "",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Status: ${effectiveStatus.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }

                if (order.deliveryPartnerPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { StoreContactHelper.dialStore(context, order.deliveryPartnerPhone) },
                        border = BorderStroke(1.dp, Color(0xFF059669)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("call_delivery_partner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Call Delivery Partner",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Rider", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

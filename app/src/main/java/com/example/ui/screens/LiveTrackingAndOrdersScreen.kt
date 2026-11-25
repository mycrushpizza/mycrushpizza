package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HajipurLocations
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import com.example.ui.components.DeliveryLiveGoogleMapCard
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.StoreContactHelper
import com.example.ui.viewmodel.MainAppUiState
import kotlinx.coroutines.delay

private val TrackPageBg = Color(0xFFF5F8FF)
private val TrackCardBg = Color.White
private val TrackDeepInk = Color(0xFF0F172A)
private val TrackMutedInk = Color(0xFF64748B)
private val TrackCrimson = Color(0xFFE31837)
private val TrackEmerald = Color(0xFF059669)
private val TrackAmber = Color(0xFFD97706)
private val TrackBorder = Color(0xFFE2E8F0)

@Composable
fun LiveTrackingAndOrdersScreen(
    uiState: MainAppUiState,
    onSelectOrderForTracking: (String) -> Unit,
    onReorderClick: (PizzaOrder) -> Unit,
    onBrowseMenuClick: () -> Unit,
    onCloseDetailsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeOrder = uiState.activeTrackedOrder
    val storePhone = uiState.storeConfig.storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    var isMapExpanded by remember(activeOrder?.id) { mutableStateOf(true) }

    val pulseTransition = rememberInfiniteTransition(label = "track_live_pulse")
    val livePulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse_alpha"
    )

    if (activeOrder == null && uiState.customerOrders.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TrackPageBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = CircleShape,
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "No Active Orders",
                            tint = TrackCrimson,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Orders Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TrackDeepInk,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Order hot pizza from My Crush Pizza Hajipur to track every step live right here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrackMutedInk,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBrowseMenuClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrackCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Order Hot Pizza Now", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrackPageBg),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .testTag("live_tracking_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Clean Top Header Bar (Back + Title + Quick Call Store)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onCloseDetailsClick != null) {
                            Surface(
                                color = Color.White,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, TrackBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("close_customer_order_details_btn")
                            ) {
                                IconButton(onClick = onCloseDetailsClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TrackDeepInk
                                    )
                                }
                            }
                        }
                        Column {
                            Text(
                                text = "Track Order",
                                style = MaterialTheme.typography.titleLarge,
                                color = TrackDeepInk,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (activeOrder != null) {
                                Text(
                                    text = "${activeOrder.orderNumber} • ${formatOrderTimestamp(activeOrder.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TrackMutedInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { StoreContactHelper.dialStore(context, storePhone) },
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TrackCrimson.copy(alpha = 0.35f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.testTag("track_header_call_store_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Call Store",
                                tint = TrackCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Help",
                                style = MaterialTheme.typography.labelMedium,
                                color = TrackCrimson,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Optional quick Order Switcher pills when customer has > 1 order
            if (uiState.customerOrders.size > 1) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.customerOrders.take(8), key = { it.id.ifBlank { it.orderNumber } }) { pastOrder ->
                            val isSelected = activeOrder?.id == pastOrder.id
                            val effStatus = pastOrder.autoProgressedOrderStatus(nowMillis)
                            val isLive = !pastOrder.isTerminal
                            Surface(
                                onClick = { onSelectOrderForTracking(pastOrder.id) },
                                color = if (isSelected) TrackCrimson else Color.White,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) TrackCrimson else TrackBorder
                                ),
                                modifier = Modifier.testTag("order_history_card_${pastOrder.orderNumber}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isLive) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFFFDE047) else TrackEmerald)
                                        )
                                    }
                                    Text(
                                        text = "${pastOrder.orderNumber} • ${effStatus.title}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else TrackDeepInk,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (activeOrder != null) {
                val activeEffectiveStatus = activeOrder.autoProgressedOrderStatus(nowMillis)
                val isCancelled = activeEffectiveStatus == OrderStatus.CANCELLED
                val isDelivered = activeEffectiveStatus == OrderStatus.DELIVERED
                val dynamicEta = HajipurLocations.estimateMinutesForDistance(
                    activeOrder.distanceKm,
                    activeEffectiveStatus
                ).coerceAtLeast(if (activeEffectiveStatus == OrderStatus.ON_THE_WAY) 5 else 8)

                // 2. Hero Live Status & Instant Visual Progress Card
                item {
                    val heroGradient = when {
                        isDelivered -> Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFF10B981)))
                        isCancelled -> Brush.linearGradient(listOf(Color(0xFF991B1B), Color(0xFFDC2626)))
                        else -> Brush.linearGradient(listOf(Color(0xFF9F1239), Color(0xFFE31837), Color(0xFFEA580C)))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracked_order_header_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(heroGradient)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!isDelivered && !isCancelled) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .alpha(livePulseAlpha)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFDE047))
                                            )
                                        }
                                        Text(
                                            text = when {
                                                isDelivered -> "ORDER COMPLETED"
                                                isCancelled -> "ORDER CANCELLED"
                                                else -> "LIVE ORDER STATUS"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.88f),
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = friendlyStatusHeadline(activeEffectiveStatus),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = friendlyShortSubtitle(activeEffectiveStatus),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Big Clear ETA / Status Badge
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = when {
                                                isDelivered -> "DONE"
                                                isCancelled -> "CLOSED"
                                                else -> "~$dynamicEta"
                                            },
                                            style = MaterialTheme.typography.titleLarge,
                                            color = when {
                                                isDelivered -> TrackEmerald
                                                isCancelled -> TrackCrimson
                                                else -> TrackCrimson
                                            },
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = when {
                                                isDelivered -> "Delivered"
                                                isCancelled -> "Cancelled"
                                                else -> "mins ETA"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TrackMutedInk,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Instant Visual 6-Step Segmented Milestone Strip
                            if (!isCancelled) {
                                val currentIdx = activeEffectiveStatus.stepIndex.coerceIn(0, 5)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        OrderStatus.trackingSteps.forEachIndexed { idx, _ ->
                                            val filled = idx <= currentIdx
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(
                                                        if (filled) Color(0xFFFDE047)
                                                        else Color.White.copy(alpha = 0.28f)
                                                    )
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Step ${currentIdx + 1} of ${OrderStatus.trackingSteps.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.92f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isDelivered) {
                                                "Enjoy your meal!"
                                            } else {
                                                "Next: ${nextStepLabel(activeEffectiveStatus)}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFEF08A),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Clean Connected Vertical Step-by-Step Timeline
                if (!isCancelled) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_status_steps_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = TrackCardBg),
                            border = BorderStroke(1.dp, TrackBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Order Journey",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TrackDeepInk,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (!isDelivered) {
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(999.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Schedule,
                                                    contentDescription = null,
                                                    tint = TrackEmerald,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Live Updates",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TrackEmerald,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val currentStepIndex = activeEffectiveStatus.stepIndex
                                val steps = OrderStatus.trackingSteps
                                steps.forEachIndexed { index, step ->
                                    val isDone = currentStepIndex > step.stepIndex ||
                                        (step == OrderStatus.DELIVERED && isDelivered)
                                    val isCurrent = currentStepIndex == step.stepIndex && !isDelivered
                                    val isLast = index == steps.lastIndex

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("tracking_step_${step.code}"),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Timeline Node + Connector Column
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(34.dp)
                                        ) {
                                            Surface(
                                                color = when {
                                                    isDone -> TrackEmerald
                                                    isCurrent -> TrackCrimson
                                                    else -> Color(0xFFF1F5F9)
                                                },
                                                shape = CircleShape,
                                                border = if (!isDone && !isCurrent) {
                                                    BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                                                } else {
                                                    null
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = if (isDone) Icons.Filled.Check else stepIcon(step),
                                                        contentDescription = step.title,
                                                        tint = when {
                                                            isDone || isCurrent -> Color.White
                                                            else -> Color(0xFF94A3B8)
                                                        },
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                            if (!isLast) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.5.dp)
                                                        .height(28.dp)
                                                        .background(
                                                            if (isDone) TrackEmerald
                                                            else Color(0xFFE2E8F0)
                                                        )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Step Label & Short Explanation
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(bottom = if (isLast) 2.dp else 14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = step.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = when {
                                                        isCurrent -> TrackCrimson
                                                        isDone -> TrackDeepInk
                                                        else -> TrackMutedInk
                                                    },
                                                    fontWeight = if (isCurrent || isDone) FontWeight.ExtraBold else FontWeight.Medium
                                                )
                                                if (isCurrent) {
                                                    Surface(
                                                        color = Color(0xFFFFF1F2),
                                                        shape = RoundedCornerShape(999.dp),
                                                        border = BorderStroke(1.dp, TrackCrimson.copy(alpha = 0.4f))
                                                    ) {
                                                        Text(
                                                            text = "NOW",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = TrackCrimson,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else if (isDone) {
                                                    Text(
                                                        text = "Done",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TrackEmerald,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = friendlyShortSubtitle(step),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCurrent) TrackDeepInk else TrackMutedInk
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Collapsible Live Delivery Map (Active Orders Only)
                if (!activeOrder.isTerminal) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { isMapExpanded = !isMapExpanded },
                                color = TrackCardBg,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, TrackBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("toggle_live_map_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = "Delivery Map",
                                            tint = TrackCrimson,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (isMapExpanded) "Hide Live Delivery Map" else "Show Live Delivery Map",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TrackDeepInk,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${activeOrder.distanceKm} km from RN College Chautha Road",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TrackMutedInk
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isMapExpanded) {
                                            Icons.Filled.KeyboardArrowUp
                                        } else {
                                            Icons.Filled.KeyboardArrowDown
                                        },
                                        contentDescription = if (isMapExpanded) "Collapse Map" else "Expand Map",
                                        tint = TrackMutedInk
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isMapExpanded) {
                                DeliveryLiveGoogleMapCard(
                                    order = activeOrder,
                                    storeLat = uiState.storeConfig.storeLat,
                                    storeLng = uiState.storeConfig.storeLng,
                                    mapHeight = 230.dp
                                )
                            }
                        }
                    }
                }

                // 5. Clean Order Summary & Delivery Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = TrackCardBg),
                        border = BorderStroke(1.dp, TrackBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = TrackCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Order Summary",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TrackDeepInk,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Payments,
                                            contentDescription = null,
                                            tint = TrackDeepInk,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${activeOrder.paymentMethod} • ${formatInr(activeOrder.totalInr)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TrackDeepInk,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            // Items Box
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TrackBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "ITEMS (${activeOrder.itemsCount})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TrackMutedInk,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = activeOrder.itemsSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TrackDeepInk,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Delivery Address Row
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFFF1F2),
                                    shape = CircleShape,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = "Address",
                                            tint = TrackCrimson,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Delivering to ${activeOrder.customerName.ifBlank { "Customer" }}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TrackDeepInk,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = activeOrder.deliveryAddress +
                                            if (activeOrder.landmark.isNotBlank()) " • Near ${activeOrder.landmark}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TrackMutedInk
                                    )
                                    if (activeOrder.customerPhone.isNotBlank()) {
                                        Text(
                                            text = "Phone: ${activeOrder.customerPhone}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TrackMutedInk
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = TrackBorder)

                            // 1-Tap Support Actions: Call Store & WhatsApp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { StoreContactHelper.dialStore(context, storePhone) },
                                    border = BorderStroke(1.2.dp, TrackCrimson),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TrackCrimson),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("call_store_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "Call Store",
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call Store",
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Button(
                                    onClick = { StoreContactHelper.openOrderOnWhatsApp(context, activeOrder) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TrackEmerald,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("whatsapp_store_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "WhatsApp Store",
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp",
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            if (activeOrder.isTerminal) {
                                Button(
                                    onClick = { onReorderClick(activeOrder) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TrackCrimson,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("reorder_btn_${activeOrder.orderNumber}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Reorder",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reorder Same Items", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun stepIcon(status: OrderStatus): ImageVector = when (status) {
    OrderStatus.CONFIRMED -> Icons.AutoMirrored.Filled.ReceiptLong
    OrderStatus.PREPARING -> Icons.Filled.LocalPizza
    OrderStatus.READY -> Icons.Filled.Fastfood
    OrderStatus.ASSIGNED -> Icons.Filled.ShoppingBag
    OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> Icons.Filled.DeliveryDining
    OrderStatus.DELIVERED -> Icons.Filled.CheckCircle
    OrderStatus.CANCELLED -> Icons.Filled.Cancel
}

private fun friendlyStatusHeadline(status: OrderStatus): String = when (status) {
    OrderStatus.CONFIRMED -> "Order Confirmed"
    OrderStatus.PREPARING -> "Baking Your Food"
    OrderStatus.READY -> "Hot & Ready!"
    OrderStatus.ASSIGNED -> "Packing Your Order"
    OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> "Out for Delivery"
    OrderStatus.DELIVERED -> "Order Delivered!"
    OrderStatus.CANCELLED -> "Order Cancelled"
}

private fun friendlyShortSubtitle(status: OrderStatus): String = when (status) {
    OrderStatus.CONFIRMED -> "Received at My Crush Pizza, RN College Chautha Road"
    OrderStatus.PREPARING -> "Chef is freshly preparing & baking your order"
    OrderStatus.READY -> "Fresh out of the oven and ready to pack"
    OrderStatus.ASSIGNED -> "Packing hot with oregano, chilli flakes & dips"
    OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> "Rider is on the way — reaching your door soon"
    OrderStatus.DELIVERED -> "Delivered safely! Enjoy your hot meal"
    OrderStatus.CANCELLED -> "This order was cancelled"
}

private fun nextStepLabel(status: OrderStatus): String = when (status) {
    OrderStatus.CONFIRMED -> "Preparing"
    OrderStatus.PREPARING -> "Ready"
    OrderStatus.READY -> "Packing"
    OrderStatus.ASSIGNED -> "Reaching Soon"
    OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> "Delivered"
    OrderStatus.DELIVERED -> "Completed"
    OrderStatus.CANCELLED -> "Closed"
}

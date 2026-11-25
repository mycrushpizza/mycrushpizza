package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.StoreContactHelper
import com.example.ui.viewmodel.MainAppUiState
import com.example.ui.viewmodel.OrderHistoryFilter
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

private val HistoryBg = Color(0xFFF5F8FF)
private val HistoryCrimson = Color(0xFFE31837)
private val HistoryDeepInk = Color(0xFF0F172A)
private val HistoryMutedInk = Color(0xFF64748B)
private val HistoryEmerald = Color(0xFF059669)
private val HistoryAmber = Color(0xFFD97706)
private val HistoryBorder = Color(0xFFE2E8F0)

/**
 * Formats a Firestore [Timestamp] into a readable local date/time string for Order History cards.
 */
fun formatOrderTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    val formatter = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH)
    return formatter.format(timestamp.toDate())
}

@Composable
fun OrderHistoryScreen(
    uiState: MainAppUiState,
    onRefreshClick: () -> Unit,
    onFilterSelected: (OrderHistoryFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTrackOrderClick: (String) -> Unit,
    onReorderClick: (PizzaOrder) -> Unit,
    onCancelOrderClick: (PizzaOrder) -> Unit,
    onBrowseMenuClick: () -> Unit,
    onRequestGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storePhone = uiState.storeConfig.storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY }
    var selectedOrderForDetail by remember { mutableStateOf<PizzaOrder?>(null) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    // Keep detail dialog synchronized with live Firestore updates
    val liveDetailOrder = selectedOrderForDetail?.let { chosen ->
        uiState.customerOrders.firstOrNull { it.id == chosen.id } ?: chosen
    }

    if (liveDetailOrder != null) {
        OrderHistoryDetailDialog(
            order = liveDetailOrder,
            effectiveStatus = liveDetailOrder.autoProgressedOrderStatus(nowMillis),
            storePhone = storePhone,
            isBusy = uiState.isBusy,
            onDismiss = { selectedOrderForDetail = null },
            onTrackLiveClick = {
                selectedOrderForDetail = null
                onTrackOrderClick(liveDetailOrder.id)
            },
            onReorderClick = {
                selectedOrderForDetail = null
                onReorderClick(liveDetailOrder)
            },
            onCancelOrderClick = {
                onCancelOrderClick(liveDetailOrder)
                selectedOrderForDetail = null
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HistoryBg),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Auth Gate for Guest Users (Never query Firestore when unauthenticated)
        if (!uiState.isSignedIn) {
            OrderHistoryGuestGate(
                onRequestGoogleSignIn = onRequestGoogleSignIn,
                onBrowseMenuClick = onBrowseMenuClick,
                modifier = Modifier.widthIn(max = 600.dp)
            )
            return@Box
        }

        // 2. Initial Firestore Loading State
        if (uiState.isOrderHistoryLoading && uiState.customerOrders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(24.dp)
                    .testTag("order_history_loading_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = HistoryCrimson, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading Your Order History...",
                    style = MaterialTheme.typography.titleMedium,
                    color = HistoryDeepInk,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fetching past orders synced to your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HistoryMutedInk,
                    textAlign = TextAlign.Center
                )
            }
            return@Box
        }

        // 3. Error State (when no cached orders are available)
        if (!uiState.orderHistoryError.isNullOrBlank() && uiState.customerOrders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(24.dp)
                    .testTag("order_history_error_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0xFFFFE4E6),
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = "Connection error",
                            tint = HistoryCrimson,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Unable to Load Orders",
                    style = MaterialTheme.typography.titleLarge,
                    color = HistoryDeepInk,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.orderHistoryError,
                    style = MaterialTheme.typography.bodySmall,
                    color = HistoryMutedInk,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onRefreshClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HistoryCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("order_history_retry_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry", fontWeight = FontWeight.ExtraBold)
                }
            }
            return@Box
        }

        // 4. Empty Order History State
        if (uiState.customerOrders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(24.dp)
                    .testTag("order_history_empty_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "No past orders",
                            tint = HistoryCrimson,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "No Past Orders Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = HistoryDeepInk,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Orders placed from ${uiState.currentUserEmail.ifBlank { "your account" }} will appear here with full receipts, live tracking, and 1-tap reorder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HistoryMutedInk,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onBrowseMenuClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HistoryCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("order_history_explore_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalPizza,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explore Menu", fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onRefreshClick,
                        border = BorderStroke(1.dp, HistoryCrimson),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HistoryCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("order_history_refresh_empty_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Order History",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh", fontWeight = FontWeight.Bold)
                    }
                }
            }
            return@Box
        }

        // 5. Populated Real-Time Order History Feed
        val filteredOrders = uiState.filteredOrderHistory

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .testTag("order_history_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Summary Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_history_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF9F1239), Color(0xFFE31837), Color(0xFFEA580C))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    Surface(
                                        color = Color.White.copy(alpha = 0.18f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = "Order History",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Order History",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = uiState.currentUserEmail.ifBlank { uiState.currentUserName },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.88f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.18f),
                                    shape = CircleShape
                                ) {
                                    IconButton(
                                        onClick = onRefreshClick,
                                        enabled = !uiState.isOrderHistoryRefreshing,
                                        modifier = Modifier.testTag("order_history_refresh_btn")
                                    ) {
                                        if (uiState.isOrderHistoryRefreshing) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = "Refresh Order History",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OrderStatChip(
                                    label = "Total Orders",
                                    value = uiState.customerOrders.size.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                OrderStatChip(
                                    label = "Delivered",
                                    value = uiState.orderHistoryDeliveredCount.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                OrderStatChip(
                                    label = "Total Spent",
                                    value = formatInr(uiState.orderHistoryTotalSpentInr),
                                    modifier = Modifier.weight(1.25f)
                                )
                            }
                        }
                    }
                }
            }

            // 1-Tap Live Order Quick Tracker Banner (shown when customer has an active in-progress order)
            val liveActiveOrder = uiState.customerOrders.firstOrNull { !it.isTerminal }
            if (liveActiveOrder != null) {
                item(key = "live_active_order_quick_track") {
                    val liveStatus = liveActiveOrder.autoProgressedOrderStatus(nowMillis)
                    val stepIdx = liveStatus.stepIndex.coerceIn(0, 5)
                    val etaMins = com.example.data.model.HajipurLocations.estimateMinutesForDistance(
                        liveActiveOrder.distanceKm,
                        liveStatus
                    ).coerceAtLeast(if (liveStatus == OrderStatus.ON_THE_WAY) 5 else 8)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackOrderClick(liveActiveOrder.id) }
                            .testTag("orders_tab_quick_live_track_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, HistoryCrimson.copy(alpha = 0.55f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = Color(0xFFFFF1F2),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.DeliveryDining,
                                                contentDescription = "Live Order",
                                                tint = HistoryCrimson,
                                                modifier = Modifier.size(21.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "LIVE ORDER • ${liveStatus.title}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = HistoryCrimson,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${liveActiveOrder.orderNumber} • Est. ~$etaMins mins • ${formatInr(liveActiveOrder.totalInr)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HistoryMutedInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onTrackOrderClick(liveActiveOrder.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HistoryCrimson,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("orders_tab_quick_track_btn")
                                ) {
                                    Text(
                                        text = "Track Live",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            // Mini 6-Step Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OrderStatus.trackingSteps.forEachIndexed { idx, _ ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .background(
                                                color = if (idx <= stepIdx) HistoryEmerald else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(999.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Inline error banner if refresh failed while cached items exist
            if (!uiState.orderHistoryError.isNullOrBlank()) {
                item {
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HistoryCrimson.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.orderHistoryError,
                                style = MaterialTheme.typography.bodySmall,
                                color = HistoryCrimson,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onRefreshClick) {
                                Text("Retry", color = HistoryCrimson, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // Search Input
            item {
                OutlinedTextField(
                    value = uiState.orderHistorySearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search by MCP order #, dish, or address...",
                            style = MaterialTheme.typography.bodySmall,
                            color = HistoryMutedInk
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search orders",
                            tint = HistoryMutedInk
                        )
                    },
                    trailingIcon = {
                        if (uiState.orderHistorySearchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    tint = HistoryMutedInk
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HistoryCrimson,
                        unfocusedBorderColor = HistoryBorder,
                        focusedTextColor = HistoryDeepInk,
                        unfocusedTextColor = HistoryDeepInk
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_history_search_input")
                )
            }

            // Status Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(OrderHistoryFilter.entries) { filter ->
                        val count = when (filter) {
                            OrderHistoryFilter.ALL -> uiState.customerOrders.size
                            OrderHistoryFilter.ACTIVE -> uiState.orderHistoryActiveCount
                            OrderHistoryFilter.DELIVERED -> uiState.orderHistoryDeliveredCount
                            OrderHistoryFilter.CANCELLED -> uiState.orderHistoryCancelledCount
                        }
                        val selected = uiState.orderHistoryFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterSelected(filter) },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HistoryCrimson,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = HistoryDeepInk
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = HistoryBorder,
                                selectedBorderColor = HistoryCrimson
                            ),
                            modifier = Modifier.testTag("order_history_filter_${filter.name}")
                        )
                    }
                }
            }

            // No-Match Filter State
            if (filteredOrders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, HistoryBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "No matching orders",
                                tint = HistoryMutedInk,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "No matching orders found",
                                style = MaterialTheme.typography.titleMedium,
                                color = HistoryDeepInk,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    onFilterSelected(OrderHistoryFilter.ALL)
                                    onSearchQueryChange("")
                                }
                            ) {
                                Text(
                                    text = "Reset Filters",
                                    color = HistoryCrimson,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            } else {
                // Past Orders List
                items(filteredOrders, key = { it.id.ifBlank { it.orderNumber } }) { order ->
                    OrderHistoryCard(
                        order = order,
                        effectiveStatus = order.autoProgressedOrderStatus(nowMillis),
                        onOpenReceiptClick = { selectedOrderForDetail = order },
                        onTrackLiveClick = { onTrackOrderClick(order.id) },
                        onReorderClick = { onReorderClick(order) },
                        onWhatsAppClick = { StoreContactHelper.openOrderOnWhatsApp(context, order) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryGuestGate(
    onRequestGoogleSignIn: () -> Unit,
    onBrowseMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("order_history_auth_gate"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFFFFF1F2),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Sign in required",
                    tint = HistoryCrimson,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Sign In for Order History",
            style = MaterialTheme.typography.headlineSmall,
            color = HistoryDeepInk,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in with Google to view your past orders, itemized receipts, delivery status, and 1-tap reorder synced from the cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = HistoryMutedInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onRequestGoogleSignIn,
            colors = ButtonDefaults.buttonColors(
                containerColor = HistoryCrimson,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("order_history_google_sign_in_btn")
        ) {
            Text("Sign in with Google", fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onBrowseMenuClick,
            modifier = Modifier.height(48.dp)
        ) {
            Text("Back to Menu", color = HistoryCrimson, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OrderStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.16f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: PizzaOrder,
    effectiveStatus: OrderStatus = order.autoProgressedOrderStatus(),
    onOpenReceiptClick: () -> Unit,
    onTrackLiveClick: () -> Unit,
    onReorderClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val statusBg: Color
    val statusText: Color
    when (effectiveStatus) {
        OrderStatus.DELIVERED -> {
            statusBg = Color(0xFFECFDF5)
            statusText = HistoryEmerald
        }
        OrderStatus.CANCELLED -> {
            statusBg = Color(0xFFFFF1F2)
            statusText = HistoryCrimson
        }
        else -> {
            statusBg = Color(0xFFFFFBEB)
            statusText = HistoryAmber
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenReceiptClick() }
            .testTag("order_history_item_${order.orderNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (!order.isTerminal) 1.5.dp else 1.dp,
            color = if (!order.isTerminal) HistoryAmber.copy(alpha = 0.6f) else HistoryBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Order Number + Date + Status Badge
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
                        Text(
                            text = order.orderNumber.ifBlank { "Order" },
                            style = MaterialTheme.typography.titleMedium,
                            color = HistoryDeepInk,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${order.itemsCount} item${if (order.itemsCount == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HistoryMutedInk,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatOrderTimestamp(order.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = HistoryMutedInk
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, statusText.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (effectiveStatus) {
                                OrderStatus.DELIVERED -> Icons.Filled.CheckCircle
                                OrderStatus.CANCELLED -> Icons.Filled.Cancel
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = effectiveStatus.title,
                            tint = statusText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = effectiveStatus.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusText,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Items Summary
            Text(
                text = order.itemsSummary.ifBlank { "Order items" },
                style = MaterialTheme.typography.bodyMedium,
                color = HistoryDeepInk,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Delivery Address + Payment + Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Delivery address",
                            tint = HistoryMutedInk,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = order.deliveryAddress.ifBlank { "Hajipur Delivery" },
                            style = MaterialTheme.typography.labelSmall,
                            color = HistoryMutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payments,
                            contentDescription = "Payment method",
                            tint = HistoryMutedInk,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${order.paymentMethod} • ${order.paymentStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HistoryMutedInk,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (order.bonusUsedInr > 0) {
                            Text(
                                text = "• Bonus -${formatInr(order.bonusUsedInr)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HistoryEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatInr(order.totalInr),
                    style = MaterialTheme.typography.titleLarge,
                    color = HistoryCrimson,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            HorizontalDivider(color = HistoryBorder)

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenReceiptClick,
                    border = BorderStroke(1.dp, HistoryBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HistoryDeepInk),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("order_history_receipt_btn_${order.orderNumber}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = "View Receipt",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                if (!order.isTerminal) {
                    Button(
                        onClick = onTrackLiveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HistoryAmber,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("order_history_track_btn_${order.orderNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeliveryDining,
                            contentDescription = "Track Live",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Track",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onWhatsAppClick,
                        border = BorderStroke(1.dp, HistoryEmerald),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HistoryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("order_history_whatsapp_btn_${order.orderNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp Help",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Help",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onReorderClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HistoryCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("order_history_reorder_btn_${order.orderNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reorder",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reorder",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryDetailDialog(
    order: PizzaOrder,
    effectiveStatus: OrderStatus = order.autoProgressedOrderStatus(),
    storePhone: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onTrackLiveClick: () -> Unit,
    onReorderClick: () -> Unit,
    onCancelOrderClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("order_history_detail_dialog"),
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Receipt • ${order.orderNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        color = HistoryDeepInk,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = formatOrderTimestamp(order.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = HistoryMutedInk
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close receipt",
                        tint = HistoryMutedInk
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Pill
                Surface(
                    color = when (effectiveStatus) {
                        OrderStatus.DELIVERED -> Color(0xFFECFDF5)
                        OrderStatus.CANCELLED -> Color(0xFFFFF1F2)
                        else -> Color(0xFFFFFBEB)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "STATUS: ${effectiveStatus.title.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = when (effectiveStatus) {
                                OrderStatus.DELIVERED -> HistoryEmerald
                                OrderStatus.CANCELLED -> HistoryCrimson
                                else -> HistoryAmber
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = effectiveStatus.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = HistoryDeepInk
                        )
                    }
                }

                // Items Ordered
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ITEMS ORDERED (${order.itemsCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = HistoryMutedInk,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HistoryDeepInk,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = HistoryBorder)

                // Bill Summary Breakdown
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BILL BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        color = HistoryMutedInk,
                        fontWeight = FontWeight.Bold
                    )
                    BillLineRow("Items Subtotal", formatInr(order.subtotalInr))
                    if (order.discountInr > 0) {
                        val couponSuffix = if (order.couponCode.isNotBlank()) " (${order.couponCode})" else ""
                        BillLineRow(
                            label = "Coupon Discount$couponSuffix",
                            value = "-${formatInr(order.discountInr)}",
                            valueColor = HistoryEmerald
                        )
                    }
                    if (order.bonusUsedInr > 0) {
                        BillLineRow(
                            label = "Bonus Cash Used",
                            value = "-${formatInr(order.bonusUsedInr)}",
                            valueColor = HistoryEmerald
                        )
                    }
                    BillLineRow(
                        label = "Delivery Fee",
                        value = if (order.deliveryFeeInr == 0) "FREE" else formatInr(order.deliveryFeeInr),
                        valueColor = if (order.deliveryFeeInr == 0) HistoryEmerald else HistoryDeepInk
                    )
                    BillLineRow("Taxes & Charges", formatInr(order.taxInr))
                    HorizontalDivider(color = HistoryBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grand Total (${order.paymentMethod} • ${order.paymentStatus})",
                            style = MaterialTheme.typography.titleSmall,
                            color = HistoryDeepInk,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatInr(order.totalInr),
                            style = MaterialTheme.typography.titleMedium,
                            color = HistoryCrimson,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                HorizontalDivider(color = HistoryBorder)

                // Delivery & Customer Info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DELIVERY DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        color = HistoryMutedInk,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.customerName} • ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HistoryDeepInk,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = order.deliveryAddress +
                            if (order.landmark.isNotBlank()) "\nLandmark: ${order.landmark}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = HistoryMutedInk
                    )
                    if (order.deliveryPartnerName.isNotBlank()) {
                        Text(
                            text = "Delivery Partner: ${order.deliveryPartnerName}" +
                                if (order.deliveryPartnerVehicle.isNotBlank()) " (${order.deliveryPartnerVehicle})" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = HistoryEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Contact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { StoreContactHelper.dialStore(context, storePhone) },
                        border = BorderStroke(1.dp, HistoryCrimson),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HistoryCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Call Store",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Store", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { StoreContactHelper.openOrderOnWhatsApp(context, order) },
                        border = BorderStroke(1.dp, HistoryEmerald),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HistoryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp Store",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Track Live CTA for Active Orders
                if (!order.isTerminal) {
                    Button(
                        onClick = onTrackLiveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HistoryAmber,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("detail_track_live_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeliveryDining,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Track Live Order Progress", fontWeight = FontWeight.ExtraBold)
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = onReorderClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryCrimson,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("detail_reorder_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reorder Now", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = HistoryMutedInk, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun BillLineRow(
    label: String,
    value: String,
    valueColor: Color = HistoryDeepInk
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HistoryMutedInk
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

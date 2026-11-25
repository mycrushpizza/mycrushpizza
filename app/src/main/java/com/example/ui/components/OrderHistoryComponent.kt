package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import com.example.data.repository.OrderAndNotificationRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface OrderHistoryComponentState {
    data object SignedOut : OrderHistoryComponentState
    data object Loading : OrderHistoryComponentState
    data class Success(val orders: List<PizzaOrder>) : OrderHistoryComponentState
    data class Error(val message: String) : OrderHistoryComponentState
}

private val ComponentCrimson = Color(0xFFE31837)
private val ComponentInk = Color(0xFF0F172A)
private val ComponentMuted = Color(0xFF64748B)
private val ComponentEmerald = Color(0xFF059669)
private val ComponentAmber = Color(0xFFD97706)
private val ComponentBorder = Color(0xFFE2E8F0)

private fun formatHistoryDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    return SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH).format(timestamp.toDate())
}

/**
 * Self-contained Firestore-backed Order History component for the currently logged-in user.
 * Observes real-time customer orders from Firestore and displays item names, status, and total price.
 */
@Composable
fun OrderHistoryComponent(
    userId: String?,
    modifier: Modifier = Modifier,
    maxVisibleOrders: Int = 5,
    onOrderClick: (PizzaOrder) -> Unit = {},
    onViewAllOrdersClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val resolvedUid = userId?.takeIf { it.isNotBlank() } ?: runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var state by remember(resolvedUid) {
        mutableStateOf<OrderHistoryComponentState>(
            if (resolvedUid.isNullOrBlank()) OrderHistoryComponentState.SignedOut
            else OrderHistoryComponentState.Loading
        )
    }

    LaunchedEffect(resolvedUid, refreshTrigger) {
        if (resolvedUid.isNullOrBlank()) {
            state = OrderHistoryComponentState.SignedOut
            return@LaunchedEffect
        }
        state = OrderHistoryComponentState.Loading
        val repository = runCatching { OrderAndNotificationRepository(context.applicationContext) }
            .getOrElse { err ->
                state = OrderHistoryComponentState.Error(
                    err.localizedMessage ?: "Unable to connect to Firestore for order history."
                )
                return@LaunchedEffect
            }
        repository.observeCustomerOrders()
            .catch { err ->
                state = OrderHistoryComponentState.Error(
                    err.localizedMessage ?: "Could not load your past orders."
                )
            }
            .collect { orders ->
                state = OrderHistoryComponentState.Success(orders)
            }
    }

    OrderHistoryComponentCard(
        state = state,
        maxVisibleOrders = maxVisibleOrders,
        onRefreshClick = { refreshTrigger += 1 },
        onOrderClick = onOrderClick,
        onViewAllOrdersClick = onViewAllOrdersClick,
        modifier = modifier
    )
}

@Composable
fun OrderHistoryComponentCard(
    state: OrderHistoryComponentState,
    modifier: Modifier = Modifier,
    maxVisibleOrders: Int = 5,
    onRefreshClick: () -> Unit = {},
    onOrderClick: (PizzaOrder) -> Unit = {},
    onViewAllOrdersClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_order_history_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, ComponentBorder)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Order History",
                                tint = ComponentCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Order History",
                            style = MaterialTheme.typography.titleSmall,
                            color = ComponentInk,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Past orders synced live from Firestore",
                            style = MaterialTheme.typography.labelSmall,
                            color = ComponentMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onViewAllOrdersClick != null) {
                        TextButton(
                            onClick = onViewAllOrdersClick,
                            modifier = Modifier.testTag("order_history_component_view_all_btn")
                        ) {
                            Text(
                                text = "View All",
                                color = ComponentCrimson,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("order_history_component_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Order History",
                            tint = ComponentInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            when (state) {
                OrderHistoryComponentState.SignedOut -> {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ComponentBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sign in with Google to view your past orders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ComponentMuted,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                OrderHistoryComponentState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ComponentCrimson,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Loading your past orders...",
                            style = MaterialTheme.typography.bodySmall,
                            color = ComponentMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                is OrderHistoryComponentState.Error -> {
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = ComponentCrimson,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onRefreshClick) {
                                Text("Retry", color = ComponentCrimson, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is OrderHistoryComponentState.Success -> {
                    if (state.orders.isEmpty()) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ComponentBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalPizza,
                                    contentDescription = "No orders yet",
                                    tint = ComponentCrimson,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "No past orders yet. Your orders will appear here automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ComponentMuted
                                )
                            }
                        }
                    } else {
                        val visibleOrders = state.orders.take(maxVisibleOrders.coerceAtLeast(1))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            visibleOrders.forEach { order ->
                                OrderHistoryRowItem(
                                    order = order,
                                    onClick = { onOrderClick(order) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryRowItem(
    order: PizzaOrder,
    onClick: () -> Unit
) {
    val effectiveStatus = order.autoProgressedOrderStatus()
    val (statusBg, statusColor, statusIcon) = when (effectiveStatus) {
        OrderStatus.DELIVERED -> Triple(Color(0xFFECFDF5), ComponentEmerald, Icons.Filled.CheckCircle)
        OrderStatus.CANCELLED -> Triple(Color(0xFFFFF1F2), ComponentCrimson, Icons.Filled.Cancel)
        else -> Triple(Color(0xFFFFFBEB), ComponentAmber, Icons.Filled.Schedule)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_history_component_row_${order.orderNumber}"),
        color = Color(0xFFFAFAFC),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ComponentBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = ComponentCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = order.orderNumber.ifBlank { "Order" },
                        style = MaterialTheme.typography.titleSmall,
                        color = ComponentInk,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = effectiveStatus.title,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = effectiveStatus.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Text(
                text = order.itemsSummary.ifBlank { "${order.itemsCount} item(s)" },
                style = MaterialTheme.typography.bodySmall,
                color = ComponentInk,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = ComponentBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatHistoryDate(order.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = ComponentMuted
                )
                Text(
                    text = formatInr(order.totalInr),
                    style = MaterialTheme.typography.titleSmall,
                    color = ComponentInk,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

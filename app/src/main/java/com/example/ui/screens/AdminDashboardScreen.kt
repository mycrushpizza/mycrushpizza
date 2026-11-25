package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Coupon
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaCategory
import com.example.data.model.PizzaOrder
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.model.formatInr
import com.example.ui.components.DeliveryLiveGoogleMapCard
import com.example.ui.components.HajipurLiveMap
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.OnlineFoodImage
import com.example.ui.components.StoreContactHelper
import com.example.ui.components.VegNonVegIndicator
import com.example.ui.components.openCustomerLocationInGoogleMaps
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushCardSurface
import com.example.ui.theme.CrushElevatedSurface
import com.example.ui.theme.CrushMutedText
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushRedDark
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.theme.VegGreenBright
import com.example.ui.viewmodel.MainAppUiState

private val AdminPageBg = Color(0xFFF8FAFC)
private val AdminCardBg = Color.White
private val AdminSoftSurface = Color(0xFFF1F5F9)
private val AdminBorder = Color(0xFFE2E8F0)
private val AdminInk = Color(0xFF0F172A)
private val AdminMutedInk = Color(0xFF64748B)
private val AdminAmber = Color(0xFFB45309)
private val AdminAmberTint = Color(0xFFFFFBEB)
private val AdminGreen = Color(0xFF059669)
private val AdminGreenTint = Color(0xFFECFDF5)
private val AdminBlue = Color(0xFF0284C7)
private val AdminPurple = Color(0xFF7C3AED)
private val AdminRedTint = Color(0xFFFEF2F2)

private val AdminLightColorScheme = lightColorScheme(
    primary = CrushRed,
    onPrimary = Color.White,
    secondary = AdminAmber,
    onSecondary = Color.White,
    background = AdminPageBg,
    onBackground = AdminInk,
    surface = AdminCardBg,
    onSurface = AdminInk,
    surfaceVariant = AdminSoftSurface,
    onSurfaceVariant = AdminMutedInk,
    outline = Color(0xFFCBD5E1)
)

private enum class AdminSubTab(val label: String, val icon: ImageVector) {
    ORDERS("Orders", Icons.Filled.ReceiptLong),
    CUSTOMER_WALLET("Bonus Wallet", Icons.Filled.CardGiftcard),
    MENU_COUPONS("Menu & Coupons", Icons.Filled.RestaurantMenu),
    LIVE_TRACKING("Live Map", Icons.Filled.LocationOn),
    RAZORPAY_PAYMENT("Payments", Icons.Filled.CreditCard),
    STORE_SETTINGS("Store & Alerts", Icons.Filled.Settings)
}

private enum class AdminOrderFilter(val label: String) {
    ALL("All"),
    PENDING("New"),
    ACTIVE("Active"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

@Composable
fun AdminDashboardScreen(
    uiState: MainAppUiState,
    onAcceptOrderAlert: (PizzaOrder) -> Unit,
    onDeclineOrderAlert: (PizzaOrder) -> Unit,
    onDoneOrderAlert: (PizzaOrder) -> Unit,
    onUpdateOrderStatus: (PizzaOrder, OrderStatus) -> Unit,
    onSaveMenuItem: (MenuItem) -> Unit,
    onDeleteMenuItem: (String) -> Unit,
    onSaveCoupon: (Coupon) -> Unit,
    onSaveStoreConfig: (StoreConfig) -> Unit,
    onSendCustomerNotification: (String, String, String, String) -> Unit,
    onBackToCustomerMenu: () -> Unit,
    onAdminCreditCustomerWallet: (String, Int, Boolean, String) -> Unit = { _, _, _, _ -> },
    onAdminDeductCustomerWallet: (String, Int, Boolean, String) -> Unit = { _, _, _, _ -> },
    onClearOldOrders: (Boolean) -> Unit = {},
    onDeleteOrder: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminSubTab.ORDERS) }
    var selectedOrderFilter by remember { mutableStateOf(AdminOrderFilter.ALL) }
    var orderSearchQuery by remember { mutableStateOf("") }
    var showClearOldOrdersDialog by remember { mutableStateOf(false) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val allOrders = remember(uiState.allAdminOrders, uiState.customerOrders) {
        (uiState.allAdminOrders + uiState.customerOrders).distinctBy { it.id }
    }
    val ringingOrders = uiState.ringingAdminOrders
    val pendingCount = remember(allOrders, nowMillis) {
        allOrders.count { it.autoProgressedOrderStatus(nowMillis) == OrderStatus.CONFIRMED }
    }
    val activeKitchenCount = remember(allOrders, nowMillis) {
        allOrders.count {
            val st = it.autoProgressedOrderStatus(nowMillis)
            st == OrderStatus.PREPARING ||
                st == OrderStatus.READY ||
                st == OrderStatus.ASSIGNED ||
                st == OrderStatus.PICKED_UP ||
                st == OrderStatus.ON_THE_WAY
        }
    }
    val deliveredCount = remember(allOrders) {
        allOrders.count { it.orderStatus == OrderStatus.DELIVERED }
    }
    val cancelledCount = remember(allOrders) {
        allOrders.count { it.orderStatus == OrderStatus.CANCELLED }
    }
    val totalRevenueInr = remember(allOrders) {
        allOrders.filter { it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.totalInr }
    }
    val storePhone = uiState.storeConfig.storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY }

    val filteredOrders = remember(allOrders, selectedOrderFilter, orderSearchQuery, nowMillis) {
        val byStatus = when (selectedOrderFilter) {
            AdminOrderFilter.ALL -> allOrders
            AdminOrderFilter.PENDING -> allOrders.filter { it.autoProgressedOrderStatus(nowMillis) == OrderStatus.CONFIRMED }
            AdminOrderFilter.ACTIVE -> allOrders.filter {
                val st = it.autoProgressedOrderStatus(nowMillis)
                st == OrderStatus.PREPARING ||
                    st == OrderStatus.READY ||
                    st == OrderStatus.ASSIGNED ||
                    st == OrderStatus.PICKED_UP ||
                    st == OrderStatus.ON_THE_WAY
            }
            AdminOrderFilter.DELIVERED -> allOrders.filter { it.orderStatus == OrderStatus.DELIVERED }
            AdminOrderFilter.CANCELLED -> allOrders.filter { it.orderStatus == OrderStatus.CANCELLED }
        }
        val query = orderSearchQuery.trim()
        if (query.isEmpty()) {
            byStatus
        } else {
            byStatus.filter { ord ->
                ord.orderNumber.contains(query, ignoreCase = true) ||
                    ord.customerName.contains(query, ignoreCase = true) ||
                    ord.customerPhone.contains(query, ignoreCase = true) ||
                    ord.deliveryAddress.contains(query, ignoreCase = true) ||
                    ord.itemsSummary.contains(query, ignoreCase = true)
            }
        }
    }

    MaterialTheme(colorScheme = AdminLightColorScheme) {
    if (showClearOldOrdersDialog) {
        val completedOrCancelledCount = deliveredCount + cancelledCount
        AlertDialog(
            onDismissRequest = { showClearOldOrdersDialog = false },
            containerColor = AdminCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Remove Old Orders?",
                    style = MaterialTheme.typography.titleMedium,
                    color = AdminInk,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose which old orders you want to permanently remove from the Admin Panel:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminMutedInk
                    )
                    if (completedOrCancelledCount > 0) {
                        Button(
                            onClick = {
                                showClearOldOrdersDialog = false
                                onClearOldOrders(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AdminBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_confirm_clear_completed_orders_btn")
                        ) {
                            Text(
                                text = "Clear Completed & Cancelled ($completedOrCancelledCount)",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Button(
                        onClick = {
                            showClearOldOrdersDialog = false
                            onClearOldOrders(true)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrushRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_confirm_clear_all_orders_btn")
                    ) {
                        Text(
                            text = "Remove ALL Orders (${allOrders.size})",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showClearOldOrdersDialog = false }) {
                    Text("Cancel", color = AdminInk, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AdminPageBg)
            .navigationBarsPadding()
            .imePadding()
            .testTag("admin_dashboard_lazy_column"),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. PERSISTENT INCOMING ORDER RINGING BANNER (Rings until ACCEPT or DECLINE)
        if (ringingOrders.isNotEmpty()) {
            val topRingingOrder = ringingOrders.first()
            item(key = "ringing_alert_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_ringing_alert_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CrushRedDark),
                    border = BorderStroke(2.5.dp, CrushYellow)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    color = CrushYellow.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Ringing Order Alarm",
                                            tint = CrushYellow,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "NEW ORDER ALARM • ${ringingOrders.size} PENDING",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CrushYellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${topRingingOrder.orderNumber} • ${topRingingOrder.customerName.ifBlank { "Customer" }}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CrushWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatInr(topRingingOrder.totalInr)} (${topRingingOrder.paymentMethod})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VegGreenBright,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Surface(
                            color = CrushBlack.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CrushYellow.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Customer: ${topRingingOrder.customerName} (${topRingingOrder.customerPhone})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrushYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Address: ${topRingingOrder.deliveryAddress}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrushWhite
                                )
                                if (topRingingOrder.landmark.isNotBlank()) {
                                    Text(
                                        text = "Landmark: ${topRingingOrder.landmark}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrushYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Items: ${topRingingOrder.itemsSummary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VegGreenBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onAcceptOrderAlert(topRingingOrder) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VegGreenBright,
                                    contentColor = CrushBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_alert_accept_btn")
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Accept Order", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ACCEPT ORDER", fontWeight = FontWeight.ExtraBold)
                            }

                            Button(
                                onClick = { onDeclineOrderAlert(topRingingOrder) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrushBlack,
                                    contentColor = CrushRed
                                ),
                                border = BorderStroke(1.5.dp, CrushYellow),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_alert_decline_btn")
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Decline Order", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DECLINE", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Executive Command Header + Live Store Switch + 4-Metric KPI Strip
        item(key = "admin_command_header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Light Executive Command Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF1F2),
                                        Color(0xFFFFFBEB),
                                        Color.White
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ADMIN COMMAND CENTER",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = CrushRed,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "My Crush Pizza Hajipur • $storePhone",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdminMutedInk,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedButton(
                                    onClick = onBackToCustomerMenu,
                                    border = BorderStroke(1.2.dp, CrushRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Storefront,
                                        contentDescription = "Back to Storefront",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Storefront", fontWeight = FontWeight.Bold)
                                }
                            }

                            // 1-Tap Live Store Open / Closed Toggle Pill
                            Surface(
                                color = if (uiState.storeConfig.isOpen) AdminGreenTint else AdminRedTint,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (uiState.storeConfig.isOpen) AdminGreen else CrushRed
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_header_store_status_bar")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (uiState.storeConfig.isOpen) AdminGreen else CrushRed
                                                )
                                        )
                                        Column {
                                            Text(
                                                text = if (uiState.storeConfig.isOpen) {
                                                    "STORE OPEN • ACCEPTING ORDERS"
                                                } else {
                                                    "STORE CLOSED • BROWSE & CART ONLY"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (uiState.storeConfig.isOpen) AdminGreen else Color(0xFFB91C1C),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = if (uiState.storeConfig.isOpen) {
                                                    "Hours: ${uiState.storeConfig.openTime} – ${uiState.storeConfig.closeTime} • Orders live"
                                                } else {
                                                    "Customers can view menu & add to cart, checkout paused"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminMutedInk
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = uiState.storeConfig.isOpen,
                                        onCheckedChange = { openNow ->
                                            onSaveStoreConfig(uiState.storeConfig.copy(isOpen = openNow))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = AdminGreen,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = CrushRed
                                        ),
                                        modifier = Modifier.testTag("admin_quick_store_open_switch")
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = AdminBorder)

                    // Body: 4 KPI Tiles + Quick Actions
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminKpiBox(
                                label = "New",
                                value = pendingCount.toString(),
                                accentColor = if (pendingCount > 0) AdminAmber else AdminMutedInk,
                                modifier = Modifier.weight(1f)
                            )
                            AdminKpiBox(
                                label = "Active",
                                value = activeKitchenCount.toString(),
                                accentColor = AdminBlue,
                                modifier = Modifier.weight(1f)
                            )
                            AdminKpiBox(
                                label = "Delivered",
                                value = deliveredCount.toString(),
                                accentColor = AdminGreen,
                                modifier = Modifier.weight(1f)
                            )
                            AdminKpiBox(
                                label = "Revenue",
                                value = formatInr(totalRevenueInr),
                                accentColor = CrushRed,
                                modifier = Modifier.weight(1.25f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedTab = AdminSubTab.ORDERS
                                    selectedOrderFilter = if (pendingCount > 0) AdminOrderFilter.PENDING else AdminOrderFilter.ACTIVE
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AdminGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1.35f)
                                    .height(48.dp)
                                    .testTag("admin_quick_active_orders_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = "Manage Orders",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTIVE ORDERS (${pendingCount + activeKitchenCount})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTab = AdminSubTab.RAZORPAY_PAYMENT }
                                    .testTag("admin_header_razorpay_banner"),
                                shape = RoundedCornerShape(12.dp),
                                color = AdminAmberTint,
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CreditCard,
                                        contentDescription = "Payment Setup",
                                        tint = AdminAmber,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "COD ONLY",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AdminAmber,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Admin Sub-Tabs Navigation Strip with Icons & Live Counts
        item(key = "admin_tabs_row") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AdminSubTab.entries, key = { it.name }) { tab ->
                    val selected = selectedTab == tab
                    val badgeSuffix = when (tab) {
                        AdminSubTab.ORDERS -> " (${allOrders.size})"
                        AdminSubTab.MENU_COUPONS -> " (${uiState.menuItems.size})"
                        else -> ""
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.testTag("admin_tab_${tab.name}"),
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (selected) Color.White else CrushRed,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "${tab.label}$badgeSuffix",
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrushRed,
                            selectedLabelColor = Color.White,
                            containerColor = AdminCardBg,
                            labelColor = AdminInk
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = AdminBorder,
                            selectedBorderColor = CrushRed
                        )
                    )
                }
            }
        }

        // 4. Tab Content
        when (selectedTab) {
            AdminSubTab.ORDERS -> {
                // Search & Status Filter Bar
                item(key = "admin_orders_filter_bar") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, AdminBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = orderSearchQuery,
                                onValueChange = { orderSearchQuery = it },
                                placeholder = { Text("Search order #, customer, phone or address...") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = "Search Orders", tint = CrushRed)
                                },
                                trailingIcon = {
                                    if (orderSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { orderSearchQuery = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Clear Search", tint = AdminMutedInk)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_order_search_input")
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(AdminOrderFilter.entries, key = { it.name }) { filter ->
                                    val count = when (filter) {
                                        AdminOrderFilter.ALL -> allOrders.size
                                        AdminOrderFilter.PENDING -> pendingCount
                                        AdminOrderFilter.ACTIVE -> activeKitchenCount
                                        AdminOrderFilter.DELIVERED -> deliveredCount
                                        AdminOrderFilter.CANCELLED -> cancelledCount
                                    }
                                    val isSelected = selectedOrderFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedOrderFilter = filter },
                                        modifier = Modifier.testTag("admin_order_filter_${filter.name}"),
                                        label = {
                                            Text(
                                                text = "${filter.label} ($count)",
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CrushRed,
                                            selectedLabelColor = Color.White,
                                            containerColor = AdminSoftSurface,
                                            labelColor = AdminInk
                                        )
                                    )
                                }
                            }

                            if (allOrders.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${filteredOrders.size} order${if (filteredOrders.size != 1) "s" else ""} shown",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AdminMutedInk,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    OutlinedButton(
                                        onClick = { showClearOldOrdersDialog = true },
                                        border = BorderStroke(1.2.dp, CrushRed.copy(alpha = 0.7f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = AdminRedTint,
                                            contentColor = CrushRed
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("admin_clear_old_orders_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Clear Old Orders",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Clear Old Orders",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredOrders.isEmpty()) {
                    item(key = "admin_orders_empty") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                            border = BorderStroke(1.dp, AdminBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = "No Orders",
                                    tint = CrushRed.copy(alpha = 0.65f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = if (orderSearchQuery.isNotBlank()) {
                                        "No orders matching \"$orderSearchQuery\""
                                    } else {
                                        "No ${selectedOrderFilter.label.lowercase()} orders right now"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AdminInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "New Hajipur orders appear here in real time with an audible kitchen alert.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AdminMutedInk
                                )
                            }
                        }
                    }
                } else {
                    items(filteredOrders, key = { it.id }) { order ->
                        AdminOrderManagementCard(
                            order = order,
                            effectiveStatus = order.autoProgressedOrderStatus(nowMillis),
                            storePhone = storePhone,
                            onAccept = { onAcceptOrderAlert(order) },
                            onDecline = { onDeclineOrderAlert(order) },
                            onUpdateStatus = { nextStatus -> onUpdateOrderStatus(order, nextStatus) },
                            onDeleteOrder = { onDeleteOrder(order.id) }
                        )
                    }
                }
            }

            AdminSubTab.CUSTOMER_WALLET -> {
                item(key = "admin_wallet_panel") {
                    AdminCustomerBonusWalletPanel(
                        currentUserId = uiState.currentUserId,
                        currentUserName = uiState.currentUserName,
                        currentBonusBalanceInr = uiState.bonusBalanceInr,
                        allUsers = uiState.allUsersForAdmin,
                        allOrders = allOrders,
                        onCreditCustomerWallet = onAdminCreditCustomerWallet,
                        onDeductCustomerWallet = onAdminDeductCustomerWallet
                    )
                }
            }

            AdminSubTab.MENU_COUPONS -> {
                item(key = "admin_menu_coupons_panel") {
                    AdminMenuAndCouponEditor(
                        menuItems = uiState.menuItems,
                        coupons = uiState.coupons,
                        onSaveMenuItem = onSaveMenuItem,
                        onDeleteMenuItem = onDeleteMenuItem,
                        onSaveCoupon = onSaveCoupon
                    )
                }
            }

            AdminSubTab.LIVE_TRACKING -> {
                item(key = "admin_live_map_panel") {
                    AdminLiveOrderTrackingPanel(orders = allOrders)
                }
            }

            AdminSubTab.RAZORPAY_PAYMENT -> {
                item(key = "admin_razorpay_panel") {
                    AdminRazorpayIntegrationPanel(
                        storeConfig = uiState.storeConfig,
                        onSaveStoreConfig = onSaveStoreConfig
                    )
                }
            }

            AdminSubTab.STORE_SETTINGS -> {
                item(key = "admin_store_settings_panel") {
                    AdminStoreSettingsAndNotificationPanel(
                        storeConfig = uiState.storeConfig,
                        onSaveStoreConfig = onSaveStoreConfig,
                        onSendNotification = onSendCustomerNotification
                    )
                }
            }
        }
    }
    }
}

/**
 * Dedicated Admin Panel section to Manage Customer BONUS Cash Balance (Deposit Cash removed as requested).
 * - Supports selecting from registered customers, recent order customers, or typing any Customer ID
 * - Add, Cut / Deduct, or Reset Bonus Cash to ₹0
 */
@Composable
private fun AdminCustomerBonusWalletPanel(
    currentUserId: String,
    currentUserName: String,
    currentBonusBalanceInr: Int,
    allUsers: List<UserProfile>,
    allOrders: List<PizzaOrder>,
    onCreditCustomerWallet: (String, Int, Boolean, String) -> Unit,
    onDeductCustomerWallet: (String, Int, Boolean, String) -> Unit = { _, _, _, _ -> }
) {
    // Strictly exclude anyone who has Admin Panel access (role ADMIN, bootstrapped admin emails, or current signed-in Admin)
    val adminUidSet = remember(allUsers, currentUserId) {
        buildSet {
            if (currentUserId.isNotBlank()) add(currentUserId)
            allUsers.filter { com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(it) }.forEach { admin ->
                if (admin.id.isNotBlank()) add(admin.id)
                if (admin.userId.isNotBlank()) add(admin.userId)
            }
        }
    }

    val nonAdminUsers = remember(allUsers, adminUidSet) {
        allUsers.filter { user ->
            val uid = user.userId.ifBlank { user.id }
            uid.isNotBlank() &&
                uid !in adminUidSet &&
                user.id !in adminUidSet &&
                !com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(user)
        }
    }

    val quickCustomers = remember(nonAdminUsers, allOrders, adminUidSet) {
        val fromUsers = nonAdminUsers.map {
            it.userId.ifBlank { it.id } to it.displayName.ifBlank { "Customer" }
        }
        val fromOrders = allOrders
            .filter { it.customerId.isNotBlank() && it.customerId !in adminUidSet }
            .map { it.customerId to it.customerName.ifBlank { "Customer" } }
        (fromUsers + fromOrders)
            .filter { it.first.isNotBlank() && it.first !in adminUidSet }
            .distinctBy { it.first }
    }

    var targetCustomerId by remember { mutableStateOf("") }
    var targetCustomerName by remember { mutableStateOf("") }
    var customerSearchName by remember { mutableStateOf("") }
    var creditAmountText by remember { mutableStateOf("100") }

    LaunchedEffect(quickCustomers) {
        if (quickCustomers.isEmpty()) {
            targetCustomerId = ""
            targetCustomerName = ""
        } else if (quickCustomers.none { it.first == targetCustomerId }) {
            val firstCustomer = quickCustomers.first()
            targetCustomerId = firstCustomer.first
            targetCustomerName = firstCustomer.second
        }
    }

    val filteredQuickCustomers = remember(quickCustomers, customerSearchName) {
        val q = customerSearchName.trim()
        if (q.isBlank()) quickCustomers else quickCustomers.filter { (_, name) ->
            name.contains(q, ignoreCase = true)
        }
    }

    val filteredUsersList = remember(nonAdminUsers, customerSearchName) {
        val q = customerSearchName.trim()
        if (q.isBlank()) nonAdminUsers else nonAdminUsers.filter { user ->
            user.displayName.contains(q, ignoreCase = true)
        }
    }

    val selectedUser = nonAdminUsers.find {
        it.userId.equals(targetCustomerId, ignoreCase = true) || it.id.equals(targetCustomerId, ignoreCase = true)
    }
    val hasSelectedCustomer = targetCustomerId.isNotBlank() && quickCustomers.any { it.first == targetCustomerId }
    val displayedCustomerName = if (hasSelectedCustomer) {
        selectedUser?.displayName?.ifBlank { targetCustomerName } ?: targetCustomerName.ifBlank { "Customer" }
    } else {
        "No Customer Selected"
    }
    val displayedBonus = if (hasSelectedCustomer) {
        selectedUser?.bonusBalanceInr ?: 0
    } else {
        0
    }

    val presetCreditAmounts = listOf(50, 100, 250, 500, 1000, 2000)
    val parsedCreditAmount = creditAmountText.filter { it.isDigit() }.toIntOrNull() ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_customer_wallet_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = AdminAmberTint,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = "Customer Bonus Wallet",
                            tint = AdminAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "CUSTOMER BONUS CASH WALLET",
                        style = MaterialTheme.typography.titleMedium,
                        color = CrushRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Select a customer by name to Add or Cut Bonus Cash.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminMutedInk
                    )
                }
            }

            // Search Customer by Name (no raw UID/ID shown anywhere!)
            OutlinedTextField(
                value = customerSearchName,
                onValueChange = { customerSearchName = it },
                label = { Text("Search Customer Name") },
                placeholder = { Text("Type customer name...") },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = "Search Customer", tint = CrushRed)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_wallet_customer_name_search"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Select Customer Chips (Customer Name ONLY)
            if (filteredQuickCustomers.isNotEmpty()) {
                Text(
                    text = "Select Customer:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminAmber,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredQuickCustomers, key = { it.first }) { (cid, cname) ->
                        val isSelected = targetCustomerId == cid
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                targetCustomerId = cid
                                targetCustomerName = cname
                            },
                            label = {
                                Text(cname, maxLines = 1, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminSoftSurface,
                                labelColor = AdminInk
                            )
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AdminSoftSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Text(
                        text = if (quickCustomers.isEmpty()) {
                            "No customers found yet. New customers will appear here automatically."
                        } else {
                            "No customer matches \"$customerSearchName\"."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminMutedInk,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Full-Width Selected Customer Name & Bonus Cash Display Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_view_bonus_box"),
                color = AdminAmberTint,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayedCustomerName,
                            style = MaterialTheme.typography.titleSmall,
                            color = AdminInk,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "CURRENT BONUS CASH",
                            style = MaterialTheme.typography.labelSmall,
                            color = AdminAmber,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = formatInr(displayedBonus),
                            style = MaterialTheme.typography.headlineSmall,
                            color = AdminInk,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Reward Cash",
                            style = MaterialTheme.typography.labelSmall,
                            color = AdminAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Preset Amounts + "Full Current Bonus" Chip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (displayedBonus > 0) {
                    item(key = "full_bal") {
                        FilterChip(
                            selected = parsedCreditAmount == displayedBonus,
                            onClick = { creditAmountText = displayedBonus.toString() },
                            modifier = Modifier.testTag("admin_cut_full_balance_chip"),
                            label = {
                                Text("Full (${formatInr(displayedBonus)})", fontWeight = FontWeight.ExtraBold)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminAmberTint,
                                labelColor = AdminAmber
                            )
                        )
                    }
                }
                items(presetCreditAmounts, key = { it }) { preset ->
                    FilterChip(
                        selected = parsedCreditAmount == preset,
                        onClick = { creditAmountText = preset.toString() },
                        label = {
                            Text(formatInr(preset), fontWeight = FontWeight.Bold)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrushRed,
                            selectedLabelColor = Color.White,
                            containerColor = AdminSoftSurface,
                            labelColor = AdminInk
                        )
                    )
                }
            }

            OutlinedTextField(
                value = creditAmountText,
                onValueChange = { creditAmountText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Enter Bonus Amount to Add or Cut (₹)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_wallet_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (hasSelectedCustomer && parsedCreditAmount > 0) {
                            onCreditCustomerWallet(
                                targetCustomerId,
                                parsedCreditAmount,
                                true,
                                displayedCustomerName
                            )
                        }
                    },
                    enabled = hasSelectedCustomer && parsedCreditAmount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("admin_credit_wallet_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add ${formatInr(parsedCreditAmount)}",
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = {
                        if (hasSelectedCustomer && parsedCreditAmount > 0) {
                            onDeductCustomerWallet(
                                targetCustomerId,
                                parsedCreditAmount,
                                true,
                                displayedCustomerName
                            )
                        }
                    },
                    enabled = hasSelectedCustomer && parsedCreditAmount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("admin_deduct_wallet_btn")
                ) {
                    Icon(Icons.Filled.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cut ${formatInr(parsedCreditAmount)}",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (hasSelectedCustomer && displayedBonus > 0) {
                OutlinedButton(
                    onClick = {
                        onDeductCustomerWallet(
                            targetCustomerId,
                            displayedBonus,
                            true,
                            displayedCustomerName
                        )
                    },
                    border = BorderStroke(1.5.dp, CrushRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_reset_customer_wallet_btn")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset ${displayedCustomerName}'s Bonus to ₹0",
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (filteredUsersList.isNotEmpty()) {
                HorizontalDivider(color = AdminBorder)
                Text(
                    text = "All Customers (${filteredUsersList.size}):",
                    style = MaterialTheme.typography.labelLarge,
                    color = AdminInk,
                    fontWeight = FontWeight.ExtraBold
                )
                filteredUsersList.forEach { user ->
                    val userUid = user.userId.ifBlank { user.id }
                    val isSelected = targetCustomerId == userUid
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                targetCustomerId = userUid
                                targetCustomerName = user.displayName.ifBlank { "Customer" }
                            },
                        color = if (isSelected) Color(0xFFFFF1F2) else AdminSoftSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CrushRed else AdminBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = if (isSelected) CrushRed else Color(0xFFE2E8F0),
                                    shape = CircleShape,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = (user.displayName.trim().firstOrNull()?.uppercaseChar() ?: 'C').toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) Color.White else AdminInk,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = user.displayName.ifBlank { "Customer" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AdminInk,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                color = AdminAmberTint,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Bonus: ${formatInr(user.bonusBalanceInr)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AdminAmber,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
private fun AdminKpiBox(
    label: String,
    value: String,
    accentColor: Color = AdminAmber,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AdminSoftSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AdminMutedInk,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminOrderManagementCard(
    order: PizzaOrder,
    effectiveStatus: OrderStatus = order.autoProgressedOrderStatus(),
    storePhone: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onDeleteOrder: () -> Unit = {}
) {
    val context = LocalContext.current
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    val customerDisplayName = order.customerName.ifBlank { "Customer" }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            containerColor = AdminCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Cancel Order ${order.orderNumber}?",
                    style = MaterialTheme.typography.titleMedium,
                    color = AdminInk,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to manually cancel $customerDisplayName's order (${formatInr(order.totalInr)})? The customer will be notified immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdminMutedInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmDialog = false
                        onUpdateStatus(OrderStatus.CANCELLED)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_manual_cancel_order_btn_${order.orderNumber}")
                ) {
                    Text("Yes, Cancel Order", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Order", color = AdminInk, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val statusColor = when (effectiveStatus) {
        OrderStatus.CONFIRMED -> AdminAmber
        OrderStatus.PREPARING, OrderStatus.READY -> AdminBlue
        OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> AdminPurple
        OrderStatus.DELIVERED -> AdminGreen
        OrderStatus.CANCELLED -> CrushRed
    }

    val nextStage: Pair<OrderStatus, String>? = when (effectiveStatus) {
        OrderStatus.PREPARING -> OrderStatus.READY to "Mark Order READY (Out of Oven)"
        OrderStatus.READY -> OrderStatus.ASSIGNED to "Mark PACKING"
        OrderStatus.ASSIGNED, OrderStatus.PICKED_UP -> OrderStatus.ON_THE_WAY to "Dispatch REACHING SOON"
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.orderNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (effectiveStatus == OrderStatus.CONFIRMED && !order.adminAlertAcknowledged) 2.dp else 1.dp,
            color = if (effectiveStatus == OrderStatus.CONFIRMED) CrushRed else AdminBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Order Header: Order # + Status Pill + Total Bill & Payment Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "${order.orderNumber} • $customerDisplayName",
                        style = MaterialTheme.typography.titleMedium,
                        color = AdminInk,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = effectiveStatus.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        if (order.customerPhone.isNotBlank()) {
                            Text(
                                text = order.customerPhone,
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminMutedInk,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatInr(order.totalInr),
                        style = MaterialTheme.typography.titleLarge,
                        color = AdminGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = order.paymentMethod.ifBlank { "COD" },
                        style = MaterialTheme.typography.labelSmall,
                        color = AdminMutedInk,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!order.isTerminal) {
                Surface(
                    color = AdminAmberTint,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Auto-advances 1 step every 5 min up to Reaching Soon • Mark Delivered manually",
                        style = MaterialTheme.typography.labelSmall,
                        color = AdminAmber,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (order.bonusUsedInr > 0) {
                Text(
                    text = "Bonus Cash Applied: -${formatInr(order.bonusUsedInr)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AdminGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            // Customer Delivery Details Box
            Surface(
                color = if (effectiveStatus != OrderStatus.CONFIRMED && effectiveStatus != OrderStatus.CANCELLED) {
                    AdminGreenTint
                } else {
                    AdminSoftSurface
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (effectiveStatus != OrderStatus.CONFIRMED && effectiveStatus != OrderStatus.CANCELLED) {
                        AdminGreen.copy(alpha = 0.45f)
                    } else {
                        AdminBorder
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_order_customer_details_${order.orderNumber}")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (effectiveStatus != OrderStatus.CONFIRMED && effectiveStatus != OrderStatus.CANCELLED) {
                                "ACCEPTED • CUSTOMER DETAILS"
                            } else {
                                "CUSTOMER DELIVERY DETAILS"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (effectiveStatus != OrderStatus.CONFIRMED && effectiveStatus != OrderStatus.CANCELLED) {
                                AdminGreen
                            } else {
                                AdminAmber
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (order.distanceKm > 0.0) {
                            Text(
                                text = "${String.format("%.1f", order.distanceKm)} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminMutedInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Address: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminInk,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (order.landmark.isNotBlank()) {
                        Text(
                            text = "Landmark: ${order.landmark}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AdminAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Items: ${order.itemsSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Primary Smart Kitchen Progression Button
            if (effectiveStatus == OrderStatus.CONFIRMED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("order_card_accept_btn_${order.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdminGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept Order", fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp)
                            .testTag("order_card_decline_btn_${order.orderNumber}"),
                        border = BorderStroke(1.5.dp, CrushRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (nextStage != null) {
                Button(
                    onClick = { onUpdateStatus(nextStage.first) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushYellow,
                        contentColor = CrushBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("order_card_next_stage_btn_${order.orderNumber}")
                ) {
                    Text(
                        text = nextStage.second,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Manual Delivery & Cancel Action Buttons (Available on every active order)
            if (order.orderStatus != OrderStatus.DELIVERED && order.orderStatus != OrderStatus.CANCELLED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdminGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("order_card_manual_deliver_btn_${order.orderNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Mark Delivered",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark Delivered",
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = { showCancelConfirmDialog = true },
                        border = BorderStroke(1.5.dp, CrushRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("order_card_manual_cancel_btn_${order.orderNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel Order",
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onDeleteOrder,
                    border = BorderStroke(1.2.dp, CrushRed.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AdminRedTint,
                        contentColor = CrushRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("order_card_delete_btn_${order.orderNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove Old Order",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remove Order from Panel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Quick Contact + Google Maps Route Row (Clean 3-button strip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val targetPhone = order.customerPhone.ifBlank { storePhone }
                        StoreContactHelper.dialStore(context, targetPhone)
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, AdminAmber),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminAmber)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Call",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = { StoreContactHelper.openOrderOnWhatsApp(context, order) },
                    modifier = Modifier.weight(1.25f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, AdminGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = {
                        openCustomerLocationInGoogleMaps(
                            context = context,
                            lat = order.customerLat,
                            lng = order.customerLng,
                            label = "$customerDisplayName (${order.orderNumber})"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, AdminBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminBlue)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Route", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Map",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Manual Stage Override Strip (Including Delivered & Cancelled)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(OrderStatus.trackingSteps + OrderStatus.CANCELLED, key = { it.code }) { statusStep ->
                    val isCurrent = effectiveStatus == statusStep
                    val chipSelectedColor = when (statusStep) {
                        OrderStatus.DELIVERED -> AdminGreen
                        OrderStatus.CANCELLED -> CrushRed
                        else -> CrushRed
                    }
                    FilterChip(
                        selected = isCurrent,
                        onClick = {
                            if (statusStep == OrderStatus.CANCELLED && !isCurrent) {
                                showCancelConfirmDialog = true
                            } else {
                                onUpdateStatus(statusStep)
                            }
                        },
                        label = { Text(statusStep.title, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipSelectedColor,
                            selectedLabelColor = Color.White,
                            containerColor = AdminSoftSurface,
                            labelColor = if (statusStep == OrderStatus.CANCELLED) CrushRed else AdminInk
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminLiveOrderTrackingPanel(
    orders: List<PizzaOrder>
) {
    val context = LocalContext.current
    val activeOrders = orders.filter { !it.isTerminal }
    var selectedOrder by remember(activeOrders) { mutableStateOf(activeOrders.firstOrNull() ?: orders.firstOrNull()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_live_tracking_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ADMIN LIVE ORDER DELIVERY MAP",
                style = MaterialTheme.typography.titleMedium,
                color = CrushRed,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Real-time Hajipur map (10 km radius) showing active orders and customer drop pins.",
                style = MaterialTheme.typography.bodySmall,
                color = AdminMutedInk
            )

            if (orders.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(orders, key = { it.id }) { ord ->
                        val isSelected = selectedOrder?.id == ord.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedOrder = ord },
                            label = { Text("${ord.orderNumber} (${ord.autoProgressedOrderStatus().title})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminSoftSurface,
                                labelColor = AdminInk
                            )
                        )
                    }
                }
            }

            val target = selectedOrder
            if (target != null) {
                DeliveryLiveGoogleMapCard(
                    order = target,
                    mapHeight = 250.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${target.orderNumber} • ${target.customerName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = AdminInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = target.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = AdminMutedInk
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            openCustomerLocationInGoogleMaps(
                                context = context,
                                lat = target.customerLat,
                                lng = target.customerLng,
                                label = "${target.customerName} (${target.orderNumber})"
                            )
                        },
                        border = BorderStroke(1.dp, AdminBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminBlue)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                HajipurLiveMap(
                    customerLat = 25.6892,
                    customerLng = 85.2145,
                    showDeliveryRider = false,
                    mapHeight = 220.dp
                )
            }
        }
    }
}

@Composable
private fun AdminMenuAndCouponEditor(
    menuItems: List<MenuItem>,
    coupons: List<Coupon>,
    onSaveMenuItem: (MenuItem) -> Unit,
    onDeleteMenuItem: (String) -> Unit,
    onSaveCoupon: (Coupon) -> Unit
) {
    var showAddDishForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PizzaCategory.VEG_PIZZA.id) }
    var isVegDish by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var priceReg by remember { mutableStateOf("199") }
    var priceMed by remember { mutableStateOf("349") }
    var priceLrg by remember { mutableStateOf("499") }

    var menuSearchQuery by remember { mutableStateOf("") }
    var selectedMenuCategory by remember { mutableStateOf("ALL") }
    var visibleMenuLimit by remember { mutableIntStateOf(15) }
    var editingDish by remember { mutableStateOf<MenuItem?>(null) }

    var couponCode by remember { mutableStateOf("") }
    var couponTitle by remember { mutableStateOf("") }
    var couponDiscount by remember { mutableStateOf("50") }

    val filteredMenuItems = remember(menuItems, menuSearchQuery, selectedMenuCategory) {
        val byCat = if (selectedMenuCategory == "ALL") {
            menuItems
        } else {
            menuItems.filter { it.category.equals(selectedMenuCategory, ignoreCase = true) }
        }
        val q = menuSearchQuery.trim()
        if (q.isEmpty()) byCat else byCat.filter {
            it.title.contains(q, ignoreCase = true) || it.category.contains(q, ignoreCase = true)
        }
    }

    // Quick Dish Price & Title Editor Dialog
    editingDish?.let { dishToEdit ->
        var editTitle by remember(dishToEdit.id) { mutableStateOf(dishToEdit.title) }
        var editReg by remember(dishToEdit.id) { mutableStateOf(dishToEdit.priceRegularInr.toString()) }
        var editMed by remember(dishToEdit.id) { mutableStateOf(dishToEdit.priceMediumInr.toString()) }
        var editLrg by remember(dishToEdit.id) { mutableStateOf(dishToEdit.priceLargeInr.toString()) }
        var editImageUrl by remember(dishToEdit.id) { mutableStateOf(dishToEdit.imageUrl) }

        AlertDialog(
            onDismissRequest = { editingDish = null },
            containerColor = AdminCardBg,
            title = {
                Text(
                    text = "Edit Dish • ${dishToEdit.title}",
                    color = AdminInk,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Dish Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editReg,
                            onValueChange = { editReg = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Reg ₹") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editMed,
                            onValueChange = { editMed = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Med ₹") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editLrg,
                            onValueChange = { editLrg = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Lrg ₹") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = editImageUrl,
                        onValueChange = { editImageUrl = it },
                        label = { Text("Custom Online Image URL (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveMenuItem(
                            dishToEdit.copy(
                                title = editTitle.trim().ifBlank { dishToEdit.title },
                                priceRegularInr = editReg.toIntOrNull() ?: dishToEdit.priceRegularInr,
                                priceMediumInr = editMed.toIntOrNull() ?: dishToEdit.priceMediumInr,
                                priceLargeInr = editLrg.toIntOrNull() ?: dishToEdit.priceLargeInr,
                                imageUrl = editImageUrl.trim()
                            )
                        )
                        editingDish = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Changes", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDish = null }) {
                    Text("Cancel", color = AdminInk)
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Collapsible Add New Dish Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddDishForm = !showAddDishForm },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = CrushRed)
                        Text(
                            text = "ADD NEW MENU DISH (CLOUD SYNC)",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrushRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Icon(
                        imageVector = if (showAddDishForm) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle Add Dish",
                        tint = CrushRed
                    )
                }

                AnimatedVisibility(visible = showAddDishForm) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Dish Title (e.g. Hajipur Paneer Supreme)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Toppings") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Category:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AdminInk,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(PizzaCategory.entries, key = { it.id }) { cat ->
                                FilterChip(
                                    selected = category == cat.id,
                                    onClick = { category = cat.id },
                                    label = { Text(cat.id, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrushRed,
                                        selectedLabelColor = Color.White,
                                        containerColor = AdminSoftSurface,
                                        labelColor = AdminInk
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isVegDish) "Pure Veg Dish" else "Non-Veg Dish",
                                color = if (isVegDish) AdminGreen else CrushRed,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(checked = isVegDish, onCheckedChange = { isVegDish = it })
                        }
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Online Food Image URL (https://...)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_dish_image_url_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = priceReg,
                                onValueChange = { priceReg = it.filter { c -> c.isDigit() } },
                                label = { Text("Regular ₹") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = priceMed,
                                onValueChange = { priceMed = it.filter { c -> c.isDigit() } },
                                label = { Text("Medium ₹") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = priceLrg,
                                onValueChange = { priceLrg = it.filter { c -> c.isDigit() } },
                                label = { Text("Large ₹") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSaveMenuItem(
                                        MenuItem(
                                            title = title.trim(),
                                            description = description.ifBlank { "Freshly prepared at My Crush Pizza – Hajipur" },
                                            category = category,
                                            priceRegularInr = priceReg.toIntOrNull() ?: 199,
                                            priceMediumInr = priceMed.toIntOrNull() ?: 349,
                                            priceLargeInr = priceLrg.toIntOrNull() ?: 499,
                                            imageKey = "hero_pizza",
                                            imageUrl = imageUrl.trim(),
                                            isVeg = isVegDish
                                        )
                                    )
                                    title = ""
                                    description = ""
                                    imageUrl = ""
                                    showAddDishForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrushRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Dish to Hajipur Menu", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // 2. Full Searchable Menu Catalog Manager (All 212 Dishes!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MANAGE ALL DISHES (${filteredMenuItems.size} / ${menuItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrushRed,
                    fontWeight = FontWeight.ExtraBold
                )
                OutlinedTextField(
                    value = menuSearchQuery,
                    onValueChange = {
                        menuSearchQuery = it
                        visibleMenuLimit = 15
                    },
                    placeholder = { Text("Search any dish (e.g. Shahi Paneer, Margherita)...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CrushRed) },
                    trailingIcon = {
                        if (menuSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { menuSearchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = AdminMutedInk)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_menu_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item(key = "cat_all") {
                        FilterChip(
                            selected = selectedMenuCategory == "ALL",
                            onClick = {
                                selectedMenuCategory = "ALL"
                                visibleMenuLimit = 15
                            },
                            label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminSoftSurface,
                                labelColor = AdminInk
                            )
                        )
                    }
                    items(PizzaCategory.entries, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedMenuCategory == cat.id,
                            onClick = {
                                selectedMenuCategory = cat.id
                                visibleMenuLimit = 15
                            },
                            label = { Text(cat.id, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminSoftSurface,
                                labelColor = AdminInk
                            )
                        )
                    }
                }

                filteredMenuItems.take(visibleMenuLimit).forEach { item ->
                    Surface(
                        color = AdminSoftSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                OnlineFoodImage(
                                    imageKey = item.imageKey,
                                    imageUrl = item.imageUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    VegNonVegIndicator(isVeg = item.isVeg)
                                    Text(
                                        text = item.title,
                                        color = AdminInk,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = if (item.supportsMultipleSizes()) {
                                        "${item.category} • ${formatInr(item.priceRegularInr)} / ${formatInr(item.priceMediumInr)} / ${formatInr(item.priceLargeInr)}"
                                    } else {
                                        "${item.category} • ${formatInr(item.priceRegularInr)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AdminAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { editingDish = item }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit ${item.title}", tint = AdminAmber)
                            }
                            IconButton(onClick = { onDeleteMenuItem(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${item.title}", tint = CrushRed)
                            }
                        }
                    }
                }

                if (filteredMenuItems.size > visibleMenuLimit) {
                    OutlinedButton(
                        onClick = { visibleMenuLimit += 25 },
                        border = BorderStroke(1.dp, CrushRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Load More Dishes (${filteredMenuItems.size - visibleMenuLimit} remaining)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Coupons & Discounts Manager
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = CrushRed)
                    Text(
                        text = "MANAGE COUPONS & DISCOUNTS (${coupons.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = CrushRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it.uppercase() },
                        label = { Text("Code (e.g. BIHAR75)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = couponDiscount,
                        onValueChange = { couponDiscount = it.filter { c -> c.isDigit() } },
                        label = { Text("Flat ₹ Off") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = couponTitle,
                    onValueChange = { couponTitle = it },
                    label = { Text("Offer Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (couponCode.isNotBlank()) {
                            val disc = couponDiscount.toIntOrNull() ?: 50
                            onSaveCoupon(
                                Coupon(
                                    id = couponCode,
                                    code = couponCode,
                                    title = couponTitle.ifBlank { "Flat ₹$disc OFF" },
                                    description = "Save ₹$disc at My Crush Pizza – Hajipur",
                                    discountType = "FLAT",
                                    discountValue = disc,
                                    minOrderInr = 249,
                                    maxDiscountInr = disc,
                                    isActive = true
                                )
                            )
                            couponCode = ""
                            couponTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Coupon Code", fontWeight = FontWeight.ExtraBold)
                }

                if (coupons.isNotEmpty()) {
                    HorizontalDivider(color = AdminBorder)
                    coupons.forEach { coupon ->
                        Surface(
                            color = AdminSoftSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AdminBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${coupon.code} • ${coupon.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = AdminInk,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Min Order: ${formatInr(coupon.minOrderInr)} • Max Off: ${formatInr(coupon.maxDiscountInr)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AdminMutedInk
                                    )
                                }
                                Surface(
                                    color = if (coupon.isActive) AdminGreenTint else AdminRedTint,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (coupon.isActive) "ACTIVE" else "PAUSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (coupon.isActive) AdminGreen else CrushRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
private fun AdminStoreSettingsAndNotificationPanel(
    storeConfig: StoreConfig,
    onSaveStoreConfig: (StoreConfig) -> Unit,
    onSendNotification: (String, String, String, String) -> Unit
) {
    var storePhone by remember(storeConfig.storePhone) {
        mutableStateOf(storeConfig.storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY })
    }
    var openTime by remember(storeConfig.openTime) { mutableStateOf(storeConfig.openTime) }
    var closeTime by remember(storeConfig.closeTime) { mutableStateOf(storeConfig.closeTime) }
    var isOpen by remember(storeConfig.isOpen) { mutableStateOf(storeConfig.isOpen) }
    var deliveryFee by remember(storeConfig.deliveryChargeInr) {
        mutableStateOf(storeConfig.deliveryChargeInr.toString())
    }
    var notifTitle by remember { mutableStateOf("Hot Flash Deal at My Crush Pizza – Hajipur!") }
    var notifMessage by remember { mutableStateOf("Use code CRUSH50 for Flat ₹50 OFF on Cheese Burst Pizzas tonight!") }
    var notifImageUrl by remember { mutableStateOf("") }
    var notifImageKey by remember { mutableStateOf("crush_special") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            notifImageUrl = uri.toString()
        }
    }

    val presetBannerOptions = remember {
        listOf(
            "crush_special" to "My Crush Pizza Banner",
            "feast_combo" to "Party Feast Combo",
            "cheese_maggi" to "Special Cheese Maggi",
            "shakes_lava" to "Choco Lava & Shakes",
            "" to "No Preset Image"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RESTAURANT WHATSAPP, HOURS & DELIVERY CHARGES",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrushRed,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = if (isOpen) AdminGreenTint else AdminRedTint,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isOpen) AdminGreen else CrushRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOpen) "Store Open for Orders" else "Store Closed (Browse & Cart Only)",
                                color = if (isOpen) AdminGreen else Color(0xFFB91C1C),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isOpen) "Customers can place live orders" else "Customers can view menu & add to cart, checkout disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminMutedInk
                            )
                        }
                        Switch(
                            checked = isOpen,
                            onCheckedChange = { openNow ->
                                isOpen = openNow
                                onSaveStoreConfig(
                                    storeConfig.copy(
                                        isOpen = openNow,
                                        storePhone = storePhone.trim().ifBlank { storeConfig.storePhone },
                                        openTime = openTime.trim().ifBlank { storeConfig.openTime },
                                        closeTime = closeTime.trim().ifBlank { storeConfig.closeTime },
                                        deliveryChargeInr = deliveryFee.toIntOrNull() ?: storeConfig.deliveryChargeInr
                                    )
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AdminGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = CrushRed
                            ),
                            modifier = Modifier.testTag("admin_settings_store_open_switch")
                        )
                    }
                }
                OutlinedTextField(
                    value = storePhone,
                    onValueChange = { storePhone = it },
                    label = { Text("Store Call & WhatsApp Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = openTime,
                        onValueChange = { openTime = it },
                        label = { Text("Opening Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = closeTime,
                        onValueChange = { closeTime = it },
                        label = { Text("Closing Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = deliveryFee,
                    onValueChange = { deliveryFee = it.filter { c -> c.isDigit() } },
                    label = { Text("Standard Delivery Charge (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onSaveStoreConfig(
                            storeConfig.copy(
                                storePhone = storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY },
                                openTime = openTime,
                                closeTime = closeTime,
                                isOpen = isOpen,
                                deliveryChargeInr = deliveryFee.toIntOrNull() ?: 25
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Store Settings", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AdminCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SEND PUSH NOTIFICATION WITH IMAGE TO CUSTOMERS",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrushRed,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Add a title, message, and offer/food image (from Gallery, URL, or Quick Banner) to broadcast to all customers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdminMutedInk
                )
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { notifTitle = it },
                    label = { Text("Notification Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_title_input")
                )
                OutlinedTextField(
                    value = notifMessage,
                    onValueChange = { notifMessage = it },
                    label = { Text("Notification Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_message_input")
                )

                OutlinedTextField(
                    value = notifImageUrl,
                    onValueChange = { notifImageUrl = it },
                    label = { Text("Notification Image URL (https://... or Pick from Gallery)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = CrushRed)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notif_image_url_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        border = BorderStroke(1.dp, CrushRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_notif_pick_gallery_btn")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Gallery Image", fontWeight = FontWeight.Bold)
                    }
                    if (notifImageUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { notifImageUrl = "" },
                            border = BorderStroke(1.dp, CrushRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed)
                        ) {
                            Text("Clear URL", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Or Select Quick Food Banner Image:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminInk,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetBannerOptions, key = { it.first }) { (key, label) ->
                        val isSelected = notifImageKey == key && notifImageUrl.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                notifImageKey = key
                                if (key.isNotBlank()) notifImageUrl = ""
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrushRed,
                                selectedLabelColor = Color.White,
                                containerColor = AdminSoftSurface,
                                labelColor = AdminInk
                            )
                        )
                    }
                }

                if (notifImageUrl.isNotBlank() || notifImageKey.isNotBlank()) {
                    Surface(
                        color = AdminSoftSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_notif_image_preview")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "LIVE NOTIFICATION IMAGE PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminAmber,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                OnlineFoodImage(
                                    imageUrl = notifImageUrl,
                                    imageKey = notifImageKey.ifBlank { "crush_special" },
                                    contentDescription = notifTitle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (notifTitle.isNotBlank() && notifMessage.isNotBlank()) {
                            onSendNotification(
                                notifTitle.trim(),
                                notifMessage.trim(),
                                notifImageUrl.trim(),
                                notifImageKey.trim()
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_broadcast_notif_btn")
                ) {
                    Text("Broadcast Notification with Image", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun AdminRazorpayIntegrationPanel(
    storeConfig: StoreConfig,
    onSaveStoreConfig: (StoreConfig) -> Unit
) {
    var razorpayEnabled by remember(storeConfig.razorpayEnabled) {
        mutableStateOf(storeConfig.razorpayEnabled)
    }
    var razorpayMode by remember(storeConfig.razorpayMode) {
        mutableStateOf(storeConfig.razorpayMode.ifBlank { "LIVE" })
    }
    var razorpayKeyId by remember(storeConfig.razorpayKeyId) {
        mutableStateOf(storeConfig.razorpayKeyId)
    }
    var razorpayMerchantName by remember(storeConfig.razorpayMerchantName) {
        mutableStateOf(storeConfig.razorpayMerchantName.ifBlank { "My Crush Pizza – Hajipur" })
    }
    var razorpayPaymentLink by remember(storeConfig.razorpayPaymentLink) {
        mutableStateOf(storeConfig.razorpayPaymentLink)
    }
    var merchantUpiId by remember(storeConfig.merchantUpiId) {
        mutableStateOf(storeConfig.merchantUpiId.ifBlank { "mycrushpizza@upi" })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_razorpay_integration_panel"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = AdminGreenTint,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AdminGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CURRENT CHECKOUT MODE: CASH ON DELIVERY (COD) ONLY",
                        style = MaterialTheme.typography.labelLarge,
                        color = AdminGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Customer orders currently accept Cash on Delivery only (plus Bonus Cash discounts). You can pre-save your Razorpay credentials below for future online payments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminInk
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = razorpayMode == "LIVE",
                    onClick = { razorpayMode = "LIVE" },
                    label = { Text("LIVE MODE", fontWeight = FontWeight.ExtraBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminGreen,
                        selectedLabelColor = Color.White,
                        containerColor = AdminSoftSurface,
                        labelColor = AdminInk
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_razorpay_mode_live")
                )
                FilterChip(
                    selected = razorpayMode == "TEST",
                    onClick = { razorpayMode = "TEST" },
                    label = { Text("TEST MODE", fontWeight = FontWeight.ExtraBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdminAmber,
                        selectedLabelColor = Color.White,
                        containerColor = AdminSoftSurface,
                        labelColor = AdminInk
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_razorpay_mode_test")
                )
            }

            OutlinedTextField(
                value = razorpayKeyId,
                onValueChange = { razorpayKeyId = it.trim() },
                label = { Text("Razorpay Key ID (Saved for future online pay)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_razorpay_key_id_input")
            )

            OutlinedTextField(
                value = razorpayMerchantName,
                onValueChange = { razorpayMerchantName = it },
                label = { Text("Merchant Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_razorpay_merchant_name_input")
            )

            OutlinedTextField(
                value = razorpayPaymentLink,
                onValueChange = { razorpayPaymentLink = it.trim() },
                label = { Text("Razorpay Payment Link (Optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_razorpay_payment_link_input")
            )

            OutlinedTextField(
                value = merchantUpiId,
                onValueChange = { merchantUpiId = it.trim() },
                label = { Text("Store Merchant UPI ID") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_merchant_upi_id_input")
            )

            Button(
                onClick = {
                    onSaveStoreConfig(
                        storeConfig.copy(
                            razorpayEnabled = razorpayEnabled,
                            razorpayKeyId = razorpayKeyId.trim(),
                            razorpayMerchantName = razorpayMerchantName.trim().ifBlank { "My Crush Pizza – Hajipur" },
                            razorpayPaymentLink = razorpayPaymentLink.trim(),
                            razorpayMode = razorpayMode,
                            merchantUpiId = merchantUpiId.trim().ifBlank { "mycrushpizza@upi" }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrushRed,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_save_razorpay_btn")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Payment Settings", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

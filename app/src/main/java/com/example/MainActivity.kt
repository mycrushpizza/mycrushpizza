package com.example

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppNotification
import com.example.data.model.AppRole
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.model.SavedAddressesPrefs
import com.example.data.model.formatInr
import com.mycrushpizza.`in`.R
import com.example.ui.components.MyCrushSplashAnimationOverlay
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.OnlineFoodImage
import com.example.ui.components.OrderPlacedSuccessDialog
import com.example.ui.components.StoreContactHelper
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.CustomerWalletScreen
import com.example.ui.screens.LiveTrackingAndOrdersScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushCardSurface
import com.example.ui.theme.CrushCharcoal
import com.example.ui.theme.CrushElevatedSurface
import com.example.ui.theme.CrushMutedText
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VegGreenBright
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.MainAppViewModel
import com.example.util.AdminOrderAlertPlayer
import com.example.util.AdminOrderMonitorService
import com.example.util.GoogleSignInHelper
import com.example.util.NetworkConnectivityObserver
import com.example.util.NotificationHelper
import com.example.util.NotificationReadStatePrefs
import com.example.util.RecentSearchPrefs
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
        @Suppress("DEPRECATION")
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        NotificationHelper.ensureChannel(this)
        setContent {
            MyApplicationTheme {
                MyCrushPizzaAppRoot()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Keep Admin incoming order alarm alive if Admin Gmail is signed in so screen-off orders still ring;
        // for all non-admin users, stop any alert immediately when screen turns off.
        val email = try {
            Firebase.auth.currentUser?.email.orEmpty().trim()
        } catch (_: Exception) {
            ""
        }
        if (!com.example.data.repository.AuthAndUserRepository.isBootstrappedAdminPanelEmail(email)) {
            AdminOrderAlertPlayer.stopGlobalRinging()
        }
    }
}

@Composable
fun MyCrushPizzaAppRoot(
    viewModel: MainAppViewModel = viewModel(factory = MainAppViewModel.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val alertPlayer = remember(context) { AdminOrderAlertPlayer.getOrCreate(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Launch-only 1.5–2s Domino's-style Branded Splash Animation
    var showLaunchSplash by rememberSaveable { mutableStateOf(true) }
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        NotificationHelper.ensureChannel(context)
    }

    var showProfileAndStaffDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var lastShownWhatsAppDialogOrderId by remember { mutableStateOf("") }
    var isHomeScrolled by remember { mutableStateOf(false) }
    var homeScrollToTopTrigger by remember { mutableIntStateOf(0) }
    var lastBackPressedTimeMs by remember { mutableLongStateOf(0L) }
    var showPressAgainToExitPrompt by remember { mutableStateOf(false) }
    val observedCustomerOrderStatuses = remember(uiState.currentUserId) { mutableStateMapOf<String, OrderStatus>() }
    val dismissedCancelledOrderDialogIds = remember(uiState.currentUserId) { mutableStateListOf<String>() }
    var adminCancelledOrderDialogTarget by remember(uiState.currentUserId) { mutableStateOf<PizzaOrder?>(null) }

    // Detect when Admin cancels a Customer's order and show a clear "Order Cancelled" popup to the Customer.
    LaunchedEffect(uiState.customerOrders, uiState.activeRole) {
        val nowMs = System.currentTimeMillis()
        val isInitialSnapshot = observedCustomerOrderStatuses.isEmpty()
        var newlyCancelledByAdmin: PizzaOrder? = null
        uiState.customerOrders.forEach { order ->
            if (order.id.isBlank()) return@forEach
            val prevStatus = observedCustomerOrderStatuses[order.id]
            val currentStatus = order.orderStatus
            val cancelledByAdmin = currentStatus == OrderStatus.CANCELLED && order.adminAlertAcknowledged
            val transitionedToCancelled = prevStatus != null && prevStatus != OrderStatus.CANCELLED && cancelledByAdmin
            val recentlyCancelledOnOpen = isInitialSnapshot && cancelledByAdmin && run {
                val updatedMs = (order.updatedAt ?: order.createdAt)?.toDate()?.time ?: 0L
                updatedMs > 0L && (nowMs - updatedMs) <= 10 * 60_000L
            }
            if ((transitionedToCancelled || recentlyCancelledOnOpen) && order.id !in dismissedCancelledOrderDialogIds) {
                newlyCancelledByAdmin = order
            }
            observedCustomerOrderStatuses[order.id] = currentStatus
        }
        if (newlyCancelledByAdmin != null && uiState.activeRole == AppRole.CUSTOMER) {
            adminCancelledOrderDialogTarget = newlyCancelledByAdmin
        }
    }

    LaunchedEffect(showPressAgainToExitPrompt) {
        if (showPressAgainToExitPrompt) {
            delay(2000L)
            showPressAgainToExitPrompt = false
        }
    }

    // Observe FirebaseAuth state changes & silent auto-sign-in on launch
    DisposableEffect(Unit) {
        val auth = try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                viewModel.onUserAuthenticated(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: "Hajipur Customer",
                    onNewNotificationDoc = { docId, title, msg ->
                        NotificationHelper.showOrderPushNotification(
                            context = context,
                            title = title,
                            message = msg,
                            recipientKey = user.uid,
                            notificationDocId = docId
                        )
                    }
                )
            } else {
                viewModel.onUserSignedOut()
            }
        }
        auth?.addAuthStateListener(listener)

        if (auth?.currentUser == null) {
            GoogleSignInHelper.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }

        onDispose {
            auth?.removeAuthStateListener(listener)
            alertPlayer.stopRinging()
        }
    }

    // Observe real-time internet connectivity for Online Cloud mode
    LaunchedEffect(context) {
        NetworkConnectivityObserver.observeOnlineState(context).collect { isOnline ->
            viewModel.setOnlineConnectionState(isOnline)
        }
    }

    // Sync Foreground Admin Order Monitor Service (strictly for pranavgamer758@gmail.com)
    // so incoming orders ring even when the app is in background or phone screen is OFF!
    LaunchedEffect(uiState.isSignedIn, uiState.currentUserEmail) {
        AdminOrderMonitorService.syncAdminMonitorState(
            context = context,
            isSignedIn = uiState.isSignedIn,
            email = uiState.currentUserEmail
        )
    }

    // Continuous Admin Incoming Order Ringing Effect + Lock-Screen Alarm Notification with Accept/Decline
    val topRingingOrder = uiState.ringingAdminOrders.firstOrNull()
    LaunchedEffect(uiState.isAdminRinging, topRingingOrder?.id) {
        if (uiState.isAdminRinging && topRingingOrder != null && !AdminOrderAlertPlayer.isOrderSilenced(topRingingOrder.id)) {
            alertPlayer.startRinging(topRingingOrder.id)
            NotificationHelper.showAdminIncomingOrderAlarmNotification(context, topRingingOrder)
        } else {
            AdminOrderAlertPlayer.stopGlobalRinging()
        }
    }

    // Auto-dismiss transient status & error banners so old messages never stay stuck or feel repeated in Admin Panel
    LaunchedEffect(uiState.statusBannerMessage, uiState.errorBannerMessage) {
        val hasError = !uiState.errorBannerMessage.isNullOrBlank()
        val hasStatus = !uiState.statusBannerMessage.isNullOrBlank()
        if (hasError || hasStatus) {
            delay(if (hasError) 5000L else 3500L)
            viewModel.clearBanners()
        }
    }

    // 1. Show 1.5–2s Branded Opening Splash Screen Animation ONLY on App Launch
    if (showLaunchSplash) {
        MyCrushSplashAnimationOverlay(
            onSplashFinished = { showLaunchSplash = false }
        )
        return
    }

    // 2. Gate initial screen: show SignInScreen until user either signs in with Google or clicks top "Skip"
    if (!uiState.isSignedIn && !uiState.isGuestMode) {
        SignInScreen(
            onSignInSuccess = {},
            onSkipToGuestMenu = { viewModel.enterGuestMode() }
        )
        return
    }

    // Load persisted Recent Searches on app launch
    LaunchedEffect(Unit) {
        viewModel.loadRecentSearches(RecentSearchPrefs.load(context))
    }

    // Load per-account saved addresses (Home/Work/Other) & notification read/floor state on sign-in
    LaunchedEffect(uiState.currentUserId) {
        val uid = uiState.currentUserId
        if (uid.isNotBlank()) {
            val savedAddrs = SavedAddressesPrefs.loadForUser(context, uid)
            if (savedAddrs.isNotEmpty()) {
                viewModel.loadSavedAddressesForUser(savedAddrs)
            }
            val readIds = NotificationReadStatePrefs.loadReadIds(context, uid)
            val floorMs = NotificationReadStatePrefs.loadAccountFloorMillis(context, uid)
            viewModel.loadNotificationReadState(readIds, floorMs)
        }
    }

    // Persist per-account saved addresses whenever updated
    LaunchedEffect(uiState.currentUserId, uiState.savedAddresses) {
        val uid = uiState.currentUserId
        if (uid.isNotBlank() && uiState.savedAddresses.isNotEmpty()) {
            SavedAddressesPrefs.saveForUser(context, uid, uiState.savedAddresses)
        }
    }

    // Persist per-account read notification IDs so badge counts stay cleared across restarts
    LaunchedEffect(uiState.currentUserId, uiState.locallyReadNotificationIds) {
        val uid = uiState.currentUserId
        if (uid.isNotBlank() && uiState.locallyReadNotificationIds.isNotEmpty()) {
            NotificationReadStatePrefs.saveReadIds(context, uid, uiState.locallyReadNotificationIds)
        }
    }

    // Persist per-account notification creation floor so new IDs never inherit old notifications
    LaunchedEffect(uiState.currentUserId, uiState.accountNotificationFloorMillis) {
        val uid = uiState.currentUserId
        if (uid.isNotBlank() && uiState.accountNotificationFloorMillis > 0L) {
            NotificationReadStatePrefs.saveAccountFloorMillis(context, uid, uiState.accountNotificationFloorMillis)
        }
    }

    // Request POST_NOTIFICATIONS permission on Android 13+ only AFTER user skips Sign-In or signs in
    LaunchedEffect(uiState.isSignedIn, uiState.isGuestMode, hasRequestedNotificationPermission) {
        if ((uiState.isSignedIn || uiState.isGuestMode) && !hasRequestedNotificationPermission) {
            hasRequestedNotificationPermission = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !NotificationHelper.hasNotificationPermission(context)
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Step-by-Step Back Navigation:
    // 1) Close open order details / Admin role / non-Menu tabs / Search / Category filter (e.g., Cheese Pizza) / Veg filter one step at a time.
    // 2) If Home menu is scrolled down, Back scrolls cleanly back to the top of the Home screen.
    // 3) ONLY when already at the top of the Home page ("All", Menu tab, not scrolled): 1st Back shows "Press again to exit", 2nd Back within 2s exits.
    BackHandler(enabled = true) {
        when {
            uiState.isCustomerOrderDetailsOpen -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.closeCustomerOrderDetails()
            }
            uiState.activeRole != AppRole.CUSTOMER -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.switchRole(AppRole.CUSTOMER)
            }
            uiState.selectedCustomerTab != CustomerTab.MENU -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.selectCustomerTab(CustomerTab.MENU)
            }
            uiState.searchQuery.isNotBlank() -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.setSearchQuery("")
            }
            !uiState.selectedCategory.equals("All", ignoreCase = true) -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.setCategoryFilter("All")
                homeScrollToTopTrigger += 1
            }
            uiState.vegOnlyFilter -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                viewModel.toggleVegOnlyFilter()
            }
            isHomeScrolled -> {
                lastBackPressedTimeMs = 0L
                showPressAgainToExitPrompt = false
                homeScrollToTopTrigger += 1
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressedTimeMs <= 2000L) {
                    showPressAgainToExitPrompt = false
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressedTimeMs = now
                    showPressAgainToExitPrompt = true
                    Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Instant Admin Incoming Order Details & Loud Alarm Popup!
    // Even if OS notifications are OFF, as soon as the app is open and a new order arrives for Admin,
    // this popup immediately displays full Order Details on screen with ONLY Accept Order & Decline Order!
    if (uiState.isAdminUser && topRingingOrder != null) {
        AlertDialog(
            onDismissRequest = {
                // Keep popup visible until Admin explicitly clicks ACCEPT ORDER or DECLINE ORDER!
            },
            modifier = Modifier.testTag("admin_incoming_order_fullscreen_popup"),
            containerColor = Color(0xFF1A060D),
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = CrushRed,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Incoming Order Alarm",
                                tint = CrushYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "🚨 NEW ORDER RECEIVED!",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrushYellow,
                            fontWeight = FontWeight.ExtraBold
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
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CrushCardSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, CrushYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CUSTOMER & ORDER DETAILS",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrushYellow,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Customer Name: ${topRingingOrder.customerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrushWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Contact No: ${topRingingOrder.customerPhone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrushYellow,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Delivery Location: ${topRingingOrder.deliveryAddress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrushWhite
                            )
                            if (topRingingOrder.landmark.isNotBlank()) {
                                Text(
                                    text = "Landmark: ${topRingingOrder.landmark}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrushYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = CrushElevatedSurface)
                            Text(
                                text = "Ordered Items: ${topRingingOrder.itemsSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = VegGreenBright,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Total Bill: ${formatInr(topRingingOrder.totalInr)} • Payment: ${topRingingOrder.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrushWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                AdminOrderAlertPlayer.silenceOrderAndStopRinging(topRingingOrder.id)
                                NotificationHelper.cancelAdminOrderAlarmNotification(context, topRingingOrder.id)
                                viewModel.acceptIncomingOrderInAdmin(topRingingOrder)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VegGreenBright,
                                contentColor = CrushBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("popup_admin_accept_order_btn")
                        ) {
                            Text("ACCEPT ORDER", fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                AdminOrderAlertPlayer.silenceOrderAndStopRinging(topRingingOrder.id)
                                NotificationHelper.cancelAdminOrderAlarmNotification(context, topRingingOrder.id)
                                viewModel.declineIncomingOrderInAdmin(topRingingOrder)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrushRed,
                                contentColor = CrushWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("popup_admin_decline_order_btn")
                        ) {
                            Text("DECLINE ORDER", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Instant Admin "ORDER ACCEPTED • CUSTOMER DETAILS" Popup shown right after Admin clicks Accept Order!
    val acceptedAdminOrder = uiState.lastAcceptedAdminOrder
    if (uiState.isAdminUser && acceptedAdminOrder != null && topRingingOrder == null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAcceptedAdminOrderDetails() },
            modifier = Modifier.testTag("admin_accepted_customer_details_dialog"),
            containerColor = Color(0xFF101914),
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = VegGreenBright,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Order Accepted",
                                tint = CrushBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "✅ ORDER ACCEPTED • CUSTOMER DETAILS",
                            style = MaterialTheme.typography.titleSmall,
                            color = VegGreenBright,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${acceptedAdminOrder.orderNumber} • ${acceptedAdminOrder.customerName.ifBlank { "Customer" }}",
                            style = MaterialTheme.typography.titleSmall,
                            color = CrushWhite,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatInr(acceptedAdminOrder.totalInr)} (${acceptedAdminOrder.paymentMethod})",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrushYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = CrushCardSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, VegGreenBright),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CUSTOMER FULL DELIVERY DETAILS",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrushYellow,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Customer Name: ${acceptedAdminOrder.customerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrushWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Contact No: ${acceptedAdminOrder.customerPhone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrushYellow,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Location / Address: ${acceptedAdminOrder.deliveryAddress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrushWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (acceptedAdminOrder.landmark.isNotBlank()) {
                                Text(
                                    text = "Landmark: ${acceptedAdminOrder.landmark}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrushYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = CrushElevatedSurface)
                            Text(
                                text = "Ordered Items: ${acceptedAdminOrder.itemsSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = VegGreenBright,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Total Bill: ${formatInr(acceptedAdminOrder.totalInr)} (${acceptedAdminOrder.paymentMethod} • ${acceptedAdminOrder.paymentStatus})",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrushWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val targetPhone = acceptedAdminOrder.customerPhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY }
                                StoreContactHelper.dialStore(context, targetPhone)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrushYellow,
                                contentColor = CrushBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Customer", fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                StoreContactHelper.openOrderOnWhatsApp(context, acceptedAdminOrder)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VegGreenBright,
                                contentColor = CrushBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAcceptedAdminOrderDetails() }) {
                    Text("Close Details", color = CrushYellow, fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    // Celebratory "Order Placed Successfully" Green-Circle Tick Animation + Chime Sound Dialog
    val placedOrder = uiState.lastPlacedOrder
    if (placedOrder != null && placedOrder.id.isNotBlank() && placedOrder.id != lastShownWhatsAppDialogOrderId) {
        OrderPlacedSuccessDialog(
            order = placedOrder,
            storePhone = OFFICIAL_STORE_PHONE_DISPLAY,
            onTrackOrderClick = {
                lastShownWhatsAppDialogOrderId = placedOrder.id
                viewModel.openCustomerOrderDetails(placedOrder.id)
            },
            onWhatsAppOrderClick = {
                lastShownWhatsAppDialogOrderId = placedOrder.id
                StoreContactHelper.openOrderOnWhatsApp(context, placedOrder)
            },
            onCallStoreClick = {
                lastShownWhatsAppDialogOrderId = placedOrder.id
                StoreContactHelper.dialStore(context, uiState.storeConfig.storePhone)
            },
            onDismiss = {
                lastShownWhatsAppDialogOrderId = placedOrder.id
            }
        )
    }

    // Customer "Order Cancelled" Popup — shown ONLY when Admin cancels the customer's order!
    val cancelledOrderPopup = adminCancelledOrderDialogTarget
    if (cancelledOrderPopup != null && uiState.activeRole == AppRole.CUSTOMER) {
        AlertDialog(
            onDismissRequest = {
                dismissedCancelledOrderDialogIds.add(cancelledOrderPopup.id)
                adminCancelledOrderDialogTarget = null
            },
            modifier = Modifier.testTag("customer_order_cancelled_dialog"),
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Order Cancelled",
                                tint = CrushRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Order Cancelled",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrushRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = cancelledOrderPopup.orderNumber,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your order has been cancelled by My Crush Pizza – Hajipur.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = cancelledOrderPopup.itemsSummary.ifBlank { "Order Items" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Total: ${formatInr(cancelledOrderPopup.totalInr)} • Status: Order Cancelled",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrushRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dismissedCancelledOrderDialogIds.add(cancelledOrderPopup.id)
                        adminCancelledOrderDialogTarget = null
                        viewModel.selectCustomerTab(CustomerTab.TRACK)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrushRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("customer_order_cancelled_view_orders_btn")
                ) {
                    Text("View Orders", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dismissedCancelledOrderDialogIds.add(cancelledOrderPopup.id)
                        adminCancelledOrderDialogTarget = null
                    }
                ) {
                    Text("OK", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Account, Cart, Wallet & Admin Portal Switcher Dialog (Opened when clicking Top Account Logo)
    if (showProfileAndStaffDialog) {
        AlertDialog(
            onDismissRequest = { showProfileAndStaffDialog = false },
            modifier = Modifier.testTag("profile_and_staff_dialog"),
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFE31837),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (uiState.isSignedIn) {
                                uiState.currentUserName.ifBlank { "My Account" }
                            } else {
                                "My Account • Cart & Wallet"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Tap Cart or Wallet below to open",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE31837),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 1. Open Cart & Checkout Card Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.switchRole(AppRole.CUSTOMER)
                                viewModel.selectCustomerTab(CustomerTab.CART)
                                showProfileAndStaffDialog = false
                            }
                            .testTag("dialog_open_cart_btn"),
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE31837))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFE31837),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingCart,
                                            contentDescription = "Open Cart",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "My Cart (${uiState.cartCount} Items)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (uiState.cartCount > 0) {
                                            "Subtotal: ${formatInr(uiState.subtotalInr)} • Tap to Checkout"
                                        } else {
                                            "View Cart & Add More Foods"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFE31837),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFFE31837),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "OPEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 2. Open My Wallet Card Button (Warm Emerald & Golden Amber Card)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.switchRole(AppRole.CUSTOMER)
                                viewModel.selectCustomerTab(CustomerTab.WALLET)
                                showProfileAndStaffDialog = false
                            }
                            .testTag("dialog_open_wallet_btn"),
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF059669))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF059669),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBalanceWallet,
                                            contentDescription = "Open Wallet",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "My Wallet (${formatInr(uiState.totalWalletBalanceInr)})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Bonus Cash: ${formatInr(uiState.bonusBalanceInr)} • Usable on Orders",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFF059669),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "OPEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 3. Open Order History Card Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.switchRole(AppRole.CUSTOMER)
                                viewModel.selectCustomerTab(CustomerTab.TRACK)
                                showProfileAndStaffDialog = false
                            }
                            .testTag("dialog_open_order_history_btn"),
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFD97706))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFD97706),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = "Open Order History",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Order History (${uiState.customerOrders.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (uiState.isSignedIn) {
                                            if (uiState.customerOrders.isEmpty()) {
                                                "View your past orders & receipts"
                                            } else {
                                                "${uiState.orderHistoryDeliveredCount} Delivered • Tap to view receipts & reorder"
                                            }
                                        } else {
                                            "Sign in to view synced cloud orders"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFFD97706),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "OPEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    if (uiState.isSignedIn) {
                        Text(
                            text = "Signed in: ${uiState.currentUserEmail.ifBlank { uiState.currentUserId }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = {
                                showProfileAndStaffDialog = false
                                GoogleSignInHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {},
                                    onAuthError = {},
                                    scope = scope
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE31837)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_google_sign_in_btn")
                        ) {
                            Text("Sign in with Google", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }

                    if (uiState.isAdminUser) {
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Text(
                            text = "Admin Access Active (${uiState.currentUserEmail.ifBlank { "Admin" }}):",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFE31837),
                            fontWeight = FontWeight.ExtraBold
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.switchRole(AppRole.CUSTOMER)
                                showProfileAndStaffDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("switch_to_customer_btn"),
                            border = BorderStroke(1.5.dp, Color(0xFFD97706))
                        ) {
                            Icon(Icons.Filled.LocalPizza, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customer Storefront & Menu", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.switchRole(AppRole.ADMIN)
                                showProfileAndStaffDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("switch_to_admin_btn"),
                            border = BorderStroke(1.5.dp, Color(0xFFE31837))
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color(0xFFE31837))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Admin Command Center (Orders & Wallet)", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.isSignedIn) {
                        OutlinedButton(
                            onClick = {
                                showProfileAndStaffDialog = false
                                GoogleSignInHelper.signOut(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = { viewModel.onUserSignedOut() }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_btn"),
                            border = BorderStroke(1.dp, Color(0xFFE31837))
                        ) {
                            Text("Sign Out", color = Color(0xFFE31837), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileAndStaffDialog = false }) {
                    Text("Close", color = Color(0xFFE31837), fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    // Full-Screen Dedicated Notifications Page (Opens just like the Dedicated Search Screen when clicking top Bell icon!)
    if (showNotificationsDialog) {
        DedicatedNotificationsScreen(
            notifications = uiState.notifications,
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onBackClick = {
                viewModel.markAllNotificationsRead()
                showNotificationsDialog = false
            },
            onExploreMenuClick = {
                viewModel.markAllNotificationsRead()
                showNotificationsDialog = false
                viewModel.switchRole(AppRole.CUSTOMER)
                viewModel.selectCustomerTab(CustomerTab.MENU)
            }
        )
        return
    }

    val isCustomerRole = uiState.activeRole == AppRole.CUSTOMER
    val isScrolledCompact = isCustomerRole && uiState.selectedCustomerTab == CustomerTab.MENU && isHomeScrolled
    val animatedTopSpacer by animateDpAsState(
        targetValue = if (isScrolledCompact) 2.dp else 12.dp,
        animationSpec = tween(durationMillis = 220),
        label = "top_bar_spacer"
    )
    val animatedRowVerticalPadding by animateDpAsState(
        targetValue = if (isScrolledCompact) 4.dp else 9.dp,
        animationSpec = tween(durationMillis = 220),
        label = "top_bar_row_padding"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (isCustomerRole) Color(0xFFFFF8F6) else CrushBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = Color.Transparent,
                shadowElevation = if (isScrolledCompact) 10.dp else 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Rich Festive Crimson-Maroon-Rose Backdrop (No plain white bar!)
                    // When scrolling, applies a frosted glass blur effect + translucent glass sheen
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .then(if (isScrolledCompact) Modifier.blur(16.dp) else Modifier)
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isScrolledCompact) {
                                        listOf(
                                            Color(0xEB15071E),
                                            Color(0xE03A0924),
                                            Color(0xD97B0D2B)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFF15071E),
                                            Color(0xFF1B0924),
                                            Color(0xFF1E0B28)
                                        )
                                    }
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0x28FFB300),
                                radius = 70.dp.toPx(),
                                center = Offset(size.width * 0.18f, size.height * 0.5f)
                            )
                            drawCircle(
                                color = Color(0x24FF6F00),
                                radius = 85.dp.toPx(),
                                center = Offset(size.width * 0.82f, size.height * 0.4f)
                            )
                            if (isScrolledCompact) {
                                // Frosted glass shimmer overlay when scrolled
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.10f),
                                            Color.White.copy(alpha = 0.18f),
                                            Color.White.copy(alpha = 0.10f)
                                        )
                                    )
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        // Top space that smoothly slides up slightly when scrolling!
                        Spacer(modifier = Modifier.height(animatedTopSpacer))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = animatedRowVerticalPadding,
                                    bottom = animatedRowVerticalPadding
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: My Crush Pizza Brand Emblem + Title + Hajipur Location (Clean 2-line lockup, never wraps vertically!)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp)
                            ) {
                                MyCrushTopBrandLogo(
                                    modifier = Modifier.size(if (isScrolledCompact) 34.dp else 36.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "MY CRUSH PIZZA",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontSize = if (uiState.isAdminUser || isScrolledCompact) 13.5.sp else 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.2.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            color = Color(0xFFFFB300),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "HAJIPUR",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = Color(0xFF4A0418),
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(
                                            text = "RN College Chautha Road",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(0xFFFFE4B5),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("top_online_status_label")
                                        )
                                    }
                                }
                            }

                            // Right: Admin Pill (only for pranavgamer758@gmail.com), Notifications Icon & Account Logo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (uiState.isAdminUser) {
                                    val isCurrentlyAdmin = uiState.activeRole == AppRole.ADMIN
                                    Surface(
                                        modifier = Modifier
                                            .height(34.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.switchRole(
                                                    if (isCurrentlyAdmin) AppRole.CUSTOMER else AppRole.ADMIN
                                                )
                                            }
                                            .testTag("top_admin_btn"),
                                        color = if (isCurrentlyAdmin) CrushYellow else Color.White.copy(alpha = 0.16f),
                                        shape = CircleShape,
                                        border = BorderStroke(1.2.dp, CrushYellow)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AdminPanelSettings,
                                                contentDescription = "Admin Panel",
                                                tint = if (isCurrentlyAdmin) CrushBlack else CrushYellow,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isCurrentlyAdmin) "Menu" else "Admin",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = if (isCurrentlyAdmin) CrushBlack else Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                // Notifications Bell Circle Button (Shows unread count ONLY; clears permanently once viewed)
                                val unreadBellCount = uiState.unreadNotificationsCount
                                BadgedBox(
                                    badge = {
                                        if (unreadBellCount > 0) {
                                            Badge(
                                                containerColor = CrushYellow,
                                                contentColor = CrushBlack
                                            ) {
                                                Text(
                                                    unreadBellCount.toString(),
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.markAllNotificationsRead()
                                                showNotificationsDialog = true
                                            }
                                            .testTag("top_notifications_btn"),
                                        color = Color.White.copy(alpha = 0.16f),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Notifications,
                                                contentDescription = "Order Notifications",
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Top Account Button — Frosted Gold-Rim Avatar Logo
                                Box(modifier = Modifier.padding(end = 1.dp)) {
                                    BadgedBox(
                                        badge = {
                                            if (uiState.cartCount > 0) {
                                                Badge(
                                                    containerColor = CrushYellow,
                                                    contentColor = CrushBlack
                                                ) {
                                                    Text(
                                                        uiState.cartCount.toString(),
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.closeCustomerOrderDetails()
                                                    viewModel.switchRole(AppRole.CUSTOMER)
                                                    viewModel.selectCustomerTab(CustomerTab.ACCOUNT)
                                                }
                                                .testTag("top_profile_btn"),
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.18f),
                                            border = BorderStroke(1.2.dp, Color(0xFFFFD54F))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AccountCircle,
                                                    contentDescription = "Account, Cart & Wallet",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Status or Error Toast Banner
                        val bannerText = uiState.errorBannerMessage ?: uiState.statusBannerMessage
                        if (!bannerText.isNullOrBlank()) {
                            val isError = uiState.errorBannerMessage != null
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.clearBanners() }
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                color = if (isError) CrushRed else Color(0xFFECFDF3),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isError) CrushYellow else VegGreenBright)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bannerText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isError) CrushWhite else Color(0xFF065F46),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "OK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isError) CrushYellow else CrushRed,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        if (isScrolledCompact) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = Color.White.copy(alpha = 0.28f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Bottom "Connect to Internet" Banner — ONLY shown when internet is OFF!
                if (!uiState.isOnlineConnected) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (uiState.activeRole != AppRole.CUSTOMER) {
                                    Modifier.navigationBarsPadding()
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("bottom_offline_internet_banner"),
                        color = Color(0xFF991B1B),
                        border = BorderStroke(1.dp, CrushYellow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "No Internet Connection",
                                tint = CrushYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Connect to Internet",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "No internet connection • Turn on Mobile Data or Wi-Fi",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color(0xFFFDE68A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (uiState.activeRole == AppRole.CUSTOMER) {
                    // Only show sticky bottom live-order tracker for ACTIVE in-progress orders;
                    // disappears immediately as soon as the order is DELIVERED or CANCELLED!
                    val activeBottomOrder = uiState.customerOrders.firstOrNull { !it.isTerminal }
                    var activeBarNowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(activeBottomOrder?.id) {
                        while (activeBottomOrder != null) {
                            delay(30_000L)
                            activeBarNowMillis = System.currentTimeMillis()
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                    // Sticky Bottom Active Order Bar (Shown while an order is active; tapping opens easy Live Order Tracking!)
                    if (activeBottomOrder != null && !uiState.isCustomerOrderDetailsOpen) {
                        val effectiveBottomStatus = activeBottomOrder.autoProgressedOrderStatus(activeBarNowMillis)
                        val progressFraction = ((effectiveBottomStatus.stepIndex.coerceIn(0, 5) + 1) / 6f)
                        val liveEtaMinutes = com.example.data.model.HajipurLocations.estimateMinutesForDistance(
                            activeBottomOrder.distanceKm,
                            effectiveBottomStatus
                        ).coerceAtLeast(if (effectiveBottomStatus == OrderStatus.ON_THE_WAY) 5 else 8)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.openCustomerOrderDetails(activeBottomOrder.id)
                                }
                                .testTag("customer_bottom_order_bar"),
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFE31837).copy(alpha = 0.45f)),
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFFFFF1F2),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (effectiveBottomStatus) {
                                                    OrderStatus.ON_THE_WAY, OrderStatus.PICKED_UP -> Icons.Filled.DeliveryDining
                                                    OrderStatus.DELIVERED -> Icons.Filled.CheckCircle
                                                    else -> Icons.Filled.LocalPizza
                                                },
                                                contentDescription = "Active Order",
                                                tint = Color(0xFFE31837),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF059669))
                                            )
                                            Text(
                                                text = "${effectiveBottomStatus.title} • ~$liveEtaMinutes min",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${activeBottomOrder.orderNumber} • ${activeBottomOrder.itemsSummary}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFE31837),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Track >",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                                // Thin progress indicator strip along the bottom edge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.5.dp)
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressFraction)
                                            .height(3.5.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFE31837), Color(0xFFF59E0B), Color(0xFF059669))
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_bottom_nav"),
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F172A),
                        tonalElevation = 10.dp
                    ) {
                        val isMenuSelected = !uiState.isCustomerOrderDetailsOpen &&
                            uiState.selectedCustomerTab == CustomerTab.MENU
                        val isOrdersSelected = !uiState.isCustomerOrderDetailsOpen &&
                            uiState.selectedCustomerTab == CustomerTab.TRACK
                        val isWalletSelected = !uiState.isCustomerOrderDetailsOpen &&
                            uiState.selectedCustomerTab == CustomerTab.WALLET
                        val isCartSelected = !uiState.isCustomerOrderDetailsOpen &&
                            uiState.selectedCustomerTab == CustomerTab.CART
                        val isAccountSelected = !uiState.isCustomerOrderDetailsOpen &&
                            uiState.selectedCustomerTab == CustomerTab.ACCOUNT

                        // 1. Menu
                        NavigationBarItem(
                            selected = isMenuSelected,
                            onClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.setCategoryFilter("All")
                                viewModel.selectCustomerTab(CustomerTab.MENU)
                            },
                            modifier = Modifier.testTag("nav_tab_${CustomerTab.MENU.name}"),
                            icon = {
                                CustomBottomNavLogo(tabType = "MENU", selected = isMenuSelected)
                            },
                            label = {
                                Text(
                                    text = "Menu",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isMenuSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFE31837),
                                selectedTextColor = Color(0xFFE31837),
                                indicatorColor = Color(0xFFFFE4E6),
                                unselectedIconColor = Color(0xFF475569),
                                unselectedTextColor = Color(0xFF475569)
                            )
                        )

                        // 2. Order History Tab
                        NavigationBarItem(
                            selected = isOrdersSelected,
                            onClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.selectCustomerTab(CustomerTab.TRACK)
                            },
                            modifier = Modifier.testTag("nav_tab_${CustomerTab.TRACK.name}"),
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (uiState.orderHistoryActiveCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFD97706),
                                                contentColor = Color.White
                                            ) {
                                                Text(uiState.orderHistoryActiveCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    CustomBottomNavLogo(tabType = "REORDER", selected = isOrdersSelected)
                                }
                            },
                            label = {
                                Text(
                                    text = "Orders",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isOrdersSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF9F1239),
                                selectedTextColor = Color(0xFF9F1239),
                                indicatorColor = Color(0xFFFFE4E6),
                                unselectedIconColor = Color(0xFF475569),
                                unselectedTextColor = Color(0xFF475569)
                            )
                        )

                        // 3. Wallet Tab
                        NavigationBarItem(
                            selected = isWalletSelected,
                            onClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.selectCustomerTab(CustomerTab.WALLET)
                            },
                            modifier = Modifier.testTag("nav_tab_${CustomerTab.WALLET.name}"),
                            icon = {
                                CustomBottomNavLogo(tabType = "WALLET", selected = isWalletSelected)
                            },
                            label = {
                                Text(
                                    text = "Wallet",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isWalletSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF059669),
                                selectedTextColor = Color(0xFF047857),
                                indicatorColor = Color(0xFFD1FAE5),
                                unselectedIconColor = Color(0xFF475569),
                                unselectedTextColor = Color(0xFF475569)
                            )
                        )

                        // 4. Cart Tab
                        NavigationBarItem(
                            selected = isCartSelected,
                            onClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.selectCustomerTab(CustomerTab.CART)
                            },
                            modifier = Modifier.testTag("nav_tab_${CustomerTab.CART.name}"),
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (uiState.cartCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFE31837),
                                                contentColor = CrushWhite
                                            ) {
                                                Text(uiState.cartCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    CustomBottomNavLogo(tabType = "CART", selected = isCartSelected)
                                }
                            },
                            label = {
                                Text(
                                    text = if (uiState.cartCount > 0) "Cart (${uiState.cartCount})" else "Cart",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isCartSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFE31837),
                                selectedTextColor = Color(0xFFE31837),
                                indicatorColor = Color(0xFFFFE4E6),
                                unselectedIconColor = Color(0xFF475569),
                                unselectedTextColor = Color(0xFF475569)
                            )
                        )

                        // 5. Account Tab
                        NavigationBarItem(
                            selected = isAccountSelected,
                            onClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.selectCustomerTab(CustomerTab.ACCOUNT)
                            },
                            modifier = Modifier.testTag("nav_tab_${CustomerTab.ACCOUNT.name}"),
                            icon = {
                                CustomBottomNavLogo(tabType = "ACCOUNT", selected = isAccountSelected)
                            },
                            label = {
                                Text(
                                    text = "Account",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isAccountSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFD97706),
                                selectedTextColor = Color(0xFFB45309),
                                indicatorColor = Color(0xFFFEF3C7),
                                unselectedIconColor = Color(0xFF475569),
                                unselectedTextColor = Color(0xFF475569)
                            )
                        )
                    }
                }
            }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    when {
                        uiState.activeRole == AppRole.ADMIN -> Color(0xFFF8FAFC)
                        isCustomerRole -> Color(0xFFF5F8FF)
                        else -> CrushBlack
                    }
                )
        ) {
            when (uiState.activeRole) {
                AppRole.CUSTOMER, AppRole.DELIVERY -> {
                    if (uiState.isCustomerOrderDetailsOpen) {
                        LiveTrackingAndOrdersScreen(
                            uiState = uiState,
                            onSelectOrderForTracking = viewModel::selectOrderForTracking,
                            onReorderClick = viewModel::reorderPastOrder,
                            onBrowseMenuClick = {
                                viewModel.closeCustomerOrderDetails()
                                viewModel.selectCustomerTab(CustomerTab.MENU)
                            },
                            onCloseDetailsClick = { viewModel.closeCustomerOrderDetails() }
                        )
                    } else {
                        when (uiState.selectedCustomerTab) {
                            CustomerTab.MENU -> CustomerHomeScreen(
                                uiState = uiState,
                                onCategorySelected = viewModel::setCategoryFilter,
                                onSearchQueryChange = viewModel::setSearchQuery,
                                onToggleVegFilter = viewModel::toggleVegOnlyFilter,
                                onAddToCart = viewModel::addToCart,
                                onUpdateCartQuantity = viewModel::updateCartQuantity,
                                onApplyCoupon = viewModel::applyCouponCode,
                                onGoToCart = { viewModel.selectCustomerTab(CustomerTab.CART) },
                                onUpdatePickedLocation = { lat, lng, addr, landmark ->
                                    viewModel.updatePickedMapLocation(lat, lng, addr, landmark)
                                },
                                onSaveFullCustomerDetails = { name, phone, lat, lng, addr, landmark ->
                                    viewModel.saveCustomerFullDeliveryDetails(name, phone, lat, lng, addr, landmark)
                                },
                                onSaveFullCustomerDetailsWithLabel = { name, phone, lat, lng, addr, landmark, label ->
                                    viewModel.saveCustomerFullDeliveryDetails(name, phone, lat, lng, addr, landmark, label)
                                },
                                onHomeScrollStateChanged = { scrolled -> isHomeScrolled = scrolled },
                                scrollToTopTrigger = homeScrollToTopTrigger,
                                onAddRecentSearch = { query ->
                                    val updated = viewModel.addRecentSearch(query)
                                    RecentSearchPrefs.save(context, updated)
                                },
                                onRemoveRecentSearch = { query ->
                                    val updated = viewModel.removeRecentSearch(query)
                                    RecentSearchPrefs.save(context, updated)
                                },
                                onClearRecentSearches = {
                                    viewModel.clearRecentSearches()
                                    RecentSearchPrefs.save(context, emptyList())
                                }
                            )
                            CustomerTab.TRACK -> OrderHistoryScreen(
                                uiState = uiState,
                                onRefreshClick = viewModel::refreshOrderHistory,
                                onFilterSelected = viewModel::setOrderHistoryFilter,
                                onSearchQueryChange = viewModel::setOrderHistorySearchQuery,
                                onTrackOrderClick = viewModel::openCustomerOrderDetails,
                                onReorderClick = viewModel::reorderPastOrder,
                                onCancelOrderClick = viewModel::cancelOwnConfirmedOrder,
                                onBrowseMenuClick = { viewModel.selectCustomerTab(CustomerTab.MENU) },
                                onRequestGoogleSignIn = {
                                    GoogleSignInHelper.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {},
                                        onAuthError = {},
                                        scope = scope
                                    )
                                }
                            )
                            CustomerTab.WALLET -> CustomerWalletScreen(
                                uiState = uiState,
                                onAddMoneyToWallet = viewModel::addMoneyToDepositWallet,
                                onApplyCoupon = viewModel::applyCouponCode,
                                onGoToMenuClick = { viewModel.selectCustomerTab(CustomerTab.MENU) },
                                onGoToCartClick = { viewModel.selectCustomerTab(CustomerTab.CART) },
                                onRequestGoogleSignIn = {
                                    GoogleSignInHelper.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {},
                                        onAuthError = {},
                                        scope = scope
                                    )
                                }
                            )
                            CustomerTab.CART -> CartAndCheckoutScreen(
                                uiState = uiState,
                                onUpdateCartQuantity = viewModel::updateCartQuantity,
                                onApplyCoupon = viewModel::applyCouponCode,
                                onRemoveCoupon = viewModel::removeAppliedCoupon,
                                onUpdateCheckoutDetails = viewModel::updateCheckoutDetails,
                                onSelectLandmark = viewModel::selectHajipurLandmark,
                                onUpdatePickedLocation = { lat, lng, addr, landmark ->
                                    viewModel.updatePickedMapLocation(lat, lng, addr, landmark)
                                },
                                onPlaceOrderClick = { triggerGoogleSignIn ->
                                    viewModel.submitCustomerOrder(onRequireGoogleSignIn = triggerGoogleSignIn)
                                },
                                onBrowseMenuClick = { viewModel.selectCustomerTab(CustomerTab.MENU) },
                                onAddMoneyToWallet = viewModel::addMoneyToDepositWallet,
                                onToggleUseDepositBalance = viewModel::toggleUseDepositBalance,
                                onToggleUseBonusBalance = viewModel::toggleUseBonusBalance,
                                onAddToCart = viewModel::addToCart,
                                onOpenWalletTab = { viewModel.selectCustomerTab(CustomerTab.WALLET) },
                                onSelectAddressLabel = viewModel::selectCheckoutAddressLabel,
                                onApplySavedAddress = viewModel::applySavedAddress,
                                onSaveFullCustomerDetails = { name, phone, lat, lng, addr, landmark, label ->
                                    viewModel.saveCustomerFullDeliveryDetails(name, phone, lat, lng, addr, landmark, label)
                                }
                            )
                            CustomerTab.ACCOUNT -> AccountScreen(
                                uiState = uiState,
                                onOpenOrdersClick = { viewModel.selectCustomerTab(CustomerTab.TRACK) },
                                onOpenWalletClick = { viewModel.selectCustomerTab(CustomerTab.WALLET) },
                                onOpenCartClick = { viewModel.selectCustomerTab(CustomerTab.CART) },
                                onOpenNotificationsClick = {
                                    viewModel.markAllNotificationsRead()
                                    showNotificationsDialog = true
                                },
                                onUpdatePickedLocation = { lat, lng, addr, landmark ->
                                    viewModel.updatePickedMapLocation(lat, lng, addr, landmark)
                                },
                                onOpenAdminPanelClick = { viewModel.switchRole(AppRole.ADMIN) },
                                onRequestGoogleSignIn = {
                                    GoogleSignInHelper.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {},
                                        onAuthError = {},
                                        scope = scope
                                    )
                                },
                                onSignOutClick = {
                                    GoogleSignInHelper.signOut(
                                        credentialManager = credentialManager,
                                        scope = scope,
                                        onSignOutComplete = { viewModel.onUserSignedOut() }
                                    )
                                },
                                onSaveFullCustomerDetails = { name, phone, lat, lng, addr, landmark, label ->
                                    viewModel.saveCustomerFullDeliveryDetails(name, phone, lat, lng, addr, landmark, label)
                                },
                                onApplySavedAddress = viewModel::applySavedAddress
                            )
                        }
                    }
                }

                AppRole.ADMIN -> {
                    AdminDashboardScreen(
                        uiState = uiState,
                        onAcceptOrderAlert = { order ->
                            AdminOrderAlertPlayer.silenceOrderAndStopRinging(order.id)
                            NotificationHelper.cancelAdminOrderAlarmNotification(context, order.id)
                            viewModel.acceptIncomingOrderInAdmin(order)
                        },
                        onDeclineOrderAlert = { order ->
                            AdminOrderAlertPlayer.silenceOrderAndStopRinging(order.id)
                            NotificationHelper.cancelAdminOrderAlarmNotification(context, order.id)
                            viewModel.declineIncomingOrderInAdmin(order)
                        },
                        onDoneOrderAlert = { order ->
                            AdminOrderAlertPlayer.silenceOrderAndStopRinging(order.id)
                            NotificationHelper.cancelAdminOrderAlarmNotification(context, order.id)
                            viewModel.acknowledgeOrderAlertDone(order)
                        },
                        onUpdateOrderStatus = { order, nextStatus ->
                            AdminOrderAlertPlayer.silenceOrderAndStopRinging(order.id)
                            NotificationHelper.cancelAdminOrderAlarmNotification(context, order.id)
                            viewModel.updateOrderStatusFromAdmin(order, nextStatus)
                        },
                        onSaveMenuItem = viewModel::saveMenuItemFromAdmin,
                        onDeleteMenuItem = viewModel::deleteMenuItemFromAdmin,
                        onSaveCoupon = viewModel::saveCouponFromAdmin,
                        onSaveStoreConfig = viewModel::saveStoreConfigFromAdmin,
                        onSendCustomerNotification = { title, msg, imageUrl, imageKey ->
                            viewModel.sendCustomCustomerNotification(
                                recipientUid = "ALL",
                                title = title,
                                message = msg,
                                imageUrl = imageUrl,
                                imageKey = imageKey
                            )
                        },
                        onBackToCustomerMenu = { viewModel.switchRole(AppRole.CUSTOMER) },
                        onAdminCreditCustomerWallet = viewModel::adminAddAmountToCustomerWallet,
                        onAdminDeductCustomerWallet = viewModel::adminDeductAmountFromCustomerWallet,
                        onClearOldOrders = viewModel::clearOldOrdersFromAdmin,
                        onDeleteOrder = viewModel::deleteSingleOrderFromAdmin
                    )
                }
            }

            // Sleek bottom "Press again to exit" pill overlay on Home page
            AnimatedVisibility(
                visible = showPressAgainToExitPrompt,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            ) {
                Surface(
                    color = Color(0xEE111827),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CrushYellow),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("press_again_to_exit_pill")
                ) {
                    Text(
                        text = "Press again to exit",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-Screen Dedicated Notifications Page (Opens just like the Dedicated Search Screen when clicking top Bell icon!)
 * Supports rich promotional & order notifications with attached food/offer images.
 */
@Composable
private fun DedicatedNotificationsScreen(
    notifications: List<AppNotification>,
    onMarkAllRead: () -> Unit,
    onBackClick: () -> Unit,
    onExploreMenuClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    // Automatically mark all notifications as seen the moment the screen opens or updates
    LaunchedEffect(notifications.size) {
        onMarkAllRead()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dedicated_notifications_screen"),
        containerColor = Color(0xFFFFF8F6),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFF1F2), CircleShape)
                                    .testTag("notifications_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFFE31837)
                                )
                            }
                            Text(
                                text = "Notifications & Offers",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("notifications_lazy_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFFFF1F2),
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFE31837),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "No Notifications Yet!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "When your pizza order is confirmed, out for delivery, or when My Crush Pizza Hajipur sends special photo offers and wallet bonus cash, they will appear right here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Button(
                                onClick = onExploreMenuClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE31837),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Explore Hot Pizzas & Combos", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    val hasImage = notif.imageUrl.isNotBlank() || notif.imageKey.isNotBlank()
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (!notif.isRead) 1.5.dp else 1.dp,
                            color = if (!notif.isRead) Color(0xFFE31837) else Color(0xFFE2E8F0)
                        ),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notification_card_${notif.id}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (hasImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(175.dp)
                                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                ) {
                                    OnlineFoodImage(
                                        imageUrl = notif.imageUrl,
                                        imageKey = notif.imageKey.ifBlank { "crush_special" },
                                        contentDescription = notif.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color(0xFFE31837),
                                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = notif.type.ifBlank { "OFFER" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFE31837),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!notif.isRead) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFD97706))
                                        ) {
                                            Text(
                                                text = "NEW",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dual-Color Brand Emblem for "MY CRUSH PIZZA" Top Header.
 */
@Composable
private fun MyCrushTopBrandLogo(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = Color.Black,
        border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
        shadowElevation = 3.dp
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_my_crush_pizza_logo),
            contentDescription = "My Crush Pizza Logo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Custom Colorful Vector Badge Logos for Bottom Navigation:
 * - MENU: Golden Cheese Pizza Slice with Pepperoni & Crust
 * - REORDER: Hot Delivery Box + Fast Circular Replay Badge
 * - COMBOS: Pizza Slice + Fizzy Cold Drink Cup with Red Straw Meal Combo
 * - WALLET: Warm Emerald Wallet + Gleaming Gold Coin
 * - CART: Hot Crimson Shopping Cart / Delivery Bag
 */
@Composable
private fun CustomBottomNavLogo(
    tabType: String,
    selected: Boolean
) {
    val bgBrush = when (tabType) {
        "MENU" -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFFE31837), Color(0xFFFF6B35)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)))
        }
        "REORDER" -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFF9F1239), Color(0xFFE11D48)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)))
        }
        "COMBOS" -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
        }
        "WALLET" -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF10B981)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)))
        }
        "ACCOUNT" -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
        }
        else -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFFE31837), Color(0xFFB91C1C)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)))
        }
    }

    val borderColor = when (tabType) {
        "MENU" -> Color(0xFFE31837)
        "REORDER" -> Color(0xFF9F1239)
        "COMBOS" -> Color(0xFFD97706)
        "WALLET" -> Color(0xFF059669)
        "ACCOUNT" -> Color(0xFFD97706)
        else -> Color(0xFFE31837)
    }

    Surface(
        modifier = Modifier.size(30.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor.copy(alpha = if (selected) 1f else 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush),
            contentAlignment = Alignment.Center
        ) {
            when (tabType) {
                "MENU" -> {
                    // Custom Golden Pizza Slice + Menu Emblem
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val w = size.width
                        val h = size.height
                        // Crust arc at top
                        drawRoundRect(
                            color = Color(0xFFD97706),
                            topLeft = Offset(w * 0.14f, h * 0.08f),
                            size = Size(w * 0.72f, h * 0.22f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Melted Cheese Triangle Slice
                        val slice = Path().apply {
                            moveTo(w * 0.18f, h * 0.22f)
                            lineTo(w * 0.82f, h * 0.22f)
                            lineTo(w * 0.50f, h * 0.92f)
                            close()
                        }
                        drawPath(path = slice, color = Color(0xFFFFD54F))
                        // Pepperoni toppings
                        drawCircle(color = Color(0xFFE31837), radius = w * 0.09f, center = Offset(w * 0.42f, h * 0.44f))
                        drawCircle(color = Color(0xFFE31837), radius = w * 0.09f, center = Offset(w * 0.58f, h * 0.48f))
                        drawCircle(color = Color(0xFF15803D), radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.66f))
                    }
                }
                "REORDER" -> {
                    // Custom Fast Reorder Box + Replay Emblem
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Reorder",
                            tint = if (selected) Color.White else Color(0xFF9F1239),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                "COMBOS" -> {
                    // Custom Meal Combo Emblem (Burger/Pizza + Drink Cup)
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Fastfood,
                            contentDescription = "Combos",
                            tint = if (selected) Color.White else Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                "WALLET" -> {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = if (selected) Color.White else Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                "ACCOUNT" -> {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Account",
                            tint = if (selected) Color.White else Color(0xFFB45309),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                else -> {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = if (selected) Color.White else Color(0xFFE31837),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


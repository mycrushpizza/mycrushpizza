package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.model.AddressLabel
import com.example.data.model.AppNotification
import com.example.data.model.HajipurLandmark
import com.example.data.model.HajipurLocations
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaSize
import com.example.data.model.SavedDeliveryAddress
import com.example.data.model.formatInr
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ui.components.CustomerLiveLocationDialog
import com.example.ui.components.HajipurLiveMap
import com.example.ui.components.OnlineFoodImage
import com.example.ui.components.VegNonVegIndicator
import com.example.ui.components.openCustomerLocationInGoogleMaps
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushCardSurface
import com.example.ui.theme.CrushElevatedSurface
import com.example.ui.theme.CrushMutedText
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.theme.VegGreenBright
import com.example.ui.viewmodel.MIN_ORDER_AMOUNT_INR
import com.example.ui.viewmodel.MainAppUiState
import com.example.util.GoogleSignInHelper
import com.example.util.GpsLocationHelper
import com.example.util.LocationSettingsCheckResult
import kotlinx.coroutines.launch

/**
 * Dedicated Customer Bonus Wallet & Rewards Screen (Separate from Cart & Checkout!).
 * - Shows Bonus Cash Balance (Reward & Promo Cash) cleanly without Deposit Cash
 * - Shows Customer Wallet ID so Store Admin can credit Bonus rewards
 */
@Composable
fun CustomerWalletScreen(
    uiState: MainAppUiState,
    onAddMoneyToWallet: (Int, String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onGoToMenuClick: () -> Unit,
    onGoToCartClick: () -> Unit,
    onRequestGoogleSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F6),
                        Color(0xFFFFFBEB),
                        Color(0xFFF0FDF4)
                    )
                )
            )
            .testTag("wallet_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WalletAndRewardsAddBalanceCard(
                customerId = uiState.currentUserId.ifBlank { "guest_customer" },
                bonusBalanceInr = uiState.bonusBalanceInr,
                bonusHistory = uiState.bonusWalletHistory,
                isSignedIn = uiState.isSignedIn,
                onSignInClick = onRequestGoogleSignIn
            )
        }

        // Quick Navigation Row in Wallet Screen (Coupons removed as requested!)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onGoToMenuClick,
                        border = BorderStroke(1.5.dp, Color(0xFFE31837)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_browse_menu_btn")
                    ) {
                        Text("Explore Menu", color = Color(0xFFE31837), fontWeight = FontWeight.ExtraBold)
                    }
                    Button(
                        onClick = onGoToCartClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE31837),
                            contentColor = CrushWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_go_to_cart_btn")
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Go to Cart (${uiState.cartCount})", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Cart & Checkout Screen (Separated from Wallet Screen!):
 * - Shows Cart items & quantity controls
 * - Checkboxes to redeem Deposit Balance & Bonus Balance on this order + quick link to open Wallet tab
 * - Live GPS & Google Maps Customer Delivery Location Picker (starts with NO pre-added fake location)
 * - Bill Summary & Place Order button
 * - "Complete Your Meal • Add More Foods" variety section at the bottom of Checkout showing different foods
 */
@Composable
fun CartAndCheckoutScreen(
    uiState: MainAppUiState,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onUpdateCheckoutDetails: (String?, String?, String?, String?, String?) -> Unit,
    onSelectLandmark: (HajipurLandmark) -> Unit,
    onUpdatePickedLocation: (Double, Double, String, String) -> Unit,
    onPlaceOrderClick: (() -> Unit) -> Unit,
    onBrowseMenuClick: () -> Unit,
    onAddMoneyToWallet: (Int, String) -> Unit = { _, _ -> },
    onToggleUseDepositBalance: (Boolean) -> Unit = {},
    onToggleUseBonusBalance: (Boolean) -> Unit = {},
    onAddToCart: (MenuItem, PizzaSize) -> Unit = { _, _ -> },
    onOpenWalletTab: () -> Unit = {},
    onSelectAddressLabel: (AddressLabel) -> Unit = {},
    onApplySavedAddress: (SavedDeliveryAddress) -> Unit = {},
    onSaveFullCustomerDetails: ((String, String, Double, Double, String, String, AddressLabel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var couponInput by remember { mutableStateOf(uiState.appliedCoupon?.code.orEmpty()) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var gpsStatusNote by remember { mutableStateOf<String?>(null) }
    var isGpsNoteError by remember { mutableStateOf(false) }
    var showRazorpayCheckoutDialog by remember { mutableStateOf(false) }
    var showFullDetailsLocationDialog by remember { mutableStateOf(false) }
    var openLocationDialogInSearchMode by remember { mutableStateOf(false) }
    // 2-Step Cart & Checkout: Step 1 = Cart & Bill Review (NO address in Cart), Step 2 = Delivery Address & Payment
    var checkoutStep by remember { mutableStateOf(1) }
    var addressValidationError by remember { mutableStateOf<String?>(null) }
    var isEditingAddress by remember { mutableStateOf(false) }
    val checkoutListState = rememberLazyListState()

    LaunchedEffect(checkoutStep) {
        checkoutListState.scrollToItem(0)
    }

    BackHandler(enabled = uiState.cartItems.isNotEmpty() && checkoutStep == 2) {
        checkoutStep = 1
    }

    val isStoreOpen = uiState.storeConfig.isOpen
    val isMinimumOrderMet = uiState.isMinimumOrderMet

    val proceedFromCartToAddressAndPayment: () -> Unit = {
        if (!isStoreOpen) {
            addressValidationError = "Store is currently closed. Your items stay saved in your cart."
        } else if (!isMinimumOrderMet) {
            addressValidationError = "Minimum order is ${formatInr(MIN_ORDER_AMOUNT_INR)}. Add ${formatInr(uiState.minOrderShortfallInr)} more."
        } else {
            addressValidationError = null
            checkoutStep = 2
        }
    }

    val validateStep2AddressAndPlaceOrder: () -> Unit = {
        val cleanDigits = uiState.checkoutPhone.filter { it.isDigit() }
        val errorMsg = when {
            !isStoreOpen -> "Store is currently closed. Ordering will resume when the store reopens."
            !isMinimumOrderMet -> "Minimum order is ${formatInr(MIN_ORDER_AMOUNT_INR)}. Add ${formatInr(uiState.minOrderShortfallInr)} more."
            !uiState.isCheckoutWithin10KmRange -> "Delivery only within 10 km (${uiState.checkoutDistanceKm} km)"
            uiState.checkoutName.trim().length < 2 -> "Please enter your full name"
            cleanDigits.length < 10 -> "Please enter a valid 10-digit mobile number"
            uiState.checkoutAddress.trim().length < 5 -> "Please enter your complete delivery address"
            uiState.checkoutLandmark.trim().length < 2 -> "Please enter a nearby landmark"
            else -> null
        }
        if (errorMsg != null) {
            addressValidationError = errorMsg
            isEditingAddress = true
            scope.launch {
                checkoutListState.animateScrollToItem(1)
            }
        } else {
            addressValidationError = null
            onPlaceOrderClick {
                GoogleSignInHelper.onGoogleSignInClicked(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = {},
                    onAuthError = {},
                    scope = scope
                )
            }
        }
    }

    if (showFullDetailsLocationDialog) {
        CustomerLiveLocationDialog(
            currentAddress = uiState.checkoutAddress,
            currentLandmark = uiState.checkoutLandmark,
            currentLat = uiState.checkoutLat,
            currentLng = uiState.checkoutLng,
            currentName = uiState.checkoutName,
            currentPhone = uiState.checkoutPhone,
            currentAddressLabel = uiState.checkoutAddressLabel,
            startInSearchMode = openLocationDialogInSearchMode,
            onSaveLocation = { lat, lng, addr, landmark ->
                onUpdatePickedLocation(lat, lng, addr, landmark)
            },
            onSaveFullCustomerDetailsWithLabel = { name, phone, lat, lng, addr, landmark, label ->
                if (onSaveFullCustomerDetails != null) {
                    onSaveFullCustomerDetails(name, phone, lat, lng, addr, landmark, label)
                } else {
                    onSelectAddressLabel(label)
                    onUpdatePickedLocation(lat, lng, addr, landmark)
                    onUpdateCheckoutDetails(name, phone, addr, landmark, null)
                }
            },
            onDismiss = {
                showFullDetailsLocationDialog = false
                openLocationDialogInSearchMode = false
            }
        )
    }

    if (showRazorpayCheckoutDialog) {
        val storeCfg = uiState.storeConfig
        val merchantUpi = storeCfg.merchantUpiId.ifBlank { "mycrushpizza@upi" }
        val merchantEncoded = Uri.encode(storeCfg.razorpayMerchantName.ifBlank { "My Crush Pizza Hajipur" })
        RazorpayOrderCheckoutDialog(
            amountInr = uiState.grandTotalInr,
            merchantName = storeCfg.razorpayMerchantName.ifBlank { "My Crush Pizza – Hajipur" },
            razorpayKeyId = storeCfg.razorpayKeyId.ifBlank { "rzp_live_MyCrushHajipur01" },
            razorpayMode = storeCfg.razorpayMode.ifBlank { "LIVE" },
            razorpayPaymentLink = storeCfg.razorpayPaymentLink,
            onOpenRazorpayLinkOrUpi = {
                runCatching {
                    val uri = if (storeCfg.razorpayPaymentLink.startsWith("http")) {
                        Uri.parse(storeCfg.razorpayPaymentLink)
                    } else {
                        Uri.parse(
                            "upi://pay?pa=$merchantUpi&pn=$merchantEncoded&am=${uiState.grandTotalInr}&cu=INR&tn=Pizza%20Order"
                        )
                    }
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            },
            onConfirmPaidAndPlaceOrder = {
                showRazorpayCheckoutDialog = false
                onUpdateCheckoutDetails(null, null, null, null, "RAZORPAY")
                onPlaceOrderClick {
                    GoogleSignInHelper.onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {},
                        onAuthError = {},
                        scope = scope
                    )
                }
            },
            onDismiss = { showRazorpayCheckoutDialog = false }
        )
    }

    val triggerCheckoutLiveGps: () -> Unit = {
        isDetectingGps = true
        isGpsNoteError = false
        gpsStatusNote = "Locating..."
        scope.launch {
            val result = GpsLocationHelper.detectCurrentCustomerAddress(context)
            isDetectingGps = false
            result.onSuccess { detected ->
                addressValidationError = null
                onUpdatePickedLocation(
                    detected.lat,
                    detected.lng,
                    detected.formattedAddress,
                    detected.landmarkName.ifBlank { "Current Location" }
                )
                isGpsNoteError = !detected.isWithin10KmRange
                gpsStatusNote = if (detected.isWithin10KmRange) {
                    "Location updated (${detected.distanceKm} km)"
                } else {
                    "Outside 10 km range (${detected.distanceKm} km)"
                }
            }.onFailure {
                isGpsNoteError = true
                gpsStatusNote = "Turn on GPS or search area"
            }
        }
    }

    val checkoutLocationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            triggerCheckoutLiveGps()
        } else {
            isDetectingGps = false
            isGpsNoteError = true
            gpsStatusNote = "Turn on Location to detect your address"
        }
    }

    val ensureLocationOnAndDetectGps: () -> Unit = {
        isDetectingGps = true
        isGpsNoteError = false
        gpsStatusNote = "Checking GPS..."
        scope.launch {
            when (val check = GpsLocationHelper.checkLocationSettingsForGps(context)) {
                LocationSettingsCheckResult.AlreadyEnabled -> {
                    triggerCheckoutLiveGps()
                }
                is LocationSettingsCheckResult.ResolutionRequired -> {
                    isDetectingGps = false
                    gpsStatusNote = "Turning on device location..."
                    runCatching {
                        checkoutLocationSettingsLauncher.launch(check.intentSenderRequest)
                    }.onFailure {
                        isGpsNoteError = true
                        gpsStatusNote = "Turn on Location in settings"
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    }
                }
                LocationSettingsCheckResult.OpenSystemSettingsFallback -> {
                    isDetectingGps = false
                    isGpsNoteError = true
                    gpsStatusNote = "Please turn ON Location in phone settings"
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                }
            }
        }
    }

    val checkoutLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            ensureLocationOnAndDetectGps()
        } else {
            isGpsNoteError = true
            gpsStatusNote = "Allow location permission or search area"
        }
    }

    val requestCheckoutCurrentLocation: () -> Unit = {
        if (GpsLocationHelper.hasLocationPermission(context)) {
            ensureLocationOnAndDetectGps()
        } else {
            isGpsNoteError = false
            gpsStatusNote = "Requesting permission..."
            checkoutLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Stable curated variety of different foods across categories for Checkout "Complete Your Meal"
    // (Does NOT remove items when added so clicking ADD + smoothly turns into - 1 + counter!)
    val suggestedAddOnFoods = remember(uiState.menuItems) {
        val priorityIds = listOf(
            "garlic_zingy_parcel",
            "bev_coldrink_can",
            "bev_mineral_water_1l",
            "shake_kitkat_shake",
            "shake_banana_shake",
            "pasta_veg_masala_maggi",
            "pasta_cheese_masala_maggi",
            "dessert_choco_lava_cake",
            "dessert_white_sponges",
            "teaser_capsicum_paneer",
            "veg_peppy_paneer",
            "cheese_kulhad_pizza",
            "burger_veg_crispy",
            "sandwich_paneer_tikka",
            "starter_paneer_pakora",
            "chinese_honey_chilli_potato",
            "noodles_veg_chowmin",
            "biryani_chicken_dum_hyderabadi"
        )
        val prioritized = priorityIds.mapNotNull { id -> uiState.menuItems.find { it.id == id } }
        val others = uiState.menuItems.filter { item -> prioritized.none { it.id == item.id } }.take(12)
        (prioritized + others).take(16)
    }

    val distanceKm = HajipurLocations.calculateDistanceKm(
        HajipurLocations.KITCHEN_LAT,
        HajipurLocations.KITCHEN_LNG,
        uiState.checkoutLat,
        uiState.checkoutLng
    )
    val estMins = HajipurLocations.estimateMinutesForDistance(distanceKm, OrderStatus.CONFIRMED)

    val paymentOptions = remember {
        listOf(
            Triple("COD", "Cash on Delivery (Pay ₹ at Doorstep)", Icons.Filled.Money)
        )
    }

    val totalSavingsInr = uiState.discountInr + uiState.walletTotalUsedInr +
        if (uiState.subtotalInr >= uiState.storeConfig.freeDeliveryMinOrderInr) uiState.storeConfig.deliveryChargeInr else 0

    val toggleUnifiedBalance: (Boolean) -> Unit = { enabled ->
        onToggleUseBonusBalance(enabled)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        LazyColumn(
            state = checkoutListState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("checkout_lazy_column"),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 12.dp,
                bottom = if (uiState.cartItems.isNotEmpty()) 148.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.cartItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color(0xFFFFF1F2),
                                shape = CircleShape,
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingBag,
                                        contentDescription = "Empty Cart",
                                        tint = Color(0xFFE31837),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your Cart is Empty!",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add hot pizzas, burgers, garlic bread, Maggi, shakes, or main course dishes below or explore our full Hajipur menu.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBrowseMenuClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE31837),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("empty_cart_browse_btn")
                            ) {
                                Text(
                                    text = "Explore Hajipur Menu",
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                if (!isStoreOpen) {
                    item(key = "cart_store_closed_notice") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_store_closed_banner"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.5.dp, Color(0xFFE31837))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "STORE CURRENTLY CLOSED • ORDERS PAUSED",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "You can still browse pizzas and add/edit items in your cart. Placing an order is paused until My Crush Pizza Hajipur reopens (${uiState.storeConfig.openTime} – ${uiState.storeConfig.closeTime}).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }

                if (!isMinimumOrderMet) {
                    item(key = "cart_min_order_notice") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_min_order_banner"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.5.dp, Color(0xFFEA580C))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "MINIMUM ORDER ${formatInr(MIN_ORDER_AMOUNT_INR)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFC2410C),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Add items worth ${formatInr(uiState.minOrderShortfallInr)} more to place your order.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9A3412),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onBrowseMenuClick() },
                                    color = Color(0xFFEA580C),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+ Add Items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 0. Domino's Signature Free Delivery & Savings Top Banner Strip
                item {
                    val freeDeliveryShortfall = (uiState.storeConfig.freeDeliveryMinOrderInr - uiState.subtotalInr).coerceAtLeast(0)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (freeDeliveryShortfall == 0) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                        border = BorderStroke(
                            1.2.dp,
                            if (freeDeliveryShortfall == 0) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (freeDeliveryShortfall == 0) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (freeDeliveryShortfall == 0) {
                                            "FREE Delivery Unlocked on this Order!"
                                        } else {
                                            "Add items worth ${formatInr(freeDeliveryShortfall)} more for FREE Delivery"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (freeDeliveryShortfall == 0) Color(0xFF065F46) else Color(0xFF92400E),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (totalSavingsInr > 0) {
                                        Text(
                                            text = "You are saving ${formatInr(totalSavingsInr)} on this order",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Text(
                                    text = "30-45 MINS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (checkoutStep == 1) {
                    // ==================== STEP 1 PAGE: CART ITEMS & DELIVERY ADDRESS ====================
                    // 1. Domino's-Style Cart Items Card (Elevated Crisp White Card with Red-Border Steppers)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
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
                                        Surface(
                                            color = Color(0xFFFFF1F2),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFECDD3))
                                        ) {
                                            Text(
                                                text = "${uiState.cartCount} ITEM${if (uiState.cartCount > 1) "S" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE31837),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = "YOUR ORDER SUMMARY",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onBrowseMenuClick() },
                                        color = Color(0xFFFFF1F2),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE31837))
                                    ) {
                                        Text(
                                            text = "+ Add More",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE31837),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                uiState.cartItems.forEachIndexed { index, cartItem ->
                                    if (index > 0) {
                                        HorizontalDivider(color = Color(0xFFF8FAFC))
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OnlineFoodImage(
                                                imageKey = cartItem.menuItem.imageKey,
                                                imageUrl = cartItem.menuItem.imageUrl,
                                                contentDescription = cartItem.menuItem.title,
                                                modifier = Modifier
                                                    .size(58.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    VegNonVegIndicator(isVeg = cartItem.menuItem.isVeg)
                                                    Text(
                                                        text = cartItem.menuItem.title,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = Color(0xFF0F172A),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = if (cartItem.menuItem.supportsMultipleSizes()) {
                                                        "${cartItem.size.label} Crust • ${formatInr(cartItem.unitPriceInr)}"
                                                    } else {
                                                        "Regular Serve • ${formatInr(cartItem.unitPriceInr)}"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF64748B),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Domino's-Style Crisp White & Crimson Quantity Stepper
                                            Surface(
                                                color = Color(0xFFFFF1F2),
                                                shape = RoundedCornerShape(9.dp),
                                                border = BorderStroke(1.3.dp, Color(0xFFE31837)),
                                                modifier = Modifier
                                                    .width(96.dp)
                                                    .height(34.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { onUpdateCartQuantity(cartItem.key, -1) },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Remove,
                                                            contentDescription = "Decrease",
                                                            tint = Color(0xFFE31837),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = cartItem.quantity.toString(),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = Color(0xFF0F172A),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1
                                                    )
                                                    IconButton(
                                                        onClick = { onUpdateCartQuantity(cartItem.key, 1) },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Add,
                                                            contentDescription = "Increase",
                                                            tint = Color(0xFFE31837),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = formatInr(cartItem.lineTotalInr),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                // Domino's Style Complimentary Seasoning Strip
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "🌶️ Oregano & Chilli Flakes sachets included",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "FREE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Domino's "Complete Your Meal • Add More Foods" Section right below Cart Items!
                    item {
                        CheckoutAddMoreFoodsSection(
                            suggestedItems = suggestedAddOnFoods,
                            cartItems = uiState.cartItems,
                            onAddFoodItem = { food -> onAddToCart(food, PizzaSize.REGULAR) },
                            onUpdateCartQuantity = onUpdateCartQuantity,
                            onBrowseFullMenu = onBrowseMenuClick
                        )
                    }

                    // 3. Bill Details Card (Shown inside Cart Section — Address comes later in Step 2!)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "BILL DETAILS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold
                                )

                                BillRow("Item Total (${uiState.cartCount} items)", formatInr(uiState.subtotalInr))
                                if (uiState.discountInr > 0) {
                                    BillRow(
                                        "Coupon Discount (${uiState.appliedCoupon?.code})",
                                        "-${formatInr(uiState.discountInr)}",
                                        valueColor = Color(0xFF059669)
                                    )
                                }
                                BillRow(
                                    "Delivery Partner Fee (Free above ₹${uiState.storeConfig.freeDeliveryMinOrderInr})",
                                    if (uiState.deliveryFeeInr == 0) "FREE" else formatInr(uiState.deliveryFeeInr),
                                    valueColor = if (uiState.deliveryFeeInr == 0) Color(0xFF059669) else Color(0xFF0F172A)
                                )
                                BillRow("Taxes & Charges (GST ${uiState.storeConfig.taxPercent}%)", formatInr(uiState.taxInr))

                                if (uiState.walletTotalUsedInr > 0) {
                                    BillRow(
                                        "Balance Used",
                                        "-${formatInr(uiState.walletTotalUsedInr)}",
                                        valueColor = Color(0xFF059669)
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "To Pay",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        if (uiState.walletTotalUsedInr > 0) {
                                            Text(
                                                text = "Includes ${formatInr(uiState.walletTotalUsedInr)} Balance discount",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF059669),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatInr(uiState.grandTotalInr),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color(0xFFE31837),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ==================== STEP 2 PAGE: DELIVERY DETAILS, BILL REVIEW & PAYMENT ====================
                    // 1. Clean 2-Step Progress Header with Back & Edit Cart
                    item(key = "step2_progress_header") {
                        CheckoutStepTwoProgressHeader(
                            cartCount = uiState.cartCount,
                            grandTotalInr = uiState.grandTotalInr,
                            onEditCartClick = { checkoutStep = 1 }
                        )
                    }

                    // 2. Smart Delivery Details Card (Compact Saved Address or Full Interactive Editor)
                    item(key = "step2_delivery_card") {
                        CheckoutStepTwoDeliveryCard(
                            uiState = uiState,
                            distanceKm = distanceKm,
                            estMins = estMins,
                            isEditingAddress = isEditingAddress,
                            onToggleEditAddress = { isEditingAddress = it },
                            isDetectingGps = isDetectingGps,
                            gpsStatusNote = gpsStatusNote,
                            isGpsNoteError = isGpsNoteError,
                            addressValidationError = addressValidationError,
                            onClearValidationError = { addressValidationError = null },
                            onUpdateCheckoutDetails = onUpdateCheckoutDetails,
                            onRequestCurrentLocation = requestCheckoutCurrentLocation,
                            onOpenSearchLocationDialog = {
                                openLocationDialogInSearchMode = true
                                showFullDetailsLocationDialog = true
                            },
                            onSelectLandmark = onSelectLandmark,
                            onMapPinPicked = { lat, lng, pickedAddr ->
                                addressValidationError = null
                                val pinDist = HajipurLocations.calculateDistanceKm(
                                    HajipurLocations.KITCHEN_LAT,
                                    HajipurLocations.KITCHEN_LNG,
                                    lat,
                                    lng
                                )
                                isGpsNoteError = !HajipurLocations.isWithinDeliveryRange(pinDist)
                                onUpdatePickedLocation(
                                    lat,
                                    lng,
                                    pickedAddr,
                                    uiState.checkoutLandmark.ifBlank { "Pinned Location" }
                                )
                                gpsStatusNote = if (isGpsNoteError) {
                                    "Outside 10 km ($pinDist km)"
                                } else {
                                    "Pin updated ($pinDist km)"
                                }
                            },
                            onSelectAddressLabel = onSelectAddressLabel,
                            onApplySavedAddress = onApplySavedAddress,
                            onConfirmAndSaveAddress = {
                                onSaveFullCustomerDetails?.invoke(
                                    uiState.checkoutName,
                                    uiState.checkoutPhone,
                                    uiState.checkoutLat,
                                    uiState.checkoutLng,
                                    uiState.checkoutAddress,
                                    uiState.checkoutLandmark,
                                    uiState.checkoutAddressLabel
                                )
                            }
                        )
                    }

                    // 3. Bonus Wallet Cash Toggle & Expandable Order Bill Review
                    item(key = "step2_review_and_wallet") {
                        CheckoutStepTwoReviewAndWalletCard(
                            uiState = uiState,
                            onToggleUseBonusBalance = toggleUnifiedBalance
                        )
                    }

                    // 4. Clean Payment Method Card
                    item(key = "step2_payment_card") {
                        CheckoutStepTwoPaymentCard(
                            grandTotalInr = uiState.grandTotalInr,
                            onSelectCod = { onUpdateCheckoutDetails(null, null, null, null, "COD") }
                        )
                    }
                }
            }
        }

        // Sticky Bottom Bar for 2-Step Checkout:
        // - Step 1 (Cart): Shows "NEXT: ADDRESS & PAYMENT →" button (No Address taken in Cart!)
        // - Step 2 (Address & Payment): Shows Payment Method Quick Bar + "PLACE ORDER" button
        if (uiState.cartItems.isNotEmpty()) {
            if (checkoutStep == 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .testTag("checkout_step_1_bottom_dock"),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(0.95f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formatInr(uiState.grandTotalInr),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "TO PAY • ${uiState.cartCount} ITEM${if (uiState.cartCount > 1) "S" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE31837),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { proceedFromCartToAddressAndPayment() },
                            enabled = isStoreOpen && isMinimumOrderMet,
                            modifier = Modifier
                                .weight(1.65f)
                                .height(52.dp)
                                .testTag("proceed_to_payment_step_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE31837),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF94A3B8),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text(
                                text = when {
                                    !isStoreOpen -> "STORE CLOSED"
                                    !isMinimumOrderMet -> "Add ${formatInr(uiState.minOrderShortfallInr)} (Min ₹99)"
                                    else -> "NEXT: ADDRESS & PAY →"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .testTag("dominos_bottom_payment_dock"),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!addressValidationError.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE31837)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = addressValidationError.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Sleek Single-Row Checkout Bar: Left = Grand Total & COD ETA | Right = Big Place Order CTA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(0.95f)) {
                                Text(
                                    text = formatInr(uiState.grandTotalInr),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Money,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "COD • 30-45 min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = { validateStep2AddressAndPlaceOrder() },
                                enabled = isStoreOpen && isMinimumOrderMet && !uiState.isBusy && uiState.isCheckoutWithin10KmRange,
                                modifier = Modifier
                                    .weight(1.65f)
                                    .height(52.dp)
                                    .testTag("place_order_btn"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isStoreOpen && isMinimumOrderMet && uiState.isCheckoutWithin10KmRange) Color(0xFFE31837) else Color(0xFF6B7280),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF94A3B8),
                                    disabledContentColor = Color.White
                                )
                            ) {
                                if (uiState.isBusy) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Placing Order...",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                } else {
                                    Text(
                                        text = when {
                                            !isStoreOpen -> "Store Closed"
                                            !isMinimumOrderMet -> "Min Order ₹99"
                                            !uiState.isCheckoutWithin10KmRange -> "Outside 10 km"
                                            uiState.isSignedIn -> "Place Order →"
                                            else -> "Sign In & Order →"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
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

/**
 * Domino's-Style "Complete Your Meal • Add More Foods" Section inside Cart & Checkout:
 * - Clean white cards with crisp red ADD + buttons that NEVER squish or wrap
 * - When an item is added to cart, smoothly turns into a live `-  1  +` quantity counter!
 */
@Composable
private fun CheckoutAddMoreFoodsSection(
    suggestedItems: List<MenuItem>,
    cartItems: List<com.example.data.model.CartItem>,
    onAddFoodItem: (MenuItem) -> Unit,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onBrowseFullMenu: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_add_more_foods_section"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFECDD3))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Fastfood,
                                contentDescription = "Add More Foods",
                                tint = Color(0xFFE31837),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "COMPLETE YOUR MEAL • ADD MORE FOODS",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Popular drinks, shakes, Maggi, sides, pizzas & desserts",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE31837),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onBrowseFullMenu() }
                )
            }

            // 1. Horizontal Quick-Add Carousel (Full-width ADD + button on each card so it never squishes!)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_addon_horizontal_row")
            ) {
                items(suggestedItems.take(8), key = { "carousel_${it.id}" }) { item ->
                    val cartKey = "${item.id}_${PizzaSize.REGULAR.name}"
                    val qtyInCart = cartItems.find { it.key == cartKey }?.quantity ?: 0

                    Surface(
                        modifier = Modifier
                            .width(178.dp)
                            .testTag("checkout_addon_card_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                OnlineFoodImage(
                                    imageKey = item.imageKey,
                                    imageUrl = item.imageUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .padding(2.dp)
                                ) {
                                    VegNonVegIndicator(isVeg = item.isVeg)
                                }
                                Surface(
                                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = formatInr(item.priceRegularInr),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.displaySubCategoryLabel(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (qtyInCart == 0) {
                                Button(
                                    onClick = { onAddFoodItem(item) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE31837),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("checkout_addon_add_${item.id}")
                                ) {
                                    Text(
                                        text = "ADD +",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFE31837),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("checkout_addon_add_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateCartQuantity(cartKey, -1) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Decrease",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "$qtyInCart ADDED",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                        IconButton(
                                            onClick = { onUpdateCartQuantity(cartKey, 1) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Increase",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Additional Vertical List of Different Foods below the Carousel (With Guaranteed-Width ADD + Buttons!)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestedItems.drop(8).take(6).forEach { item ->
                    val cartKey = "${item.id}_${PizzaSize.REGULAR.name}"
                    val qtyInCart = cartItems.find { it.key == cartKey }?.quantity ?: 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_more_food_row_${item.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OnlineFoodImage(
                                    imageKey = item.imageKey,
                                    imageUrl = item.imageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        VegNonVegIndicator(isVeg = item.isVeg)
                                        Text(
                                            text = item.displaySubCategoryLabel(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE31837),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatInr(item.priceRegularInr),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            if (qtyInCart == 0) {
                                Button(
                                    onClick = { onAddFoodItem(item) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE31837),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .width(106.dp)
                                        .height(38.dp)
                                        .testTag("checkout_addon_add_${item.id}")
                                ) {
                                    Text(
                                        text = "ADD +",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFE31837),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .width(106.dp)
                                        .height(38.dp)
                                        .testTag("checkout_addon_add_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateCartQuantity(cartKey, -1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Decrease",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = qtyInCart.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                        IconButton(
                                            onClick = { onUpdateCartQuantity(cartKey, 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Increase",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
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
}

@Composable
private fun WalletTopUpConfirmDialog(
    amountInr: Int,
    method: String,
    razorpayEnabled: Boolean = false,
    razorpayKeyId: String = "",
    razorpayMode: String = "LIVE",
    razorpayPaymentLink: String = "",
    onOpenUpi: () -> Unit,
    onConfirmPaid: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("wallet_payment_confirm_dialog"),
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
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Add ${formatInr(amountInr)} to Wallet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (method == "RAZORPAY" || razorpayEnabled) {
                            "Razorpay ($razorpayMode) • Key: ${razorpayKeyId.ifBlank { "Instant" }}"
                        } else {
                            "Method: $method • Instant Deposit Cash"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE31837),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Complete your ${formatInr(amountInr)} payment via $method. Once paid, ${formatInr(amountInr)} will be added immediately to your Deposit Balance and is 100% usable on any food order.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155)
                )

                OutlinedButton(
                    onClick = onOpenUpi,
                    border = BorderStroke(1.5.dp, Color(0xFFE31837)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (method == "RAZORPAY" && razorpayPaymentLink.isNotBlank()) {
                            "Open Razorpay Payment Link"
                        } else {
                            "Open UPI / Razorpay App (GPay / PhonePe / Paytm)"
                        },
                        color = Color(0xFFE31837),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onConfirmPaid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE31837),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_wallet_payment_btn")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Payment & Add ${formatInr(amountInr)}",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun RazorpayOrderCheckoutDialog(
    amountInr: Int,
    merchantName: String,
    razorpayKeyId: String,
    razorpayMode: String,
    razorpayPaymentLink: String,
    onOpenRazorpayLinkOrUpi: () -> Unit,
    onConfirmPaidAndPlaceOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("razorpay_order_checkout_dialog"),
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = "Razorpay",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Razorpay Secure Checkout ($razorpayMode) • $razorpayKeyId",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Payable Order Amount: ${formatInr(amountInr)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "UPI (GPay / PhonePe / Paytm) • Visa / MasterCard / RuPay • NetBanking",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0369A1)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenRazorpayLinkOrUpi,
                    border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (razorpayPaymentLink.isNotBlank()) {
                            "Open Razorpay Payment Page"
                        } else {
                            "Pay via UPI App (GPay / PhonePe / Paytm)"
                        },
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onConfirmPaidAndPlaceOrder,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_razorpay_order_payment_btn")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay ${formatInr(amountInr)} & Place Order",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * My Crush Bonus Wallet & Rewards Card (Bonus Cash Only — Deposit Cash removed as requested).
 * - Warm Crimson-Berry & Golden Gradient Hero Wallet Header
 * - Dedicated Golden-Amber Bonus Cash Balance Card + Wallet ID
 * - Clear usage guide showing how Bonus Cash applies automatically at Checkout
 */
@Composable
fun WalletAndRewardsAddBalanceCard(
    customerId: String,
    bonusBalanceInr: Int,
    bonusHistory: List<AppNotification> = emptyList(),
    isSignedIn: Boolean = false,
    onSignInClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_crush_wallet_rewards_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFECDD3))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Warm Crimson, Rose & Amber Gradient Hero Wallet Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF9F1239),
                                Color(0xFFE31837),
                                Color(0xFFD97706)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = "My Crush Bonus Wallet",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "MY CRUSH BONUS WALLET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatInr(bonusBalanceInr),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Available Bonus Cash • Ready to use",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFE4E6)
                        )
                    }

                    Surface(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "REWARDS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "1-Tap Use",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 2. Full-Width Golden Amber BONUS CASH Card (Deposit Cash Removed)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bonus_balance_box"),
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "BONUS CASH",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = formatInr(bonusBalanceInr),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("bonus_balance_value")
                            )
                            Text(
                                text = "Reward & Promo Cash • 100% Usable on Hajipur Food Orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 3. Bonus History Card (Shows "No History" until Admin credits bonus to this user's ID)
                val dateFormatter = remember {
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_bonus_history_card"),
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = "Bonus History",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Bonus History",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            if (bonusHistory.isNotEmpty()) {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${bonusHistory.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (!isSignedIn) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Sign in to view your bonus history",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(
                                    onClick = onSignInClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE31837),
                                        contentColor = CrushWhite
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("wallet_sign_in_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sign In",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        } else if (bonusHistory.isEmpty() && bonusBalanceInr <= 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Transactions Yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.testTag("wallet_no_history_text")
                                )
                            }
                        } else if (bonusHistory.isEmpty() && bonusBalanceInr > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bonus Credited",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Added to your Wallet ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+${formatInr(bonusBalanceInr)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            bonusHistory.forEachIndexed { idx, entry ->
                                val isDebit = entry.type == "WALLET_DEBIT"
                                val amountMatch = Regex("₹\\s*([0-9,]+)").find(entry.title)
                                    ?: Regex("₹\\s*([0-9,]+)").find(entry.message)
                                val amountLabel = amountMatch?.groupValues?.getOrNull(1)?.let {
                                    if (isDebit) "-₹$it" else "+₹$it"
                                } ?: if (isDebit) "Deducted" else "Credited"
                                val timeText = entry.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Just now"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("wallet_history_item_$idx"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = entry.title.ifBlank {
                                                if (isDebit) "Bonus Deducted" else "Bonus Credited"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = timeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Surface(
                                        color = if (isDebit) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = amountLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isDebit) Color(0xFFB91C1C) else Color(0xFF15803D),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (idx < bonusHistory.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
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
private fun BillRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}


package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mycrushpizza.`in`.R
import com.example.data.model.AddressLabel
import com.example.data.model.AppNotification
import com.example.data.model.AppRole
import com.example.data.model.CartItem
import com.example.data.model.Coupon
import com.example.data.model.DeliveryPartner
import com.example.data.model.HajipurLandmark
import com.example.data.model.HajipurLocations
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.model.PizzaSize
import com.example.data.model.SavedAddressCodec
import com.example.data.model.SavedDeliveryAddress
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.AuthAndUserRepository
import com.example.data.repository.OrderAndNotificationRepository
import com.example.data.repository.StoreAndMenuRepository
import com.example.util.AdminOrderAlarmEvents
import com.example.util.AdminOrderAlertPlayer
import com.example.util.NotificationHelper
import com.example.util.NotificationReadStatePrefs
import com.example.util.RecentSearchPrefs
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

enum class CustomerTab(val label: String) {
    MENU("Menu"),
    WALLET("Wallet"),
    CART("Cart"),
    TRACK("Orders"),
    ACCOUNT("Account")
}

enum class OrderHistoryFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

const val MIN_ORDER_AMOUNT_INR = 99

data class MainAppUiState(
    val isSignedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val isOnlineConnected: Boolean = true,
    val currentUserId: String = "",
    val currentUserEmail: String = "",
    val currentUserName: String = "",
    val userProfile: UserProfile? = null,
    val activeRole: AppRole = AppRole.CUSTOMER,
    val selectedCustomerTab: CustomerTab = CustomerTab.MENU,

    // Catalog & Store Config (Available in both Guest & Authenticated modes)
    val storeConfig: StoreConfig = StoreAndMenuRepository.defaultStoreConfig(),
    val menuItems: List<MenuItem> = StoreAndMenuRepository.defaultHajipurMenu(),
    val coupons: List<Coupon> = StoreAndMenuRepository.defaultCoupons(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val recentSearches: List<String> = emptyList(),
    val vegOnlyFilter: Boolean = false,

    // Wallet & Rewards State (Deposit Balance vs Bonus Balance shown separately, both start at ₹0!)
    val depositBalanceInr: Int = 0,
    val bonusBalanceInr: Int = 0,
    val useDepositBalanceInCheckout: Boolean = true,
    val useBonusBalanceInCheckout: Boolean = true,

    // Cart & Checkout State (Starts with NO pre-added address; customer sets live GPS / Google Map location)
    val cartItems: List<CartItem> = emptyList(),
    val appliedCoupon: Coupon? = null,
    val couponMessage: String? = null,
    val checkoutName: String = "",
    val checkoutPhone: String = "",
    val checkoutAddress: String = "",
    val checkoutLandmark: String = "",
    val checkoutAddressLabel: AddressLabel = AddressLabel.HOME,
    val savedAddresses: List<SavedDeliveryAddress> = emptyList(),
    val checkoutLat: Double = 25.6879,
    val checkoutLng: Double = 85.2118,
    val selectedPaymentMethod: String = "COD",
    val pendingOrderAfterSignIn: Boolean = false,
    val lastPlacedOrder: PizzaOrder? = null,

    // Orders, Order History & Live Tracking
    val customerOrders: List<PizzaOrder> = emptyList(),
    val isOrderHistoryLoading: Boolean = false,
    val isOrderHistoryRefreshing: Boolean = false,
    val orderHistoryError: String? = null,
    val orderHistoryFilter: OrderHistoryFilter = OrderHistoryFilter.ALL,
    val orderHistorySearchQuery: String = "",
    val trackedOrderId: String = "",
    val assignedDeliveryOrders: List<PizzaOrder> = emptyList(),
    val allAdminOrders: List<PizzaOrder> = emptyList(),
    val allUsersForAdmin: List<UserProfile> = emptyList(),
    val deliveryPartners: List<DeliveryPartner> = StoreAndMenuRepository.defaultDeliveryPartners(),

    // Delivery Boy Session State
    val isDeliveryPartnerLoggedIn: Boolean = false,
    val activeDeliveryPartner: DeliveryPartner? = null,
    val isLiveGpsStreaming: Boolean = false,

    // Admin Incoming Order Continuous Alert State (Restricted exclusively to pranavgamer758@gmail.com when logged in)
    val hasAdminPanelAccess: Boolean = false,
    val unacknowledgedAdminOrders: List<PizzaOrder> = emptyList(),
    val locallyDismissedAlertOrderIds: Set<String> = emptySet(),
    val locallyAcceptedOrderIds: Set<String> = emptySet(),
    val locallyDeclinedOrderIds: Set<String> = emptySet(),
    val locallyDeletedOrderIds: Set<String> = emptySet(),
    val lastAcceptedAdminOrder: PizzaOrder? = null,
    val isCustomerOrderDetailsOpen: Boolean = false,

    // Notifications & Status Messages
    val notifications: List<AppNotification> = emptyList(),
    val locallyReadNotificationIds: Set<String> = emptySet(),
    val accountNotificationFloorMillis: Long = 0L,
    val statusBannerMessage: String? = null,
    val errorBannerMessage: String? = null,
    val isBusy: Boolean = false
) {
    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead && it.id !in locallyReadNotificationIds }

    val totalWalletBalanceInr: Int
        get() = bonusBalanceInr

    val bonusWalletHistory: List<AppNotification>
        get() = notifications.filter {
            it.type == "WALLET_CREDIT" || it.type == "WALLET_DEBIT"
        }

    val cartCount: Int
        get() = cartItems.sumOf { it.quantity }

    val subtotalInr: Int
        get() = cartItems.sumOf { it.lineTotalInr }

    val isMinimumOrderMet: Boolean
        get() = subtotalInr >= MIN_ORDER_AMOUNT_INR

    val minOrderShortfallInr: Int
        get() = (MIN_ORDER_AMOUNT_INR - subtotalInr).coerceAtLeast(0)

    val discountInr: Int
        get() = appliedCoupon?.calculateDiscount(subtotalInr) ?: 0

    val deliveryFeeInr: Int
        get() = if (subtotalInr == 0 || subtotalInr >= storeConfig.freeDeliveryMinOrderInr) {
            0
        } else {
            storeConfig.deliveryChargeInr
        }

    val taxInr: Int
        get() = (((subtotalInr - discountInr).coerceAtLeast(0) * storeConfig.taxPercent) / 100.0).roundToInt()

    val preWalletTotalInr: Int
        get() = ((subtotalInr - discountInr).coerceAtLeast(0) + deliveryFeeInr + taxInr).coerceAtLeast(0)

    val bonusUsedInr: Int
        get() = if (useBonusBalanceInCheckout && preWalletTotalInr > 0) {
            bonusBalanceInr.coerceAtMost(preWalletTotalInr)
        } else {
            0
        }

    val depositUsedInr: Int
        get() = 0

    val walletTotalUsedInr: Int
        get() = bonusUsedInr

    val useWalletBalanceInCheckout: Boolean
        get() = useBonusBalanceInCheckout

    val grandTotalInr: Int
        get() = (preWalletTotalInr - walletTotalUsedInr).coerceAtLeast(0)

    val checkoutDistanceKm: Double
        get() = HajipurLocations.calculateDistanceKm(
            storeConfig.storeLat,
            storeConfig.storeLng,
            checkoutLat,
            checkoutLng
        )

    val isCheckoutWithin10KmRange: Boolean
        get() = HajipurLocations.isWithinDeliveryRange(checkoutDistanceKm)

    val activeTrackedOrder: PizzaOrder?
        get() = customerOrders.firstOrNull { it.id == trackedOrderId }
            ?: customerOrders.firstOrNull { !it.isTerminal }
            ?: lastPlacedOrder
            ?: customerOrders.firstOrNull()

    val orderHistoryActiveCount: Int
        get() = customerOrders.count { !it.isTerminal }

    val orderHistoryDeliveredCount: Int
        get() = customerOrders.count { it.orderStatus == OrderStatus.DELIVERED }

    val orderHistoryCancelledCount: Int
        get() = customerOrders.count { it.orderStatus == OrderStatus.CANCELLED }

    val orderHistoryTotalSpentInr: Int
        get() = customerOrders
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .sumOf { it.totalInr }

    val filteredOrderHistory: List<PizzaOrder>
        get() {
            val byStatus = when (orderHistoryFilter) {
                OrderHistoryFilter.ALL -> customerOrders
                OrderHistoryFilter.ACTIVE -> customerOrders.filter { !it.isTerminal }
                OrderHistoryFilter.DELIVERED -> customerOrders.filter { it.orderStatus == OrderStatus.DELIVERED }
                OrderHistoryFilter.CANCELLED -> customerOrders.filter { it.orderStatus == OrderStatus.CANCELLED }
            }
            val query = orderHistorySearchQuery.trim()
            if (query.isBlank()) return byStatus
            return byStatus.filter { order ->
                order.orderNumber.contains(query, ignoreCase = true) ||
                    order.itemsSummary.contains(query, ignoreCase = true) ||
                    order.deliveryAddress.contains(query, ignoreCase = true) ||
                    order.paymentMethod.contains(query, ignoreCase = true) ||
                    order.orderStatus.title.contains(query, ignoreCase = true)
            }
        }

    val isAdminUser: Boolean
        get() = (isSignedIn && (
            AuthAndUserRepository.isBootstrappedAdminPanelEmail(currentUserEmail) ||
                hasAdminPanelAccess
            )) || activeRole == AppRole.ADMIN

    val ringingAdminOrders: List<PizzaOrder>
        get() = (allAdminOrders + customerOrders)
            .distinctBy { it.id }
            .filter {
                it.orderStatus == OrderStatus.CONFIRMED &&
                    !it.adminAlertAcknowledged &&
                    it.id !in locallyDismissedAlertOrderIds &&
                    it.id !in locallyAcceptedOrderIds &&
                    it.id !in locallyDeclinedOrderIds &&
                    !AdminOrderAlertPlayer.isOrderSilenced(it.id)
            }

    val isAdminRinging: Boolean
        get() = isAdminUser && ringingAdminOrders.isNotEmpty()
}

class MainAppViewModel(
    private val userRepo: AuthAndUserRepository,
    private val storeRepo: StoreAndMenuRepository,
    private val orderRepo: OrderAndNotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainAppUiState())
    val uiState: StateFlow<MainAppUiState> = _uiState.asStateFlow()

    private val listenerJobs = mutableListOf<Job>()
    private val adminListenerJobs = mutableListOf<Job>()
    private var observedUid: String? = null
    private val notifiedIds = mutableSetOf<String>()
    private val customerMessageDeliveryCounts = mutableMapOf<String, Int>()

    init {
        viewModelScope.launch {
            AdminOrderAlarmEvents.events.collect { event ->
                val targetOrder = (_uiState.value.allAdminOrders + _uiState.value.customerOrders)
                    .firstOrNull { it.id == event.orderId }
                if (targetOrder != null) {
                    if (event.accepted) {
                        acceptIncomingOrderInAdmin(targetOrder)
                    } else {
                        declineIncomingOrderInAdmin(targetOrder)
                    }
                } else {
                    _uiState.update { state ->
                        val nextStatus = if (event.accepted) OrderStatus.PREPARING else OrderStatus.CANCELLED
                        state.copy(
                            locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + event.orderId,
                            allAdminOrders = state.allAdminOrders.map {
                                if (it.id == event.orderId) it.copy(status = nextStatus.code, adminAlertAcknowledged = true) else it
                            },
                            customerOrders = state.customerOrders.map {
                                if (it.id == event.orderId) it.copy(status = nextStatus.code, adminAlertAcknowledged = true) else it
                            },
                            statusBannerMessage = if (event.accepted) {
                                "Order ${event.orderNumber} ACCEPTED from Alarm Notification!"
                            } else {
                                "Order ${event.orderNumber} DECLINED from Alarm Notification."
                            }
                        )
                    }
                }
            }
        }
    }

    fun enterGuestMode() {
        _uiState.update {
            it.copy(
                isGuestMode = true,
                hasAdminPanelAccess = false,
                activeRole = AppRole.CUSTOMER,
                errorBannerMessage = null
            )
        }
    }

    fun onUserAuthenticated(
        uid: String,
        email: String,
        displayName: String,
        onNewNotification: (String, String) -> Unit = { _, _ -> },
        onNewNotificationDoc: (String, String, String) -> Unit = { _, title, msg -> onNewNotification(title, msg) }
    ) {
        if (uid.isBlank()) return
        val isFirstAttachForUid = observedUid != uid
        observedUid = uid
        val isAdminEmail = userRepo.isBootstrappedAdminEmail(email)

        _uiState.update { state ->
            state.copy(
                isSignedIn = true,
                isGuestMode = false,
                currentUserId = uid,
                currentUserEmail = email.trim(),
                currentUserName = displayName.ifBlank { state.currentUserName.ifBlank { "Hajipur Customer" } },
                hasAdminPanelAccess = isAdminEmail,
                activeRole = if (!isAdminEmail && state.activeRole == AppRole.ADMIN) {
                    AppRole.CUSTOMER
                } else {
                    state.activeRole
                },
                checkoutName = state.checkoutName.ifBlank { displayName },
                errorBannerMessage = null
            )
        }

        if (isFirstAttachForUid) {
            val authCreationMs = runCatching {
                Firebase.auth.currentUser?.metadata?.creationTimestamp ?: 0L
            }.getOrDefault(0L)
            if (authCreationMs > 0L) {
                _uiState.update { state ->
                    state.copy(
                        accountNotificationFloorMillis = maxOf(state.accountNotificationFloorMillis, authCreationMs)
                    )
                }
            }
            cancelAllFirestoreListeners()
            startAuthenticatedListeners(uid, email.trim(), displayName, onNewNotificationDoc)
            if (isAdminEmail) {
                attachAdminListeners()
            }
        } else if (isAdminEmail) {
            attachAdminListeners()
        }

        // If the customer clicked Place Order while in Guest mode and just completed Google Sign-In,
        // automatically submit their pending order now!
        if (_uiState.value.pendingOrderAfterSignIn && _uiState.value.cartItems.isNotEmpty()) {
            _uiState.update { it.copy(pendingOrderAfterSignIn = false) }
            submitCustomerOrder(onRequireGoogleSignIn = {})
        }
    }

    fun onUserSignedOut() {
        observedUid = null
        notifiedIds.clear()
        cancelAllFirestoreListeners()
        _uiState.update { state ->
            state.copy(
                isSignedIn = false,
                currentUserId = "",
                currentUserEmail = "",
                currentUserName = "",
                userProfile = null,
                hasAdminPanelAccess = false,
                activeRole = AppRole.CUSTOMER,
                checkoutName = "",
                checkoutPhone = "",
                checkoutAddress = "",
                checkoutLandmark = "",
                checkoutAddressLabel = AddressLabel.HOME,
                savedAddresses = emptyList(),
                checkoutLat = HajipurLocations.KITCHEN_LAT,
                checkoutLng = HajipurLocations.KITCHEN_LNG,
                customerOrders = emptyList(),
                isOrderHistoryLoading = false,
                isOrderHistoryRefreshing = false,
                orderHistoryError = null,
                orderHistoryFilter = OrderHistoryFilter.ALL,
                orderHistorySearchQuery = "",
                assignedDeliveryOrders = emptyList(),
                allAdminOrders = emptyList(),
                unacknowledgedAdminOrders = emptyList(),
                notifications = emptyList(),
                locallyReadNotificationIds = emptySet(),
                accountNotificationFloorMillis = 0L,
                isLiveGpsStreaming = false
            )
        }
    }

    private fun cancelAllFirestoreListeners() {
        listenerJobs.forEach { it.cancel() }
        listenerJobs.clear()
        adminListenerJobs.forEach { it.cancel() }
        adminListenerJobs.clear()
    }

    private fun upsertSavedAddressList(
        existing: List<SavedDeliveryAddress>,
        entry: SavedDeliveryAddress
    ): List<SavedDeliveryAddress> {
        if (entry.address.isBlank()) return existing
        val withoutLabel = existing.filterNot { it.label == entry.label }
        return (listOf(entry) + withoutLabel).sortedBy { it.label.ordinal }
    }

    private fun isValidGeoCoord(lat: Double, lng: Double): Boolean {
        return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
    }

    private fun isLegacyPlaceholderAddress(raw: String): Boolean {
        val trimmed = raw.trim()
        return trimmed.equals("Station Road, Hajipur, Bihar 844101", ignoreCase = true) ||
            trimmed.equals("Station Road, Near Dakbunglow Chowk, Hajipur 844101", ignoreCase = true)
    }

    fun loadSavedAddressesForUser(addresses: List<SavedDeliveryAddress>) {
        if (addresses.isEmpty()) return
        _uiState.update { state ->
            val merged = addresses.fold(state.savedAddresses) { acc, item ->
                upsertSavedAddressList(acc, item)
            }
            val primary = merged.firstOrNull { it.label == state.checkoutAddressLabel } ?: merged.first()
            if (state.checkoutAddress.isBlank()) {
                state.copy(
                    savedAddresses = merged,
                    checkoutAddressLabel = primary.label,
                    checkoutName = state.checkoutName.ifBlank { primary.name },
                    checkoutPhone = state.checkoutPhone.ifBlank { primary.phone },
                    checkoutAddress = primary.address,
                    checkoutLandmark = state.checkoutLandmark.ifBlank { primary.landmark },
                    checkoutLat = if (isValidGeoCoord(primary.lat, primary.lng)) primary.lat else state.checkoutLat,
                    checkoutLng = if (isValidGeoCoord(primary.lat, primary.lng)) primary.lng else state.checkoutLng
                )
            } else {
                state.copy(savedAddresses = merged)
            }
        }
    }

    fun loadNotificationReadState(readIds: Set<String>, accountFloorMillis: Long) {
        _uiState.update { state ->
            val mergedReadIds = state.locallyReadNotificationIds + readIds
            val effectiveFloor = maxOf(state.accountNotificationFloorMillis, accountFloorMillis)
            val filteredNotifs = state.notifications
                .filter { notif ->
                    NotificationReadStatePrefs.isNotificationEligibleForAccount(
                        notifCreatedMillis = notif.createdAt?.toDate()?.time,
                        accountFloorMillis = effectiveFloor
                    )
                }
                .map { notif ->
                    if (notif.id in mergedReadIds) notif.copy(isRead = true) else notif
                }
            state.copy(
                locallyReadNotificationIds = mergedReadIds,
                accountNotificationFloorMillis = effectiveFloor,
                notifications = filteredNotifs
            )
        }
    }

    private fun startAuthenticatedListeners(
        uid: String,
        email: String,
        displayName: String,
        onNewNotificationDoc: (String, String, String) -> Unit
    ) {
        viewModelScope.launch {
            val currentEncodedAddr = SavedAddressCodec.encodeForProfile(
                label = _uiState.value.checkoutAddressLabel,
                address = _uiState.value.checkoutAddress,
                landmark = _uiState.value.checkoutLandmark
            )
            val profileResult = userRepo.ensureUserProfile(
                displayName = displayName,
                email = email,
                phone = _uiState.value.checkoutPhone,
                defaultAddress = currentEncodedAddr
            )
            profileResult.onSuccess { profile ->
                val isAdminUser = userRepo.isBootstrappedAdminEmail(email)
                val decoded = SavedAddressCodec.decodeFromProfile(profile.defaultAddress)
                val hasValidSavedAddr = decoded.address.isNotBlank() && !isLegacyPlaceholderAddress(decoded.address)
                val profileCreatedMs = profile.createdAt?.toDate()?.time ?: 0L
                val authCreatedMs = runCatching {
                    Firebase.auth.currentUser?.metadata?.creationTimestamp ?: 0L
                }.getOrDefault(0L)
                val computedFloorMs = maxOf(profileCreatedMs, authCreatedMs)

                _uiState.update { state ->
                    val mergedDeposit = maxOf(state.depositBalanceInr, profile.depositBalanceInr)
                    val effectiveFloor = maxOf(state.accountNotificationFloorMillis, computedFloorMs)
                    val restoredSavedList = if (hasValidSavedAddr) {
                        upsertSavedAddressList(
                            state.savedAddresses,
                            SavedDeliveryAddress(
                                label = decoded.label,
                                name = profile.displayName.ifBlank { state.checkoutName },
                                phone = profile.phone.ifBlank { state.checkoutPhone },
                                address = decoded.address,
                                landmark = decoded.landmark,
                                lat = if (isValidGeoCoord(profile.defaultLat, profile.defaultLng)) profile.defaultLat else state.checkoutLat,
                                lng = if (isValidGeoCoord(profile.defaultLat, profile.defaultLng)) profile.defaultLng else state.checkoutLng
                            )
                        )
                    } else {
                        state.savedAddresses
                    }
                    val shouldAdoptProfileAddr = state.checkoutAddress.isBlank() && hasValidSavedAddr
                    val filteredNotifs = state.notifications.filter { notif ->
                        NotificationReadStatePrefs.isNotificationEligibleForAccount(
                            notifCreatedMillis = notif.createdAt?.toDate()?.time,
                            accountFloorMillis = effectiveFloor
                        )
                    }
                    state.copy(
                        userProfile = profile.copy(
                            role = if (isAdminUser) AppRole.ADMIN.name else AppRole.CUSTOMER.name,
                            depositBalanceInr = mergedDeposit
                        ),
                        hasAdminPanelAccess = isAdminUser,
                        depositBalanceInr = mergedDeposit,
                        bonusBalanceInr = profile.bonusBalanceInr,
                        accountNotificationFloorMillis = effectiveFloor,
                        notifications = filteredNotifs,
                        savedAddresses = restoredSavedList,
                        checkoutName = state.checkoutName.ifBlank { profile.displayName },
                        checkoutPhone = state.checkoutPhone.ifBlank { profile.phone },
                        checkoutAddressLabel = if (shouldAdoptProfileAddr) decoded.label else state.checkoutAddressLabel,
                        checkoutAddress = if (shouldAdoptProfileAddr) decoded.address else state.checkoutAddress,
                        checkoutLandmark = if (shouldAdoptProfileAddr) {
                            decoded.landmark.ifBlank { state.checkoutLandmark }
                        } else {
                            state.checkoutLandmark
                        },
                        checkoutLat = if (shouldAdoptProfileAddr && isValidGeoCoord(profile.defaultLat, profile.defaultLng)) {
                            profile.defaultLat
                        } else {
                            state.checkoutLat
                        },
                        checkoutLng = if (shouldAdoptProfileAddr && isValidGeoCoord(profile.defaultLat, profile.defaultLng)) {
                            profile.defaultLng
                        } else {
                            state.checkoutLng
                        }
                    )
                }
                if (isAdminUser) {
                    storeRepo.seedInitialHajipurCatalogIfAdmin()
                    attachAdminListeners()
                }
            }
        }

        // 0. Real-time User Profile (Deposit, Bonus Wallet Balances & Saved Address) listener
        listenerJobs += viewModelScope.launch {
            userRepo.observeCurrentUserProfile()
                .catch { /* keep local profile */ }
                .collect { profile ->
                    if (profile != null) {
                        val decoded = SavedAddressCodec.decodeFromProfile(profile.defaultAddress)
                        val hasValidSavedAddr = decoded.address.isNotBlank() && !isLegacyPlaceholderAddress(decoded.address)
                        val profileCreatedMs = profile.createdAt?.toDate()?.time ?: 0L
                        _uiState.update { state ->
                            val effectiveFloor = maxOf(state.accountNotificationFloorMillis, profileCreatedMs)
                            val updatedSaved = if (hasValidSavedAddr) {
                                upsertSavedAddressList(
                                    state.savedAddresses,
                                    SavedDeliveryAddress(
                                        label = decoded.label,
                                        name = profile.displayName.ifBlank { state.checkoutName },
                                        phone = profile.phone.ifBlank { state.checkoutPhone },
                                        address = decoded.address,
                                        landmark = decoded.landmark,
                                        lat = if (isValidGeoCoord(profile.defaultLat, profile.defaultLng)) profile.defaultLat else state.checkoutLat,
                                        lng = if (isValidGeoCoord(profile.defaultLat, profile.defaultLng)) profile.defaultLng else state.checkoutLng
                                    )
                                )
                            } else {
                                state.savedAddresses
                            }
                            val shouldAdoptProfileAddr = state.checkoutAddress.isBlank() && hasValidSavedAddr
                            state.copy(
                                userProfile = profile,
                                depositBalanceInr = profile.depositBalanceInr,
                                bonusBalanceInr = profile.bonusBalanceInr,
                                accountNotificationFloorMillis = effectiveFloor,
                                savedAddresses = updatedSaved,
                                checkoutName = state.checkoutName.ifBlank { profile.displayName },
                                checkoutPhone = state.checkoutPhone.ifBlank { profile.phone },
                                checkoutAddressLabel = if (shouldAdoptProfileAddr) decoded.label else state.checkoutAddressLabel,
                                checkoutAddress = if (shouldAdoptProfileAddr) decoded.address else state.checkoutAddress,
                                checkoutLandmark = if (shouldAdoptProfileAddr) {
                                    decoded.landmark.ifBlank { state.checkoutLandmark }
                                } else {
                                    state.checkoutLandmark
                                },
                                checkoutLat = if (shouldAdoptProfileAddr && isValidGeoCoord(profile.defaultLat, profile.defaultLng)) {
                                    profile.defaultLat
                                } else {
                                    state.checkoutLat
                                },
                                checkoutLng = if (shouldAdoptProfileAddr && isValidGeoCoord(profile.defaultLat, profile.defaultLng)) {
                                    profile.defaultLng
                                } else {
                                    state.checkoutLng
                                }
                            )
                        }
                    }
                }
        }

        // 1. Store Config listener
        listenerJobs += viewModelScope.launch {
            storeRepo.observeStoreConfig()
                .catch { /* keep fallback store config */ }
                .collect { config ->
                    _uiState.update { it.copy(storeConfig = config) }
                }
        }

        // 2. Menu Items listener
        listenerJobs += viewModelScope.launch {
            storeRepo.observeMenuItems()
                .catch { /* keep fallback Hajipur menu */ }
                .collect { items ->
                    _uiState.update { it.copy(menuItems = items) }
                }
        }

        // 3. Active Coupons listener
        listenerJobs += viewModelScope.launch {
            storeRepo.observeActiveCoupons()
                .catch { /* keep fallback coupons */ }
                .collect { coupons ->
                    _uiState.update { it.copy(coupons = coupons) }
                }
        }

        // 4. Customer's Own Orders & Order History real-time Firestore listener
        _uiState.update {
            it.copy(
                isOrderHistoryLoading = it.customerOrders.isEmpty(),
                orderHistoryError = null
            )
        }
        listenerJobs += viewModelScope.launch {
            orderRepo.observeCustomerOrders()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    _uiState.update { state ->
                        state.copy(
                            isOrderHistoryLoading = false,
                            isOrderHistoryRefreshing = false,
                            orderHistoryError = rootError.localizedMessage
                                ?: "Unable to fetch your order history right now. Tap Retry to try again."
                        )
                    }
                }
                .collect { orders ->
                    _uiState.update { state ->
                        val mergedOrders = applyLocalOrderOverrides(orders, state)
                        val activeId = state.trackedOrderId.takeIf { id -> mergedOrders.any { it.id == id } }
                            ?: mergedOrders.firstOrNull()?.id.orEmpty()
                        state.copy(
                            customerOrders = mergedOrders,
                            isOrderHistoryLoading = false,
                            isOrderHistoryRefreshing = false,
                            orderHistoryError = null,
                            trackedOrderId = activeId
                        )
                    }
                }
        }

        // 5. Delivery Partner Assigned Orders listener
        listenerJobs += viewModelScope.launch {
            orderRepo.observeAssignedDeliveryOrders()
                .catch { /* handled in repo */ }
                .collect { assigned ->
                    _uiState.update { it.copy(assignedDeliveryOrders = assigned) }
                }
        }

        // 6. Customer Real-Time Notifications listener (filters out pre-signup notifications for new accounts & respects locally read IDs)
        listenerJobs += viewModelScope.launch {
            var isInitialNotifSnapshot = true
            orderRepo.observeNotificationsForUser()
                .catch { /* handled in repo */ }
                .collect { notifs ->
                    val currentState = _uiState.value
                    val recipientKey = currentState.currentUserId.ifBlank { "customer" }
                    val floorMillis = currentState.accountNotificationFloorMillis
                    val localReadIds = currentState.locallyReadNotificationIds

                    val eligibleForAccount = notifs.filter { notif ->
                        NotificationReadStatePrefs.isNotificationEligibleForAccount(
                            notifCreatedMillis = notif.createdAt?.toDate()?.time,
                            accountFloorMillis = floorMillis
                        )
                    }

                    val seenMessageCounts = mutableMapOf<String, Int>()
                    val cappedNotifs = eligibleForAccount.filter { notif ->
                        val msgKey = NotificationHelper.buildMessageCapKey(recipientKey, notif.title, notif.message)
                        val count = seenMessageCounts.getOrDefault(msgKey, 0)
                        if (count < NotificationHelper.MAX_SAME_MESSAGE_PER_CUSTOMER) {
                            seenMessageCounts[msgKey] = count + 1
                            true
                        } else {
                            false
                        }
                    }.map { notif ->
                        if (notif.id in localReadIds) notif.copy(isRead = true) else notif
                    }

                    // Do not re-spam historical notifications on app launch / listener reconnect
                    if (isInitialNotifSnapshot) {
                        isInitialNotifSnapshot = false
                        cappedNotifs.forEach { notifiedIds += it.id }
                    } else if (_uiState.value.activeRole != AppRole.ADMIN) {
                        cappedNotifs.firstOrNull { !it.isRead && it.id !in notifiedIds && it.id !in localReadIds }?.let { newest ->
                            notifiedIds += newest.id
                            val msgKey = NotificationHelper.buildMessageCapKey(recipientKey, newest.title, newest.message)
                            val deliveredCount = customerMessageDeliveryCounts.getOrDefault(msgKey, 0)
                            if (deliveredCount < NotificationHelper.MAX_SAME_MESSAGE_PER_CUSTOMER) {
                                customerMessageDeliveryCounts[msgKey] = deliveredCount + 1
                                onNewNotificationDoc(newest.id, newest.title, newest.message)
                            }
                        }
                    } else {
                        // Admin is viewing Admin Panel: mark incoming customer notifications seen so they never echo
                        cappedNotifs.forEach { notifiedIds += it.id }
                    }
                    _uiState.update { state ->
                        val mergedRead = state.locallyReadNotificationIds
                        state.copy(
                            notifications = cappedNotifs.map { notif ->
                                if (notif.id in mergedRead) notif.copy(isRead = true) else notif
                            }
                        )
                    }
                }
        }
    }

    private fun applyLocalOrderOverrides(orders: List<PizzaOrder>, state: MainAppUiState): List<PizzaOrder> {
        return orders.map { ord ->
            when {
                ord.id in state.locallyAcceptedOrderIds && ord.orderStatus == OrderStatus.CONFIRMED ->
                    ord.copy(status = OrderStatus.PREPARING.code, adminAlertAcknowledged = true)
                ord.id in state.locallyDeclinedOrderIds && ord.orderStatus == OrderStatus.CONFIRMED ->
                    ord.copy(status = OrderStatus.CANCELLED.code, adminAlertAcknowledged = true)
                ord.id in state.locallyDismissedAlertOrderIds || AdminOrderAlertPlayer.isOrderSilenced(ord.id) ->
                    ord.copy(adminAlertAcknowledged = true)
                else -> ord
            }
        }
    }

    private val oldOrdersCleanupSubmittedIds = mutableSetOf<String>()
    private val oldCustomersCleanupSubmittedIds = mutableSetOf<String>()

    private fun isPreCleanupLegacyOrder(order: PizzaOrder): Boolean {
        val createdMillis = (order.createdAt ?: order.updatedAt)?.toDate()?.time ?: return true
        return createdMillis < OLD_ORDERS_CLEANUP_CUTOFF_MILLIS
    }

    private fun isPreCleanupLegacyCustomer(user: UserProfile): Boolean {
        if (userRepo.isAdminProfile(user)) return false
        val createdMillis = (user.createdAt ?: user.updatedAt)?.toDate()?.time ?: return true
        return createdMillis < OLD_CUSTOMERS_CLEANUP_CUTOFF_MILLIS
    }

    private fun attachAdminListeners() {
        // Idempotent guard: never stack duplicate Admin Firestore listeners on role switch or auth refresh
        if (adminListenerJobs.any { it.isActive }) return
        adminListenerJobs.clear()

        adminListenerJobs += viewModelScope.launch {
            orderRepo.observeAllOrdersForAdmin()
                .catch { /* non-admin or offline */ }
                .collect { allOrders ->
                    // Permanently remove all pre-existing legacy orders created before the cleanup cutoff
                    val legacyOldOrderIds = allOrders
                        .filter { isPreCleanupLegacyOrder(it) && it.id.isNotBlank() }
                        .map { it.id }
                    val pendingDeleteIds = legacyOldOrderIds.filter { it !in oldOrdersCleanupSubmittedIds }
                    if (pendingDeleteIds.isNotEmpty()) {
                        oldOrdersCleanupSubmittedIds.addAll(pendingDeleteIds)
                        pendingDeleteIds.forEach { AdminOrderAlertPlayer.silenceOrderAndStopRinging(it) }
                        viewModelScope.launch {
                            orderRepo.deleteOrdersByAdmin(pendingDeleteIds)
                        }
                    }

                    val legacyIdSet = legacyOldOrderIds.toSet()
                    _uiState.update { state ->
                        val activeVisibleOrders = allOrders.filterNot {
                            it.id in legacyIdSet || it.id in state.locallyDeletedOrderIds
                        }
                        val mergedAll = applyLocalOrderOverrides(activeVisibleOrders, state)
                        val updatedAccepted = state.lastAcceptedAdminOrder
                            ?.takeUnless { it.id in legacyIdSet || it.id in state.locallyDeletedOrderIds }
                            ?.let { currentAccepted ->
                                mergedAll.firstOrNull { it.id == currentAccepted.id } ?: currentAccepted
                            }
                        val updatedCustomerOrders = state.customerOrders.filterNot {
                            it.id in legacyIdSet || it.id in state.locallyDeletedOrderIds
                        }
                        state.copy(
                            allAdminOrders = mergedAll,
                            customerOrders = updatedCustomerOrders,
                            lastAcceptedAdminOrder = updatedAccepted,
                            unacknowledgedAdminOrders = mergedAll.filter {
                                it.orderStatus == OrderStatus.CONFIRMED &&
                                    !it.adminAlertAcknowledged &&
                                    it.id !in state.locallyDismissedAlertOrderIds &&
                                    !AdminOrderAlertPlayer.isOrderSilenced(it.id)
                            }
                        )
                    }
                }
        }

        adminListenerJobs += viewModelScope.launch {
            userRepo.observeDeliveryPartnersForUser(isAdminUser = true)
                .catch { /* keep default riders */ }
                .collect { partners ->
                    if (partners.isNotEmpty()) {
                        _uiState.update { it.copy(deliveryPartners = partners) }
                    }
                }
        }

        adminListenerJobs += viewModelScope.launch {
            userRepo.observeAllUsersForAdmin()
                .catch { /* ignore */ }
                .collect { users ->
                    val currentUid = _uiState.value.currentUserId
                    // Permanently remove all pre-existing old non-admin customer records created before cutoff.
                    // Never touch Admin Panel accounts!
                    val legacyOldCustomerIds = users
                        .filter { user ->
                            val docId = user.id.ifBlank { user.userId }
                            docId.isNotBlank() &&
                                docId != currentUid &&
                                user.userId != currentUid &&
                                !userRepo.isAdminProfile(user) &&
                                isPreCleanupLegacyCustomer(user)
                        }
                        .map { it.id.ifBlank { it.userId } }

                    val pendingCustomerDeleteIds = legacyOldCustomerIds.filter {
                        it !in oldCustomersCleanupSubmittedIds
                    }
                    if (pendingCustomerDeleteIds.isNotEmpty()) {
                        oldCustomersCleanupSubmittedIds.addAll(pendingCustomerDeleteIds)
                        viewModelScope.launch {
                            userRepo.deleteUsersByAdmin(pendingCustomerDeleteIds)
                        }
                    }

                    val legacyCustomerIdSet = legacyOldCustomerIds.toSet()
                    val visibleCustomersOnly = users.filter { user ->
                        val docId = user.id.ifBlank { user.userId }
                        !userRepo.isAdminProfile(user) &&
                            docId != currentUid &&
                            user.userId != currentUid &&
                            docId !in legacyCustomerIdSet
                    }
                    _uiState.update { it.copy(allUsersForAdmin = visibleCustomersOnly) }
                }
        }

        // Automatically advance active orders by 1 step every 5 minutes up to ON_THE_WAY ("Reaching Soon").
        // Final DELIVERED step is strictly manual-only by Admin.
        adminListenerJobs += viewModelScope.launch {
            val inFlightAutoStep = mutableMapOf<String, String>()
            while (isActive) {
                delay(30_000L)
                val now = System.currentTimeMillis()
                val orders = _uiState.value.allAdminOrders
                for (order in orders) {
                    if (order.isTerminal) {
                        inFlightAutoStep.remove(order.id)
                        continue
                    }
                    val nextStep = order.nextAutoProgressStatus(now) ?: continue
                    if (nextStep == OrderStatus.DELIVERED) continue
                    val targetCode = nextStep.code
                    if (inFlightAutoStep[order.id] == targetCode) continue
                    inFlightAutoStep[order.id] = targetCode
                    val res = orderRepo.updateOrderStatusByAdmin(order, nextStep)
                    if (res.isFailure) {
                        inFlightAutoStep.remove(order.id)
                    }
                }
            }
        }
    }

    // Navigation, Online State & Role Switching
    fun setOnlineConnectionState(isOnline: Boolean) {
        _uiState.update { it.copy(isOnlineConnected = isOnline) }
    }

    fun selectCustomerTab(tab: CustomerTab) {
        _uiState.update { it.copy(selectedCustomerTab = tab, activeRole = AppRole.CUSTOMER) }
    }

    fun switchRole(role: AppRole) {
        if (role == AppRole.ADMIN && !_uiState.value.isAdminUser) {
            _uiState.update {
                it.copy(
                    activeRole = AppRole.CUSTOMER,
                    errorBannerMessage = "Admin access is restricted to authorized Admin accounts."
                )
            }
            return
        }
        _uiState.update { it.copy(activeRole = role, errorBannerMessage = null) }
        if (role == AppRole.ADMIN && _uiState.value.isSignedIn) {
            attachAdminListeners()
        }
    }

    // Wallet & Rewards Actions (Customer Top-Up + Checkout Toggles + Admin Customer Credit)
    fun toggleUseWalletBalance(enabled: Boolean) {
        _uiState.update {
            it.copy(
                useDepositBalanceInCheckout = enabled,
                useBonusBalanceInCheckout = enabled
            )
        }
    }

    fun toggleUseDepositBalance(enabled: Boolean) {
        _uiState.update { it.copy(useDepositBalanceInCheckout = enabled) }
    }

    fun toggleUseBonusBalance(enabled: Boolean) {
        _uiState.update { it.copy(useBonusBalanceInCheckout = enabled) }
    }

    fun addMoneyToDepositWallet(amountInr: Int, paymentMethod: String = "UPI") {
        val cleanAmount = amountInr.coerceIn(1, 100000)
        _uiState.update { state ->
            val nextDeposit = (state.depositBalanceInr + cleanAmount).coerceAtMost(1000000)
            val updatedProfile = state.userProfile?.copy(depositBalanceInr = nextDeposit)
            state.copy(
                depositBalanceInr = nextDeposit,
                userProfile = updatedProfile,
                useDepositBalanceInCheckout = true,
                statusBannerMessage = "Payment Successful ($paymentMethod)! ₹$cleanAmount added to your Deposit Wallet."
            )
        }
        if (_uiState.value.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                userRepo.addCustomerDepositBalance(cleanAmount)
            }
        }
    }

    fun adminAddAmountToCustomerWallet(
        targetUserId: String,
        amountInr: Int,
        isBonus: Boolean,
        customerNameHint: String = ""
    ) {
        val cleanId = targetUserId.trim().ifBlank { _uiState.value.currentUserId.ifBlank { "guest_customer" } }
        val cleanAmount = amountInr.coerceIn(1, 500000)
        val balanceTypeLabel = if (isBonus) "Bonus Balance" else "Deposit Balance"

        _uiState.update { state ->
            val isSelf = cleanId == state.currentUserId || state.currentUserId.isBlank()
            val nextSelfDeposit = if (isSelf && !isBonus) state.depositBalanceInr + cleanAmount else state.depositBalanceInr
            val nextSelfBonus = if (isSelf && isBonus) state.bonusBalanceInr + cleanAmount else state.bonusBalanceInr

            val existingUser = state.allUsersForAdmin.find {
                it.userId.equals(cleanId, ignoreCase = true) || it.id.equals(cleanId, ignoreCase = true)
            }
            val updatedUsers = if (existingUser != null) {
                state.allUsersForAdmin.map { u ->
                    if (u.userId.equals(cleanId, ignoreCase = true) || u.id.equals(cleanId, ignoreCase = true)) {
                        u.copy(
                            depositBalanceInr = if (isBonus) u.depositBalanceInr else u.depositBalanceInr + cleanAmount,
                            bonusBalanceInr = if (isBonus) u.bonusBalanceInr + cleanAmount else u.bonusBalanceInr
                        )
                    } else {
                        u
                    }
                }
            } else {
                listOf(
                    UserProfile(
                        id = cleanId,
                        userId = cleanId,
                        displayName = customerNameHint.ifBlank { "Customer ($cleanId)" },
                        depositBalanceInr = if (isBonus) 0 else cleanAmount,
                        bonusBalanceInr = if (isBonus) cleanAmount else 0
                    )
                ) + state.allUsersForAdmin
            }

            val localWalletEntry = if (isSelf && isBonus) {
                AppNotification(
                    id = "local_wallet_credit_${System.currentTimeMillis()}",
                    recipientUid = cleanId,
                    title = "₹$cleanAmount Bonus Credited",
                    message = "My Crush Pizza – Hajipur Admin added ₹$cleanAmount to your Bonus Wallet.",
                    type = "WALLET_CREDIT",
                    isRead = false,
                    createdAt = Timestamp.now()
                )
            } else {
                null
            }

            state.copy(
                depositBalanceInr = nextSelfDeposit,
                bonusBalanceInr = nextSelfBonus,
                allUsersForAdmin = updatedUsers,
                notifications = if (localWalletEntry != null) {
                    listOf(localWalletEntry) + state.notifications
                } else {
                    state.notifications
                },
                statusBannerMessage = "Added ₹$cleanAmount to $balanceTypeLabel for ${customerNameHint.ifBlank { existingUser?.displayName?.ifBlank { "Customer" } ?: "Customer" }}"
            )
        }

        if (_uiState.value.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                userRepo.adminCreditCustomerWallet(
                    targetUserId = cleanId,
                    amountInr = cleanAmount,
                    isBonus = isBonus,
                    customerNameHint = customerNameHint
                )
                orderRepo.sendNotification(
                    recipientUid = cleanId,
                    title = "₹$cleanAmount Credited to Your $balanceTypeLabel!",
                    message = "My Crush Pizza – Hajipur Admin added ₹$cleanAmount to your $balanceTypeLabel. Use it on your next order!",
                    type = "WALLET_CREDIT"
                )
            }
        }
    }

    fun adminDeductAmountFromCustomerWallet(
        targetUserId: String,
        amountInr: Int,
        isBonus: Boolean,
        customerNameHint: String = ""
    ) {
        val cleanId = targetUserId.trim().ifBlank { _uiState.value.currentUserId.ifBlank { "guest_customer" } }
        val cleanAmount = amountInr.coerceIn(1, 500000)
        val balanceTypeLabel = if (isBonus) "Bonus Balance" else "Deposit Balance"

        _uiState.update { state ->
            val isSelf = cleanId == state.currentUserId || state.currentUserId.isBlank()
            val nextSelfDeposit = if (isSelf && !isBonus) {
                (state.depositBalanceInr - cleanAmount).coerceAtLeast(0)
            } else {
                state.depositBalanceInr
            }
            val nextSelfBonus = if (isSelf && isBonus) {
                (state.bonusBalanceInr - cleanAmount).coerceAtLeast(0)
            } else {
                state.bonusBalanceInr
            }

            val existingUser = state.allUsersForAdmin.find {
                it.userId.equals(cleanId, ignoreCase = true) || it.id.equals(cleanId, ignoreCase = true)
            }
            val updatedUsers = if (existingUser != null) {
                state.allUsersForAdmin.map { u ->
                    if (u.userId.equals(cleanId, ignoreCase = true) || u.id.equals(cleanId, ignoreCase = true)) {
                        u.copy(
                            depositBalanceInr = if (isBonus) {
                                u.depositBalanceInr
                            } else {
                                (u.depositBalanceInr - cleanAmount).coerceAtLeast(0)
                            },
                            bonusBalanceInr = if (isBonus) {
                                (u.bonusBalanceInr - cleanAmount).coerceAtLeast(0)
                            } else {
                                u.bonusBalanceInr
                            }
                        )
                    } else {
                        u
                    }
                }
            } else {
                state.allUsersForAdmin
            }

            val localWalletDebitEntry = if (isSelf && isBonus) {
                AppNotification(
                    id = "local_wallet_debit_${System.currentTimeMillis()}",
                    recipientUid = cleanId,
                    title = "₹$cleanAmount Bonus Deducted",
                    message = "My Crush Pizza – Hajipur Admin deducted ₹$cleanAmount from your Bonus Wallet.",
                    type = "WALLET_DEBIT",
                    isRead = false,
                    createdAt = Timestamp.now()
                )
            } else {
                null
            }

            state.copy(
                depositBalanceInr = nextSelfDeposit,
                bonusBalanceInr = nextSelfBonus,
                allUsersForAdmin = updatedUsers,
                notifications = if (localWalletDebitEntry != null) {
                    listOf(localWalletDebitEntry) + state.notifications
                } else {
                    state.notifications
                },
                statusBannerMessage = "Cut ₹$cleanAmount from $balanceTypeLabel for ${customerNameHint.ifBlank { existingUser?.displayName?.ifBlank { "Customer" } ?: "Customer" }}"
            )
        }

        if (_uiState.value.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                userRepo.adminDeductCustomerWallet(
                    targetUserId = cleanId,
                    amountInr = cleanAmount,
                    isBonus = isBonus,
                    customerNameHint = customerNameHint
                )
                orderRepo.sendNotification(
                    recipientUid = cleanId,
                    title = "₹$cleanAmount Deducted from Your $balanceTypeLabel",
                    message = "My Crush Pizza – Hajipur Admin deducted ₹$cleanAmount from your $balanceTypeLabel.",
                    type = "WALLET_DEBIT"
                )
            }
        }
    }

    // Menu Filtering & Cart Actions
    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadRecentSearches(searches: List<String>) {
        _uiState.update { it.copy(recentSearches = searches.take(RecentSearchPrefs.MAX_RECENT_SEARCHES)) }
    }

    fun addRecentSearch(query: String): List<String> {
        val merged = RecentSearchPrefs.mergeQuery(_uiState.value.recentSearches, query)
        if (merged != _uiState.value.recentSearches) {
            _uiState.update { it.copy(recentSearches = merged) }
        }
        return merged
    }

    fun removeRecentSearch(query: String): List<String> {
        val updated = _uiState.value.recentSearches.filterNot { it.equals(query, ignoreCase = true) }
        _uiState.update { it.copy(recentSearches = updated) }
        return updated
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun toggleVegOnlyFilter() {
        _uiState.update { it.copy(vegOnlyFilter = !it.vegOnlyFilter) }
    }

    fun addToCart(menuItem: MenuItem, size: PizzaSize = PizzaSize.REGULAR) {
        _uiState.update { state ->
            val effectiveSize = if (menuItem.supportsMultipleSizes()) size else PizzaSize.REGULAR
            val targetKey = "${menuItem.id}_${effectiveSize.name}"
            val existing = state.cartItems.find { it.key == targetKey }
            val updatedCart = if (existing != null) {
                state.cartItems.map {
                    if (it.key == targetKey) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cartItems + CartItem(menuItem = menuItem, size = effectiveSize, quantity = 1)
            }
            state.copy(
                cartItems = updatedCart,
                statusBannerMessage = "Added ${menuItem.title} to Cart"
            )
        }
    }

    fun updateCartQuantity(cartKey: String, delta: Int) {
        _uiState.update { state ->
            val updated = state.cartItems.mapNotNull { item ->
                if (item.key == cartKey) {
                    val nextQty = item.quantity + delta
                    if (nextQty <= 0) null else item.copy(quantity = nextQty)
                } else {
                    item
                }
            }
            state.copy(cartItems = updated)
        }
    }

    fun applyCouponCode(rawCode: String) {
        val code = rawCode.trim().uppercase()
        val state = _uiState.value
        val match = state.coupons.firstOrNull { it.code.equals(code, ignoreCase = true) && it.isActive }
        if (match == null) {
            _uiState.update {
                it.copy(
                    appliedCoupon = null,
                    couponMessage = "Invalid or expired coupon code: $code"
                )
            }
            return
        }
        if (state.subtotalInr < match.minOrderInr) {
            _uiState.update {
                it.copy(
                    appliedCoupon = null,
                    couponMessage = "Add ₹${match.minOrderInr - state.subtotalInr} more to use ${match.code}"
                )
            }
            return
        }
        val discount = match.calculateDiscount(state.subtotalInr)
        _uiState.update {
            it.copy(
                appliedCoupon = match,
                couponMessage = "${match.code} applied! You saved ₹$discount"
            )
        }
    }

    fun removeAppliedCoupon() {
        _uiState.update { it.copy(appliedCoupon = null, couponMessage = null) }
    }

    // Checkout Customer Details & GPS Location
    fun updateCheckoutDetails(
        name: String? = null,
        phone: String? = null,
        address: String? = null,
        landmark: String? = null,
        paymentMethod: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                checkoutName = name ?: state.checkoutName,
                checkoutPhone = phone ?: state.checkoutPhone,
                checkoutAddress = address ?: state.checkoutAddress,
                checkoutLandmark = landmark ?: state.checkoutLandmark,
                selectedPaymentMethod = "COD"
            )
        }
    }

    fun selectCheckoutAddressLabel(label: AddressLabel) {
        _uiState.update { state ->
            state.copy(checkoutAddressLabel = label)
        }
    }

    fun applySavedAddress(saved: SavedDeliveryAddress) {
        _uiState.update { state ->
            val distKm = HajipurLocations.calculateDistanceKm(
                state.storeConfig.storeLat,
                state.storeConfig.storeLng,
                saved.lat,
                saved.lng
            )
            val withinRange = HajipurLocations.isWithinDeliveryRange(distKm)
            state.copy(
                checkoutAddressLabel = saved.label,
                checkoutName = saved.name.ifBlank { state.checkoutName },
                checkoutPhone = saved.phone.ifBlank { state.checkoutPhone },
                checkoutAddress = saved.address,
                checkoutLandmark = saved.landmark,
                checkoutLat = saved.lat,
                checkoutLng = saved.lng,
                errorBannerMessage = if (withinRange) null else "Delivery only within 10 km ($distKm km)",
                statusBannerMessage = if (withinRange) "Switched to ${saved.label.displayName} address" else null
            )
        }
        persistDeliveryAddressToAccount(
            label = saved.label,
            name = saved.name.ifBlank { _uiState.value.checkoutName },
            phone = saved.phone.ifBlank { _uiState.value.checkoutPhone },
            address = saved.address,
            landmark = saved.landmark,
            lat = saved.lat,
            lng = saved.lng
        )
    }

    private fun persistDeliveryAddressToAccount(
        label: AddressLabel,
        name: String,
        phone: String,
        address: String,
        landmark: String,
        lat: Double,
        lng: Double
    ) {
        val cleanAddr = address.trim()
        if (cleanAddr.isBlank()) return
        val cleanName = name.trim().ifBlank { _uiState.value.currentUserName.ifBlank { "Customer" } }
        val cleanPhone = phone.trim()
        val cleanLandmark = landmark.trim()
        val savedEntry = SavedDeliveryAddress(
            label = label,
            name = cleanName,
            phone = cleanPhone,
            address = cleanAddr,
            landmark = cleanLandmark,
            lat = lat,
            lng = lng
        )
        _uiState.update { state ->
            state.copy(
                savedAddresses = upsertSavedAddressList(state.savedAddresses, savedEntry)
            )
        }
        if (_uiState.value.isSignedIn && Firebase.auth.currentUser != null) {
            val encodedForProfile = SavedAddressCodec.encodeForProfile(
                label = label,
                address = cleanAddr,
                landmark = cleanLandmark
            )
            viewModelScope.launch {
                userRepo.updateCustomerAddressAndPhone(
                    displayName = cleanName,
                    phone = cleanPhone,
                    address = encodedForProfile,
                    lat = lat,
                    lng = lng
                )
            }
        }
    }

    fun selectHajipurLandmark(landmark: HajipurLandmark) {
        val currentLabel = _uiState.value.checkoutAddressLabel
        _uiState.update { state ->
            val distKm = HajipurLocations.calculateDistanceKm(
                state.storeConfig.storeLat,
                state.storeConfig.storeLng,
                landmark.lat,
                landmark.lng
            )
            val withinRange = HajipurLocations.isWithinDeliveryRange(distKm)
            state.copy(
                checkoutLandmark = landmark.name,
                checkoutAddress = landmark.addressLine,
                checkoutLat = landmark.lat,
                checkoutLng = landmark.lng,
                errorBannerMessage = if (withinRange) {
                    null
                } else {
                    "Delivery Not Available Above 10 km! (${landmark.name} is $distKm km from My Crush Pizza Hajipur)"
                },
                statusBannerMessage = if (withinRange) {
                    "Pinned ${currentLabel.displayName} location: ${landmark.name} ($distKm km)"
                } else {
                    null
                }
            )
        }
        persistDeliveryAddressToAccount(
            label = currentLabel,
            name = _uiState.value.checkoutName,
            phone = _uiState.value.checkoutPhone,
            address = landmark.addressLine,
            landmark = landmark.name,
            lat = landmark.lat,
            lng = landmark.lng
        )
    }

    fun updatePickedMapLocation(
        lat: Double,
        lng: Double,
        formattedAddress: String,
        landmarkName: String = "Live GPS Pin",
        addressLabel: AddressLabel = _uiState.value.checkoutAddressLabel
    ) {
        _uiState.update { state ->
            val distKm = HajipurLocations.calculateDistanceKm(
                state.storeConfig.storeLat,
                state.storeConfig.storeLng,
                lat,
                lng
            )
            val withinRange = HajipurLocations.isWithinDeliveryRange(distKm)
            state.copy(
                checkoutAddressLabel = addressLabel,
                checkoutLat = lat,
                checkoutLng = lng,
                checkoutAddress = formattedAddress,
                checkoutLandmark = landmarkName,
                errorBannerMessage = if (withinRange) {
                    null
                } else {
                    "Delivery Not Available Above 10 km! Selected location is $distKm km from My Crush Pizza Hajipur (Max 10 km)."
                },
                statusBannerMessage = if (withinRange) {
                    "${addressLabel.displayName} address saved ($distKm km from My Crush Pizza Hajipur)"
                } else {
                    null
                }
            )
        }
        persistDeliveryAddressToAccount(
            label = addressLabel,
            name = _uiState.value.checkoutName,
            phone = _uiState.value.checkoutPhone,
            address = formattedAddress,
            landmark = landmarkName,
            lat = lat,
            lng = lng
        )
    }

    fun saveCustomerFullDeliveryDetails(
        name: String,
        phone: String,
        lat: Double,
        lng: Double,
        formattedAddress: String,
        landmarkName: String,
        addressLabel: AddressLabel = _uiState.value.checkoutAddressLabel
    ) {
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanAddress = formattedAddress.trim()
        val cleanLandmark = landmarkName.trim()
        _uiState.update { state ->
            val distKm = HajipurLocations.calculateDistanceKm(
                state.storeConfig.storeLat,
                state.storeConfig.storeLng,
                lat,
                lng
            )
            val withinRange = HajipurLocations.isWithinDeliveryRange(distKm)
            state.copy(
                checkoutAddressLabel = addressLabel,
                checkoutName = cleanName,
                checkoutPhone = cleanPhone,
                checkoutLat = lat,
                checkoutLng = lng,
                checkoutAddress = cleanAddress,
                checkoutLandmark = cleanLandmark,
                errorBannerMessage = if (withinRange) {
                    null
                } else {
                    "Delivery Not Available Above 10 km! Selected location is $distKm km from My Crush Pizza Hajipur (Max 10 km)."
                },
                statusBannerMessage = if (withinRange) {
                    "${addressLabel.displayName} address saved to account ($distKm km)"
                } else {
                    null
                }
            )
        }
        persistDeliveryAddressToAccount(
            label = addressLabel,
            name = cleanName.ifBlank { _uiState.value.currentUserName },
            phone = cleanPhone,
            address = cleanAddress,
            landmark = cleanLandmark,
            lat = lat,
            lng = lng
        )
    }

    // Place Order (Requires Google Sign-In if currently in Guest mode!)
    fun submitCustomerOrder(onRequireGoogleSignIn: () -> Unit) {
        val state = _uiState.value
        if (!state.storeConfig.isOpen) {
            _uiState.update {
                it.copy(
                    errorBannerMessage = "Store is currently closed. You can browse & keep items in your cart, and place your order once My Crush Pizza Hajipur reopens."
                )
            }
            return
        }
        if (state.cartItems.isEmpty()) {
            _uiState.update { it.copy(errorBannerMessage = "Your cart is empty. Add a delicious pizza first!") }
            return
        }
        if (!state.isMinimumOrderMet) {
            _uiState.update {
                it.copy(
                    errorBannerMessage = "Minimum order amount is ₹${MIN_ORDER_AMOUNT_INR}. Please add ₹${state.minOrderShortfallInr} more to place your order."
                )
            }
            return
        }
        if (state.checkoutName.trim().length < 2) {
            _uiState.update { it.copy(errorBannerMessage = "Please enter your full name for delivery.") }
            return
        }
        val cleanPhone = state.checkoutPhone.filter { it.isDigit() }
        if (cleanPhone.length < 10) {
            _uiState.update { it.copy(errorBannerMessage = "Please enter a valid 10-digit contact mobile number.") }
            return
        }
        if (state.checkoutAddress.trim().length < 5) {
            _uiState.update { it.copy(errorBannerMessage = "Please enter or pin your Hajipur delivery location/address.") }
            return
        }
        if (state.checkoutLandmark.trim().length < 2) {
            _uiState.update { it.copy(errorBannerMessage = "Please enter your nearby landmark (e.g., Near RN College / Chowk).") }
            return
        }
        if (!state.isCheckoutWithin10KmRange) {
            _uiState.update {
                it.copy(
                    errorBannerMessage = "Delivery Not Available Above 10 km! Your location is ${state.checkoutDistanceKm} km from My Crush Pizza Hajipur. Orders are accepted only within 10 km range."
                )
            }
            return
        }

        // Gate order creation behind Google Sign-In
        if (!state.isSignedIn || Firebase.auth.currentUser == null) {
            _uiState.update {
                it.copy(
                    pendingOrderAfterSignIn = true,
                    statusBannerMessage = "Please Sign in with Google to confirm & place your order."
                )
            }
            onRequireGoogleSignIn()
            return
        }

        val itemsSummary = state.cartItems.joinToString(", ") { item ->
            if (item.menuItem.supportsMultipleSizes()) {
                "${item.quantity}x ${item.menuItem.title} (${item.size.label})"
            } else {
                "${item.quantity}x ${item.menuItem.title}"
            }
        }

        val depositToDeduct = state.depositUsedInr
        val bonusToDeduct = state.bonusUsedInr
        val effectivePaymentMethod = if (state.grandTotalInr == 0 && state.walletTotalUsedInr > 0) {
            "WALLET"
        } else {
            "COD"
        }

        _uiState.update { it.copy(isBusy = true, errorBannerMessage = null) }
        val encodedProfileAddress = SavedAddressCodec.encodeForProfile(
            label = state.checkoutAddressLabel,
            address = state.checkoutAddress.trim(),
            landmark = state.checkoutLandmark.trim()
        )
        val labeledOrderAddress = "[${state.checkoutAddressLabel.displayName}] ${state.checkoutAddress.trim()}"
        val savedEntry = SavedDeliveryAddress(
            label = state.checkoutAddressLabel,
            name = state.checkoutName.trim(),
            phone = cleanPhone.takeLast(10),
            address = state.checkoutAddress.trim(),
            landmark = state.checkoutLandmark.trim(),
            lat = state.checkoutLat,
            lng = state.checkoutLng
        )
        _uiState.update { current ->
            current.copy(savedAddresses = upsertSavedAddressList(current.savedAddresses, savedEntry))
        }
        viewModelScope.launch {
            userRepo.updateCustomerAddressAndPhone(
                displayName = state.checkoutName.trim(),
                phone = cleanPhone.takeLast(10),
                address = encodedProfileAddress,
                lat = state.checkoutLat,
                lng = state.checkoutLng
            )
            if (depositToDeduct > 0 || bonusToDeduct > 0) {
                userRepo.deductWalletBalancesForOrder(
                    depositUsedInr = depositToDeduct,
                    bonusUsedInr = bonusToDeduct
                )
            }
            val result = orderRepo.createOrder(
                customerName = state.checkoutName.trim(),
                customerPhone = cleanPhone.takeLast(10),
                deliveryAddress = labeledOrderAddress,
                landmark = state.checkoutLandmark,
                customerLat = state.checkoutLat,
                customerLng = state.checkoutLng,
                itemsSummary = itemsSummary,
                itemsCount = state.cartCount,
                subtotalInr = state.subtotalInr,
                discountInr = state.discountInr,
                couponCode = state.appliedCoupon?.code.orEmpty(),
                deliveryFeeInr = state.deliveryFeeInr,
                taxInr = state.taxInr,
                totalInr = state.grandTotalInr,
                paymentMethod = effectivePaymentMethod,
                depositUsedInr = depositToDeduct,
                bonusUsedInr = bonusToDeduct
            )
            result.onSuccess { createdOrder ->
                _uiState.update { current ->
                    current.copy(
                        isBusy = false,
                        cartItems = emptyList(),
                        appliedCoupon = null,
                        couponMessage = null,
                        depositBalanceInr = (current.depositBalanceInr - depositToDeduct).coerceAtLeast(0),
                        bonusBalanceInr = (current.bonusBalanceInr - bonusToDeduct).coerceAtLeast(0),
                        lastPlacedOrder = createdOrder,
                        trackedOrderId = createdOrder.id,
                        customerOrders = listOf(createdOrder) + current.customerOrders.filterNot { it.id == createdOrder.id },
                        allAdminOrders = listOf(createdOrder) + current.allAdminOrders.filterNot { it.id == createdOrder.id },
                        selectedCustomerTab = CustomerTab.MENU,
                        isCustomerOrderDetailsOpen = false,
                        statusBannerMessage = "Order ${createdOrder.orderNumber} Confirmed! Tap bottom order bar to view details."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorBannerMessage = err.localizedMessage ?: "Failed to place order. Please try again."
                    )
                }
            }
        }
    }

    fun setOrderHistoryFilter(filter: OrderHistoryFilter) {
        _uiState.update { it.copy(orderHistoryFilter = filter) }
    }

    fun setOrderHistorySearchQuery(query: String) {
        _uiState.update { it.copy(orderHistorySearchQuery = query) }
    }

    fun refreshOrderHistory() {
        val state = _uiState.value
        if (!state.isSignedIn || state.currentUserId.isBlank()) {
            _uiState.update {
                it.copy(
                    isOrderHistoryLoading = false,
                    isOrderHistoryRefreshing = false,
                    orderHistoryError = null
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOrderHistoryRefreshing = true,
                    orderHistoryError = null
                )
            }
            val result = orderRepo.fetchCustomerOrderHistoryOnce()
            result.onSuccess { orders ->
                _uiState.update { current ->
                    val mergedOrders = applyLocalOrderOverrides(orders, current)
                    val activeId = current.trackedOrderId.takeIf { id -> mergedOrders.any { it.id == id } }
                        ?: mergedOrders.firstOrNull()?.id.orEmpty()
                    current.copy(
                        customerOrders = mergedOrders,
                        trackedOrderId = activeId,
                        isOrderHistoryLoading = false,
                        isOrderHistoryRefreshing = false,
                        orderHistoryError = null
                    )
                }
            }.onFailure { err ->
                _uiState.update { current ->
                    current.copy(
                        isOrderHistoryLoading = false,
                        isOrderHistoryRefreshing = false,
                        orderHistoryError = err.localizedMessage
                            ?: "Could not refresh your order history. Please check your connection."
                    )
                }
            }
        }
    }

    fun cancelOwnConfirmedOrder(order: PizzaOrder) {
        // Orders can ONLY be cancelled by Admin; customer self-cancel is intentionally disabled.
        _uiState.update {
            it.copy(
                statusBannerMessage = "Only My Crush Pizza Hajipur Admin can cancel an order."
            )
        }
    }

    fun reorderPastOrder(order: PizzaOrder) {
        val state = _uiState.value
        val matchedItems = state.menuItems.filter { menu ->
            order.itemsSummary.contains(menu.title, ignoreCase = true)
        }.ifEmpty {
            listOfNotNull(state.menuItems.firstOrNull())
        }
        val newCart = matchedItems.map { item ->
            val size = when {
                item.supportsMultipleSizes() && order.itemsSummary.contains("Large", ignoreCase = true) -> PizzaSize.LARGE
                item.supportsMultipleSizes() && order.itemsSummary.contains("Medium", ignoreCase = true) -> PizzaSize.MEDIUM
                else -> PizzaSize.REGULAR
            }
            CartItem(menuItem = item, size = size, quantity = 1)
        }
        _uiState.update {
            it.copy(
                cartItems = newCart,
                selectedCustomerTab = CustomerTab.CART,
                isCustomerOrderDetailsOpen = false,
                statusBannerMessage = "Loaded items from ${order.orderNumber} into your cart!"
            )
        }
    }

    fun selectOrderForTracking(orderId: String) {
        _uiState.update {
            it.copy(
                trackedOrderId = orderId,
                isCustomerOrderDetailsOpen = true
            )
        }
    }

    fun openCustomerOrderDetails(orderId: String = "") {
        _uiState.update { state ->
            val resolvedId = orderId.ifBlank { state.activeTrackedOrder?.id.orEmpty() }
            state.copy(
                trackedOrderId = resolvedId,
                isCustomerOrderDetailsOpen = true
            )
        }
    }

    fun closeCustomerOrderDetails() {
        _uiState.update { it.copy(isCustomerOrderDetailsOpen = false) }
    }

    fun dismissAcceptedAdminOrderDetails() {
        _uiState.update { it.copy(lastAcceptedAdminOrder = null) }
    }

    fun openAdminOrderCustomerDetails(order: PizzaOrder) {
        _uiState.update { it.copy(lastAcceptedAdminOrder = order) }
    }

    // Admin Incoming Order Ringing Controls (Accept / Decline / Done)
    fun acceptIncomingOrderInAdmin(order: PizzaOrder) {
        val allRingingIds = (_uiState.value.ringingAdminOrders.map { it.id } + order.id).toSet()
        AdminOrderAlertPlayer.silenceOrdersAndStopRinging(allRingingIds)

        val acceptedOrder = order.copy(
            status = OrderStatus.PREPARING.code,
            adminAlertAcknowledged = true
        )

        _uiState.update { state ->
            state.copy(
                activeRole = if (state.isAdminUser) AppRole.ADMIN else state.activeRole,
                locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + allRingingIds,
                locallyAcceptedOrderIds = state.locallyAcceptedOrderIds + order.id,
                lastAcceptedAdminOrder = acceptedOrder,
                allAdminOrders = state.allAdminOrders.map {
                    when {
                        it.id == order.id -> acceptedOrder
                        it.id in allRingingIds -> it.copy(adminAlertAcknowledged = true)
                        else -> it
                    }
                }.let { list ->
                    if (list.any { it.id == order.id }) list else listOf(acceptedOrder) + list
                },
                customerOrders = state.customerOrders.map {
                    if (it.id == order.id) acceptedOrder else it
                },
                lastPlacedOrder = if (state.lastPlacedOrder?.id == order.id) acceptedOrder else state.lastPlacedOrder,
                unacknowledgedAdminOrders = emptyList(),
                statusBannerMessage = "Order ${order.orderNumber} ACCEPTED & Alarm Stopped! Showing Customer Details."
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.updateOrderStatusByAdmin(order, OrderStatus.PREPARING)
                allRingingIds.filter { it != order.id }.forEach { otherId ->
                    orderRepo.acknowledgeAdminOrderAlert(otherId)
                }
            }
        }
    }

    fun declineIncomingOrderInAdmin(order: PizzaOrder) {
        val allRingingIds = (_uiState.value.ringingAdminOrders.map { it.id } + order.id).toSet()
        AdminOrderAlertPlayer.silenceOrdersAndStopRinging(allRingingIds)

        val declinedOrder = order.copy(
            status = OrderStatus.CANCELLED.code,
            adminAlertAcknowledged = true
        )

        _uiState.update { state ->
            state.copy(
                locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + allRingingIds,
                locallyDeclinedOrderIds = state.locallyDeclinedOrderIds + order.id,
                allAdminOrders = state.allAdminOrders.map {
                    when {
                        it.id == order.id -> declinedOrder
                        it.id in allRingingIds -> it.copy(adminAlertAcknowledged = true)
                        else -> it
                    }
                },
                customerOrders = state.customerOrders.map {
                    if (it.id == order.id) declinedOrder else it
                },
                lastPlacedOrder = if (state.lastPlacedOrder?.id == order.id) declinedOrder else state.lastPlacedOrder,
                unacknowledgedAdminOrders = emptyList(),
                statusBannerMessage = "Order ${order.orderNumber} DECLINED & alarm silenced."
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.updateOrderStatusByAdmin(order, OrderStatus.CANCELLED)
                allRingingIds.filter { it != order.id }.forEach { otherId ->
                    orderRepo.acknowledgeAdminOrderAlert(otherId)
                }
            }
        }
    }

    fun acknowledgeOrderAlertDone(order: PizzaOrder) {
        val allRingingIds = (_uiState.value.ringingAdminOrders.map { it.id } + order.id).toSet()
        AdminOrderAlertPlayer.silenceOrdersAndStopRinging(allRingingIds)
        _uiState.update { state ->
            state.copy(
                locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + allRingingIds,
                allAdminOrders = state.allAdminOrders.map {
                    if (it.id in allRingingIds) it.copy(adminAlertAcknowledged = true) else it
                },
                unacknowledgedAdminOrders = emptyList()
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                allRingingIds.forEach { id ->
                    orderRepo.acknowledgeAdminOrderAlert(id)
                }
            }
        }
    }

    fun updateOrderStatusFromAdmin(order: PizzaOrder, nextStatus: OrderStatus) {
        val isFinal = nextStatus == OrderStatus.DELIVERED || nextStatus == OrderStatus.CANCELLED
        val updatedOrder = order.copy(
            status = nextStatus.code,
            paymentStatus = if (nextStatus == OrderStatus.DELIVERED) "PAID" else order.paymentStatus,
            isLocationSharingActive = if (isFinal) false else order.isLocationSharingActive,
            distanceKm = if (nextStatus == OrderStatus.DELIVERED) 0.0 else order.distanceKm,
            estimatedMinutes = if (isFinal) 0 else order.estimatedMinutes,
            adminAlertAcknowledged = true
        )
        val bannerText = when (nextStatus) {
            OrderStatus.DELIVERED -> "Order ${order.orderNumber} manually marked as DELIVERED"
            OrderStatus.CANCELLED -> "Order ${order.orderNumber} CANCELLED by Admin"
            else -> "Updated ${order.orderNumber} to ${nextStatus.title}"
        }
        _uiState.update { state ->
            state.copy(
                locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + order.id,
                locallyDeclinedOrderIds = if (nextStatus == OrderStatus.CANCELLED) {
                    state.locallyDeclinedOrderIds + order.id
                } else {
                    state.locallyDeclinedOrderIds
                },
                unacknowledgedAdminOrders = state.unacknowledgedAdminOrders.filterNot { it.id == order.id },
                allAdminOrders = state.allAdminOrders.map {
                    if (it.id == order.id) updatedOrder else it
                },
                customerOrders = state.customerOrders.map {
                    if (it.id == order.id) updatedOrder else it
                },
                assignedDeliveryOrders = state.assignedDeliveryOrders.map {
                    if (it.id == order.id) updatedOrder else it
                },
                lastPlacedOrder = if (state.lastPlacedOrder?.id == order.id) updatedOrder else state.lastPlacedOrder,
                statusBannerMessage = bannerText,
                errorBannerMessage = null
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.updateOrderStatusByAdmin(order, nextStatus)
            }
        }
    }

    /**
     * Barcode Scan Dispatch: When Admin or Delivery Boy scans the order barcode,
     * automatically assigns the delivery boy and reflects the delivery boy's name, phone,
     * vehicle number, and live GPS location on the Customer's Live Tracking screen!
     */
    fun scanOrderBarcodeAndAssignDeliveryPartner(
        scannedBarcode: String,
        partner: DeliveryPartner,
        targetStatus: OrderStatus = OrderStatus.ASSIGNED
    ) {
        val cleanCode = scannedBarcode.trim().uppercase()
        val state = _uiState.value
        val candidatePool = (state.allAdminOrders + state.customerOrders + state.assignedDeliveryOrders)
            .distinctBy { it.id }
        val matchedOrder = candidatePool.firstOrNull {
            it.barcodeValue.equals(cleanCode, ignoreCase = true) ||
                it.orderNumber.equals(cleanCode, ignoreCase = true) ||
                it.id.equals(cleanCode, ignoreCase = true)
        }

        if (matchedOrder == null) {
            _uiState.update {
                it.copy(errorBannerMessage = "No active order found matching barcode: $cleanCode")
            }
            return
        }

        assignDeliveryPartnerToOrder(matchedOrder, partner, targetStatus)
    }

    fun assignDeliveryPartnerToOrder(
        order: PizzaOrder,
        partner: DeliveryPartner,
        targetStatus: OrderStatus = OrderStatus.ASSIGNED
    ) {
        val partnerUid = partner.partnerUid.ifBlank { _uiState.value.currentUserId.ifBlank { "rider_hajipur_1" } }
        val updatedOrder = order.copy(
            status = targetStatus.code,
            deliveryPartnerId = partnerUid,
            deliveryPartnerName = partner.name,
            deliveryPartnerPhone = partner.phone,
            deliveryPartnerVehicle = partner.vehicleNumber,
            deliveryLat = partner.currentLat,
            deliveryLng = partner.currentLng,
            isLocationSharingActive = true,
            adminAlertAcknowledged = true
        )

        _uiState.update { state ->
            state.copy(
                locallyDismissedAlertOrderIds = state.locallyDismissedAlertOrderIds + order.id,
                allAdminOrders = state.allAdminOrders.map { if (it.id == order.id) updatedOrder else it },
                customerOrders = state.customerOrders.map { if (it.id == order.id) updatedOrder else it },
                assignedDeliveryOrders = (listOf(updatedOrder) + state.assignedDeliveryOrders.filterNot { it.id == order.id }),
                statusBannerMessage = "Barcode ${order.barcodeValue} scanned! Assigned ${partner.name} to ${order.orderNumber}."
            )
        }

        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.assignDeliveryPartnerAndScanBarcode(
                    order = order,
                    partner = partner.copy(partnerUid = partnerUid),
                    targetStatus = targetStatus
                )
            }
        }
    }

    // Delivery Partner Login & Live GPS Updates
    fun loginDeliveryPartnerWithPin(mobile: String, pin: String) {
        val cleanMobile = mobile.filter { it.isDigit() }.takeLast(10)
        val matched = _uiState.value.deliveryPartners.firstOrNull {
            it.phone.filter { ch -> ch.isDigit() }.takeLast(10) == cleanMobile && it.pinCode == pin.trim()
        }
        if (matched != null) {
            _uiState.update {
                it.copy(
                    isDeliveryPartnerLoggedIn = true,
                    activeDeliveryPartner = matched,
                    statusBannerMessage = "Logged in as ${matched.name} (${matched.vehicleNumber})",
                    errorBannerMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(errorBannerMessage = "Invalid credentials.")
            }
        }
    }

    fun quickSelectDeliveryPartner(partner: DeliveryPartner) {
        _uiState.update {
            it.copy(
                isDeliveryPartnerLoggedIn = true,
                activeDeliveryPartner = partner,
                statusBannerMessage = "Active Rider: ${partner.name}",
                errorBannerMessage = null
            )
        }
    }

    fun setLiveGpsStreamingActive(active: Boolean) {
        _uiState.update { it.copy(isLiveGpsStreaming = active) }
    }

    fun updateDeliveryOrderStepAndGps(
        order: PizzaOrder,
        newStatus: OrderStatus,
        riderLat: Double = order.deliveryLat,
        riderLng: Double = order.deliveryLng
    ) {
        val isDelivered = newStatus == OrderStatus.DELIVERED
        val nextLat = if (isDelivered) order.customerLat else riderLat
        val nextLng = if (isDelivered) order.customerLng else riderLng
        val distKm = if (isDelivered) {
            0.0
        } else {
            HajipurLocations.calculateDistanceKm(nextLat, nextLng, order.customerLat, order.customerLng)
        }
        val etaMins = if (isDelivered) {
            0
        } else {
            HajipurLocations.estimateMinutesForDistance(distKm, newStatus)
        }

        val updatedOrder = order.copy(
            status = newStatus.code,
            paymentStatus = if (isDelivered) "PAID" else order.paymentStatus,
            deliveryLat = nextLat,
            deliveryLng = nextLng,
            isLocationSharingActive = !isDelivered && newStatus != OrderStatus.CANCELLED,
            distanceKm = distKm,
            estimatedMinutes = etaMins
        )

        _uiState.update { state ->
            state.copy(
                allAdminOrders = state.allAdminOrders.map { if (it.id == order.id) updatedOrder else it },
                customerOrders = state.customerOrders.map { if (it.id == order.id) updatedOrder else it },
                assignedDeliveryOrders = state.assignedDeliveryOrders.map { if (it.id == order.id) updatedOrder else it },
                isLiveGpsStreaming = if (isDelivered) false else state.isLiveGpsStreaming,
                statusBannerMessage = "Order ${order.orderNumber}: ${newStatus.title}"
            )
        }

        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.updateDeliveryPartnerOrderStepAndGps(
                    order = order,
                    newStatus = newStatus,
                    riderLat = nextLat,
                    riderLng = nextLng
                )
            }
        }
    }

    // Admin Catalog, Coupons, Partners, and Store Config CRUD
    fun saveMenuItemFromAdmin(item: MenuItem) {
        _uiState.update { state ->
            val updatedList = if (item.id.isNotBlank() && state.menuItems.any { it.id == item.id }) {
                state.menuItems.map { if (it.id == item.id) item else it }
            } else {
                val withId = if (item.id.isBlank()) item.copy(id = "custom_${System.currentTimeMillis()}") else item
                listOf(withId) + state.menuItems
            }
            state.copy(
                menuItems = updatedList,
                statusBannerMessage = "Saved menu item: ${item.title}"
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                storeRepo.saveMenuItem(item)
            }
        }
    }

    fun deleteMenuItemFromAdmin(itemId: String) {
        _uiState.update { state ->
            state.copy(
                menuItems = state.menuItems.filterNot { it.id == itemId },
                statusBannerMessage = "Deleted menu item"
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                storeRepo.deleteMenuItem(itemId)
            }
        }
    }

    fun saveCouponFromAdmin(coupon: Coupon) {
        _uiState.update { state ->
            val clean = coupon.copy(
                id = coupon.id.ifBlank { coupon.code.trim().uppercase() },
                code = coupon.code.trim().uppercase()
            )
            val updated = state.coupons.filterNot { it.code.equals(clean.code, ignoreCase = true) } + clean
            state.copy(
                coupons = updated,
                statusBannerMessage = "Saved coupon ${clean.code}"
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                storeRepo.saveCoupon(coupon)
            }
        }
    }

    fun saveStoreConfigFromAdmin(config: StoreConfig) {
        _uiState.update { state ->
            val nextPaymentMethod = "COD"
            val storeOpenChanged = state.storeConfig.isOpen != config.isOpen
            val bannerMsg = when {
                storeOpenChanged && !config.isOpen ->
                    "Store is now CLOSED — customers can browse menu & add to cart, but ordering is paused."
                storeOpenChanged && config.isOpen ->
                    "Store is now OPEN — accepting live orders from My Crush Pizza Hajipur."
                config.razorpayEnabled -> {
                    val keyLabel = config.razorpayKeyId.ifBlank { "Instant Razorpay Gateway" }
                    "Saved Razorpay settings (${config.razorpayMode} • $keyLabel). Orders currently use Cash on Delivery (COD)."
                }
                else -> "Updated My Crush Pizza – Hajipur store & payment settings"
            }
            state.copy(
                storeConfig = config,
                selectedPaymentMethod = nextPaymentMethod,
                statusBannerMessage = bannerMsg
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                storeRepo.updateStoreConfig(config).onFailure { err ->
                    _uiState.update {
                        it.copy(
                            errorBannerMessage = err.localizedMessage
                                ?: "Could not update store open/closed status. Please try again."
                        )
                    }
                }
            }
        }
    }

    fun addDeliveryPartnerFromAdmin(name: String, phone: String, pin: String, vehicle: String) {
        val docId = "rider_${phone.filter { it.isDigit() }.takeLast(6)}"
        val uid = _uiState.value.currentUserId.ifBlank { "rider_hajipur_1" }
        val newPartner = DeliveryPartner(
            id = docId,
            partnerUid = uid,
            name = name.trim(),
            phone = phone.trim(),
            pinCode = pin.trim().ifBlank { "2468" },
            vehicleNumber = vehicle.trim().ifBlank { "BR-31-R-1111" },
            isActive = true,
            isAvailable = true
        )
        _uiState.update { state ->
            state.copy(
                deliveryPartners = state.deliveryPartners.filterNot { it.id == docId } + newPartner,
                statusBannerMessage = "Added delivery partner: ${newPartner.name}"
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                userRepo.registerOrUpdateDeliveryPartner(
                    partnerDocId = docId,
                    partnerUid = uid,
                    name = newPartner.name,
                    phone = newPartner.phone,
                    pinCode = newPartner.pinCode,
                    vehicleNumber = newPartner.vehicleNumber
                )
            }
        }
    }

    fun sendCustomCustomerNotification(
        recipientUid: String,
        title: String,
        message: String,
        imageUrl: String = "",
        imageKey: String = ""
    ): Boolean {
        val cleanTitle = title.trim()
        val cleanMessage = message.trim()
        if (cleanTitle.isBlank() || cleanMessage.isBlank()) return false

        val targetUid = recipientUid.ifBlank { "ALL" }
        val capKey = NotificationHelper.buildMessageCapKey(targetUid, cleanTitle, cleanMessage)
        val existingMatches = _uiState.value.notifications.count {
            it.title.trim().equals(cleanTitle, ignoreCase = true) &&
                it.message.trim().equals(cleanMessage, ignoreCase = true)
        }
        val sentSoFar = maxOf(customerMessageDeliveryCounts.getOrDefault(capKey, 0), existingMatches)
        if (sentSoFar >= NotificationHelper.MAX_SAME_MESSAGE_PER_CUSTOMER) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = "Limit reached: Same message can only be sent 1–2 times per customer."
                )
            }
            return false
        }

        customerMessageDeliveryCounts[capKey] = sentSoFar + 1
        val localNotif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            recipientUid = "ALL",
            title = cleanTitle,
            message = cleanMessage,
            type = "PROMO",
            imageUrl = imageUrl.trim(),
            imageKey = imageKey.trim(),
            isRead = false
        )
        notifiedIds += localNotif.id
        _uiState.update { state ->
            state.copy(
                notifications = listOf(localNotif) + state.notifications,
                statusBannerMessage = "Notification sent (${sentSoFar + 1}/${NotificationHelper.MAX_SAME_MESSAGE_PER_CUSTOMER}): $cleanTitle"
            )
        }
        if (_uiState.value.isSignedIn) {
            viewModelScope.launch {
                orderRepo.sendNotification(
                    recipientUid = "ALL",
                    title = cleanTitle,
                    message = cleanMessage,
                    type = "PROMO",
                    imageUrl = imageUrl.trim(),
                    imageKey = imageKey.trim()
                )
            }
        }
        return true
    }

    fun markAllNotificationsRead() {
        val currentState = _uiState.value
        val allIds = currentState.notifications.map { it.id }.filter { it.isNotBlank() }.toSet()
        val personalUnreadIds = currentState.notifications
            .filter { !it.isRead && it.id.isNotBlank() && it.recipientUid == currentState.currentUserId }
            .map { it.id }

        _uiState.update { state ->
            state.copy(
                locallyReadNotificationIds = state.locallyReadNotificationIds + allIds,
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }

        if (personalUnreadIds.isNotEmpty() && currentState.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                personalUnreadIds.forEach { notifId ->
                    runCatching { orderRepo.markNotificationRead(notifId) }
                }
            }
        }
    }

    fun clearOldOrdersFromAdmin(includeActiveOrders: Boolean = false) {
        val state = _uiState.value
        val combinedOrders = (state.allAdminOrders + state.customerOrders).distinctBy { it.id }
        val targetOrders = if (includeActiveOrders) {
            combinedOrders
        } else {
            combinedOrders.filter { it.isTerminal }
        }
        val idsToDelete = targetOrders.map { it.id }.filter { it.isNotBlank() }.distinct()
        if (idsToDelete.isEmpty()) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (includeActiveOrders) {
                        "No orders found to remove."
                    } else {
                        "No completed or cancelled old orders to clear."
                    }
                )
            }
            return
        }
        val idSet = idsToDelete.toSet()
        idsToDelete.forEach { AdminOrderAlertPlayer.silenceOrderAndStopRinging(it) }
        _uiState.update { current ->
            current.copy(
                locallyDeletedOrderIds = current.locallyDeletedOrderIds + idSet,
                allAdminOrders = current.allAdminOrders.filterNot { it.id in idSet },
                customerOrders = current.customerOrders.filterNot { it.id in idSet },
                unacknowledgedAdminOrders = current.unacknowledgedAdminOrders.filterNot { it.id in idSet },
                lastAcceptedAdminOrder = current.lastAcceptedAdminOrder?.takeUnless { it.id in idSet },
                statusBannerMessage = "Removed ${idsToDelete.size} old order${if (idsToDelete.size > 1) "s" else ""} from Admin Panel."
            )
        }
        if (state.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                val res = orderRepo.deleteOrdersByAdmin(idsToDelete)
                res.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            errorBannerMessage = err.localizedMessage ?: "Failed to remove orders from cloud."
                        )
                    }
                }
            }
        }
    }

    fun deleteSingleOrderFromAdmin(orderId: String) {
        val cleanId = orderId.trim()
        if (cleanId.isBlank()) return
        AdminOrderAlertPlayer.silenceOrderAndStopRinging(cleanId)
        _uiState.update { current ->
            current.copy(
                locallyDeletedOrderIds = current.locallyDeletedOrderIds + cleanId,
                allAdminOrders = current.allAdminOrders.filterNot { it.id == cleanId },
                customerOrders = current.customerOrders.filterNot { it.id == cleanId },
                unacknowledgedAdminOrders = current.unacknowledgedAdminOrders.filterNot { it.id == cleanId },
                lastAcceptedAdminOrder = current.lastAcceptedAdminOrder?.takeUnless { it.id == cleanId },
                statusBannerMessage = "Order removed from Admin Panel."
            )
        }
        if (_uiState.value.isSignedIn && Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                orderRepo.deleteOrdersByAdmin(listOf(cleanId))
            }
        }
    }

    fun clearBanners() {
        _uiState.update { it.copy(statusBannerMessage = null, errorBannerMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllFirestoreListeners()
    }

    companion object {
        // Auto-clean pre-existing old Admin orders & old non-admin customers created prior to 2026-11-24 05:00:00 UTC.
        val OLD_ORDERS_CLEANUP_CUTOFF_MILLIS: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 24, 5, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val OLD_CUSTOMERS_CLEANUP_CUTOFF_MILLIS: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.NOVEMBER, 24, 5, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainAppViewModel(
                    userRepo = AuthAndUserRepository(db),
                    storeRepo = StoreAndMenuRepository(db),
                    orderRepo = OrderAndNotificationRepository(db)
                )
            }
        }
    }
}

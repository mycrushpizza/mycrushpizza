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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AddressLabel
import com.example.data.model.HajipurLocations
import com.example.data.model.SavedDeliveryAddress
import com.example.data.model.formatInr
import com.example.ui.components.CustomerLiveLocationDialog
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.OrderHistoryComponent
import com.example.ui.components.StoreContactHelper
import com.example.ui.theme.CrushWhite
import com.example.ui.viewmodel.MainAppUiState

/**
 * Dedicated Customer Account Section:
 * - Profile card (Sign In with Google if guest, or User Name / Email / Copy Wallet ID)
 * - Quick stats (Orders, Bonus Balance, Cart)
 * - Account menu (My Orders, Bonus Wallet, Cart, Delivery Address, Notifications, Admin Panel for Admin)
 * - Store info & 1-tap Call / WhatsApp / Google Maps directions to My Crush Pizza Hajipur
 * - Sign Out
 */
@Composable
fun AccountScreen(
    uiState: MainAppUiState,
    onOpenOrdersClick: () -> Unit,
    onOpenWalletClick: () -> Unit,
    onOpenCartClick: () -> Unit,
    onOpenNotificationsClick: () -> Unit,
    onUpdatePickedLocation: (Double, Double, String, String) -> Unit,
    onOpenAdminPanelClick: () -> Unit,
    onRequestGoogleSignIn: () -> Unit,
    onSignOutClick: () -> Unit,
    onSaveFullCustomerDetails: ((String, String, Double, Double, String, String, AddressLabel) -> Unit)? = null,
    onApplySavedAddress: (SavedDeliveryAddress) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddressDialog by remember { mutableStateOf(false) }

    if (showAddressDialog) {
        CustomerLiveLocationDialog(
            currentAddress = uiState.checkoutAddress,
            currentLandmark = uiState.checkoutLandmark,
            currentLat = uiState.checkoutLat,
            currentLng = uiState.checkoutLng,
            currentName = uiState.checkoutName.ifBlank { uiState.currentUserName },
            currentPhone = uiState.checkoutPhone,
            currentAddressLabel = uiState.checkoutAddressLabel,
            onSaveLocation = { lat, lng, addr, landmark ->
                onUpdatePickedLocation(lat, lng, addr, landmark)
            },
            onSaveFullCustomerDetailsWithLabel = { name, phone, lat, lng, addr, landmark, label ->
                if (onSaveFullCustomerDetails != null) {
                    onSaveFullCustomerDetails(name, phone, lat, lng, addr, landmark, label)
                } else {
                    onUpdatePickedLocation(lat, lng, addr, landmark)
                }
            },
            onDismiss = { showAddressDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F6),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .testTag("account_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Profile Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_profile_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color(0xFFFECDD3))
            ) {
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
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val initial = uiState.currentUserName.trim().firstOrNull()?.uppercaseChar()
                                    if (uiState.isSignedIn && initial != null) {
                                        Text(
                                            text = initial.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Account",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (uiState.isSignedIn) {
                                        uiState.currentUserName.ifBlank { "My Crush Customer" }
                                    } else {
                                        "Guest Account"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (uiState.isSignedIn) {
                                        uiState.currentUserEmail.ifBlank { "Signed in" }
                                    } else {
                                        "Sign in to sync orders & bonus"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFE4E6),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (!uiState.isSignedIn) {
                            Button(
                                onClick = onRequestGoogleSignIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFFE31837)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("account_google_sign_in_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In with Google",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Stats Row (Orders, Bonus, Cart)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccountStatTile(
                    label = "Orders",
                    value = uiState.customerOrders.size.toString(),
                    accent = Color(0xFFD97706),
                    bg = Color(0xFFFFFBEB),
                    onClick = onOpenOrdersClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("account_stat_orders")
                )
                AccountStatTile(
                    label = "Bonus",
                    value = formatInr(uiState.bonusBalanceInr),
                    accent = Color(0xFF059669),
                    bg = Color(0xFFECFDF5),
                    onClick = onOpenWalletClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("account_stat_bonus")
                )
                AccountStatTile(
                    label = "Cart",
                    value = "${uiState.cartCount} Items",
                    accent = Color(0xFFE31837),
                    bg = Color(0xFFFFF1F2),
                    onClick = onOpenCartClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("account_stat_cart")
                )
            }
        }

        // 2B. Saved Delivery Address Card (Home / Work / Other — synced to Logged-In Account)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_saved_address_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val labelIcon = when (uiState.checkoutAddressLabel) {
                                        AddressLabel.HOME -> Icons.Filled.Home
                                        AddressLabel.WORK -> Icons.Filled.Work
                                        AddressLabel.OTHER -> Icons.Filled.LocationOn
                                    }
                                    Icon(
                                        imageVector = labelIcon,
                                        contentDescription = "Saved Address",
                                        tint = Color(0xFFE31837),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Saved Delivery Address",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (uiState.isSignedIn) "Synced with your Account" else "Sign in to save permanently",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.isSignedIn) Color(0xFF059669) else Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showAddressDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE31837)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("account_edit_address_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Address",
                                tint = Color(0xFFE31837),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.checkoutAddress.isBlank()) "Add" else "Edit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE31837),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (uiState.savedAddresses.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.savedAddresses, key = { it.label.code }) { saved ->
                                val isSelected = saved.label == uiState.checkoutAddressLabel
                                val chipIcon = when (saved.label) {
                                    AddressLabel.HOME -> Icons.Filled.Home
                                    AddressLabel.WORK -> Icons.Filled.Work
                                    AddressLabel.OTHER -> Icons.Filled.LocationOn
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onApplySavedAddress(saved) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = chipIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = saved.label.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE31837),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = Color(0xFFF8FAFC),
                                        labelColor = Color(0xFF0F172A)
                                    )
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAddressDialog = true }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (uiState.checkoutAddress.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFFFFF1F2),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = uiState.checkoutAddressLabel.displayName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE31837),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (uiState.checkoutPhone.isNotBlank()) {
                                        Text(
                                            text = uiState.checkoutPhone,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = uiState.checkoutAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (uiState.checkoutLandmark.isNotBlank()) {
                                    Text(
                                        text = "Landmark: ${uiState.checkoutLandmark}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "Tap Add to save your Home, Work, or Other delivery address for faster checkout.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2C. Live Firestore Order History Component (Recent Past Orders)
        if (uiState.isSignedIn) {
            item {
                OrderHistoryComponent(
                    userId = uiState.currentUserId,
                    maxVisibleOrders = 3,
                    onOrderClick = { onOpenOrdersClick() },
                    onViewAllOrdersClick = onOpenOrdersClick
                )
            }
        }

        // 3. Account Menu Options Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AccountMenuRow(
                        icon = Icons.Filled.History,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFFFBEB),
                        title = "My Orders",
                        subtitle = if (uiState.customerOrders.isEmpty()) {
                            "View past orders & live status"
                        } else {
                            "${uiState.customerOrders.size} orders"
                        },
                        onClick = onOpenOrdersClick,
                        testTag = "account_orders_row"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    AccountMenuRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconTint = Color(0xFF059669),
                        iconBg = Color(0xFFECFDF5),
                        title = "Bonus Wallet",
                        subtitle = "Balance: ${formatInr(uiState.bonusBalanceInr)}",
                        onClick = onOpenWalletClick,
                        testTag = "account_wallet_row"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    AccountMenuRow(
                        icon = Icons.Filled.ShoppingCart,
                        iconTint = Color(0xFFE31837),
                        iconBg = Color(0xFFFFF1F2),
                        title = "My Cart",
                        subtitle = if (uiState.cartCount > 0) {
                            "${uiState.cartCount} items • ${formatInr(uiState.subtotalInr)}"
                        } else {
                            "Empty cart"
                        },
                        onClick = onOpenCartClick,
                        testTag = "account_cart_row"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    AccountMenuRow(
                        icon = Icons.Filled.LocationOn,
                        iconTint = Color(0xFFE31837),
                        iconBg = Color(0xFFFFF1F2),
                        title = "Delivery Address",
                        subtitle = if (uiState.checkoutAddress.isNotBlank()) {
                            "${uiState.checkoutAddressLabel.displayName} • ${uiState.checkoutAddress}"
                        } else {
                            "Set your Home / Work / Other address"
                        },
                        onClick = { showAddressDialog = true },
                        testTag = "account_address_row"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                    val unreadNotifs = uiState.unreadNotificationsCount
                    AccountMenuRow(
                        icon = Icons.Filled.Notifications,
                        iconTint = Color(0xFF9F1239),
                        iconBg = Color(0xFFFFF1F2),
                        title = "Notifications",
                        subtitle = if (unreadNotifs > 0) "$unreadNotifs new" else "Offers & order alerts",
                        onClick = onOpenNotificationsClick,
                        testTag = "account_notifications_row"
                    )

                    if (uiState.isAdminUser) {
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        AccountMenuRow(
                            icon = Icons.Filled.AdminPanelSettings,
                            iconTint = Color(0xFFE31837),
                            iconBg = Color(0xFFFFF1F2),
                            title = "Admin Command Center",
                            subtitle = "Manage orders, menu & bonus wallet",
                            onClick = onOpenAdminPanelClick,
                            testTag = "account_admin_row"
                        )
                    }
                }
            }
        }

        // 4. Store Location & Support Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { StoreContactHelper.openStoreLocationInGoogleMaps(context) }
                            .padding(vertical = 2.dp),
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
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = "Store",
                                    tint = Color(0xFFE31837),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = HajipurLocations.STORE_LOCATION_NAME,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = HajipurLocations.STORE_FULL_ADDRESS,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { StoreContactHelper.dialStore(context) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFE31837)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_call_store_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Call Store",
                                tint = Color(0xFFE31837),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE31837),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                StoreContactHelper.openWhatsAppWithText(
                                    context = context,
                                    message = "Hello My Crush Pizza Hajipur! I need help with my order."
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFF059669)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_whatsapp_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WhatsApp",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                StoreContactHelper.openStoreLocationInGoogleMaps(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_store_map_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = "Directions",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Maps",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = "Helpline: $OFFICIAL_STORE_PHONE_DISPLAY • 10:00 AM – 11:00 PM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // 5. Sign Out Button (when signed in)
        if (uiState.isSignedIn) {
            item {
                OutlinedButton(
                    onClick = onSignOutClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE31837)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFE31837)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("account_sign_out_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountStatTile(
    label: String,
    value: String,
    accent: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AccountMenuRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = iconBg,
            shape = CircleShape,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

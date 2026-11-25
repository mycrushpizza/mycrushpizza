package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AddressLabel
import com.example.data.model.HajipurLandmark
import com.example.data.model.HajipurLocations
import com.example.data.model.SavedDeliveryAddress
import com.example.data.model.formatInr
import com.example.ui.components.AddressLabelSelectorRow
import com.example.ui.components.HajipurLiveMap
import com.example.ui.viewmodel.MainAppUiState

private val Step2Ink = Color(0xFF0F172A)
private val Step2Muted = Color(0xFF64748B)
private val Step2Crimson = Color(0xFFE31837)
private val Step2Emerald = Color(0xFF059669)
private val Step2Border = Color(0xFFE2E8F0)

/**
 * Clean 2-Step Checkout Progress Header shown at the top of Step 2 ("Delivery & Pay").
 */
@Composable
fun CheckoutStepTwoProgressHeader(
    cartCount: Int,
    grandTotalInr: Int,
    onEditCartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_confirmed_cart_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Step2Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                        color = Color(0xFFF8FAFC),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Step2Border),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onEditCartClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Cart",
                                tint = Step2Ink,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Delivery & Payment",
                            style = MaterialTheme.typography.titleMedium,
                            color = Step2Ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$cartCount item${if (cartCount > 1) "s" else ""} • Total ${formatInr(grandTotalInr)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Step2Muted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditCartClick,
                    border = BorderStroke(1.dp, Step2Crimson.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("back_to_cart_step_btn")
                ) {
                    Text(
                        text = "Edit Cart",
                        style = MaterialTheme.typography.labelSmall,
                        color = Step2Crimson,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Visual 2-Step Progress Pill Row: (✓ 1. Cart) ━━━━ (2. Delivery & Pay)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Step2Emerald,
                    shape = CircleShape,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Cart Done",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.labelMedium,
                    color = Step2Emerald,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Step2Emerald)
                )
                Surface(
                    color = Step2Crimson,
                    shape = CircleShape,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "2",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "Delivery & Pay",
                    style = MaterialTheme.typography.labelMedium,
                    color = Step2Crimson,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Smart Delivery Details Card:
 * - Shows a compact, scannable saved-address summary when all fields are already filled.
 * - Expands cleanly into the full Contact + Live GPS + Pin Map + Quick Areas editor on 1 tap (or automatically if fields are incomplete).
 */
@Composable
fun CheckoutStepTwoDeliveryCard(
    uiState: MainAppUiState,
    distanceKm: Double,
    estMins: Int,
    isEditingAddress: Boolean,
    onToggleEditAddress: (Boolean) -> Unit,
    isDetectingGps: Boolean,
    gpsStatusNote: String?,
    isGpsNoteError: Boolean,
    addressValidationError: String?,
    onClearValidationError: () -> Unit,
    onUpdateCheckoutDetails: (String?, String?, String?, String?, String?) -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onOpenSearchLocationDialog: () -> Unit,
    onSelectLandmark: (HajipurLandmark) -> Unit,
    onMapPinPicked: (Double, Double, String) -> Unit,
    onSelectAddressLabel: (AddressLabel) -> Unit = {},
    onApplySavedAddress: (SavedDeliveryAddress) -> Unit = {},
    onConfirmAndSaveAddress: () -> Unit = {}
) {
    val cleanPhoneDigits = uiState.checkoutPhone.filter { it.isDigit() }
    val isDetailsComplete = uiState.checkoutName.trim().length >= 2 &&
        cleanPhoneDigits.length >= 10 &&
        uiState.checkoutAddress.trim().length >= 5 &&
        uiState.checkoutLandmark.trim().length >= 2

    val showFullForm = isEditingAddress || !isDetailsComplete || !uiState.isCheckoutWithin10KmRange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_step2_address_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (!uiState.isCheckoutWithin10KmRange || !addressValidationError.isNullOrBlank()) {
                Step2Crimson
            } else {
                Step2Border
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header with Distance & ETA Pill
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
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Delivery Address",
                                tint = Step2Crimson,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Delivering To",
                            style = MaterialTheme.typography.titleMedium,
                            color = Step2Ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isDetailsComplete && !showFullForm) {
                                "Address verified • Ready to order"
                            } else {
                                "Add or confirm your Hajipur address"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDetailsComplete && !showFullForm) Step2Emerald else Step2Muted
                        )
                    }
                }

                Surface(
                    color = if (uiState.isCheckoutWithin10KmRange) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isCheckoutWithin10KmRange) Color(0xFF10B981) else Step2Crimson
                    ),
                    modifier = Modifier.testTag("checkout_range_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = if (uiState.isCheckoutWithin10KmRange) Step2Emerald else Step2Crimson,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (uiState.isCheckoutWithin10KmRange) {
                                "${String.format("%.1f", distanceKm)} km • 30-45 min"
                            } else {
                                "${String.format("%.1f", distanceKm)} km • >10 km"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isCheckoutWithin10KmRange) Color(0xFF047857) else Color(0xFFB91C1C),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Validation / Range Alert Banners
            if (!uiState.isCheckoutWithin10KmRange) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Step2Crimson.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Delivery available only within 10 km of My Crush Pizza Hajipur. Please pick a closer location.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB91C1C),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (!addressValidationError.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Step2Crimson.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = addressValidationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB91C1C),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // COMPACT SAVED-ADDRESS SUMMARY VIEW (Fast 2-second checkout!)
            if (!showFullForm) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Step2Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                val tagIcon = when (uiState.checkoutAddressLabel) {
                                    AddressLabel.HOME -> Icons.Filled.Home
                                    AddressLabel.WORK -> Icons.Filled.Work
                                    AddressLabel.OTHER -> Icons.Filled.LocationOn
                                }
                                Surface(
                                    color = Color(0xFFFFF1F2),
                                    shape = RoundedCornerShape(7.dp),
                                    border = BorderStroke(1.dp, Step2Crimson.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = tagIcon,
                                            contentDescription = uiState.checkoutAddressLabel.displayName,
                                            tint = Step2Crimson,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = uiState.checkoutAddressLabel.displayName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Step2Crimson,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = "${uiState.checkoutName} • ${uiState.checkoutPhone}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Step2Ink,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            TextButton(
                                onClick = { onToggleEditAddress(true) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("checkout_edit_saved_address_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Address",
                                    tint = Step2Crimson,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Change",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Step2Crimson,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (uiState.savedAddresses.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.savedAddresses, key = { it.label.code }) { saved ->
                                    val isActiveSlot = saved.label == uiState.checkoutAddressLabel
                                    FilterChip(
                                        selected = isActiveSlot,
                                        onClick = { onApplySavedAddress(saved) },
                                        label = {
                                            Text(
                                                text = saved.label.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Step2Crimson,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White,
                                            labelColor = Step2Ink
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = uiState.checkoutAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = Step2Ink,
                            fontWeight = FontWeight.Medium
                        )
                        if (uiState.checkoutLandmark.isNotBlank()) {
                            Text(
                                text = "Landmark: ${uiState.checkoutLandmark}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Step2Muted
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRequestCurrentLocation,
                                enabled = !isDetectingGps,
                                border = BorderStroke(1.dp, Step2Crimson.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Step2Crimson),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("checkout_detect_gps_btn")
                            ) {
                                if (isDetectingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Step2Crimson,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(15.dp))
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isDetectingGps) "Locating..." else "Update GPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenSearchLocationDialog,
                                border = BorderStroke(1.dp, Step2Border),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Step2Ink),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("checkout_open_full_location_dialog_btn")
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Search Area",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                // FULL ADDRESS & CONTACT EDITOR FORM
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Step2Crimson,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedTextColor = Step2Ink,
                    unfocusedTextColor = Step2Ink,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedLabelColor = Step2Crimson,
                    unfocusedLabelColor = Step2Muted
                )

                // Quick Location Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestCurrentLocation,
                        enabled = !isDetectingGps,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Step2Crimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("checkout_detect_gps_btn")
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDetectingGps) "Locating..." else "Use Live GPS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenSearchLocationDialog,
                        border = BorderStroke(1.2.dp, Step2Crimson),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Step2Crimson),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("checkout_open_full_location_dialog_btn")
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Search Area",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                // Interactive Map Picker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HajipurLiveMap(
                        customerLat = uiState.checkoutLat,
                        customerLng = uiState.checkoutLng,
                        estimatedMinutes = estMins,
                        distanceKm = distanceKm,
                        interactivePicker = true,
                        mapHeight = 165.dp,
                        onDetectGpsClick = onRequestCurrentLocation,
                        onLocationPicked = onMapPinPicked
                    )
                    if (!gpsStatusNote.isNullOrBlank()) {
                        Text(
                            text = gpsStatusNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGpsNoteError) Color(0xFFB91C1C) else Step2Emerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Hajipur Areas Strip
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quick Select Hajipur Area",
                        style = MaterialTheme.typography.labelSmall,
                        color = Step2Muted,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HajipurLocations.landmarks, key = { it.name }) { lm ->
                            val isSelectedLandmark = uiState.checkoutLandmark.equals(lm.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelectedLandmark,
                                onClick = {
                                    onClearValidationError()
                                    onSelectLandmark(lm)
                                },
                                label = {
                                    Text(
                                        text = lm.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelectedLandmark) FontWeight.ExtraBold else FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFF1F2),
                                    selectedLabelColor = Step2Crimson
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Save Address As: Home / Work / Other
                AddressLabelSelectorRow(
                    selectedLabel = uiState.checkoutAddressLabel,
                    onSelectLabel = { label ->
                        onSelectAddressLabel(label)
                        uiState.savedAddresses.firstOrNull { it.label == label }?.let { existing ->
                            onApplySavedAddress(existing)
                        }
                    }
                )

                // Contact & Address Fields
                OutlinedTextField(
                    value = uiState.checkoutName,
                    onValueChange = {
                        onClearValidationError()
                        onUpdateCheckoutDetails(it, null, null, null, null)
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("Enter your name") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "Name", tint = Step2Crimson)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = uiState.checkoutPhone,
                    onValueChange = { raw ->
                        onClearValidationError()
                        val filtered = raw.filter { it.isDigit() || it == '+' || it == ' ' }.take(15)
                        onUpdateCheckoutDetails(null, filtered, null, null, null)
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("10-digit mobile number") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = "Phone", tint = Step2Crimson)
                    },
                    trailingIcon = {
                        if (cleanPhoneDigits.length >= 10) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Valid Phone",
                                tint = Step2Emerald,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = uiState.checkoutAddress,
                    onValueChange = {
                        onClearValidationError()
                        onUpdateCheckoutDetails(null, null, it, null, null)
                    },
                    label = { Text("Complete Address") },
                    placeholder = { Text("House / Flat / Street / Mohalla") },
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Address", tint = Step2Crimson)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = uiState.checkoutLandmark,
                    onValueChange = {
                        onClearValidationError()
                        onUpdateCheckoutDetails(null, null, null, it, null)
                    },
                    label = { Text("Nearby Landmark") },
                    placeholder = { Text("e.g., Near RN College, Chowk, Temple") },
                    leadingIcon = {
                        Icon(Icons.Filled.Map, contentDescription = "Landmark", tint = Step2Crimson)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_landmark_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                if (isDetailsComplete && uiState.isCheckoutWithin10KmRange) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                onConfirmAndSaveAddress()
                                onToggleEditAddress(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFECFDF5),
                                contentColor = Color(0xFF047857)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("checkout_confirm_save_address_btn")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save & Confirm Address", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bonus Wallet Redemption + Expandable Bill & Items Review Card for Step 2.
 */
@Composable
fun CheckoutStepTwoReviewAndWalletCard(
    uiState: MainAppUiState,
    onToggleUseBonusBalance: (Boolean) -> Unit
) {
    var showItemsBreakdown by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Bonus Wallet Cash Redemption Toggle (shown automatically whenever customer has Bonus Cash > ₹0)
        if (uiState.bonusBalanceInr > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleUseBonusBalance(!uiState.useBonusBalanceInCheckout) }
                    .testTag("checkout_step2_bonus_wallet_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.useBonusBalanceInCheckout) Color(0xFFECFDF5) else Color.White
                ),
                border = BorderStroke(
                    1.2.dp,
                    if (uiState.useBonusBalanceInCheckout) Step2Emerald else Step2Border
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = "Bonus Wallet",
                                    tint = Step2Emerald,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Use Bonus Wallet Cash (${formatInr(uiState.bonusBalanceInr)})",
                                style = MaterialTheme.typography.titleSmall,
                                color = Step2Ink,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (uiState.useBonusBalanceInCheckout && uiState.walletTotalUsedInr > 0) {
                                    "Saving ${formatInr(uiState.walletTotalUsedInr)} on this order!"
                                } else {
                                    "Tap to apply your reward balance instantly"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.useBonusBalanceInCheckout) Step2Emerald else Step2Muted,
                                fontWeight = if (uiState.useBonusBalanceInCheckout) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Checkbox(
                        checked = uiState.useBonusBalanceInCheckout,
                        onCheckedChange = onToggleUseBonusBalance,
                        colors = CheckboxDefaults.colors(checkedColor = Step2Emerald)
                    )
                }
            }
        }

        // Compact Expandable Order & Bill Review Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("checkout_step2_bill_review_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Step2Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showItemsBreakdown = !showItemsBreakdown },
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
                            tint = Step2Crimson,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "Bill Summary (${uiState.cartCount} item${if (uiState.cartCount > 1) "s" else ""})",
                            style = MaterialTheme.typography.titleSmall,
                            color = Step2Ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (showItemsBreakdown) "Hide Items" else "View Items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Step2Crimson,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (showItemsBreakdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Step2Crimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showItemsBreakdown) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Step2Border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.cartItems.forEach { cartItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cartItem.quantity}× ${cartItem.menuItem.title}" +
                                            if (cartItem.menuItem.supportsMultipleSizes()) " (${cartItem.size.label})" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Step2Ink,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatInr(cartItem.lineTotalInr),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Step2Ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Step2BillRow("Item Total", formatInr(uiState.subtotalInr))
                if (uiState.discountInr > 0) {
                    Step2BillRow(
                        "Coupon (${uiState.appliedCoupon?.code})",
                        "-${formatInr(uiState.discountInr)}",
                        valueColor = Step2Emerald
                    )
                }
                Step2BillRow(
                    "Delivery Fee",
                    if (uiState.deliveryFeeInr == 0) "FREE" else formatInr(uiState.deliveryFeeInr),
                    valueColor = if (uiState.deliveryFeeInr == 0) Step2Emerald else Step2Ink
                )
                Step2BillRow("GST (${uiState.storeConfig.taxPercent}%)", formatInr(uiState.taxInr))
                if (uiState.walletTotalUsedInr > 0) {
                    Step2BillRow(
                        "Bonus Wallet Discount",
                        "-${formatInr(uiState.walletTotalUsedInr)}",
                        valueColor = Step2Emerald
                    )
                }

                HorizontalDivider(color = Step2Border)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grand Total",
                        style = MaterialTheme.typography.titleMedium,
                        color = Step2Ink,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = formatInr(uiState.grandTotalInr),
                        style = MaterialTheme.typography.titleLarge,
                        color = Step2Crimson,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun Step2BillRow(
    label: String,
    value: String,
    valueColor: Color = Step2Ink
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Step2Muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Clean, trustworthy Cash on Delivery Payment Card for Step 2.
 */
@Composable
fun CheckoutStepTwoPaymentCard(
    grandTotalInr: Int,
    onSelectCod: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_step2_payment_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Step2Border)
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
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    color = Step2Ink,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Pay at Doorstep",
                        style = MaterialTheme.typography.labelSmall,
                        color = Step2Emerald,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCod() }
                    .testTag("payment_option_COD"),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.5.dp, Step2Emerald)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Step2Emerald,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Money,
                                contentDescription = "Cash on Delivery",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cash on Delivery (COD)",
                            style = MaterialTheme.typography.titleSmall,
                            color = Step2Ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Pay ${formatInr(grandTotalInr)} in cash to our delivery partner when your hot order arrives",
                            style = MaterialTheme.typography.labelSmall,
                            color = Step2Muted
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = Step2Emerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Reassuring trust footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = Step2Muted,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Freshly baked on order at My Crush Pizza, RN College Chautha Road",
                    style = MaterialTheme.typography.labelSmall,
                    color = Step2Muted
                )
            }
        }
    }
}

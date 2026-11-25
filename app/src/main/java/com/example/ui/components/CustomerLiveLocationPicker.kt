package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AddressLabel
import com.example.data.model.HajipurLandmark
import com.example.data.model.HajipurLocations
import com.example.data.model.OrderStatus
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.VegGreenBright
import com.example.util.GpsLocationHelper
import com.example.util.LocationSearchSuggestion
import com.example.util.LocationSettingsCheckResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun openCustomerLocationInGoogleMaps(
    context: Context,
    lat: Double,
    lng: Double,
    label: String = "My Delivery Location"
) {
    val encodedLabel = Uri.encode(label)
    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedLabel)")
    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(mapsIntent)
    } catch (_: Exception) {
    }
}

/**
 * Clean, easy-to-use full-screen Delivery Location Picker:
 * - Search bar + 1-tap Current Location (GPS)
 * - Scrollable Hajipur map pin with 10 km range check
 * - Minimal confirmation fields & 1-tap Confirm Location
 */
@Composable
fun AddressLabelSelectorRow(
    selectedLabel: AddressLabel,
    onSelectLabel: (AddressLabel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Save Address As",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AddressLabel.entries.forEach { label ->
                val isSelected = selectedLabel == label
                val icon = when (label) {
                    AddressLabel.HOME -> Icons.Filled.Home
                    AddressLabel.WORK -> Icons.Filled.Work
                    AddressLabel.OTHER -> Icons.Filled.LocationOn
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .clickable { onSelectLabel(label) }
                        .testTag("address_label_chip_${label.code.lowercase()}"),
                    shape = RoundedCornerShape(11.dp),
                    color = if (isSelected) CrushRed else Color(0xFFF8FAFC),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CrushRed else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label.displayName,
                            tint = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = label.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerLiveLocationDialog(
    currentAddress: String,
    currentLandmark: String,
    currentLat: Double,
    currentLng: Double,
    onSaveLocation: (Double, Double, String, String) -> Unit,
    onDismiss: () -> Unit,
    currentName: String = "",
    currentPhone: String = "",
    currentAddressLabel: AddressLabel = AddressLabel.HOME,
    startInSearchMode: Boolean = false,
    onSaveFullCustomerDetails: ((String, String, Double, Double, String, String) -> Unit)? = null,
    onSaveLocationWithLabel: ((Double, Double, String, String, AddressLabel) -> Unit)? = null,
    onSaveFullCustomerDetailsWithLabel: ((String, String, Double, Double, String, String, AddressLabel) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedLat by remember(currentLat) { mutableDoubleStateOf(currentLat) }
    var pickedLng by remember(currentLng) { mutableDoubleStateOf(currentLng) }
    var nameInput by remember(currentName) { mutableStateOf(currentName) }
    var phoneInput by remember(currentPhone) { mutableStateOf(currentPhone) }
    var addressInput by remember(currentAddress) { mutableStateOf(currentAddress) }
    var landmarkInput by remember(currentLandmark) { mutableStateOf(currentLandmark) }
    var selectedAddressLabel by remember(currentAddressLabel) { mutableStateOf(currentAddressLabel) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchSheet by remember(startInSearchMode) { mutableStateOf(startInSearchMode) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var isSearchingLocations by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }
    var isGpsStatusError by remember { mutableStateOf(false) }
    var showOpenLocationSettingsAction by remember { mutableStateOf(false) }
    var searchResults by remember {
        mutableStateOf(
            HajipurLocations.landmarks.map {
                LocationSearchSuggestion(
                    title = it.name,
                    subtitle = it.addressLine,
                    lat = it.lat,
                    lng = it.lng
                )
            }
        )
    }

    val recentSearches = remember {
        mutableStateListOf(
            HajipurLandmark("Shahi Colony, Hajipur", "Near Dakbunglow Road, Hajipur", 25.6886, 85.2132),
            HajipurLandmark("Cinema Road", "Cinema Rd, Hajipur", 25.6850, 85.2044)
        )
    }

    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isBlank()) {
            isSearchingLocations = false
            searchResults = HajipurLocations.landmarks.map {
                LocationSearchSuggestion(
                    title = it.name,
                    subtitle = it.addressLine,
                    lat = it.lat,
                    lng = it.lng
                )
            }
            return@LaunchedEffect
        }
        isSearchingLocations = true
        delay(280L)
        searchResults = GpsLocationHelper.searchLocationsByQuery(context, trimmed)
        isSearchingLocations = false
    }

    val runLiveGpsDetection: () -> Unit = {
        isDetectingGps = true
        isGpsStatusError = false
        showOpenLocationSettingsAction = false
        gpsStatusMessage = "Locating..."
        scope.launch {
            val result = GpsLocationHelper.detectCurrentCustomerAddress(context)
            isDetectingGps = false
            result.onSuccess { detected ->
                pickedLat = detected.lat
                pickedLng = detected.lng
                addressInput = detected.formattedAddress
                landmarkInput = detected.landmarkName.ifBlank { "Current Location" }
                isGpsStatusError = !detected.isWithin10KmRange
                gpsStatusMessage = if (detected.isWithin10KmRange) {
                    "Location updated (${detected.distanceKm} km)"
                } else {
                    "Outside 10 km range (${detected.distanceKm} km)"
                }
                showSearchSheet = false
            }.onFailure {
                isGpsStatusError = true
                showOpenLocationSettingsAction = !GpsLocationHelper.isLocationServicesEnabled(context)
                gpsStatusMessage = if (showOpenLocationSettingsAction) {
                    "Turn on GPS"
                } else {
                    "Could not locate. Search area below."
                }
                showSearchSheet = false
            }
        }
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            runLiveGpsDetection()
        } else {
            isDetectingGps = false
            isGpsStatusError = true
            showOpenLocationSettingsAction = true
            gpsStatusMessage = "Turn on Location to detect your address."
        }
    }

    val ensureLocationOnAndRunGps: () -> Unit = {
        isDetectingGps = true
        isGpsStatusError = false
        showOpenLocationSettingsAction = false
        gpsStatusMessage = "Checking GPS..."
        scope.launch {
            when (val check = GpsLocationHelper.checkLocationSettingsForGps(context)) {
                LocationSettingsCheckResult.AlreadyEnabled -> {
                    runLiveGpsDetection()
                }
                is LocationSettingsCheckResult.ResolutionRequired -> {
                    isDetectingGps = false
                    gpsStatusMessage = "Turning on device location..."
                    runCatching {
                        locationSettingsLauncher.launch(check.intentSenderRequest)
                    }.onFailure {
                        isGpsStatusError = true
                        showOpenLocationSettingsAction = true
                        gpsStatusMessage = "Turn on Location in phone settings."
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
                    isGpsStatusError = true
                    showOpenLocationSettingsAction = true
                    gpsStatusMessage = "Please turn ON Location in settings."
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            ensureLocationOnAndRunGps()
        } else {
            isGpsStatusError = true
            showOpenLocationSettingsAction = false
            gpsStatusMessage = "Allow location permission or search area."
        }
    }

    val triggerGpsOrRequestPermission: () -> Unit = {
        if (GpsLocationHelper.hasLocationPermission(context)) {
            ensureLocationOnAndRunGps()
        } else {
            gpsStatusMessage = "Requesting permission..."
            isGpsStatusError = false
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val distanceKm = HajipurLocations.calculateDistanceKm(
        HajipurLocations.KITCHEN_LAT,
        HajipurLocations.KITCHEN_LNG,
        pickedLat,
        pickedLng
    )
    val isWithin10KmRange = HajipurLocations.isWithinDeliveryRange(distanceKm)
    val estMins = HajipurLocations.estimateMinutesForDistance(distanceKm, OrderStatus.CONFIRMED)
    val (liveAreaName, liveAreaSubtitle) = resolveLocationAddressFromCoordinates(pickedLat, pickedLng)
    val displayTitle = landmarkInput.ifBlank { liveAreaName }
    val displaySubtitle = addressInput.ifBlank { liveAreaSubtitle }

    val formScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .testTag("customer_live_location_dialog"),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                // 1. Clean Top Bar with compact distance pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (showSearchSheet) {
                                showSearchSheet = false
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
                        )
                    }
                    Text(
                        text = "Select Location",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = if (isWithin10KmRange) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isWithin10KmRange) VegGreenBright else CrushRed
                        ),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("location_picker_range_banner")
                    ) {
                        Text(
                            text = if (isWithin10KmRange) "$distanceKm km" else "$distanceKm km (>10 km)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isWithin10KmRange) Color(0xFF15803D) else CrushRed,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // 2. Search Bar + 1-Tap Current Location Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search Location",
                                tint = CrushRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search area or road...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6B7280),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        if (it.isNotBlank()) showSearchSheet = true
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF111827),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    cursorBrush = SolidColor(CrushRed),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("live_location_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (showSearchSheet) "Map" else "Areas",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = CrushRed,
                                modifier = Modifier
                                    .clickable { showSearchSheet = !showSearchSheet }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = triggerGpsOrRequestPermission,
                        enabled = !isDetectingGps,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, CrushRed),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFF1F2),
                            contentColor = CrushRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("detect_live_gps_btn")
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CrushRed,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = CrushRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isDetectingGps) "Locating..." else "Use Current Location",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Compact GPS status line (shown only when active)
                if (!gpsStatusMessage.isNullOrBlank()) {
                    val bannerBg = if (isGpsStatusError) Color(0xFFFFF1F2) else Color(0xFFECFDF5)
                    val bannerTint = if (isGpsStatusError) CrushRed else Color(0xFF047857)
                    val bannerTextColor = if (isGpsStatusError) Color(0xFF991B1B) else Color(0xFF065F46)
                    Surface(
                        color = bannerBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .testTag("gps_status_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GpsFixed,
                                contentDescription = null,
                                tint = bannerTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = gpsStatusMessage.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = bannerTextColor,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (showOpenLocationSettingsAction) {
                                Text(
                                    text = "Turn ON",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrushRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .clickable {
                                            runCatching {
                                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Search Results List OR Scrollable Map + Minimal Details
                if (showSearchSheet) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSearchingLocations) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = CrushRed,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Searching...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                            }
                        }

                        if (searchQuery.isNotBlank() && searchResults.isEmpty() && !isSearchingLocations) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            landmarkInput = searchQuery.trim()
                                            addressInput = "${searchQuery.trim()}, Hajipur"
                                            recentSearches.add(
                                                0,
                                                HajipurLandmark(
                                                    searchQuery.trim(),
                                                    "${searchQuery.trim()}, Hajipur",
                                                    pickedLat,
                                                    pickedLng
                                                )
                                            )
                                            showSearchSheet = false
                                        },
                                    color = Color(0xFFFFF1F2),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CrushRed)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CrushRed)
                                        Text(
                                            text = "Use \"${searchQuery.trim()}\" on Map",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CrushRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (currentAddress.isNotBlank() && searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = "Saved",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            addressInput = currentAddress
                                            landmarkInput = currentLandmark.ifBlank { "Saved" }
                                            pickedLat = currentLat
                                            pickedLng = currentLng
                                            showSearchSheet = false
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = CrushRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentLandmark.ifBlank { "Saved Address" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFF111827),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = currentAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "$distanceKm km",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                            }
                        }

                        if (recentSearches.isNotEmpty() && searchQuery.isBlank()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recent",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFF6B7280),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrushRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { recentSearches.clear() }
                                    )
                                }
                            }
                            items(recentSearches, key = { "recent_${it.name}_${it.lat}" }) { recent ->
                                val dist = HajipurLocations.calculateDistanceKm(
                                    HajipurLocations.KITCHEN_LAT,
                                    HajipurLocations.KITCHEN_LNG,
                                    recent.lat,
                                    recent.lng
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pickedLat = recent.lat
                                            pickedLng = recent.lng
                                            landmarkInput = recent.name
                                            addressInput = recent.addressLine
                                            isGpsStatusError = !HajipurLocations.isWithinDeliveryRange(dist)
                                            gpsStatusMessage = "${recent.name} ($dist km)"
                                            showSearchSheet = false
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = recent.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$dist km",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }

                        if (searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = "Popular Areas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        items(
                            items = searchResults,
                            key = { "res_${it.title}_${it.lat}_${it.lng}" }
                        ) { loc ->
                            val dist = loc.distanceKm
                            val inRange = loc.isWithin10KmRange
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        pickedLat = loc.lat
                                        pickedLng = loc.lng
                                        landmarkInput = loc.title
                                        addressInput = loc.subtitle
                                        if (recentSearches.none { it.name.equals(loc.title, ignoreCase = true) }) {
                                            recentSearches.add(
                                                0,
                                                HajipurLandmark(loc.title, loc.subtitle, loc.lat, loc.lng)
                                            )
                                        }
                                        isGpsStatusError = !inRange
                                        gpsStatusMessage = if (inRange) {
                                            "${loc.title} ($dist km)"
                                        } else {
                                            "Outside 10 km range ($dist km)"
                                        }
                                        searchQuery = ""
                                        showSearchSheet = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = if (inRange) CrushRed else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = loc.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = loc.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "$dist km",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (inRange) Color(0xFF15803D) else CrushRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(formScrollState)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            HajipurLiveMap(
                                customerLat = pickedLat,
                                customerLng = pickedLng,
                                estimatedMinutes = estMins,
                                distanceKm = distanceKm,
                                interactivePicker = true,
                                mapHeight = 180.dp,
                                onDetectGpsClick = triggerGpsOrRequestPermission,
                                onLocationPicked = { lat, lng, addr ->
                                    pickedLat = lat
                                    pickedLng = lng
                                    val (areaTitle, _) = resolveLocationAddressFromCoordinates(lat, lng)
                                    landmarkInput = areaTitle
                                    addressInput = addr
                                    val pinDist = HajipurLocations.calculateDistanceKm(
                                        HajipurLocations.KITCHEN_LAT,
                                        HajipurLocations.KITCHEN_LNG,
                                        lat,
                                        lng
                                    )
                                    isGpsStatusError = !HajipurLocations.isWithinDeliveryRange(pinDist)
                                    gpsStatusMessage = if (HajipurLocations.isWithinDeliveryRange(pinDist)) {
                                        null
                                    } else {
                                        "Outside 10 km ($pinDist km)"
                                    }
                                }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = CrushRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = displayTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF111827),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = displaySubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = "Change",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CrushRed,
                                    modifier = Modifier
                                        .clickable { showSearchSheet = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val fieldColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrushRed,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedLabelColor = CrushRed,
                                unfocusedLabelColor = Color(0xFF64748B)
                            )

                            AddressLabelSelectorRow(
                                selectedLabel = selectedAddressLabel,
                                onSelectLabel = { selectedAddressLabel = it }
                            )

                            val hasFullDetailsCallback = onSaveFullCustomerDetails != null || onSaveFullCustomerDetailsWithLabel != null
                            if (hasFullDetailsCallback) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = {
                                        nameInput = it
                                        validationError = null
                                    },
                                    label = { Text("Name") },
                                    placeholder = { Text("Your name") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Name",
                                            tint = CrushRed
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("live_location_name_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors
                                )

                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = {
                                        phoneInput = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' }.take(15)
                                        validationError = null
                                    },
                                    label = { Text("Phone") },
                                    placeholder = { Text("10-digit mobile") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = "Phone",
                                            tint = CrushRed
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("live_location_phone_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors
                                )
                            }

                            OutlinedTextField(
                                value = addressInput,
                                onValueChange = {
                                    addressInput = it
                                    validationError = null
                                },
                                label = { Text("Address") },
                                placeholder = { Text("House / Road / Area") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = "Address",
                                        tint = CrushRed
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("live_location_address_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )

                            OutlinedTextField(
                                value = landmarkInput,
                                onValueChange = {
                                    landmarkInput = it
                                    validationError = null
                                },
                                label = { Text("Landmark") },
                                placeholder = { Text("Nearby landmark") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.MyLocation,
                                        contentDescription = "Landmark",
                                        tint = CrushRed
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("live_location_landmark_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Pinned Sticky Bottom Bar so "Confirm Location" is ALWAYS visible & tappable (never hidden behind nav bar or cut off!)
                    Surface(
                        color = Color.White,
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val hasFullDetailsCallback =
                            onSaveFullCustomerDetails != null || onSaveFullCustomerDetailsWithLabel != null
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!validationError.isNullOrBlank()) {
                                Text(
                                    text = validationError.orEmpty(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CrushRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isWithin10KmRange) {
                                        validationError = "Delivery only within 10 km ($distanceKm km)"
                                        return@Button
                                    }
                                    val cleanName = nameInput.trim()
                                    val cleanDigits = phoneInput.filter { it.isDigit() }
                                    val finalAddress = addressInput.trim().ifBlank { displaySubtitle }
                                    val finalLandmark = landmarkInput.trim().ifBlank { displayTitle }

                                    if (hasFullDetailsCallback) {
                                        if (cleanName.length < 2) {
                                            validationError = "Enter your name"
                                            return@Button
                                        }
                                        if (cleanDigits.length < 10) {
                                            validationError = "Enter 10-digit phone"
                                            return@Button
                                        }
                                        if (finalAddress.length < 4) {
                                            validationError = "Enter address"
                                            return@Button
                                        }
                                        if (finalLandmark.length < 2) {
                                            validationError = "Enter landmark"
                                            return@Button
                                        }
                                        if (onSaveFullCustomerDetailsWithLabel != null) {
                                            onSaveFullCustomerDetailsWithLabel(
                                                cleanName,
                                                phoneInput.trim(),
                                                pickedLat,
                                                pickedLng,
                                                finalAddress,
                                                finalLandmark,
                                                selectedAddressLabel
                                            )
                                        } else {
                                            onSaveFullCustomerDetails?.invoke(
                                                cleanName,
                                                phoneInput.trim(),
                                                pickedLat,
                                                pickedLng,
                                                finalAddress,
                                                finalLandmark
                                            )
                                        }
                                    } else if (onSaveLocationWithLabel != null) {
                                        onSaveLocationWithLabel(
                                            pickedLat,
                                            pickedLng,
                                            finalAddress,
                                            finalLandmark,
                                            selectedAddressLabel
                                        )
                                    } else {
                                        onSaveLocation(pickedLat, pickedLng, finalAddress, finalLandmark)
                                    }
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWithin10KmRange) CrushRed else Color(0xFF6B7280),
                                    contentColor = CrushWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("save_live_location_btn")
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isWithin10KmRange) "Confirm Location" else "Outside 10 km Range",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

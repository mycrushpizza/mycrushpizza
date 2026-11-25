package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycrushpizza.`in`.R
import com.example.data.model.AddressLabel
import com.example.data.model.HajipurLocations
import com.example.data.model.MenuItem
import com.example.data.model.PizzaCategory
import com.example.data.model.PizzaSize
import com.example.data.model.formatInr
import com.example.ui.components.CustomerLiveLocationDialog
import com.example.ui.components.OFFICIAL_STORE_PHONE_DISPLAY
import com.example.ui.components.OnlineFoodImage
import com.example.ui.components.StoreContactHelper
import com.example.ui.components.VegNonVegIndicator
import com.example.ui.components.resolveFoodDrawable
import com.example.ui.theme.CrushBlack
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.ui.viewmodel.MainAppUiState
import com.example.util.FuzzyFoodSearch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data model for the 4 Best Pizza Offer Cards shown inside the Domino's-style Festive Top Hero Section.
 */
data class BestPizzaOfferCardItem(
    val id: String,
    val title: String,
    val priceNumber: String,
    val targetCategory: String,
    val imageKey: String,
    val couponCodeToApply: String? = null
)

private val dominoStyleBestOfferCards: List<BestPizzaOfferCardItem> = listOf(
    BestPizzaOfferCardItem(
        id = "menu_99",
        title = "Menu@",
        priceNumber = "99",
        targetCategory = FuzzyFoodSearch.CATEGORY_MENU_99,
        imageKey = "peri_peri_fries"
    ),
    BestPizzaOfferCardItem(
        id = "regular_89",
        title = "Regular Pizzas@",
        priceNumber = "89",
        targetCategory = FuzzyFoodSearch.CATEGORY_REGULAR_PIZZA_89,
        imageKey = "pizza_teaser"
    ),
    BestPizzaOfferCardItem(
        id = "maxxx_239",
        title = "Maxxx Deals",
        priceNumber = "239",
        targetCategory = "Special Pizza",
        imageKey = "feast_combo"
    ),
    BestPizzaOfferCardItem(
        id = "cheesy_249",
        title = "Cheesy Pizzas@",
        priceNumber = "249",
        targetCategory = "Cheese Pizza",
        imageKey = "crush_special"
    )
)

/**
 * Data model for the 12 Domino's-style "What are you craving for?" scalloped golden medallions.
 */
data class CravingMedallionItem(
    val id: String,
    val label: String,
    val targetCategory: String,
    val imageKey: String,
    val topRibbonBadge: String? = null,
    val showBottomVegMark: Boolean = false,
    val couponCodeToApply: String? = null
)

private val dominoStyleCravingItems: List<CravingMedallionItem> = listOf(
    CravingMedallionItem(
        id = "2in1_cheese_burst",
        label = "My Crush Special",
        targetCategory = "Cheese Pizza",
        imageKey = "crush_special",
        topRibbonBadge = "NEW"
    ),
    CravingMedallionItem(
        id = "veg_pizza",
        label = "Veg Pizza",
        targetCategory = "Veg Pizza",
        imageKey = "peppy_paneer",
        showBottomVegMark = true
    ),
    CravingMedallionItem(
        id = "pizza_mania",
        label = "Pizza Teasers",
        targetCategory = "Veg Pizza",
        imageKey = "pizza_teaser"
    ),
    CravingMedallionItem(
        id = "combos",
        label = "Combos",
        targetCategory = "Combos",
        imageKey = "feast_combo"
    ),
    CravingMedallionItem(
        id = "big_big_pizza",
        label = "Kulhad Pizza",
        targetCategory = "Kulhad Pizza",
        imageKey = "kulhad_pizza"
    ),
    CravingMedallionItem(
        id = "cheese_volcano",
        label = "Cheese Burst",
        targetCategory = "Cheese Pizza",
        imageKey = "cheese_pizza"
    ),
    CravingMedallionItem(
        id = "garlic_breads_dips",
        label = "Garlic Bread & Parcel",
        targetCategory = "Garlic Bread",
        imageKey = "zingy_garlic"
    ),
    CravingMedallionItem(
        id = "deals",
        label = "Burgers & Sandwich",
        targetCategory = "Burgers",
        imageKey = "grilled_sandwich",
        topRibbonBadge = "HOT DEAL"
    ),
    CravingMedallionItem(
        id = "royal_feast",
        label = "Tandoori & Tikka",
        targetCategory = "Special Pizza",
        imageKey = "tandoori_kabab"
    ),
    CravingMedallionItem(
        id = "choco_lava_desserts",
        label = "Shakes & Choco Lava",
        targetCategory = "Beverages",
        imageKey = "shakes_lava"
    ),
    CravingMedallionItem(
        id = "beverages",
        label = "Handi & Biryani",
        targetCategory = "Combos",
        imageKey = "paneer_handi"
    ),
    CravingMedallionItem(
        id = "burgers_pasta",
        label = "Maggi & Chinese",
        targetCategory = "Pasta",
        imageKey = "cheese_maggi"
    )
)

@Composable
fun CustomerHomeScreen(
    uiState: MainAppUiState,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleVegFilter: () -> Unit,
    onAddToCart: (MenuItem, PizzaSize) -> Unit,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onGoToCart: () -> Unit,
    onUpdatePickedLocation: (Double, Double, String, String) -> Unit = { _, _, _, _ -> },
    onSaveFullCustomerDetails: (String, String, Double, Double, String, String) -> Unit = { _, _, lat, lng, addr, landmark ->
        onUpdatePickedLocation(lat, lng, addr, landmark)
    },
    onSaveFullCustomerDetailsWithLabel: ((String, String, Double, Double, String, String, AddressLabel) -> Unit)? = null,
    onHomeScrollStateChanged: (Boolean) -> Unit = {},
    scrollToTopTrigger: Int = 0,
    onAddRecentSearch: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showLiveLocationDialog by remember { mutableStateOf(false) }
    var isDedicatedSearchOpen by remember { mutableStateOf(false) }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 45
        }
    }

    LaunchedEffect(isScrolled) {
        onHomeScrollStateChanged(isScrolled)
    }

    val filteredMenu = remember(
        uiState.menuItems,
        uiState.selectedCategory,
        uiState.searchQuery,
        uiState.vegOnlyFilter
    ) {
        FuzzyFoodSearch.filterAndRank(
            items = uiState.menuItems,
            selectedCategory = uiState.selectedCategory,
            rawQuery = uiState.searchQuery,
            vegOnly = uiState.vegOnlyFilter
        )
    }

    if (showLiveLocationDialog) {
        CustomerLiveLocationDialog(
            currentAddress = uiState.checkoutAddress,
            currentLandmark = uiState.checkoutLandmark,
            currentLat = uiState.checkoutLat,
            currentLng = uiState.checkoutLng,
            currentName = uiState.checkoutName,
            currentPhone = uiState.checkoutPhone,
            currentAddressLabel = uiState.checkoutAddressLabel,
            onSaveLocation = { lat, lng, addr, landmark ->
                onUpdatePickedLocation(lat, lng, addr, landmark)
            },
            onSaveFullCustomerDetails = { name, phone, lat, lng, addr, landmark ->
                onSaveFullCustomerDetails(name, phone, lat, lng, addr, landmark)
            },
            onSaveFullCustomerDetailsWithLabel = onSaveFullCustomerDetailsWithLabel,
            onDismiss = { showLiveLocationDialog = false }
        )
    }

    // When user clicks on Search or types a search query, open ONLY the Dedicated Search View!
    if (isDedicatedSearchOpen || uiState.searchQuery.isNotEmpty()) {
        DedicatedFoodSearchScreen(
            uiState = uiState,
            filteredMenu = filteredMenu,
            onSearchQueryChange = onSearchQueryChange,
            onAddRecentSearch = onAddRecentSearch,
            onRemoveRecentSearch = onRemoveRecentSearch,
            onClearRecentSearches = onClearRecentSearches,
            onToggleVegFilter = onToggleVegFilter,
            onAddToCart = onAddToCart,
            onUpdateCartQuantity = onUpdateCartQuantity,
            onGoToCart = onGoToCart,
            onCloseSearch = {
                isDedicatedSearchOpen = false
                onSearchQueryChange("")
            },
            modifier = modifier
        )
        return
    }

    val isCombosMode = uiState.selectedCategory.equals("Combos", ignoreCase = true)

    LaunchedEffect(uiState.selectedCategory) {
        if (isCombosMode) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("customer_home_lazy_column"),
            contentPadding = PaddingValues(
                bottom = 74.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.storeConfig.isOpen) {
                item(key = "home_store_closed_banner") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("customer_home_store_closed_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.5.dp, CrushRed)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = "Store Closed",
                                tint = CrushRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "STORE CURRENTLY CLOSED • ORDERS PAUSED",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "You can browse all pizzas & add items to your cart now. Ordering will resume once the store opens (${uiState.storeConfig.openTime} – ${uiState.storeConfig.closeTime}).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }
            }

            if (isCombosMode) {
                // Dedicated Combos & Party Meals Header shown immediately at the top when "Combos" is selected!
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("combos_mode_banner"),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = CrushRed,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "SAVE UP TO ₹250 ON COMBOS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "My Crush Combos & Meal Platters",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Pizza + Drink + Garlic Bread Combos, Dal, Tandoori Naan, Paratha & Accompaniments",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onCategorySelected("All") },
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CrushRed)
                                ) {
                                    Text(
                                        text = "Full Menu",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrushRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Quick Add Featured Pizza & Drink Value Combos
                            val featuredComboItems = remember(uiState.menuItems) {
                                val specials = uiState.menuItems.filter {
                                    it.title.contains("My Crush", ignoreCase = true) ||
                                        it.title.contains("Farmhouse", ignoreCase = true) ||
                                        it.title.contains("Garlic", ignoreCase = true) ||
                                        it.title.contains("Lava", ignoreCase = true)
                                }.take(3)
                                specials
                            }
                            if (featuredComboItems.isNotEmpty()) {
                                Text(
                                    text = "BESTSELLER PIZZA & SIDE VALUE DEALS:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(featuredComboItems, key = { "feat_combo_${it.id}" }) { dish ->
                                        val comboCartKey = "${dish.id}_${PizzaSize.REGULAR.name}"
                                        val comboQty = uiState.cartItems.find { it.key == comboCartKey }?.quantity ?: 0
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.width(216.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                OnlineFoodImage(
                                                    imageKey = dish.imageKey,
                                                    imageUrl = dish.imageUrl,
                                                    contentDescription = dish.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(96.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                )
                                                Text(
                                                    text = dish.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color(0xFF111827),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Combo Price: ${formatInr(dish.priceRegularInr)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = CrushRed,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (comboQty == 0) {
                                                    Button(
                                                        onClick = { onAddToCart(dish, PizzaSize.REGULAR) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = CrushRed,
                                                            contentColor = Color.White
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(38.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Add,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "ADD COMBO",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                } else {
                                                    Surface(
                                                        color = CrushRed,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(38.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxSize(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            IconButton(
                                                                onClick = { onUpdateCartQuantity(comboCartKey, -1) },
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Remove,
                                                                    contentDescription = "Decrease",
                                                                    tint = Color.White
                                                                )
                                                            }
                                                            Text(
                                                                text = "$comboQty ADDED",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                maxLines = 1
                                                            )
                                                            IconButton(
                                                                onClick = { onUpdateCartQuantity(comboCartKey, 1) },
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Add,
                                                                    contentDescription = "Increase",
                                                                    tint = Color.White
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
                }
            } else {
                // 1. Festive Crimson-Gold Hero Section with Top Gap, Live Delivery Header, Search Bar & Offer Cards
                item {
                    DominoStyleFestiveTopHeroSection(
                        storeName = uiState.storeConfig.storeName,
                        storePhone = uiState.storeConfig.storePhone.ifBlank { OFFICIAL_STORE_PHONE_DISPLAY },
                        currentName = uiState.checkoutName,
                        currentPhone = uiState.checkoutPhone,
                        currentAddress = uiState.checkoutAddress,
                        currentLandmark = uiState.checkoutLandmark,
                        onOpenLiveLocationPicker = { showLiveLocationDialog = true },
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onOpenDedicatedSearch = { isDedicatedSearchOpen = true },
                        vegOnlyFilter = uiState.vegOnlyFilter,
                        onToggleVegFilter = onToggleVegFilter,
                        onOfferCardClicked = { offer ->
                            if (offer.couponCodeToApply != null) {
                                onApplyCoupon(offer.couponCodeToApply)
                            }
                            onCategorySelected(offer.targetCategory)
                            val targetMenuIndex = if (uiState.storeConfig.isOpen) 2 else 3
                            coroutineScope.launch {
                                listState.animateScrollToItem(targetMenuIndex)
                            }
                        }
                    )
                }

                // 2. "What are you craving for?" 3x4 Scalloped Golden Medallion Grid
                item {
                    WhatAreYouCravingSection(
                        selectedCategory = uiState.selectedCategory,
                        onCravingClicked = { craving ->
                            if (craving.couponCodeToApply != null) {
                                onApplyCoupon(craving.couponCodeToApply)
                            }
                            onCategorySelected(craving.targetCategory)
                            val targetMenuIndex = if (uiState.storeConfig.isOpen) 3 else 4
                            coroutineScope.launch {
                                listState.animateScrollToItem(targetMenuIndex)
                            }
                        },
                        onScrollToDishes = {
                            val targetMenuIndex = if (uiState.storeConfig.isOpen) 3 else 4
                            coroutineScope.launch {
                                listState.animateScrollToItem(targetMenuIndex)
                            }
                        }
                    )
                }
            }

            // 4. Horizontal Category Filter Pills (Coupons removed as requested!)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Pills Row (includes active special filters like Regular Pizza ₹89, Menu ₹99, Kulhad Pizza)
                    val baseCategories = listOf("All") + PizzaCategory.entries.map { it.id }
                    val categories = when {
                        uiState.selectedCategory.equals(FuzzyFoodSearch.CATEGORY_REGULAR_PIZZA_89, ignoreCase = true) -> {
                            listOf("All", FuzzyFoodSearch.CATEGORY_REGULAR_PIZZA_89) + PizzaCategory.entries.map { it.id }
                        }
                        uiState.selectedCategory.equals(FuzzyFoodSearch.CATEGORY_MENU_99, ignoreCase = true) -> {
                            listOf("All", FuzzyFoodSearch.CATEGORY_MENU_99) + PizzaCategory.entries.map { it.id }
                        }
                        uiState.selectedCategory.equals("Kulhad Pizza", ignoreCase = true) -> {
                            listOf("All", "Kulhad Pizza") + PizzaCategory.entries.map { it.id }
                        }
                        else -> baseCategories
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories, key = { it }) { category ->
                            val selected = uiState.selectedCategory.equals(category, ignoreCase = true)
                            FilterChip(
                                selected = selected,
                                onClick = { onCategorySelected(category) },
                                modifier = Modifier.testTag("category_chip_$category"),
                                label = {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrushRed,
                                    selectedLabelColor = CrushWhite,
                                    containerColor = Color(0xFFF6F6FA),
                                    labelColor = Color(0xFF222226)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Color(0xFFE2E2EA),
                                    selectedBorderColor = CrushRed
                                )
                            )
                        }
                    }
                }
            }

            // 5. Active Category Menu Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Hot Menu",
                            tint = CrushRed
                        )
                        Text(
                            text = if (uiState.selectedCategory == "All") {
                                "HAJIPUR HOTSELLERS & MENU (${filteredMenu.size})"
                            } else {
                                "${uiState.selectedCategory.uppercase()} (${filteredMenu.size})"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1E1E24),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (uiState.selectedCategory != "All") {
                        Text(
                            text = "Show All",
                            style = MaterialTheme.typography.labelLarge,
                            color = CrushRed,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable { onCategorySelected("All") }
                        )
                    }
                }
            }

            // 6. Menu Product Cards (Explore All Pizzas section removed from bottom as requested!)
            items(filteredMenu, key = { it.id }) { menuItem ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PizzaMenuItemCard(
                        menuItem = menuItem,
                        cartItems = uiState.cartItems,
                        onAddToCart = onAddToCart,
                        onUpdateCartQuantity = onUpdateCartQuantity
                    )
                }
            }
        }

        // Sticky Frosted-Blur Location & Search Bar when user scrolls down!
        if (isScrolled && !isCombosMode) {
            val localityTitle = if (uiState.checkoutAddress.isNotBlank()) {
                if (uiState.isCheckoutWithin10KmRange) {
                    uiState.checkoutLandmark.ifBlank { "My Crush Pizza Hajipur" }
                } else {
                    "Above 10 km • Not Available"
                }
            } else {
                "My Crush Pizza Hajipur"
            }
            Surface(
                color = Color.Transparent,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .testTag("sticky_frosted_blur_header")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Blurred Frosted Glass Backdrop
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(18.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xD96B0928),
                                        Color(0xD98E1132),
                                        Color(0xD9A51438)
                                    )
                                )
                            )
                    )
                    // Translucent Frosted Glass Sheen + Content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left: Compact Frosted Location Pill
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showLiveLocationDialog = true },
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFFB300),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "30-45M",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = Color(0xFF4A0418),
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Delivery at $localityTitle",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Change Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Right: Compact Frosted Search Trigger Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isDedicatedSearchOpen = true },
                            color = Color.White.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFFE31837),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1F1F24),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Single Compact Bottom Bar:
        // When cart is empty -> Shows the slim Free Delivery Promo Strip.
        // When cart has items -> Shows ONLY a single sleek, compact Checkout Bar.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (uiState.cartCount > 0) {
                CompactBottomCheckoutBar(
                    cartCount = uiState.cartCount,
                    grandTotalInr = uiState.grandTotalInr,
                    subtotalInr = uiState.subtotalInr,
                    freeDeliveryMinOrderInr = uiState.storeConfig.freeDeliveryMinOrderInr,
                    onGoToCart = onGoToCart
                )
            } else {
                DominoStyleFreeDeliveryBottomStrip(
                    subtotalInr = uiState.subtotalInr,
                    freeDeliveryMinOrderInr = uiState.storeConfig.freeDeliveryMinOrderInr
                )
            }
        }
    }
}

/**
 * Dedicated Full-Screen Search View opened when the user clicks on the Search Bar.
 * Shows ONLY the Search Bar, Quick Popular Search Chips, Smart Typo/Fuzzy Match Suggestions,
 * and the matching food items list without any home banners or category grids.
 */
@Composable
private fun DedicatedFoodSearchScreen(
    uiState: MainAppUiState,
    filteredMenu: List<MenuItem>,
    onSearchQueryChange: (String) -> Unit,
    onAddRecentSearch: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onToggleVegFilter: () -> Unit,
    onAddToCart: (MenuItem, PizzaSize) -> Unit,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onGoToCart: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchListState = rememberLazyListState()
    val hasActiveQuery = uiState.searchQuery.isNotBlank()
    val activeQueryKey = remember(uiState.searchQuery, uiState.vegOnlyFilter) {
        "${uiState.searchQuery.trim().lowercase()}|${uiState.vegOnlyFilter}"
    }
    val popularQuickSearches = remember {
        listOf(
            "Pizza",
            "Cheese Maggi",
            "Veg Maggi",
            "Kulhad Pizza",
            "Garlic Bread",
            "Coldrink",
            "Kit-Kat Shake",
            "Banana Shake",
            "White Sponges",
            "Mineral Water",
            "Paneer",
            "Biryani",
            "Burger"
        )
    }
    val didYouMean = remember(uiState.searchQuery, filteredMenu) {
        FuzzyFoodSearch.didYouMeanSuggestion(uiState.searchQuery, filteredMenu)
    }

    BackHandler {
        onCloseSearch()
    }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    // Always snap immediately to the #1 top matching food item whenever search text, veg filter, or top match changes
    LaunchedEffect(activeQueryKey, filteredMenu.firstOrNull()?.id) {
        searchListState.scrollToItem(0)
    }

    // Automatically record finished searches (debounced so partial keystrokes aren't saved)
    LaunchedEffect(uiState.searchQuery) {
        val trimmed = uiState.searchQuery.trim()
        if (trimmed.length >= 2) {
            delay(1200L)
            if (filteredMenu.isNotEmpty()) {
                onAddRecentSearch(trimmed)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("dedicated_search_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Dedicated Search Header
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = if (hasActiveQuery) 8.dp else 12.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(if (hasActiveQuery) 6.dp else 10.dp)
                ) {
                    if (!hasActiveQuery) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onCloseSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("search_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Menu",
                                tint = Color(0xFF1F1F24)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            color = Color(0xFFF4F4F8),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, CrushRed)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = CrushRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search any dish (even with spelling mistakes)...",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                            color = Color(0xFF686870),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = uiState.searchQuery,
                                        onValueChange = onSearchQueryChange,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                val trimmed = uiState.searchQuery.trim()
                                                if (trimmed.length >= 2) {
                                                    onAddRecentSearch(trimmed)
                                                }
                                                keyboardController?.hide()
                                            }
                                        ),
                                        textStyle = TextStyle(
                                            color = Color(0xFF1A1A1A),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        cursorBrush = SolidColor(CrushRed),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .testTag("search_food_input")
                                    )
                                }
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("clear_search_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Clear Search",
                                            tint = Color(0xFF75757A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Pure Veg filter pill inside Search
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleVegFilter() }
                                .testTag("veg_filter_chip"),
                            color = if (uiState.vegOnlyFilter) Color(0xFFE8F5E9) else Color(0xFFF4F4F8),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (uiState.vegOnlyFilter) Color(0xFF2E7D32) else Color(0xFFDADCE0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = "Pure Veg",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Veg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.vegOnlyFilter) Color(0xFF1B5E20) else Color(0xFF3A3A40),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Recent Searches Section (Shown when search query is empty & history exists)
                    if (uiState.recentSearches.isNotEmpty() && uiState.searchQuery.isBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recent_searches_section"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = "Recent Searches",
                                        tint = CrushRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Recent Searches",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFF1E1E24),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "Clear all",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CrushRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onClearRecentSearches() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("clear_all_recent_searches_btn")
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recent_searches_row")
                            ) {
                                items(uiState.recentSearches, key = { "recent_$it" }) { recent ->
                                    Surface(
                                        color = Color(0xFFFFF5F6),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, CrushRed.copy(alpha = 0.28f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable {
                                                onSearchQueryChange(recent)
                                                onAddRecentSearch(recent)
                                            }
                                            .testTag("recent_search_chip_$recent")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = null,
                                                tint = CrushRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = recent,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF1F1F24),
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .clickable { onRemoveRecentSearch(recent) }
                                                    .testTag("remove_recent_search_$recent"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Remove $recent",
                                                    tint = Color(0xFF75757A),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Popular Quick-Tap Search Chips (shown only before typing so top food results sit immediately below search bar!)
                    if (!hasActiveQuery) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("popular_search_chips_row")
                        ) {
                            items(popularQuickSearches, key = { it }) { chip ->
                                val selected = uiState.searchQuery.equals(chip, ignoreCase = true)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onSearchQueryChange(chip)
                                        onAddRecentSearch(chip)
                                    },
                                    modifier = Modifier.testTag("search_chip_$chip"),
                                    label = {
                                        Text(
                                            text = chip,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrushRed,
                                        selectedLabelColor = CrushWhite,
                                        containerColor = Color(0xFFF6F6FA),
                                        labelColor = Color(0xFF222226)
                                    )
                                )
                            }
                        }
                    }

                    // Typo-Tolerant Smart Suggestion Banner
                    if (didYouMean != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fuzzy_match_suggestion_banner"),
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFC107))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Smart Match for \"${uiState.searchQuery}\": Showing $didYouMean & similar (${filteredMenu.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF5D4037),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else if (hasActiveQuery) {
                        Text(
                            text = "Top ${filteredMenu.size} matching dishes for \"${uiState.searchQuery.trim()}\"",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrushRed,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Dedicated Search Results List ONLY — anchored strictly at index 0 so the #1 top food always shows first!
            LazyColumn(
                state = searchListState,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .testTag("customer_home_lazy_column"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredMenu.isEmpty() && hasActiveQuery) {
                    item(key = "empty_search_state") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF5F6),
                            border = BorderStroke(1.dp, CrushRed.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "No dishes found",
                                    tint = CrushRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "No matching dish for \"${uiState.searchQuery.trim()}\"",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF1F1F24),
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Try searching Pizza, Maggi, Burger, Shake, Paneer, or clear Pure Veg filter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMenu, key = { "$activeQueryKey|${it.id}" }) { menuItem ->
                        PizzaMenuItemCard(
                            menuItem = menuItem,
                            cartItems = uiState.cartItems,
                            onAddToCart = { item, size ->
                                val activeQuery = uiState.searchQuery.trim()
                                if (activeQuery.length >= 2) {
                                    onAddRecentSearch(activeQuery)
                                }
                                onAddToCart(item, size)
                            },
                            onUpdateCartQuantity = onUpdateCartQuantity
                        )
                    }
                }
            }
        }

        // Bottom Checkout Bar inside Dedicated Search
        if (uiState.cartCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                CompactBottomCheckoutBar(
                    cartCount = uiState.cartCount,
                    grandTotalInr = uiState.grandTotalInr,
                    subtotalInr = uiState.subtotalInr,
                    freeDeliveryMinOrderInr = uiState.storeConfig.freeDeliveryMinOrderInr,
                    onGoToCart = onGoToCart
                )
            }
        }
    }
}

/**
 * Domino's-Style Festive Crimson-Maroon-Gold Top Hero Section matching the reference screenshot:
 * - Festive hanging lights & marigold garlands (`toran`) + glowing lanterns
 * - "30 MINS" badge + "Delivery at Set Live Location" live GPS selector + Pure Veg & Call Store
 * - White rounded "Search for Garlic Bread" bar (tapping opens Dedicated Search-Only Screen!)
 * - "CELEBRATE Festive Joy - Lights of happiness with flavors of joy!"
 * - 2x2 Best Pizza Offer Cards Grid:
 *     1. Menu@ ₹89
 *     2. Regular Pizzas@ ₹179
 *     3. Maxxx Deals ₹239
 *     4. Cheesy Pizzas@ ₹249
 * - Bottom Maroon Strip "LOWEST PRICES ONLY ON MY CRUSH APP | FREE DELIVERY ABOVE ₹99" with white scalloped zig-zag edge
 */
@Composable
private fun DominoStyleFestiveTopHeroSection(
    storeName: String,
    storePhone: String,
    currentName: String,
    currentPhone: String,
    currentAddress: String,
    currentLandmark: String,
    onOpenLiveLocationPicker: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenDedicatedSearch: () -> Unit,
    vegOnlyFilter: Boolean,
    onToggleVegFilter: () -> Unit,
    onOfferCardClicked: (BestPizzaOfferCardItem) -> Unit
) {
    val hasLiveLocation = currentAddress.isNotBlank()
    val localityTitle = if (hasLiveLocation) {
        currentLandmark.ifBlank { "My Crush Pizza Hajipur" }
    } else {
        "My Crush Pizza Hajipur"
    }
    val localitySubtitle = if (hasLiveLocation) {
        val customerPrefix = listOf(currentName.trim(), currentPhone.trim())
            .filter { it.isNotBlank() }
            .joinToString(" • ")
        if (customerPrefix.isNotBlank()) {
            "$customerPrefix • $currentAddress"
        } else {
            currentAddress
        }
    } else {
        "10 km Delivery Range • Tap to set your delivery location"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dominos_festive_hero_section")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E0B28),
                        Color(0xFF7B0D2B),
                        Color(0xFFC81832),
                        Color(0xFFE6511B)
                    )
                )
            )
    ) {
        // Modern Oven-Baked Radial Sunburst Rays, Concentric Golden Rings & Sparkle Accents Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width * 0.5f
            val centerY = size.height * 0.43f
            val maxRadius = size.maxDimension * 0.85f

            // 1. Warm golden radial oven glow behind the centerpiece
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x44FFD54F),
                        Color(0x1FFF8F00),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = size.width * 0.62f
                ),
                radius = size.width * 0.62f,
                center = Offset(centerX, centerY)
            )

            // 2. Subtle geometric sunburst rays radiating from center
            val rayCount = 12
            val sweepAngle = 360f / (rayCount * 2)
            for (i in 0 until rayCount) {
                val startDeg = i * (360f / rayCount)
                val rad1 = Math.toRadians(startDeg.toDouble())
                val rad2 = Math.toRadians((startDeg + sweepAngle).toDouble())
                val rayPath = Path().apply {
                    moveTo(centerX, centerY)
                    lineTo(
                        centerX + (maxRadius * cos(rad1)).toFloat(),
                        centerY + (maxRadius * sin(rad1)).toFloat()
                    )
                    lineTo(
                        centerX + (maxRadius * cos(rad2)).toFloat(),
                        centerY + (maxRadius * sin(rad2)).toFloat()
                    )
                    close()
                }
                drawPath(
                    path = rayPath,
                    color = Color(0x12FFE082)
                )
            }

            // 3. Modern concentric golden orbit rings around the centerpiece
            drawCircle(
                color = Color(0x26FFD54F),
                radius = 96.dp.toPx(),
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = Color(0x18FFFFFF),
                radius = 148.dp.toPx(),
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // 4. Crisp corner ambient glow orbs & golden star-dots
            drawCircle(
                color = Color(0x24FFAB00),
                radius = 75.dp.toPx(),
                center = Offset(size.width * 0.12f, size.height * 0.18f)
            )
            drawCircle(
                color = Color(0x24FF6D00),
                radius = 85.dp.toPx(),
                center = Offset(size.width * 0.88f, size.height * 0.20f)
            )

            val sparkleOffsets = listOf(
                Offset(size.width * 0.10f, size.height * 0.38f),
                Offset(size.width * 0.18f, size.height * 0.45f),
                Offset(size.width * 0.82f, size.height * 0.44f),
                Offset(size.width * 0.90f, size.height * 0.37f)
            )
            sparkleOffsets.forEachIndexed { idx, pt ->
                val r = if (idx % 2 == 0) 4.dp.toPx() else 2.8.dp.toPx()
                drawCircle(
                    color = Color(0xCCFFE082),
                    radius = r,
                    center = pt
                )
                drawCircle(
                    color = Color(0x38FFE082),
                    radius = r * 2.4f,
                    center = pt
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Smooth top gap beneath the Crimson-Maroon header
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Clean Top Delivery Location Row (30 MINS glass pill + "Delivery at ..." + Pure Veg pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenLiveLocationPicker() }
                            .padding(vertical = 4.dp)
                            .testTag("hero_locality_selector"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left: Translucent Frosted Glass "30 MINS" Box (Matching Screenshot!)
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.38f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "30-45",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "MINS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }

                        // Middle: "Delivery at <Location> ⌄" + Subtitle below (No blue box, no MAP PIN badge!)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        ) {
                                            append("Delivery ")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        ) {
                                            append("at $localityTitle")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Change Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = localitySubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right side: Pure Veg filter pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onToggleVegFilter() }
                            .testTag("veg_filter_chip"),
                        color = if (vegOnlyFilter) Color(0xFFE8F5E9) else Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (vegOnlyFilter) Color(0xFF2E7D32) else Color.White.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "Pure Veg",
                                tint = if (vegOnlyFilter) Color(0xFF2E7D32) else Color(0xFFA5D6A7),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Pure Veg",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (vegOnlyFilter) Color(0xFF1B5E20) else Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // 2. Full-Width White Search Bar ("Search for Garlic Bread") — Clicking opens ONLY Dedicated Search!
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(14.dp))
                        .clickable { onOpenDedicatedSearch() }
                        .testTag("search_bar_trigger"),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1F1F24),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenDedicatedSearch() },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color(0xFF686870), fontWeight = FontWeight.Normal)) {
                                            append("Search for  ")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFF1F1F24), fontWeight = FontWeight.ExtraBold)) {
                                            append("Pizza, Maggi, Shake, Paneer...")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    onOpenDedicatedSearch()
                                    onSearchQueryChange(it)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color(0xFF1A1A1A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(CrushRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            onOpenDedicatedSearch()
                                        }
                                    }
                                    .testTag("search_food_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search",
                                    tint = Color(0xFF75757A)
                                )
                            }
                        }
                    }
                }

                // 3. Signature Pizzeria Centerpiece Banner (Replacing "Celebrate Festive Feast")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = Color(0x33FFD54F),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0x99FFD54F))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
                                contentDescription = "My Crush Pizza Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "FRESH FROM OVEN • HAJIPUR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    letterSpacing = 1.4.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFFFF8E1)
                            )
                        }
                    }
                    Text(
                        text = "Hot & Cheesy",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "100% Real Mozzarella • Loaded Toppings • 30-Min Delivery",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFFFECB3),
                        textAlign = TextAlign.Center
                    )
                }

                // 4. 2x2 Grid of Best Pizza Offer Cards (Menu@ ₹89, Regular Pizzas@ ₹179, Maxxx Deals ₹239, Cheesy Pizzas@ ₹249)
                val offerRows = remember { dominoStyleBestOfferCards.chunked(2) }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    offerRows.forEach { rowOffers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowOffers.forEach { offer ->
                                DominoBestOfferCard(
                                    offer = offer,
                                    onClick = { onOfferCardClicked(offer) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Bottom Maroon Ribbon Strip + White Scalloped Zig-Zag Edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF7B081D))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                                append("LOWEST PRICES ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xFFFFCDD2))) {
                                append("ONLY ON MY CRUSH APP | ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                                append("FREE DELIVERY ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xFFFFCDD2))) {
                                append("ABOVE ₹299")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )

                    // White Scalloped Zig-Zag Edge transitioning to white background below
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    ) {
                        val teethCount = 26
                        val toothWidth = size.width / teethCount
                        val path = Path().apply {
                            moveTo(0f, size.height)
                            for (i in 0 until teethCount) {
                                val startX = i * toothWidth
                                val midX = startX + toothWidth / 2f
                                val endX = startX + toothWidth
                                lineTo(midX, 0f)
                                lineTo(endX, size.height)
                            }
                            close()
                        }
                        drawPath(path = path, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Individual Best Pizza Offer Card inside the 2x2 Festive Hero Grid:
 * Warm dark-brown-to-amber card with thin golden border, title ("Menu@"), big price ("₹89"),
 * red circular arrow button, and realistic food photograph on the right.
 */
@Composable
private fun DominoBestOfferCard(
    offer: BestPizzaOfferCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(114.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("best_offer_card_${offer.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.75f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5D190E),
                            Color(0xFF7B2412),
                            Color(0xFFA93C18)
                        )
                    )
                )
        ) {
            // Right-side overflowing circular food photo (Bright & Clear)
            OnlineFoodImage(
                imageKey = offer.imageKey,
                contentDescription = offer.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(106.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = 4.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFFD54F), CircleShape)
            )

            // Left-side Offer Text & Red Arrow Pill
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 64.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFFFFE0B2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                baselineShift = BaselineShift.Superscript
                            )
                        ) {
                            append("₹")
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append(offer.priceNumber)
                        }
                    },
                    color = Color.White
                )

                // Red circular pill with white right arrow
                Surface(
                    color = Color(0xFFE50914),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(width = 30.dp, height = 20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Explore ${offer.title}",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slim, sleek single-line Checkout Bar when cart has items.
 */
@Composable
private fun CompactBottomCheckoutBar(
    cartCount: Int,
    grandTotalInr: Int,
    subtotalInr: Int,
    freeDeliveryMinOrderInr: Int,
    onGoToCart: () -> Unit
) {
    val minOrderShortfall = (com.example.ui.viewmodel.MIN_ORDER_AMOUNT_INR - subtotalInr).coerceAtLeast(0)
    val isFreeDelivery = subtotalInr >= freeDeliveryMinOrderInr
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("floating_view_cart_bar"),
        color = CrushRed,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onGoToCart() }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CrushYellow,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$cartCount ITEM${if (cartCount > 1) "S" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrushBlack,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatInr(grandTotalInr),
                        style = MaterialTheme.typography.titleMedium,
                        color = CrushWhite,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = when {
                            minOrderShortfall > 0 -> "Min ₹99 • Add ${formatInr(minOrderShortfall)} more"
                            isFreeDelivery -> "FREE Delivery Unlocked!"
                            else -> "Tap to Checkout"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = CrushYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onGoToCart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrushYellow,
                    contentColor = CrushBlack
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("go_to_checkout_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCartCheckout,
                    contentDescription = "View Cart",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "View Cart",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Domino's-Style "What are you craving for?" Section:
 * - White background
 * - Bold dark title "What are you craving for?"
 * - 3x4 Grid of 12 Circular Food Medallions with Scalloped Golden Sunburst Rings,
 *   "NEW" and "UPTO 50% OFF" Yellow Ribbon Badges, and Bottom Veg Mark
 */
@Composable
private fun WhatAreYouCravingSection(
    selectedCategory: String,
    onCravingClicked: (CravingMedallionItem) -> Unit,
    onScrollToDishes: () -> Unit
) {
    var activeCravingPage by remember { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 6.dp, bottom = 10.dp)
            .testTag("what_are_you_craving_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            // Bold Dark Heading: "What are you craving for?"
            Text(
                text = "What are you craving for?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color(0xFF1F1F24)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3x4 Grid of 12 Scalloped Golden Medallions (4 rows of 3 items)
            val rows = remember { dominoStyleCravingItems.chunked(3) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowItems.forEach { item ->
                            val isSelected = selectedCategory.equals(item.targetCategory, ignoreCase = true) &&
                                selectedCategory != "All"
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ScallopedGoldenMedallionCard(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = { onCravingClicked(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Circular White Collapse/Scroll Button (^) at Bottom-Left of the Grid
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 26.dp)
                .size(42.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .clickable {
                    activeCravingPage = if (activeCravingPage == 1) 2 else 1
                    onScrollToDishes()
                }
                .testTag("craving_floating_chevron_btn"),
            color = Color(0xFFF3F3F7),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Quick Scroll Menu",
                    tint = Color(0xFF2C2C34),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Single Circular Food Medallion with:
 * - Custom-drawn 28-point Scalloped Golden Sunburst Ring
 * - Circular High-Res Food Photograph inside a crisp inner ring
 * - Optional Top Curved Yellow Ribbon Badge ("NEW" or "UPTO 50% OFF")
 * - Optional Bottom Veg Icon ("Veg Pizza")
 * - Clean Dark Label below
 */
@Composable
private fun ScallopedGoldenMedallionCard(
    item: CravingMedallionItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
            .testTag("craving_medallion_${item.id}")
    ) {
        Box(
            modifier = Modifier.size(102.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Custom Scalloped Golden Sunburst Outer Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerR = size.minDimension / 2f
                val innerR = outerR * 0.89f
                val numPoints = 28

                val starPath = Path()
                for (i in 0 until numPoints * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = i * Math.PI / numPoints
                    val x = cx + (r * cos(angle)).toFloat()
                    val y = cy + (r * sin(angle)).toFloat()
                    if (i == 0) {
                        starPath.moveTo(x, y)
                    } else {
                        starPath.lineTo(x, y)
                    }
                }
                starPath.close()

                drawPath(
                    path = starPath,
                    brush = Brush.radialGradient(
                        colors = if (isSelected) {
                            listOf(Color(0xFFFFD54F), Color(0xFFE50914))
                        } else {
                            listOf(Color(0xFFFFF3CD), Color(0xFFF6B93B), Color(0xFFE58E26))
                        },
                        center = Offset(cx, cy),
                        radius = outerR
                    )
                )

                // Soft warm inner circle border
                drawCircle(
                    color = Color(0xFFFFF9EC),
                    radius = innerR * 0.96f,
                    center = Offset(cx, cy)
                )
            }

            // 2. Circular Food Image (Bright & Clear)
            OnlineFoodImage(
                imageKey = item.imageKey,
                contentDescription = item.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 1.2.dp,
                        color = if (isSelected) CrushRed else Color(0xFFFFD166),
                        shape = CircleShape
                    )
            )

            // 3. Optional Top Yellow Ribbon Badge ("NEW" or "UPTO 50% OFF")
            if (item.topRibbonBadge != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 5.dp),
                    color = Color(0xFFFFCA28),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF8D6E63))
                ) {
                    Text(
                        text = item.topRibbonBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (item.topRibbonBadge.length > 5) 8.5.sp else 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // 4. Optional Bottom Veg Mark Inside Medallion (for Veg Pizza)
            if (item.showBottomVegMark) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 11.dp)
                        .background(Color.White, RoundedCornerShape(3.dp))
                ) {
                    VegNonVegIndicator(isVeg = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Crisp Centered Dark Label Below Medallion
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = if (isSelected) CrushRed else Color(0xFF3A3A40),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Slim Sticky Bottom Promo Strip above the bottom navigation bar matching the Domino's screenshot:
 * "Lowest Prices on My Crush App & Free Delivery"
 * "Add items worth ₹99 to get FREE delivery!"
 */
@Composable
private fun DominoStyleFreeDeliveryBottomStrip(
    subtotalInr: Int,
    freeDeliveryMinOrderInr: Int
) {
    val remainingForFreeDelivery = (freeDeliveryMinOrderInr - subtotalInr).coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("free_delivery_promo_strip"),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE5E5EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                        )
                    )
                    .border(1.dp, CrushRed.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DeliveryDining,
                    contentDescription = "Free Delivery Scooter",
                    tint = CrushRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lowest Prices on My Crush App & Free Delivery",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFF1F1F24),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (subtotalInr == 0) {
                        "Add items worth ${formatInr(freeDeliveryMinOrderInr)} to get FREE delivery!"
                    } else if (remainingForFreeDelivery > 0) {
                        "Add items worth ${formatInr(remainingForFreeDelivery)} more to get FREE delivery!"
                    } else {
                        "Yay! You unlocked FREE delivery on your Hajipur order!"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF686870),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Clean White Domino's-Style Product Card with red & golden-yellow accents.
 */
@Composable
private fun PizzaMenuItemCard(
    menuItem: MenuItem,
    cartItems: List<com.example.data.model.CartItem>,
    onAddToCart: (MenuItem, PizzaSize) -> Unit,
    onUpdateCartQuantity: (String, Int) -> Unit
) {
    var selectedSize by remember(menuItem.id) {
        mutableStateOf(PizzaSize.REGULAR)
    }
    val currentPrice = if (menuItem.supportsMultipleSizes()) {
        menuItem.priceForSize(selectedSize)
    } else {
        menuItem.priceRegularInr
    }
    val cartKey = "${menuItem.id}_${selectedSize.name}"
    val qtyInCart = cartItems.find { it.key == cartKey }?.quantity ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_item_card_${menuItem.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFECECF2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Food Photo Thumbnail (Crystal Clear, Bright & Unobstructed!)
                Box(
                    modifier = Modifier
                        .size(114.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    OnlineFoodImage(
                        imageKey = menuItem.imageKey,
                        imageUrl = menuItem.imageUrl,
                        contentDescription = menuItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Item Details on Clean White Card
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VegNonVegIndicator(isVeg = menuItem.isVeg)
                        Text(
                            text = menuItem.displaySubCategoryLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CrushRed,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = menuItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1D1D22),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64646E),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatInr(currentPrice),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF1D1D22),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Size selector for pizzas
            if (menuItem.supportsMultipleSizes()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PizzaSize.entries.forEach { size ->
                        val isSelected = selectedSize == size
                        val sizePrice = menuItem.priceForSize(size)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSize = size },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFFFF3E0) else Color(0xFFF6F6FA),
                            border = BorderStroke(
                                1.2.dp,
                                if (isSelected) CrushRed else Color(0xFFE2E2EA)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = size.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CrushRed else Color(0xFF42424A),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatInr(sizePrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1D1D22),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add / Quantity Row — Guaranteed width for ADD + button so it never squishes or wraps!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = menuItem.preparationSubtitle(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (qtyInCart == 0) {
                    Button(
                        onClick = { onAddToCart(menuItem, selectedSize) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrushRed,
                            contentColor = CrushWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .width(116.dp)
                            .height(40.dp)
                            .testTag("add_to_cart_btn_${menuItem.id}")
                    ) {
                        Text(
                            text = "ADD +",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Surface(
                        color = CrushRed,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .width(118.dp)
                            .height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onUpdateCartQuantity(cartKey, -1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Decrease Quantity",
                                    tint = CrushWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = qtyInCart.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = CrushWhite,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            IconButton(
                                onClick = { onUpdateCartQuantity(cartKey, 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Increase Quantity",
                                    tint = CrushWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


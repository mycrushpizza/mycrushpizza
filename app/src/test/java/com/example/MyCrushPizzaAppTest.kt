package com.example

import com.example.data.model.AppRole
import com.example.data.model.CartItem
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaCategory
import com.example.data.model.PizzaSize
import com.example.data.model.formatInr
import com.example.data.repository.StoreAndMenuRepository
import com.example.ui.components.resolveFoodDrawable
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.MainAppUiState
import com.example.util.FuzzyFoodSearch
import com.mycrushpizza.`in`.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MyCrushPizzaAppTest {

    @Test
    fun inrFormattingAndCatalogCategories_areComplete_andEveryFoodHasUniqueImage() {
        assertEquals("₹349", formatInr(349))
        assertTrue(formatInr(1250).startsWith("₹"))

        val config = StoreAndMenuRepository.defaultStoreConfig()
        assertEquals(299, config.freeDeliveryMinOrderInr)

        val menu = StoreAndMenuRepository.defaultHajipurMenu()
        assertEquals(8, PizzaCategory.entries.size)
        assertEquals(212, menu.size)

        // Verify all items from the uploaded MY CRUSH PIZZA menu card exist with exact prices
        val menuByTitle = menu.associateBy { it.title }
        assertEquals(109, menuByTitle.getValue("Veg Kulhad Pizza").priceRegularInr)
        assertEquals(139, menuByTitle.getValue("Paneer Pizza").priceRegularInr)
        assertEquals(149, menuByTitle.getValue("Mushroom Pizza").priceRegularInr)
        assertEquals(109, menuByTitle.getValue("Margherita Pizza").priceRegularInr)
        assertEquals(209, menuByTitle.getValue("Margherita Pizza").priceMediumInr)
        assertEquals(339, menuByTitle.getValue("Margherita Pizza").priceLargeInr)
        assertEquals(159, menuByTitle.getValue("Double Cheese Margherita").priceRegularInr)
        assertEquals(269, menuByTitle.getValue("Double Cheese Margherita").priceMediumInr)
        assertEquals(379, menuByTitle.getValue("Double Cheese Margherita").priceLargeInr)
        assertEquals(129, menuByTitle.getValue("Capsicum Pizza").priceRegularInr)
        assertEquals(129, menuByTitle.getValue("Corn Pizza").priceRegularInr)
        assertEquals(119, menuByTitle.getValue("Onion Pizza").priceRegularInr)
        assertEquals(139, menuByTitle.getValue("Onion & Capsicum Pizza").priceRegularInr)
        assertEquals(139, menuByTitle.getValue("Tomato & Corn Pizza").priceRegularInr)
        assertEquals(149, menuByTitle.getValue("Onion & Paneer Pizza").priceRegularInr)
        assertEquals(159, menuByTitle.getValue("Capsicum & Paneer Pizza").priceRegularInr)
        assertEquals(159, menuByTitle.getValue("Jalapeño & Olive Pizza").priceRegularInr)
        assertEquals(69, menuByTitle.getValue("Cheese Burger").priceRegularInr)
        assertEquals(89, menuByTitle.getValue("Tandoori Paneer Burger").priceRegularInr)
        assertEquals(79, menuByTitle.getValue("Cheese & Corn Sandwich").priceRegularInr)
        assertEquals(119, menuByTitle.getValue("Tandoori Paneer Wrap").priceRegularInr)
        assertEquals(49, menuByTitle.getValue("Classic Maggi").priceRegularInr)
        assertEquals(59, menuByTitle.getValue("Veg Maggi").priceRegularInr)
        assertEquals(79, menuByTitle.getValue("Cheese Maggi").priceRegularInr)
        assertEquals(99, menuByTitle.getValue("Banana Shake").priceRegularInr)
        assertEquals(99, menuByTitle.getValue("Oreo Shake").priceRegularInr)
        assertEquals(99, menuByTitle.getValue("Cold Coffee").priceRegularInr)
        assertEquals(99, menuByTitle.getValue("Chocolate Shake").priceRegularInr)
        assertEquals(59, menuByTitle.getValue("Black Forest Pastry").priceRegularInr)
        assertEquals(69, menuByTitle.getValue("Chocolate Pastry").priceRegularInr)
        assertEquals(50, menuByTitle.getValue("Butterscotch Pastry").priceRegularInr)
        assertEquals(50, menuByTitle.getValue("White Forest Pastry").priceRegularInr)
        assertEquals(49, menuByTitle.getValue("Zingy Parcel").priceRegularInr)
        assertEquals(109, menuByTitle.getValue("Garlic Bread").priceRegularInr)

        // Verify no two food items share the same imageKey or drawable resource!
        val uniqueImageKeys = menu.map { it.imageKey }.distinct()
        assertEquals("Every food item must have a unique imageKey", menu.size, uniqueImageKeys.size)
        val uniqueDrawables = menu.map { resolveFoodDrawable(it.imageKey) }.distinct()
        assertEquals("Every food item must resolve to a unique drawable", menu.size, uniqueDrawables.size)

        // Verify no item has grouped price lists in its description
        assertTrue(menu.none { it.description.contains("₹") })

        // Verify default CartItem size is REGULAR
        val sampleCartItem = CartItem(menuItem = menu.first())
        assertEquals(PizzaSize.REGULAR, sampleCartItem.size)

        PizzaCategory.entries.forEach { category ->
            assertTrue(
                "Category ${category.id} must have dishes",
                menu.any { it.category == category.id }
            )
        }
    }

    @Test
    fun fuzzyFoodSearch_matchesSpellingMistakesAndTyposAcrossAllDishes() {
        val menu = StoreAndMenuRepository.defaultHajipurMenu()

        // 1. "piza" -> matches Pizzas
        val pizaResults = FuzzyFoodSearch.filterAndRank(menu, "All", "piza", vegOnly = false)
        assertTrue("Typo 'piza' should find pizzas", pizaResults.isNotEmpty())
        assertTrue(pizaResults.first().title.contains("Pizza", ignoreCase = true))

        // 2. "magi" -> matches Veg Maggi, Plain Maggi, Cheese Maggi
        val magiResults = FuzzyFoodSearch.filterAndRank(menu, "All", "magi", vegOnly = false)
        assertTrue("Typo 'magi' should find Maggi dishes", magiResults.any { it.title.contains("Maggi", ignoreCase = true) })

        // 3. "paner" -> matches Paneer dishes
        val panerResults = FuzzyFoodSearch.filterAndRank(menu, "All", "paner", vegOnly = false)
        assertTrue("Typo 'paner' should find Paneer dishes", panerResults.any { it.title.contains("Paneer", ignoreCase = true) })

        // 4. "chiken birani" -> matches Chicken Biryani
        val biraniResults = FuzzyFoodSearch.filterAndRank(menu, "All", "chiken birani", vegOnly = false)
        assertTrue("Typo 'chiken birani' should find Chicken Biryani", biraniResults.any { it.title.contains("Biryani", ignoreCase = true) })

        // 5. "chomin" -> matches Chowmin
        val chominResults = FuzzyFoodSearch.filterAndRank(menu, "All", "chomin", vegOnly = false)
        assertTrue("Typo 'chomin' should find Chowmin", chominResults.any { it.title.contains("Chowmin", ignoreCase = true) })

        // 6. "watr" / "spong" / "kitkat"
        val waterResults = FuzzyFoodSearch.filterAndRank(menu, "All", "minral watr", vegOnly = false)
        assertTrue(waterResults.any { it.id == "bev_mineral_water_1l" })

        val spongeResults = FuzzyFoodSearch.filterAndRank(menu, "All", "white spong", vegOnly = false)
        assertTrue(spongeResults.any { it.id == "dessert_white_sponges" })

        val suggestion = FuzzyFoodSearch.didYouMeanSuggestion("magi", magiResults)
        assertNotNull(suggestion)
    }

    @Test
    fun initialLocation_isEmptyUntilCustomerSetsLiveGpsOrGoogleMapPin_andWalletAndCartAreSeparateTabs() {
        val initialState = MainAppUiState()
        assertEquals("", initialState.checkoutAddress)
        assertEquals("", initialState.checkoutLandmark)

        // Verify Wallet starts with ₹0 Deposit and ₹0 Bonus (no pre-added ₹50)
        assertEquals(0, initialState.depositBalanceInr)
        assertEquals(0, initialState.bonusBalanceInr)
        assertEquals(0, initialState.totalWalletBalanceInr)

        // Verify Razorpay Payment Integration can be enabled/configured anytime in StoreConfig
        val defaultConfig = initialState.storeConfig
        assertFalse(defaultConfig.razorpayEnabled)
        val razorpayActiveConfig = defaultConfig.copy(
            razorpayEnabled = true,
            razorpayKeyId = "rzp_live_MyCrushHajipur01",
            razorpayMode = "LIVE",
            razorpayMerchantName = "My Crush Pizza – Hajipur"
        )
        assertTrue(razorpayActiveConfig.razorpayEnabled)
        assertEquals("rzp_live_MyCrushHajipur01", razorpayActiveConfig.razorpayKeyId)

        // Verify Tomato Soup and Tomato Creamy Soup have distinct drawable images
        val tomatoSoupDrawable = resolveFoodDrawable("tomato_soup_solo")
        val tomatoCreamySoupDrawable = resolveFoodDrawable("tomato_creamy_soup_solo")
        assertTrue(tomatoSoupDrawable != tomatoCreamySoupDrawable)

        // Verify French Fries and Peri-Peri French Fries have distinct drawable images
        val frenchFriesDrawable = resolveFoodDrawable("french_fries_solo")
        val periPeriFriesDrawable = resolveFoodDrawable("peri_peri_fries")
        assertTrue(frenchFriesDrawable != periPeriFriesDrawable)

        // Verify Chicken Pakora and Chicken Lollypop have distinct drawable images
        val chickenPakoraDrawable = resolveFoodDrawable("chicken_pakora_solo")
        val chickenLollypopDrawable = resolveFoodDrawable("chicken_lollypop")
        assertTrue(chickenPakoraDrawable != chickenLollypopDrawable)

        // Verify Raita, Mushroom, Paneer, Roti, Pakora, Bread, Dal, Rice, Noodle, and Curry groups have distinct drawable images
        assertTrue(resolveFoodDrawable("cucumber_raita_solo") != resolveFoodDrawable("onion_raita_solo"))
        assertTrue(resolveFoodDrawable("mushroom_masala_solo") != resolveFoodDrawable("mushroom_do_pyaza_closeup"))
        assertTrue(resolveFoodDrawable("plain_tawa_roti_solo") != resolveFoodDrawable("tandoori_roti_solo"))

        val pakoraDrawables = listOf(
            "paneer_pakora",
            "veg_pakora_spring_roll",
            "aloo_pakora_solo",
            "veg_pakora_solo",
            "onion_veg_pakora_closeup",
            "chicken_pakora_solo"
        ).map { resolveFoodDrawable(it) }.distinct()
        assertEquals(6, pakoraDrawables.size)

        val breadDrawables = listOf(
            "butter_naan_solo",
            "nacho_garlic_bread",
            "kashmiri_naan_solo",
            "veg_stuff_naan_solo",
            "paneer_stuff_naan_solo",
            "aloo_paratha_solo",
            "gobi_paratha_solo",
            "paneer_butter_paratha_solo",
            "onion_masala_kulcha_solo",
            "paneer_masala_kulcha_solo",
            "desi_ghee_roti_solo"
        ).map { resolveFoodDrawable(it) }.distinct()
        assertEquals(11, breadDrawables.size)

        val riceAndDalDrawables = listOf(
            "jeera_fried_rice_closeup",
            "paneer_fried_rice_solo",
            "chilly_garlic_fried_rice_solo",
            "baby_corn_fried_rice_solo",
            "mushroom_onion_fried_rice_solo",
            "chicken_fried_rice_solo",
            "chicken_egg_fried_rice_solo",
            "chef_spl_fried_rice_solo",
            "chef_spl_chicken_fried_rice_solo",
            "kashmiri_pulao_solo",
            "dal_makhni_jeera_rice",
            "plain_dal_solo",
            "dal_bukhara_solo"
        ).map { resolveFoodDrawable(it) }.distinct()
        assertEquals(13, riceAndDalDrawables.size)

        val noodleAndCurryDrawables = listOf(
            "veg_chowmin_solo",
            "jaipuri_chowmin_solo",
            "veg_manchurian_chowmin_solo",
            "schezwan_chicken_chowmein",
            "chicken_schezwan_chowmin_solo",
            "chicken_singapuri_chowmin_solo",
            "chicken_egg_chowmin_solo",
            "mushroom_masala_mix_veg",
            "paneer_do_pyaza_solo",
            "malai_kofta_kaju_korma",
            "paneer_malai_kofta_solo",
            "paneer_changezi_solo",
            "chicken_afgani_curry_solo",
            "omlet_curry_solo"
        ).map { resolveFoodDrawable(it) }.distinct()
        assertEquals(14, noodleAndCurryDrawables.size)

        assertEquals(R.drawable.img_shahi_paneer_fresh_1795084318961, resolveFoodDrawable("shahi_paneer"))
        assertEquals(R.drawable.img_chicken_tikka_fresh_1795088658770, resolveFoodDrawable("tandoori_kabab"))
        assertEquals(R.drawable.img_chicken_boti_kabab_fresh_1795088660330, resolveFoodDrawable("chicken_boti_kabab_solo"))
        assertTrue(resolveFoodDrawable("tandoori_kabab") != resolveFoodDrawable("chicken_boti_kabab_solo"))
        assertEquals(R.drawable.img_chicken_manchurian_fresh_1795089158911, resolveFoodDrawable("chicken_manchurian_closeup"))
        assertEquals(R.drawable.img_chicken_chowmein_fresh_1795089162359, resolveFoodDrawable("chicken_chowmein_closeup"))
        assertEquals(R.drawable.img_veg_manchurian_fresh_1795090312016, resolveFoodDrawable("gobi_veg_manchurian_closeup"))
        assertEquals(R.drawable.img_fry_egg_fresh_1795090796126, resolveFoodDrawable("fry_egg_solo"))
        assertEquals(R.drawable.img_roasted_papad_fresh_1795096289435, resolveFoodDrawable("roasted_papad_solo"))
        assertEquals(R.drawable.img_fried_papad_fresh_1795096259873, resolveFoodDrawable("fried_papad_solo"))
        assertEquals(R.drawable.img_masala_papad_fresh_1795096284273, resolveFoodDrawable("masala_papad_solo"))
        assertEquals(R.drawable.img_veg_kulhad_pizza_clean, resolveFoodDrawable("kulhad_pizza"))
        val refreshedZeroZoomDrawables = listOf(
            "chicken_manchurian_closeup",
            "chicken_chowmein_closeup",
            "chicken_do_pyaza_solo",
            "chicken_curry_masala_solo",
            "chicken_kadai_solo",
            "mutton_rogan_chicken_dehati",
            "chicken_changezi_solo",
            "mutton_kadai_solo",
            "mutton_lal_maas_solo",
            "chicken_tash_fry_solo",
            "chicken_reshmi_kabab_solo",
            "chicken_spring_roll_solo",
            "veg_burger_solo",
            "veg_sandwich_solo",
            "chicken_burger_sandwich",
            "gobi_manchurian_solo",
            "chilli_potato_manchurian",
            "veg_clear_soup_solo",
            "chicken_clear_soup_solo",
            "chicken_hot_sour_soup_solo",
            "chicken_manchow_soup_solo",
            "green_salad_raita_solo",
            "onion_cucumber_salad_solo",
            "onion_rings_chaat_solo"
        ).map { resolveFoodDrawable(it) }.distinct()
        assertEquals(24, refreshedZeroZoomDrawables.size)

        // Verify Wallet and Cart are distinct tabs in CustomerTab
        val tabs = CustomerTab.entries.map { it.name }
        assertTrue(tabs.contains("MENU"))
        assertTrue(tabs.contains("WALLET"))
        assertTrue(tabs.contains("CART"))
        assertTrue(tabs.contains("TRACK"))
    }

    @Test
    fun walletRedemptionAndOnlineState_computeCorrectNetPayable() {
        val samplePizza = StoreAndMenuRepository.defaultHajipurMenu().first { it.priceRegularInr >= 150 }
        val state = MainAppUiState(
            isSignedIn = true,
            isOnlineConnected = true,
            depositBalanceInr = 100,
            bonusBalanceInr = 50,
            useDepositBalanceInCheckout = true,
            useBonusBalanceInCheckout = true,
            cartItems = listOf(CartItem(menuItem = samplePizza, size = PizzaSize.REGULAR, quantity = 2))
        )
        // Deposit Cash is removed for now; only Bonus Cash counts in Wallet & Checkout, and payment defaults to COD
        assertEquals("COD", state.selectedPaymentMethod)
        assertEquals(50, state.totalWalletBalanceInr)
        assertTrue(state.isOnlineConnected)
        assertEquals(50, state.bonusUsedInr)
        assertEquals(0, state.depositUsedInr)
        assertEquals(50, state.walletTotalUsedInr)
    }

    @Test
    fun adminPermission_isGrantedOnlyToPranavGamer758WhenLoggedIn_andHiddenForCustomersAndGuests() {
        val guestState = MainAppUiState(isSignedIn = false, isGuestMode = true)
        assertFalse(guestState.isAdminUser)

        val regularCustomerState = MainAppUiState(
            isSignedIn = true,
            currentUserId = "cust_123",
            currentUserEmail = "customer@gmail.com"
        )
        assertFalse(regularCustomerState.isAdminUser)

        val unauthenticatedAdminEmailState = MainAppUiState(
            isSignedIn = false,
            currentUserEmail = "pranavgamer758@gmail.com"
        )
        assertFalse(unauthenticatedAdminEmailState.isAdminUser)

        val loggedInAdminState = MainAppUiState(
            isSignedIn = true,
            currentUserId = "admin_uid_1",
            currentUserEmail = "pranavgamer758@gmail.com"
        )
        assertTrue(loggedInAdminState.isAdminUser)

        val unauthenticatedSecondAdminState = MainAppUiState(
            isSignedIn = false,
            currentUserEmail = "poojajha844101@gmail.com"
        )
        assertFalse(unauthenticatedSecondAdminState.isAdminUser)

        val loggedInSecondAdminState = MainAppUiState(
            isSignedIn = true,
            currentUserId = "admin_uid_2",
            currentUserEmail = "poojajha844101@gmail.com"
        )
        assertTrue(loggedInSecondAdminState.isAdminUser)

        val incomingOrder = com.example.data.model.PizzaOrder(
            id = "order_ring_1",
            orderNumber = "MCP-7788",
            customerName = "Rahul Kumar",
            customerPhone = "9876543210",
            deliveryAddress = "Chauhatta RN College Road, Hajipur",
            landmark = "Near RN College Gate",
            status = OrderStatus.CONFIRMED.code,
            adminAlertAcknowledged = false
        )
        val ringingState = loggedInAdminState.copy(allAdminOrders = listOf(incomingOrder))
        assertTrue(ringingState.isAdminRinging)

        // When Admin accepts the order, alarm stops immediately and customer details are shown
        com.example.util.AdminOrderAlertPlayer.silenceOrderAndStopRinging(incomingOrder.id)
        val acceptedOrder = incomingOrder.copy(
            status = OrderStatus.PREPARING.code,
            adminAlertAcknowledged = true
        )
        val afterAcceptState = ringingState.copy(
            locallyDismissedAlertOrderIds = setOf(incomingOrder.id),
            locallyAcceptedOrderIds = setOf(incomingOrder.id),
            lastAcceptedAdminOrder = acceptedOrder,
            allAdminOrders = listOf(acceptedOrder)
        )
        assertFalse(afterAcceptState.isAdminRinging)
        assertNotNull(afterAcceptState.lastAcceptedAdminOrder)
        assertEquals("Rahul Kumar", afterAcceptState.lastAcceptedAdminOrder?.customerName)
        assertEquals("9876543210", afterAcceptState.lastAcceptedAdminOrder?.customerPhone)
        assertEquals("Chauhatta RN College Road, Hajipur", afterAcceptState.lastAcceptedAdminOrder?.deliveryAddress)
        assertEquals("Near RN College Gate", afterAcceptState.lastAcceptedAdminOrder?.landmark)
    }

    @Test
    fun storeLocation_isMyCrushPizzaHajipur_andEnforces10KmDeliveryRange() {
        val storeConfig = StoreAndMenuRepository.defaultStoreConfig()
        assertEquals("My Crush Pizza Hajipur", storeConfig.storeName)
        assertEquals("My Crush Pizza Hajipur", com.example.data.model.HajipurLocations.STORE_LOCATION_NAME)
        assertEquals(10.0, com.example.data.model.HajipurLocations.MAX_DELIVERY_RADIUS_KM, 0.001)

        // 1. Customer within 10 km of My Crush Pizza Hajipur (e.g., Dakbunglow Chowk / Ramaashish Chowk)
        val withinRangeState = MainAppUiState(
            checkoutLat = 25.6982,
            checkoutLng = 85.2215
        )
        assertTrue(withinRangeState.checkoutDistanceKm <= 10.0)
        assertTrue(withinRangeState.isCheckoutWithin10KmRange)

        // 2. Customer above 10 km from My Crush Pizza Hajipur (e.g., >10 km away in Patna / far north)
        val above10KmState = MainAppUiState(
            checkoutLat = 25.5941,
            checkoutLng = 85.1376
        )
        assertTrue(above10KmState.checkoutDistanceKm > 10.0)
        assertFalse(above10KmState.isCheckoutWithin10KmRange)
        val (resolvedTitle, resolvedAddress) = com.example.ui.components.resolveLocationAddressFromCoordinates(
            above10KmState.checkoutLat,
            above10KmState.checkoutLng
        )
        assertTrue(resolvedTitle.contains("Outside 10 km Range"))
        assertTrue(resolvedAddress.contains("Max 10 km"))

        // Verify customer notification cap is strictly 2 max deliveries per unique message per customer
        assertEquals(2, com.example.util.NotificationHelper.MAX_SAME_MESSAGE_PER_CUSTOMER)
        val keyA = com.example.util.NotificationHelper.buildMessageCapKey("cust_1", "Offer!", "Flat ₹50 Off")
        val keyB = com.example.util.NotificationHelper.buildMessageCapKey("cust_1", "  offer! ", "flat ₹50 off ")
        assertEquals(keyA, keyB)
    }

    @Test
    fun orderHistoryFilteringSearchAndStats_workAccurately() {
        val activeOrder = com.example.data.model.PizzaOrder(
            id = "ord_1",
            orderNumber = "MCP-1001",
            customerId = "user_1",
            itemsSummary = "1x Veg Kulhad Pizza, 1x Oreo Shake",
            deliveryAddress = "Station Road, Hajipur",
            totalInr = 208,
            paymentMethod = "COD",
            status = OrderStatus.PREPARING.code
        )
        val deliveredOrder = com.example.data.model.PizzaOrder(
            id = "ord_2",
            orderNumber = "MCP-1002",
            customerId = "user_1",
            itemsSummary = "2x Margherita Pizza (Medium)",
            deliveryAddress = "Cinema Road, Hajipur",
            totalInr = 418,
            paymentMethod = "UPI",
            status = OrderStatus.DELIVERED.code
        )
        val cancelledOrder = com.example.data.model.PizzaOrder(
            id = "ord_3",
            orderNumber = "MCP-1003",
            customerId = "user_1",
            itemsSummary = "1x Paneer Tikka Masala",
            deliveryAddress = "Dakbunglow Chowk, Hajipur",
            totalInr = 260,
            paymentMethod = "COD",
            status = OrderStatus.CANCELLED.code
        )

        val baseState = MainAppUiState(
            isSignedIn = true,
            currentUserId = "user_1",
            customerOrders = listOf(activeOrder, deliveredOrder, cancelledOrder)
        )

        assertEquals(1, baseState.orderHistoryActiveCount)
        assertEquals(1, baseState.orderHistoryDeliveredCount)
        assertEquals(1, baseState.orderHistoryCancelledCount)
        // Cancelled order should not count toward total spent
        assertEquals(626, baseState.orderHistoryTotalSpentInr)
        assertEquals(3, baseState.filteredOrderHistory.size)

        val activeOnly = baseState.copy(orderHistoryFilter = com.example.ui.viewmodel.OrderHistoryFilter.ACTIVE)
        assertEquals(listOf("ord_1"), activeOnly.filteredOrderHistory.map { it.id })

        val deliveredOnly = baseState.copy(orderHistoryFilter = com.example.ui.viewmodel.OrderHistoryFilter.DELIVERED)
        assertEquals(listOf("ord_2"), deliveredOnly.filteredOrderHistory.map { it.id })

        val cancelledOnly = baseState.copy(orderHistoryFilter = com.example.ui.viewmodel.OrderHistoryFilter.CANCELLED)
        assertEquals(listOf("ord_3"), cancelledOnly.filteredOrderHistory.map { it.id })

        val searchByOrderNum = baseState.copy(orderHistorySearchQuery = "MCP-1002")
        assertEquals(listOf("ord_2"), searchByOrderNum.filteredOrderHistory.map { it.id })

        val searchByDish = baseState.copy(orderHistorySearchQuery = "kulhad")
        assertEquals(listOf("ord_1"), searchByDish.filteredOrderHistory.map { it.id })

        assertEquals("Just now", com.example.ui.screens.formatOrderTimestamp(null))
    }

    @Test
    fun googleMapsLiveDeliveryHelpers_validateApiKeyAndComputeBounds() {
        assertFalse(com.example.ui.components.hasValidGoogleMapsApiKey(""))
        assertFalse(com.example.ui.components.hasValidGoogleMapsApiKey("YOUR_GOOGLE_MAPS_API_KEY"))
        assertFalse(com.example.ui.components.hasValidGoogleMapsApiKey("MY_MAPS_API_KEY"))
        assertTrue(com.example.ui.components.hasValidGoogleMapsApiKey("AIzaSyFakeTestKeyExample12345"))

        val bounds = com.example.ui.components.computeLiveDeliveryMapBounds(
            storeLat = 25.6858,
            storeLng = 85.2095,
            deliveryLat = 25.6880,
            deliveryLng = 85.2120,
            customerLat = 25.6915,
            customerLng = 85.2160
        )
        assertTrue(bounds.contains(com.google.android.gms.maps.model.LatLng(25.6858, 85.2095)))
        assertTrue(bounds.contains(com.google.android.gms.maps.model.LatLng(25.6880, 85.2120)))
        assertTrue(bounds.contains(com.google.android.gms.maps.model.LatLng(25.6915, 85.2160)))
    }

    @Test
    fun autoProgressOrderStatus_advancesOneStepEvery5Minutes_andNeverAutoMarksDelivered() {
        val baseMillis = 1_700_000_000_000L
        val fiveMin = com.example.data.model.PizzaOrder.AUTO_PROGRESS_STEP_INTERVAL_MS
        val order = com.example.data.model.PizzaOrder(
            id = "ord_auto_1",
            orderNumber = "MCP-9001",
            status = OrderStatus.CONFIRMED.code,
            createdAt = com.google.firebase.Timestamp(baseMillis / 1000L, 0),
            updatedAt = com.google.firebase.Timestamp(baseMillis / 1000L, 0)
        )

        // Before 5 minutes: stays CONFIRMED
        assertEquals(OrderStatus.CONFIRMED, order.autoProgressedOrderStatus(baseMillis + 4 * 60 * 1000L))
        assertEquals(null, order.nextAutoProgressStatus(baseMillis + 4 * 60 * 1000L))

        // At 5 minutes: advances 1 step to PREPARING
        assertEquals(OrderStatus.PREPARING, order.autoProgressedOrderStatus(baseMillis + fiveMin))
        assertEquals(OrderStatus.PREPARING, order.nextAutoProgressStatus(baseMillis + fiveMin))

        // At 10 minutes: READY
        assertEquals(OrderStatus.READY, order.autoProgressedOrderStatus(baseMillis + 2 * fiveMin))

        // At 15 minutes: ASSIGNED (Packing)
        assertEquals(OrderStatus.ASSIGNED, order.autoProgressedOrderStatus(baseMillis + 3 * fiveMin))

        // At 20 minutes: ON_THE_WAY (Reaching Soon)
        assertEquals(OrderStatus.ON_THE_WAY, order.autoProgressedOrderStatus(baseMillis + 4 * fiveMin))

        // Even after 2 hours: MUST remain ON_THE_WAY, NEVER auto-mark DELIVERED
        assertEquals(OrderStatus.ON_THE_WAY, order.autoProgressedOrderStatus(baseMillis + 24 * fiveMin))
        val onTheWayOrder = order.copy(status = OrderStatus.ON_THE_WAY.code)
        assertEquals(OrderStatus.ON_THE_WAY, onTheWayOrder.autoProgressedOrderStatus(baseMillis + 24 * fiveMin))
        assertEquals(null, onTheWayOrder.nextAutoProgressStatus(baseMillis + 24 * fiveMin))

        // Once Admin manually marks DELIVERED, it stays DELIVERED
        val deliveredOrder = order.copy(status = OrderStatus.DELIVERED.code)
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.autoProgressedOrderStatus(baseMillis + 24 * fiveMin))
        assertEquals(null, deliveredOrder.nextAutoProgressStatus(baseMillis + 24 * fiveMin))
    }

    @Test
    fun kulhadPizzaCategoryFilter_returnsOnlyKulhadPizza() {
        val menu = StoreAndMenuRepository.defaultHajipurMenu()
        val kulhadItems = FuzzyFoodSearch.filterAndRank(
            items = menu,
            selectedCategory = "Kulhad Pizza",
            rawQuery = "",
            vegOnly = false
        )
        assertEquals(1, kulhadItems.size)
        assertEquals("Veg Kulhad Pizza", kulhadItems.first().title)
    }

    @Test
    fun minimumOrder99AndDeliveryTime30To45Mins_areEnforcedAccurately() {
        assertEquals(99, com.example.ui.viewmodel.MIN_ORDER_AMOUNT_INR)

        val menu = StoreAndMenuRepository.defaultHajipurMenu()
        val under99Item = menu.first { it.priceRegularInr == 89 }
        val exact99Item = menu.first { it.priceRegularInr == 99 }

        val belowMinState = MainAppUiState(
            cartItems = listOf(CartItem(menuItem = under99Item, size = PizzaSize.REGULAR, quantity = 1))
        )
        assertFalse(belowMinState.isMinimumOrderMet)
        assertEquals(10, belowMinState.minOrderShortfallInr)

        val exactMinState = MainAppUiState(
            cartItems = listOf(CartItem(menuItem = exact99Item, size = PizzaSize.REGULAR, quantity = 1))
        )
        assertTrue(exactMinState.isMinimumOrderMet)
        assertEquals(0, exactMinState.minOrderShortfallInr)

        // Verify confirmed delivery ETA is within 30-45 mins across the 10 km Hajipur delivery zone
        for (dist in listOf(0.5, 2.0, 4.5, 8.0, 10.0)) {
            val eta = com.example.data.model.HajipurLocations.estimateMinutesForDistance(dist, OrderStatus.CONFIRMED)
            assertTrue("ETA $eta for $dist km must be in 30..45 mins", eta in 30..45)
        }
    }

    @Test
    fun savedAddressCodec_encodesAndDecodesHomeWorkOtherAndLegacyAddresses() {
        val encodedWork = com.example.data.model.SavedAddressCodec.encodeForProfile(
            label = com.example.data.model.AddressLabel.WORK,
            address = "Cinema Road, Hajipur",
            landmark = "Near SBI Branch"
        )
        assertEquals("[Work] Cinema Road, Hajipur | Landmark: Near SBI Branch", encodedWork)
        val decodedWork = com.example.data.model.SavedAddressCodec.decodeFromProfile(encodedWork)
        assertEquals(com.example.data.model.AddressLabel.WORK, decodedWork.label)
        assertEquals("Cinema Road, Hajipur", decodedWork.address)
        assertEquals("Near SBI Branch", decodedWork.landmark)

        val encodedOther = com.example.data.model.SavedAddressCodec.encodeForProfile(
            label = com.example.data.model.AddressLabel.OTHER,
            address = "Shahi Colony, Hajipur",
            landmark = ""
        )
        assertEquals("[Other] Shahi Colony, Hajipur", encodedOther)
        val decodedOther = com.example.data.model.SavedAddressCodec.decodeFromProfile(encodedOther)
        assertEquals(com.example.data.model.AddressLabel.OTHER, decodedOther.label)
        assertEquals("Shahi Colony, Hajipur", decodedOther.address)
        assertEquals("", decodedOther.landmark)

        // Legacy plain address without prefix defaults cleanly to HOME
        val legacyDecoded = com.example.data.model.SavedAddressCodec.decodeFromProfile("RN College Road, Hajipur")
        assertEquals(com.example.data.model.AddressLabel.HOME, legacyDecoded.label)
        assertEquals("RN College Road, Hajipur", legacyDecoded.address)
        assertEquals("", legacyDecoded.landmark)
    }

    @Test
    fun notificationReadStateAndNewAccountFloor_filterOldNotifsAndClearBadge() {
        val signupTimeMs = 1_760_000_000_000L
        // Notification created 1 hour before new user signed up -> MUST NOT appear in new ID
        assertFalse(
            com.example.util.NotificationReadStatePrefs.isNotificationEligibleForAccount(
                notifCreatedMillis = signupTimeMs - 3_600_000L,
                accountFloorMillis = signupTimeMs
            )
        )
        // Notification created right after signup -> MUST appear
        assertTrue(
            com.example.util.NotificationReadStatePrefs.isNotificationEligibleForAccount(
                notifCreatedMillis = signupTimeMs + 5_000L,
                accountFloorMillis = signupTimeMs
            )
        )

        val n1 = com.example.data.model.AppNotification(id = "n1", title = "Offer", message = "Flat 20% off", isRead = false)
        val n2 = com.example.data.model.AppNotification(id = "n2", title = "Order", message = "Confirmed", isRead = false)
        val unreadState = MainAppUiState(notifications = listOf(n1, n2))
        assertEquals(2, unreadState.unreadNotificationsCount)

        // Once viewed, locallyReadNotificationIds clears unread count to 0 permanently
        val viewedState = unreadState.copy(locallyReadNotificationIds = setOf("n1", "n2"))
        assertEquals(0, viewedState.unreadNotificationsCount)
    }

    @Test
    fun regularPizza89AndMenu99Filters_sortAndFilterAccurately() {
        val menu = StoreAndMenuRepository.defaultHajipurMenu()

        // 1. Regular Pizza ₹89: starts with ₹89 pizzas, then strictly ascending higher-priced pizzas, NO starters
        val regular89Pizzas = FuzzyFoodSearch.filterAndRank(
            items = menu,
            selectedCategory = FuzzyFoodSearch.CATEGORY_REGULAR_PIZZA_89,
            rawQuery = "",
            vegOnly = false
        )
        assertTrue(regular89Pizzas.isNotEmpty())
        assertEquals(89, regular89Pizzas.first().priceRegularInr)
        assertTrue(regular89Pizzas.all { FuzzyFoodSearch.isPizzaItem(it) && !it.id.startsWith("starter_") })
        val prices = regular89Pizzas.map { it.priceRegularInr }
        assertEquals(prices.sorted(), prices)
        assertTrue("Expected ascending pizzas above ₹89 as well", prices.last() > 89)

        // 2. Menu ₹99: returns ALL foods priced ₹99 and under across all categories (sorted ascending by price)
        val menu99Items = FuzzyFoodSearch.filterAndRank(
            items = menu,
            selectedCategory = FuzzyFoodSearch.CATEGORY_MENU_99,
            rawQuery = "",
            vegOnly = false
        )
        val expected99AndUnderCount = menu.count { it.priceRegularInr <= 99 }
        assertTrue(expected99AndUnderCount >= 10)
        assertEquals(expected99AndUnderCount, menu99Items.size)
        assertTrue(menu99Items.all { it.priceRegularInr <= 99 })
        assertTrue("Expected foods under ₹99 as well", menu99Items.any { it.priceRegularInr < 99 })
        assertTrue("Expected ₹99 foods included", menu99Items.any { it.priceRegularInr == 99 })
        val menu99Prices = menu99Items.map { it.priceRegularInr }
        assertEquals(menu99Prices.sorted(), menu99Prices)

        // 3. Exact search ranks the exact food right at index 0
        val paneerPizzaSearch = FuzzyFoodSearch.filterAndRank(
            items = menu,
            selectedCategory = "All",
            rawQuery = "Paneer Pizza",
            vegOnly = false
        )
        assertEquals("Paneer Pizza", paneerPizzaSearch.first().title)

        val tomatoPizzaSearch = FuzzyFoodSearch.filterAndRank(
            items = menu,
            selectedCategory = "All",
            rawQuery = "Tomato Pizza",
            vegOnly = false
        )
        assertEquals("Tomato Pizza", tomatoPizzaSearch.first().title)
    }

    @Test
    fun adminExcludedFromCustomerWallet_orderCancelledCopyAndCleanupCutoffs_areValid() {
        val adminByEmail = com.example.data.model.UserProfile(
            id = "admin_1",
            userId = "admin_1",
            displayName = "Pranav Admin",
            email = "pranavgamer758@gmail.com",
            role = "CUSTOMER"
        )
        val adminByRole = com.example.data.model.UserProfile(
            id = "admin_2",
            userId = "admin_2",
            displayName = "Manager",
            email = "manager@example.com",
            role = "ADMIN"
        )
        val regularCustomer = com.example.data.model.UserProfile(
            id = "cust_1",
            userId = "cust_1",
            displayName = "Rahul Kumar",
            email = "rahul@example.com",
            role = "CUSTOMER"
        )

        val secondAdminByEmail = com.example.data.model.UserProfile(
            id = "admin_pooja",
            userId = "admin_pooja",
            displayName = "Pooja Jha",
            email = "poojajha844101@gmail.com",
            role = "CUSTOMER"
        )

        assertTrue(com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(adminByEmail))
        assertTrue(com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(secondAdminByEmail))
        assertTrue(com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(adminByRole))
        assertFalse(com.example.data.repository.AuthAndUserRepository.isAdminUserProfile(regularCustomer))

        // Order Cancelled title is clean and shown to customer when Admin cancels
        assertEquals("Order Cancelled", OrderStatus.CANCELLED.title)

        // Cleanup cutoffs are set to 2026-11-24 05:00:00 UTC
        assertEquals(
            com.example.ui.viewmodel.MainAppViewModel.OLD_ORDERS_CLEANUP_CUTOFF_MILLIS,
            com.example.ui.viewmodel.MainAppViewModel.OLD_CUSTOMERS_CLEANUP_CUTOFF_MILLIS
        )
        assertTrue(com.example.ui.viewmodel.MainAppViewModel.OLD_ORDERS_CLEANUP_CUTOFF_MILLIS > 1_795_000_000_000L)
    }
}




package com.example.data.repository

import android.util.Log
import com.example.data.model.Coupon
import com.example.data.model.DeliveryPartner
import com.example.data.model.HajipurLocations
import com.example.data.model.MenuItem
import com.example.data.model.StoreConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class StoreAndMenuRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")

    private enum class OperationType {
        GET, LIST, WRITE, DELETE
    }

    private fun handleFirestoreError(
        error: Exception,
        operationType: OperationType,
        path: String?
    ) {
        val currentUser = auth.currentUser
        val errorInfo = JSONObject().apply {
            put("error", error.message ?: "Unknown Firestore error")
            put("operationType", operationType.name.lowercase())
            put("path", path ?: "")
            put("authInfo", JSONObject().apply {
                put("userId", currentUser?.uid ?: "")
                put("email", currentUser?.email ?: "")
                put("emailVerified", currentUser?.isEmailVerified ?: false)
                put("isAnonymous", currentUser?.isAnonymous ?: false)
            })
        }
        Log.e("FirestorePermissions", errorInfo.toString(), error)
    }

    fun observeStoreConfig(): Flow<StoreConfig> = flow {
        val uid = requireUserId()
        val path = "store_config/hajipur_main"
        emitAll(
            db.document(path)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        val defaults = defaultStoreConfig()
                        val parsed = snap.toObject(
                            StoreConfig::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ) ?: defaults
                        // Kotlin boolean property `isOpen` compiles to getter `isOpen()`, which Firestore's
                        // toObject() maps to field name "open" instead of "isOpen". Read explicitly so the
                        // Admin Restaurant Accepting Orders ON/OFF toggle always stays in sync!
                        val resolvedIsOpen = snap.getBoolean("isOpen")
                            ?: snap.getBoolean("open")
                            ?: parsed.isOpen
                        val normalizedAddress = if (
                            parsed.branchAddress.contains("Station Road", ignoreCase = true) ||
                            parsed.branchAddress.contains("Chipi Tola", ignoreCase = true) ||
                            parsed.branchAddress.contains("Chauhatta RN College", ignoreCase = true)
                        ) {
                            defaults.branchAddress
                        } else {
                            parsed.branchAddress.ifBlank { defaults.branchAddress }
                        }
                        parsed.copy(
                            isOpen = resolvedIsOpen,
                            branchAddress = normalizedAddress
                        )
                    } else {
                        defaultStoreConfig()
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updateStoreConfig(config: StoreConfig): Result<Unit> = runCatching {
        requireUserId()
        val defaults = defaultStoreConfig()
        val docRef = db.collection("store_config").document("hajipur_main")
        val payload = mapOf(
            "storeName" to config.storeName.trim().ifBlank { defaults.storeName }.take(120),
            "branchAddress" to config.branchAddress.trim().ifBlank { defaults.branchAddress }.take(250),
            "storePhone" to config.storePhone.trim().ifBlank { defaults.storePhone }.take(40),
            "openTime" to config.openTime.trim().ifBlank { defaults.openTime }.take(40),
            "closeTime" to config.closeTime.trim().ifBlank { defaults.closeTime }.take(40),
            "isOpen" to config.isOpen,
            "deliveryChargeInr" to config.deliveryChargeInr.coerceIn(0, 500),
            "freeDeliveryMinOrderInr" to config.freeDeliveryMinOrderInr.coerceIn(0, 10000),
            "taxPercent" to config.taxPercent.coerceIn(0, 30),
            "storeLat" to config.storeLat,
            "storeLng" to config.storeLng,
            "razorpayEnabled" to config.razorpayEnabled,
            "razorpayKeyId" to config.razorpayKeyId.trim().take(120),
            "razorpayMerchantName" to config.razorpayMerchantName.trim().ifBlank { "My Crush Pizza – Hajipur" }.take(120),
            "razorpayPaymentLink" to config.razorpayPaymentLink.trim().take(300),
            "razorpayMode" to (config.razorpayMode.trim().uppercase().takeIf { it in listOf("LIVE", "TEST") } ?: "LIVE"),
            "merchantUpiId" to config.merchantUpiId.trim().ifBlank { "mycrushpizza@upi" }.take(100),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeMenuItems(): Flow<List<MenuItem>> = flow {
        val uid = requireUserId()
        val path = "menu_items"
        val canonicalMenu = defaultHajipurMenu()
        val canonicalIds = canonicalMenu.map { it.id }.toSet()
        val canonicalImageKeys = canonicalMenu.map { it.imageKey }.toSet()
        emitAll(
            db.collection(path)
                .whereEqualTo("isAvailable", true)
                .snapshots()
                .map { snap ->
                    val allRemote = snap.toObjects(
                        MenuItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    val remoteById = allRemote.associateBy { it.id }
                    val mergedCanonical = canonicalMenu.map { base ->
                        val remote = remoteById[base.id]
                        if (remote != null) {
                            val resolvedOnlineUrl = remote.imageUrl.trim().takeIf {
                                it.startsWith("https://") || it.startsWith("http://")
                            } ?: base.imageUrl
                            base.copy(
                                priceRegularInr = base.priceRegularInr,
                                priceMediumInr = base.priceMediumInr,
                                priceLargeInr = base.priceLargeInr,
                                imageUrl = resolvedOnlineUrl,
                                isAvailable = remote.isAvailable
                            )
                        } else {
                            base
                        }
                    }.filter { it.isAvailable }

                    val remoteCustomItems = allRemote.filter { remote ->
                        remote.id !in canonicalIds &&
                            remote.imageKey !in canonicalImageKeys &&
                            !remote.description.contains("₹") &&
                            !remote.description.contains("Rs", ignoreCase = true) &&
                            !remote.title.contains("/")
                    }.distinctBy { it.imageKey }
                    mergedCanonical + remoteCustomItems
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveMenuItem(item: MenuItem): Result<String> = runCatching {
        requireUserId()
        val docRef = if (item.id.isNotBlank()) {
            db.collection("menu_items").document(item.id)
        } else {
            db.collection("menu_items").document()
        }
        val payload = mapOf(
            "title" to item.title.take(120),
            "description" to item.description.take(500),
            "category" to item.category.take(60),
            "priceRegularInr" to item.priceRegularInr.coerceIn(10, 10000),
            "priceMediumInr" to item.priceMediumInr.coerceIn(10, 10000),
            "priceLargeInr" to item.priceLargeInr.coerceIn(10, 10000),
            "imageKey" to item.imageKey.ifBlank { "crush_special" }.take(100),
            "imageUrl" to item.imageUrl.trim().take(500),
            "isVeg" to item.isVeg,
            "isBestseller" to item.isBestseller,
            "isAvailable" to item.isAvailable,
            "spiceLevel" to item.spiceLevel.ifBlank { "Medium" }.take(30),
            "createdAt" to (item.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMenuItem(itemId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("menu_items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeActiveCoupons(): Flow<List<Coupon>> = flow {
        val uid = requireUserId()
        val path = "coupons"
        emitAll(
            db.collection(path)
                .whereEqualTo("isActive", true)
                .snapshots()
                .map { snap ->
                    val list = snap.toObjects(
                        Coupon::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    list.ifEmpty { defaultCoupons() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCoupon(coupon: Coupon): Result<String> = runCatching {
        requireUserId()
        val docId = coupon.id.ifBlank { coupon.code.trim().uppercase() }
        val docRef = db.collection("coupons").document(docId)
        val payload = mapOf(
            "code" to coupon.code.trim().uppercase().take(30),
            "title" to coupon.title.take(120),
            "description" to coupon.description.take(300),
            "discountType" to coupon.discountType.uppercase(),
            "discountValue" to coupon.discountValue.coerceIn(1, 5000),
            "minOrderInr" to coupon.minOrderInr.coerceIn(0, 20000),
            "maxDiscountInr" to coupon.maxDiscountInr.coerceIn(1, 5000),
            "isActive" to coupon.isActive,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCoupon(couponId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("coupons").document(couponId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialHajipurCatalogIfAdmin(): Result<Unit> = runCatching {
        requireUserId()
        val existingConfigSnap = runCatching {
            db.collection("store_config").document("hajipur_main").get().await()
        }.getOrNull()
        if (existingConfigSnap == null || !existingConfigSnap.exists()) {
            updateStoreConfig(defaultStoreConfig()).getOrNull()
        } else {
            val currentBranch = existingConfigSnap.getString("branchAddress").orEmpty()
            if (
                currentBranch.contains("Station Road", ignoreCase = true) ||
                currentBranch.contains("Chipi Tola", ignoreCase = true) ||
                currentBranch.contains("Chauhatta RN College", ignoreCase = true)
            ) {
                runCatching {
                    db.collection("store_config").document("hajipur_main")
                        .update("branchAddress", defaultStoreConfig().branchAddress)
                        .await()
                }
            }
        }
        val canonicalMenu = defaultHajipurMenu()
        val canonicalIds = canonicalMenu.map { it.id }.toSet()
        // Delete any stale/legacy grouped menu documents from Firestore
        runCatching {
            val existingSnap = db.collection("menu_items").get().await()
            val staleDocs = existingSnap.documents.filter { it.id !in canonicalIds }
            staleDocs.chunked(50).forEach { chunk ->
                val deleteBatch = db.batch()
                chunk.forEach { doc -> deleteBatch.delete(doc.reference) }
                deleteBatch.commit().await()
            }
        }
        // Upsert clean canonical 210 items in fast batches
        canonicalMenu.chunked(50).forEach { chunk ->
            runCatching {
                val writeBatch = db.batch()
                chunk.forEach { item ->
                    val docRef = db.collection("menu_items").document(item.id)
                    val payload = mapOf(
                        "title" to item.title.take(120),
                        "description" to item.description.take(500),
                        "category" to item.category.take(60),
                        "priceRegularInr" to item.priceRegularInr.coerceIn(10, 10000),
                        "priceMediumInr" to item.priceMediumInr.coerceIn(10, 10000),
                        "priceLargeInr" to item.priceLargeInr.coerceIn(10, 10000),
                        "imageKey" to item.imageKey.ifBlank { "crush_special" }.take(100),
                        "isVeg" to item.isVeg,
                        "isBestseller" to item.isBestseller,
                        "isAvailable" to item.isAvailable,
                        "spiceLevel" to item.spiceLevel.ifBlank { "Medium" }.take(30),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    writeBatch.set(docRef, payload)
                }
                writeBatch.commit().await()
            }
        }
        // Remove any legacy coupons from Firestore as all coupons are removed
        runCatching {
            val existingCoupons = db.collection("coupons").get().await()
            existingCoupons.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        }
    }

    companion object {
        fun defaultStoreConfig(): StoreConfig = StoreConfig(
            id = "hajipur_main",
            storeName = "My Crush Pizza Hajipur",
            branchAddress = HajipurLocations.STORE_FULL_ADDRESS,
            storePhone = "+91 95238 41563",
            openTime = "10:00 AM",
            closeTime = "11:00 PM",
            isOpen = true,
            deliveryChargeInr = 25,
            freeDeliveryMinOrderInr = 299,
            taxPercent = 5,
            storeLat = HajipurLocations.KITCHEN_LAT,
            storeLng = HajipurLocations.KITCHEN_LNG
        )

        fun defaultCoupons(): List<Coupon> = emptyList()

        fun defaultDeliveryPartners(fallbackUid: String = ""): List<DeliveryPartner> = emptyList()

        private fun item(
            id: String,
            title: String,
            description: String,
            category: String,
            reg: Int,
            med: Int = reg,
            lrg: Int = med,
            imageKey: String,
            isVeg: Boolean = true,
            @Suppress("UNUSED_PARAMETER") isBestseller: Boolean = false,
            spiceLevel: String = "Medium"
        ) = MenuItem(
            id = id,
            title = title.replace(" (Best Seller)", ""),
            description = description,
            category = category,
            priceRegularInr = reg,
            priceMediumInr = med,
            priceLargeInr = lrg,
            imageKey = imageKey,
            isVeg = isVeg,
            isBestseller = false,
            isAvailable = true,
            spiceLevel = spiceLevel
        )

        /**
         * Complete 210 Authentic Individual Food Items from all 10 uploaded MY CRUSH PIZZA & Restaurant Menu Pages.
         * Every single dish has its own separate MenuItem entry and its own 100% unique studio photograph (`imageKey`),
         * ensuring ZERO image duplication across the entire 210-dish catalog!
         */
        fun defaultHajipurMenu(): List<MenuItem> = listOf(
            // =========================================================================
            // 1. VEG PIZZA & PIZZA TEASERS (15 individual items — 15 unique studio photos)
            // =========================================================================
            item("veg_paneer_pizza", "Paneer Pizza", "Freshly baked pizza loaded with spiced golden roasted cottage cheese paneer cubes, herbs & 100% melted mozzarella.", "Veg Pizza", 139, 239, 349, "paneer_pizza_solo", isVeg = true, isBestseller = true),
            item("veg_mushroom_pizza", "Mushroom Pizza", "Freshly baked pizza topped with juicy sliced button mushrooms, Italian herbs & 100% real mozzarella cheese.", "Veg Pizza", 149, 249, 359, "mushroom_pizza_solo", isVeg = true, isBestseller = true),
            item("veg_farmhouse_crush", "Farm House Pizza", "Fresh crunchy capsicum, juicy tomatoes, button mushrooms, sweet corn & black olives with 100% melted mozzarella.", "Veg Pizza", 179, 299, 429, "farmhouse_pizza", isVeg = true),
            item("veg_peppy_paneer", "Peppy Paneer Pizza", "Spiced smoky paneer cubes, crisp green capsicum & fiery red paprika on a freshly baked golden crust.", "Veg Pizza", 199, 329, 469, "peppy_paneer", isVeg = true, spiceLevel = "Spicy"),
            item("veg_country_feast", "Country Feast Pizza", "Garden fresh onions, crisp capsicum, sweet golden corn, mushrooms & signature herb tomato sauce.", "Veg Pizza", 179, 299, 429, "hero_pizza", isVeg = true),
            item("veg_tandoori_paneer", "Tandoori Paneer Pizza", "Char-grilled tandoori marinated paneer cubes, red onions, green capsicum & mint mayo swirl on melted mozzarella.", "Veg Pizza", 199, 329, 469, "tandoori_paneer_pizza", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("teaser_tomato_pizza", "Tomato Pizza", "Personal pizza teaser topped with juicy vine-ripened tomato slices, Italian herbs & 100% real mozzarella cheese.", "Veg Pizza", 89, 159, 249, "pizza_teaser", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("teaser_capsicum_pizza", "Capsicum Pizza", "Freshly baked pizza teaser topped with crunchy green bell pepper strips & bubbling white mozzarella cheese.", "Veg Pizza", 129, 199, 289, "capsicum_corn_pizza", isVeg = true, spiceLevel = "Mild"),
            item("teaser_corn_pizza", "Corn Pizza", "Personal pizza teaser loaded with sweet golden American corn kernels and stretchy molten mozzarella.", "Veg Pizza", 129, 199, 289, "corn_pizza_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("teaser_onion_pizza", "Onion Pizza", "Crispy personal pizza topped with caramelized red onion rings, oregano seasoning & melted mozzarella.", "Veg Pizza", 119, 189, 279, "onion_pizza_solo", isVeg = true, spiceLevel = "Mild"),
            item("teaser_onion_capsicum_pizza", "Onion & Capsicum Pizza", "Personal pizza teaser topped with crunchy red onions, roasted green capsicum & bubbling mozzarella.", "Veg Pizza", 139, 209, 309, "onion_capsicum_pizza", isVeg = true),
            item("teaser_tomato_corn_pizza", "Tomato & Corn Pizza", "Tangy ripe tomatoes and sweet golden corn baked together on a rich cheese and tomato herb base.", "Veg Pizza", 139, 209, 309, "tomato_corn_pizza_solo", isVeg = true, spiceLevel = "Mild"),
            item("teaser_onion_paneer_pizza", "Onion & Paneer Pizza", "Loaded with spiced golden paneer cubes, caramelized red onions & stretchy mozzarella cheese.", "Veg Pizza", 149, 229, 329, "onion_paneer_pizza", isVeg = true, isBestseller = true),
            item("teaser_capsicum_paneer_pizza", "Capsicum & Paneer Pizza", "Roasted cottage cheese cubes and crisp green bell peppers baked over molten mozzarella cheese.", "Veg Pizza", 159, 239, 339, "capsicum_paneer_pizza_solo", isVeg = true, isBestseller = true),
            item("teaser_jalapeno_black_olive", "Jalapeño & Olive Pizza", "Tangy pickled green jalapeño slices & Mediterranean black olives baked on bubbling white mozzarella cheese.", "Veg Pizza", 159, 249, 369, "jalapeno_olive_pizza", isVeg = true, spiceLevel = "Spicy"),

            // =========================================================================
            // 2. CHEESE PIZZA & KULHAD PIZZA (3 individual items — 3 unique studio photos)
            // =========================================================================
            item("cheese_veg_kulhad_pizza", "Veg Kulhad Pizza", "Served piping hot in an earthen clay kulhad overflowing with bubbling mozzarella, paneer, sweet corn & herbs.", "Cheese Pizza", 109, 159, 209, "kulhad_pizza", isVeg = true, isBestseller = true),
            item("cheese_margherita_classic", "Margherita Pizza", "Signature My Crush Pizza Margherita with rich Italian tomato sauce, fresh basil & 100% real mozzarella cheese.", "Cheese Pizza", 109, 209, 339, "margherita_pizza", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("cheese_double_burst", "Double Cheese Margherita", "Loaded with an extra double layer of molten liquid cheddar & stretchy mozzarella cheese.", "Cheese Pizza", 159, 269, 379, "cheese_pizza", isVeg = true, isBestseller = true, spiceLevel = "Mild"),

            // =========================================================================
            // 3. SPECIAL PIZZA & VEG / CHICKEN / CHINESE STARTERS (28 individual items — 28 unique studio photos)
            // =========================================================================
            item("special_my_crush_royal", "★ MY CRUSH PIZZA", "Our crown-jewel signature pizza loaded with tandoori paneer, sweet corn, red paprika, black olives, jalapenos & extra cheese pull!", "Special Pizza", 229, 369, 519, "crush_special", isVeg = true, spiceLevel = "Spicy"),
            item("special_spicy_tango", "Spicy Tango Pizza", "Zesty Mexican tango of golden corn, fiery green jalapenos, red paprika & tangy peri-peri cheese drizzle.", "Special Pizza", 199, 329, 469, "spicy_tango_pizza", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("special_paprika_pineapple", "Red Paprika & Pineapple Pizza", "Juicy golden roasted pineapple chunks balanced with smoky spicy red paprika & melted mozzarella cheese.", "Special Pizza", 139, 239, 349, "paprika_pineapple_pizza", isVeg = true),
            item("starter_veg_pakora", "Veg Pakora (8 Pcs)", "Crispy golden gram-flour fritters of fresh garden vegetables served hot with green mint chutney.", "Special Pizza", 150, 150, 150, "veg_pakora_solo", isVeg = true),
            item("starter_onion_pakora", "Onion Pakora (8 Pcs)", "Crunchy spiced onion bhajiya fritters fried golden and dusted with chaat masala & mint chutney.", "Special Pizza", 150, 150, 150, "onion_veg_pakora_closeup", isVeg = true),
            item("starter_paneer_pakora", "Paneer Pakora (6 Pcs)", "Soft spiced cottage cheese cubes dipped in seasoned gram-flour batter and fried golden with mint chutney.", "Special Pizza", 149, 185, 185, "paneer_pakora", isVeg = true, isBestseller = true),
            item("starter_mushroom_pakora", "Mushroom Pakora (8 Pcs)", "Fresh button mushrooms coated in spiced besan batter and deep-fried crisp with tangy dips.", "Special Pizza", 170, 170, 170, "veg_pakora_spring_roll", isVeg = true),
            item("starter_aloo_pakora", "Aloo Pakora (8 Pcs)", "Golden spiced potato slices fried in herb-seasoned gram-flour batter with green chutney.", "Special Pizza", 175, 175, 175, "aloo_pakora_solo", isVeg = true),
            item("starter_chicken_pakora", "Chicken Pakora (8 Pcs)", "Juicy spiced boneless chicken fritters fried golden and served with onion rings & mint chutney.", "Special Pizza", 250, 250, 250, "chicken_pakora_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_tikka", "Chicken Tikka (8 Pcs)", "Smoky clay-oven roasted boneless chicken tikka chunks marinated in yogurt and tandoori spices.", "Special Pizza", 280, 280, 280, "tandoori_kabab", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_seek_kabab", "Chicken Seek Kabab (8 Pcs)", "Minced spiced chicken skewers char-grilled in the clay tandoor with fresh coriander & mint chutney.", "Special Pizza", 280, 280, 280, "chicken_seek_kabab_solo", isVeg = false, spiceLevel = "Spicy"),
            item("starter_chicken_boti_kabab", "Chicken Boti Kabab (8 Pcs)", "Tender marinated chicken boti morsels roasted over glowing charcoal in our tandoor.", "Special Pizza", 270, 270, 270, "chicken_boti_kabab_solo", isVeg = false, spiceLevel = "Spicy"),
            item("starter_chicken_angara_kabab", "Chicken Angara Kabab (8 Pcs)", "Fiery smoky red-chilli marinated chicken angara kababs char-grilled to perfection.", "Special Pizza", 300, 300, 300, "chicken_angara_kabab_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_malai_tikka", "Chicken Malai Tikka (8 Pcs)", "Melt-in-mouth creamy cashew and cheese marinated chicken malai tikka roasted in the tandoor.", "Special Pizza", 280, 280, 280, "chicken_malai_tikka_solo", isVeg = false, isBestseller = true, spiceLevel = "Mild"),
            item("starter_chicken_tandoori", "Chicken Tandoori F/H (8 Pcs)", "Classic whole/half Tandoori Chicken marinated in spiced hung curd and roasted smoky in a clay oven.", "Special Pizza", 280, 400, 400, "chicken_tandoori_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_reshmi_kabab", "Chicken Reshmi Kabab (8 Pcs)", "Silky tender chicken skewers marinated in cream, cashew paste, cardamom and white pepper.", "Special Pizza", 350, 350, 350, "chicken_reshmi_kabab_solo", isVeg = false, spiceLevel = "Mild"),
            item("starter_chicken_tash_fry", "Chicken Tash Fry (8 Pcs)", "Bihar-style tawa-seared spicy Chicken Tash Fry cooked with crushed garlic, ginger and roasted spices.", "Special Pizza", 280, 280, 280, "chicken_tash_fry_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_hara_bhara_kabab", "Chicken Hara Bhara Kabab (8 Pcs)", "Char-grilled chicken kababs marinated in fresh green coriander, mint, spinach and green chillies.", "Special Pizza", 280, 280, 280, "chicken_hara_bhara_kabab_solo", isVeg = false, spiceLevel = "Medium"),
            item("starter_spring_roll", "Spring Roll", "Golden fried Indo-Chinese vegetable spring rolls stuffed with julienned veggies & noodles with hot garlic dip.", "Special Pizza", 220, 220, 220, "spring_roll_closeup", isVeg = true, isBestseller = true),
            item("starter_paneer_hong_kong", "Paneer Hong Kong (4 Pcs)", "Crispy cottage cheese cubes tossed in spicy Hong Kong style star-anise, garlic & red chilli sauce.", "Special Pizza", 220, 220, 220, "paneer_hong_kong_solo", isVeg = true, spiceLevel = "Spicy"),
            item("starter_finger_chips", "Finger Chips", "Crunchy golden-fried potato finger chips lightly salted and served with tomato ketchup.", "Special Pizza", 150, 150, 150, "finger_chips_solo", isVeg = true, spiceLevel = "Mild"),
            item("starter_crispy_baby_corn", "Crispy Baby Corn", "Batter-fried crunchy golden baby corn tossed with spring onions, cracked pepper & garlic.", "Special Pizza", 220, 220, 220, "crispy_baby_corn_solo", isVeg = true, isBestseller = true),
            item("starter_chilly_baby_corn", "Chilly Baby Corn", "Wok-tossed crispy baby corn in fiery Indo-Chinese soy, green chilli, capsicum & garlic glaze.", "Special Pizza", 240, 240, 240, "chilly_baby_corn_solo", isVeg = true, spiceLevel = "Spicy"),
            item("starter_chicken_lollypop", "Chicken Lollypop (6 Pcs / 8 Pcs)", "Sizzling Indo-Chinese frenched chicken winglet lollypops tossed in spicy Schezwan garlic glaze.", "Special Pizza", 260, 299, 299, "chicken_lollypop", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_spring_roll", "Chicken Spring Roll", "Crispy golden rolls stuffed with spiced minced chicken, cabbage and carrots served with Schezwan dip.", "Special Pizza", 245, 245, 245, "chicken_spring_roll_solo", isVeg = false),
            item("starter_chicken_chilly_bone", "Chicken Chilly Bone H/F (4 Pcs / 8 Pcs)", "Classic bone-in Chilli Chicken wok-tossed with crunchy bell peppers, onions, green chillies & soy garlic sauce.", "Special Pizza", 180, 220, 240, "chicken_chilly_bone_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_chilly_boneless", "Chicken Chilly Boneless H/F (4 Pcs / 8 Pcs)", "Juicy boneless chicken chunks tossed in sizzling Indo-Chinese chilli garlic sauce with capsicum.", "Special Pizza", 180, 240, 265, "chicken_chilly_boneless_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("starter_chicken_crispy", "Chicken Crispy (8 Pcs)", "Crunchy batter-fried chicken strips tossed in tangy sweet-and-spicy Schezwan sesame glaze.", "Special Pizza", 220, 220, 220, "chicken_crispy_solo", isVeg = false, spiceLevel = "Spicy"),

            // =========================================================================
            // 4. BURGERS, SANDWICHES, WRAPS, FRIES & EGG / OMLET (16 individual items — 16 unique studio photos)
            // =========================================================================
            item("burger_cheese_supreme", "Cheese Burger", "Golden herb-crusted patty topped with melted cheddar cheese slice, fresh lettuce, tomatoes & creamy burger sauce.", "Burgers", 69, 69, 69, "burger_pasta", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("burger_veg_classic", "Veg Burger", "Crispy vegetable patty served in a toasted sesame bun with fresh onion rings, tomato & tangy mayo.", "Burgers", 100, 100, 100, "veg_burger_solo", isVeg = true, spiceLevel = "Mild"),
            item("burger_tandoori_paneer", "Tandoori Paneer Burger", "Char-grilled spiced paneer slab on a toasted sesame brioche bun with melted cheese, lettuce & smoky tandoori mayo.", "Burgers", 89, 89, 89, "tandoori_burger", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("burger_crispy_chicken", "Chicken Burger", "Crispy golden chicken fillet burger with melted cheddar cheese, fresh lettuce, red onion rings & signature mayo.", "Burgers", 180, 180, 180, "chicken_burger_closeup", isVeg = false, isBestseller = true),
            item("sandwich_corn_cheese_grilled", "Cheese & Corn Sandwich", "Jumbo golden-toasted bread stuffed with sweet American corn, molten mozzarella, bell peppers & green chutney.", "Burgers", 79, 79, 79, "grilled_sandwich", isVeg = true, isBestseller = true),
            item("sandwich_veg_club", "Veg Sandwich", "Fresh garden cucumber, tomato, onion and spiced potato club sandwich with mint chutney and butter.", "Burgers", 100, 100, 100, "veg_sandwich_solo", isVeg = true, spiceLevel = "Mild"),
            item("sandwich_veg_grilled", "Veg Grilled Sandwich", "Butter-grilled jumbo sandwich loaded with spiced vegetables, melted cheese & tangy sandwich masala.", "Burgers", 150, 150, 150, "chicken_burger_sandwich", isVeg = true, isBestseller = true),
            item("sandwich_chicken_grilled", "Chicken Grilled Sandwich", "Toasted artisan bread loaded with spiced grilled chicken chunks, melted cheese, bell peppers & herb mayo.", "Burgers", 180, 180, 180, "chicken_grilled_sandwich_closeup", isVeg = false, isBestseller = true),
            item("wrap_paneer_tikka_roll", "Tandoori Paneer Wrap", "Flaky grilled tortilla wrap loaded with smoky paneer tikka cubes, crunchy onions, capsicum & mint mayo dip.", "Burgers", 119, 119, 119, "paneer_wrap", isVeg = true, isBestseller = true),
            item("fries_classic_french", "French Fries", "Crispy golden salted French fries served hot with classic tomato ketchup.", "Burgers", 110, 110, 110, "french_fries_solo", isVeg = true, spiceLevel = "Mild"),
            item("fries_peri_peri_basket", "Peri-Peri French Fries", "Crispy golden French fries tossed in fiery African peri-peri spice blend, served with cheesy jalapeno dip.", "Burgers", 99, 130, 130, "peri_peri_fries", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("egg_boiled_2p", "Boiled Egg (2 Pcs)", "Farm-fresh hard-boiled eggs sliced and seasoned with black pepper, roasted cumin & rock salt.", "Burgers", 25, 60, 60, "boiled_egg_solo", isVeg = false, spiceLevel = "Mild"),
            item("egg_omlet_double", "Omlet Double Egg (2 Pcs)", "Fluffy two-egg masala omelette cooked with finely chopped onions, green chillies, tomatoes & fresh coriander.", "Burgers", 50, 75, 75, "egg_omlet_double_solo", isVeg = false),
            item("egg_fry_2p", "Fry Egg (2 Pcs)", "Sunny-side-up / half-fried double eggs sizzled in butter with cracked black pepper and chilli flakes.", "Burgers", 99, 99, 99, "fry_egg_solo", isVeg = false),
            item("egg_omlet_curry", "Omlet Curry", "Spiced masala omelette simmered in a rich dhaba-style onion tomato gravy with garam masala.", "Burgers", 85, 85, 85, "omlet_curry_solo", isVeg = false, spiceLevel = "Spicy"),
            item("egg_omlet_burgi", "Omlet Burgi (Egg Bhurji)", "Street-style scrambled spiced eggs stir-fried with onions, tomatoes, green chillies, butter & pav masala.", "Burgers", 80, 80, 80, "omlet_burgi_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),

            // =========================================================================
            // 5. GARLIC BREAD, ZINGY PARCEL & NACHO CHIPS (4 individual items — 4 unique studio photos)
            // =========================================================================
            item("garlic_zingy_paneer_parcel", "Zingy Parcel", "Golden-baked triangular puff pastry parcel brushed with garlic herb butter and filled with liquid cheese & spiced paneer.", "Garlic Bread", 49, 49, 49, "zingy_parcel", isVeg = true, isBestseller = true),
            item("garlic_classic_herb_bread", "Garlic Bread", "Oven-toasted crusty baguette slices brushed with roasted garlic herb butter and melted mozzarella cheese.", "Garlic Bread", 109, 109, 109, "classic_garlic_bread_closeup", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("garlic_stuffed_cheesy_bread", "Stuffed Garlic Bread", "Freshly baked golden garlic breadsticks stuffed with molten mozzarella, sweet corn & tangy jalapenos with cheesy dip.", "Garlic Bread", 129, 129, 129, "zingy_garlic", isVeg = true, isBestseller = true),
            item("sides_nacho_chips_cheese", "Nacho Chips", "Crunchy Mexican corn tortilla nacho chips drizzled with warm liquid jalapeno cheddar cheese sauce and tangy salsa.", "Garlic Bread", 99, 99, 99, "nacho_chips_closeup", isVeg = true, isBestseller = true),

            // =========================================================================
            // 6. PASTA, MAGGI, VEG/NON-VEG CHINESE & CHOWMEIN (24 individual items — 24 unique studio photos)
            // =========================================================================
            item("pasta_white_sauce_alfredo", "White Sauce Pasta", "Penne pasta tossed in rich cheesy white béchamel sauce with sweet corn, black olives, capsicum & Italian herbs.", "Pasta", 120, 120, 120, "white_pasta", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("pasta_red_sauce_arrabbiata", "Red Sauce Pasta", "Steaming penne pasta tossed in tangy spiced Italian tomato basil sauce with black olives, bell peppers & parmesan.", "Pasta", 110, 110, 110, "red_sauce_pasta", isVeg = true),
            item("pasta_mixed_sauce_pink", "Mixed Sauce Pasta", "Creamy pink rose sauce pasta blending rich tomato & cheesy Alfredo sauces with sweet corn, bell peppers & melted mozzarella.", "Pasta", 120, 120, 120, "mixed_sauce_pasta", isVeg = true, isBestseller = true),
            item("maggi_plain_special", "Classic Maggi", "Hot comforting classic masala Maggi noodles cooked with signature aromatic spices.", "Pasta", 49, 49, 49, "maggi_plain_solo", isVeg = true, isBestseller = true),
            item("maggi_veg_special", "Veg Maggi", "Masala Maggi noodles cooked with crunchy green peas, carrots, sweet corn, capsicum & fresh coriander.", "Pasta", 59, 59, 59, "veg_masala_maggi_solo", isVeg = true, isBestseller = true),
            item("pasta_cheese_masala_maggi", "Cheese Maggi", "Steaming masala Maggi noodles loaded with garden veggies and a thick layer of melted cheddar & mozzarella cheese.", "Pasta", 79, 79, 79, "cheese_maggi", isVeg = true, isBestseller = true),
            item("chinese_veg_manchurian", "Veg Manchurian F/H (Dry/Gravy)", "Crispy vegetable dumplings tossed in aromatic Indo-Chinese soy, ginger, garlic & spring onion manchurian sauce.", "Pasta", 110, 180, 190, "gobi_veg_manchurian_closeup", isVeg = true, isBestseller = true),
            item("chinese_gobi_manchurian", "Gobi Manchurian", "Crunchy batter-fried cauliflower florets wok-tossed in tangy spicy garlic soy manchurian glaze.", "Pasta", 185, 185, 185, "gobi_manchurian_solo", isVeg = true),
            item("chinese_honey_sesame_chilli_potato", "Honey Sesame Chilli Potato", "Crispy potato wedges tossed in glossy honey chilli garlic sauce and garnished with toasted white sesame seeds.", "Pasta", 185, 185, 185, "honey_chilli_potato_closeup", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("chinese_indo_paneer_manchurian", "Indo Chinese Paneer Manchurian", "Soft cottage cheese cubes fried golden and simmered in rich Indo-Chinese garlic coriander manchurian sauce.", "Pasta", 220, 220, 220, "paneer_manchurian_solo", isVeg = true, isBestseller = true),
            item("chinese_paneer_65_masala", "Paneer 65 Masala", "Hyderabadi-style fiery red spiced Paneer 65 tempered with curry leaves, green chillies & garlic yogurt masala.", "Pasta", 210, 210, 210, "chilli_potato_manchurian", isVeg = true, spiceLevel = "Spicy"),
            item("chinese_paneer_chilly", "Paneer Chilly (Dry/Gravy)", "Wok-tossed cottage cheese cubes with diced bell peppers, onions, green chillies & dark soy chilli garlic sauce.", "Pasta", 205, 205, 205, "paneer_chilly_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("chinese_mushroom_chilly", "Mushroom Chilly (Dry/Gravy)", "Crispy button mushrooms wok-tossed with crunchy capsicum, onions and fiery Indo-Chinese chilli garlic sauce.", "Pasta", 195, 225, 225, "mushroom_chilly_solo", isVeg = true, spiceLevel = "Spicy"),
            item("noodles_veg_chowmin", "Veg Chowmin", "Street-style wok-tossed noodles with julienned cabbage, carrots, capsicum, spring onions & savory soy sauce.", "Pasta", 140, 140, 140, "veg_chowmin_solo", isVeg = true, isBestseller = true),
            item("noodles_jaipuri_chilly_chowmin", "Jaipuri Chilly Chowmin", "Spicy Rajasthani-Chinese fusion noodles tossed with crushed red chillies, crunchy veggies & tangy masala.", "Pasta", 165, 165, 165, "jaipuri_chowmin_solo", isVeg = true, spiceLevel = "Spicy"),
            item("noodles_chilly_garlic_chowmin", "Chilly Garlic Chowmin", "Smoky wok-tossed noodles flavored with burnt brown garlic, fiery red chillies & crisp spring onions.", "Pasta", 165, 165, 165, "schezwan_chicken_chowmein", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("noodles_paneer_hakka", "Paneer Hakka Noodles", "Wok-tossed Indo-Chinese Hakka noodles loaded with golden roasted paneer cubes and crunchy bell peppers.", "Pasta", 185, 185, 185, "chilli_noodles", isVeg = true, isBestseller = true),
            item("noodles_paneer_gravy", "Paneer Gravy Noodles", "Crispy/steamed noodles topped with rich Indo-Chinese vegetable and cottage cheese manchurian gravy.", "Pasta", 185, 185, 185, "paneer_gravy_noodles_solo", isVeg = true),
            item("noodles_veg_manchurian_chowmin", "Veg Manchurian Chowmin (8 Pcs)", "Combo platter of smoky Veg Chowmin served topped with 8 juicy Veg Manchurian balls.", "Pasta", 200, 200, 200, "veg_manchurian_chowmin_solo", isVeg = true, isBestseller = true),
            item("noodles_chicken_chowmin", "Chicken Chowmin F/H", "Wok-tossed noodles loaded with tender shredded chicken, egg ribbons, crunchy cabbage, capsicum & soy glaze.", "Pasta", 180, 240, 240, "chicken_chowmein_closeup", isVeg = false, isBestseller = true),
            item("noodles_chicken_schezwan_chowmin", "Chicken Schezwan Chowmin", "Fiery Schezwan peppercorn wok-tossed chicken noodles with bell peppers and spring onions.", "Pasta", 195, 195, 195, "chicken_schezwan_chowmin_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("noodles_chicken_singapuri_chowmin", "Chicken Singapuri Chowmin", "Aromatic curry-spiced Singapore-style thin noodles tossed with chicken strips, veggies & red chillies.", "Pasta", 195, 195, 195, "chicken_singapuri_chowmin_solo", isVeg = false, spiceLevel = "Spicy"),
            item("noodles_chicken_egg_chowmin", "Chicken Egg Chowmin", "Double-protein street-style chowmin wok-tossed with scrambled eggs, juicy chicken chunks & crisp vegetables.", "Pasta", 200, 200, 200, "chicken_egg_chowmin_solo", isVeg = false, isBestseller = true),
            item("chinese_chicken_manchurian_8pcs", "Chicken Manchurian (8 Pcs)", "Golden-fried boneless chicken dumplings simmered in savory Indo-Chinese ginger garlic soy manchurian sauce.", "Pasta", 245, 249, 249, "chicken_manchurian_closeup", isVeg = false, isBestseller = true),

            // =========================================================================
            // 7. BEVERAGES, SHAKES, PASTRY, DESSERTS, COLDRINKS & SOUPS (30 individual items — 30 unique studio photos)
            // =========================================================================
            item("pastry_black_forest", "Black Forest Pastry", "Freshly whipped cream German chocolate sponge pastry topped with dark chocolate shavings and red cherries.", "Beverages", 59, 59, 59, "black_forest_pastry_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("pastry_chocolate", "Chocolate Pastry", "Rich layered Dutch chocolate truffle pastry glazed with silky dark chocolate ganache.", "Beverages", 69, 69, 69, "chocolate_pastry_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("pastry_butter_scotch", "Butterscotch Pastry", "Moist vanilla sponge layered with golden caramel cream and crunchy butterscotch praline nuts.", "Beverages", 50, 50, 50, "butterscotch_pastry", isVeg = true, spiceLevel = "Mild"),
            item("pastry_white_forest", "White Forest Pastry", "Soft vanilla cream sponge pastry layered with sweet cherries and milky white chocolate curls.", "Beverages", 50, 50, 50, "white_forest_pastry_solo", isVeg = true, spiceLevel = "Mild"),
            item("shake_banana_special", "Banana Shake", "Thick creamy fresh ripe banana milkshake blended with chilled milk, vanilla ice cream & dry fruit garnish.", "Beverages", 99, 99, 99, "banana_shake_solo", isVeg = true, spiceLevel = "Mild"),
            item("shake_oreo_special", "Oreo Shake", "Crunchy Oreo cookies blended with rich vanilla ice cream, chocolate fudge swirl & whipped cream.", "Beverages", 99, 99, 99, "shakes_lava", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("shake_kitkat_special", "Kit-Kat Shake", "Thick chocolate milkshake blended with crispy Kit-Kat wafer bars and topped with chocolate drizzle.", "Beverages", 99, 99, 99, "kitkat_shake", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("shake_cold_coffee", "Cold Coffee", "Frothy café-style chilled cold coffee blended with rich roasted espresso, milk & vanilla ice cream.", "Beverages", 99, 99, 99, "cold_coffee", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("shake_chocolate_special", "Chocolate Shake", "Indulgent Belgian dark chocolate milkshake topped with whipped cream and chocolate chips.", "Beverages", 99, 99, 99, "chocolate_shake_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("dessert_cones", "Cones", "Crispy waffle ice cream cone filled with creamy chocolate and vanilla swirl topped with nuts.", "Beverages", 30, 30, 30, "ice_cream_cone_solo", isVeg = true, spiceLevel = "Mild"),
            item("dessert_choco_muse", "Choco Muse", "Velvety whipped dark chocolate mousse cup topped with cocoa ganache and chocolate curls.", "Beverages", 59, 59, 59, "choco_mousse_cone_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("dessert_butter_scotch_muse", "Butter Scotch Muse", "Creamy caramel butterscotch mousse dessert cup topped with crunchy golden praline pearls.", "Beverages", 59, 59, 59, "butterscotch_muse_solo", isVeg = true, spiceLevel = "Mild"),
            item("dessert_choco_lava_cake_solo", "Choco Lava Cake", "Warm oven-baked dark chocolate cake with a gooey molten chocolate fudge center.", "Beverages", 49, 49, 49, "choco_lava_cake_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("dessert_gulab_jamun", "Gulab Jamun", "Soft melt-in-mouth khoya Gulab Jamun soaked in warm rose and green cardamom sugar syrup.", "Beverages", 25, 25, 25, "gulab_jamun_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("dessert_white_sponges", "White Sponges (Rasgulla)", "Spongy Bengali cottage cheese White Rasgullas served chilled in light cardamom syrup.", "Beverages", 25, 25, 25, "white_sponges_solo", isVeg = true, spiceLevel = "Mild"),
            item("bev_kulhad_lassi", "Lassi", "Thick traditional sweet Punjabi curd lassi served chilled in an earthen clay kulhad with malai & pistachios.", "Beverages", 75, 75, 75, "gulab_jamun", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("bev_mineral_water_1l", "Mineral Water 1L", "Chilled 1-Litre sealed packaged mineral drinking water bottle.", "Beverages", 20, 20, 20, "mineral_water_solo", isVeg = true, spiceLevel = "Mild"),
            item("bev_coldrink_glass_250ml", "Coldrink 250 ML (Glass)", "Chilled 250ml refreshing fizzy soft drink served with ice.", "Beverages", 40, 40, 40, "coldrink_glass_solo", isVeg = true, spiceLevel = "Mild"),
            item("bev_coldrink_can", "Coldrink Can", "Chilled 300ml beverage can served ice-cold.", "Beverages", 60, 60, 60, "coldrink_can_solo", isVeg = true, spiceLevel = "Mild"),
            item("soup_veg_clear", "Veg Clear Soup", "Light, comforting vegetable broth simmered with finely diced carrots, beans, broccoli, ginger & black pepper.", "Beverages", 110, 110, 110, "veg_clear_soup_solo", isVeg = true, spiceLevel = "Mild"),
            item("soup_veg_hot_sour", "Veg Hot & Sour Soup", "Tangy and spicy Indo-Chinese soup loaded with finely chopped vegetables, bamboo shoots, soy & vinegar.", "Beverages", 110, 110, 110, "veg_hot_sour_soup_solo", isVeg = true, spiceLevel = "Spicy"),
            item("soup_veg_manchow", "Veg Manchow Soup", "Thick dark soy garlic soup with minced garden veggies, coriander and a topping of crispy fried noodles.", "Beverages", 125, 125, 125, "manchow_soup_salad", isVeg = true, isBestseller = true),
            item("soup_lemon_coriander", "Lemon Coriander Soup", "Refreshing vitamin-rich clear soup infused with fresh lemon juice, coriander leaves and diced vegetables.", "Beverages", 100, 100, 100, "lemon_coriander_soup_solo", isVeg = true, spiceLevel = "Mild"),
            item("soup_tomato", "Tomato Soup", "Classic velvety roasted vine-tomato soup served piping hot with crunchy buttered bread croutons.", "Beverages", 110, 110, 110, "tomato_soup_solo", isVeg = true, spiceLevel = "Mild"),
            item("soup_tomato_creamy", "Tomato Creamy Soup", "Rich cream of tomato soup finished with fresh dairy cream swirl, herbs and toasted croutons.", "Beverages", 125, 125, 125, "tomato_creamy_soup_solo", isVeg = true, spiceLevel = "Mild"),
            item("soup_mushroom_black_pepper", "Mushroom Black Pepper Soup", "Earthy button mushroom soup simmered with freshly cracked black pepper, garlic & herbs.", "Beverages", 125, 125, 125, "mushroom_pepper_soup_solo", isVeg = true),
            item("soup_chicken_clear", "Chicken Clear Soup", "Nourishing slow-simmered chicken broth with tender shredded chicken, spring onions & black pepper.", "Beverages", 125, 125, 125, "chicken_clear_soup_solo", isVeg = false, spiceLevel = "Mild"),
            item("soup_chicken_hot_sour", "Chicken Hot & Sour Soup", "Spicy and tangy Indo-Chinese chicken soup with shredded chicken, egg drop, veggies & red chilli oil.", "Beverages", 125, 125, 125, "chicken_hot_sour_soup_solo", isVeg = false, spiceLevel = "Spicy"),
            item("soup_chicken_manchow", "Chicken Manchow Soup", "Sizzling dark garlic soy chicken soup topped with crunchy golden fried noodles.", "Beverages", 125, 125, 125, "chicken_manchow_soup_solo", isVeg = false, isBestseller = true),
            item("soup_chicken_sweet_corn", "Chicken Sweet Corn Soup", "Creamy comfort soup of sweet golden corn kernels, tender diced chicken & egg-drop ribbons.", "Beverages", 125, 125, 125, "chicken_sweet_corn_soup_solo", isVeg = false, spiceLevel = "Mild"),

            // =========================================================================
            // 8. INDIAN MAIN COURSE, BIRYANI, RICE/PULAO, DAL, BREADS, SALAD, RAITA & PAPAD (92 individual items — 92 unique studio photos)
            // =========================================================================
            // --- Indian Veg & Paneer Main Course (17 items) ---
            item("main_paneer_masala", "Paneer Masala (8 Pcs)", "Fresh cottage cheese cubes simmered in a spiced onion-tomato masala gravy with aromatic garam masala.", "Combos", 250, 250, 250, "mushroom_masala_mix_veg", isVeg = true, isBestseller = true),
            item("main_paneer_changezi", "Downtown Spl Paneer Changezi (8 Pcs)", "Mughlai-style roasted paneer cubes cooked in a rich, smoky tomato-cashew and roasted spice gravy.", "Combos", 245, 245, 245, "paneer_changezi_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("main_paneer_kadai", "Paneer Kadai (8 Pcs)", "Cottage cheese cubes tossed with crunchy bell peppers, onions & freshly pounded kadai masala in an iron wok.", "Combos", 275, 275, 275, "paneer_kadai_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("main_paneer_handi", "Paneer Handi (8 Pcs)", "Authentic slow-cooked earthen clay pot Paneer Handi in a rich, creamy spiced gravy.", "Combos", 275, 275, 275, "paneer_handi", isVeg = true, isBestseller = true),
            item("main_paneer_punjabi", "Paneer Punjabi (8 Pcs)", "Highway dhaba-style bold and spicy Punjabi paneer curry tempered with whole garlic and kasuri methi.", "Combos", 275, 275, 275, "paneer_punjabi_solo", isVeg = true, spiceLevel = "Spicy"),
            item("main_paneer_do_pyaza", "Paneer Do-Pyaza (8 Pcs)", "Soft paneer cubes cooked with double the caramelized onion petals, tomatoes and roasted coriander seeds.", "Combos", 245, 245, 245, "paneer_do_pyaza_solo", isVeg = true),
            item("main_paneer_kolhapuri", "Paneer Kolhapuri (8 Pcs)", "Fiery Maharashtrian-style Paneer Kolhapuri cooked in roasted sesame, coconut & red Lavangi chilli masala.", "Combos", 275, 275, 275, "paneer_kolhapuri_solo", isVeg = true, spiceLevel = "Spicy"),
            item("main_paneer_lawabdar", "Paneer Lawabdar (8 Pcs)", "Cottage cheese cubes in a luscious, mildly tangy tomato-butter-cashew gravy finished with grated paneer & cream.", "Combos", 275, 275, 275, "malai_kofta_kaju_korma", isVeg = true, isBestseller = true),
            item("main_shahi_paneer", "Shahi Paneer (8 Pcs)", "Royal cottage cheese cubes simmered in a velvety saffron, cardamom and cashew-cream Mughlai gravy.", "Combos", 295, 295, 295, "shahi_paneer", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("main_paneer_malai_kofta", "Paneer Malai Kofta (8 Pcs)", "Stuffed cottage cheese and khoya dumplings served in a rich, creamy saffron cashew gravy.", "Combos", 295, 295, 295, "paneer_malai_kofta_solo", isVeg = true, spiceLevel = "Mild"),
            item("main_paneer_tikka_masala", "Paneer Tikka Masala (8 Pcs)", "Clay-oven charred paneer tikka cubes simmered in a smoky, spiced makhani masala gravy.", "Combos", 320, 320, 320, "paneer_tikka_masala_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("main_kaju_korma", "Kaju Korma (100g)", "Whole golden roasted cashews simmered in a rich, royal white khoya-cashew Mughlai gravy.", "Combos", 350, 350, 350, "kaju_korma_closeup", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("main_malai_kofta", "Malai Kofta (4 Pcs)", "Melt-in-mouth paneer, potato & dry-fruit kofta dumplings in a silky sweet-and-savory cream gravy.", "Combos", 350, 350, 350, "malai_kofta_closeup", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("main_mushroom_do_pyaza", "Mushroom Do-Pyaza (8 Pcs)", "Fresh button mushrooms stir-fried with crunchy onion petals, tomatoes and roasted Indian spices.", "Combos", 245, 245, 245, "mushroom_do_pyaza_closeup", isVeg = true),
            item("main_mushroom_masala", "Mushroom Masala (8 Pcs)", "Juicy button mushrooms simmered in a rich, spiced onion-tomato and cashew masala gravy.", "Combos", 245, 245, 245, "mushroom_masala_solo", isVeg = true),
            item("main_mix_veg", "Mix Veg", "Seasonal garden vegetables, paneer, cauliflower, carrots, beans & green peas in spiced restaurant gravy.", "Combos", 180, 180, 180, "mix_veg_curry_closeup", isVeg = true),
            item("main_party_paneer_tikka_platter", "Downtown Party Spl Double Paneer Tikka Platter (24 Pcs)", "Grand 24-piece Kashmiri saffron double Paneer Tikka party platter served with fresh Caesar salad & dips.", "Combos", 990, 990, 990, "feast_combo", isVeg = true, isBestseller = true),

            // --- Non-Veg Chicken & Chinese Main Course (15 items) ---
            item("main_chicken_do_pyaza", "Chicken Do-Pyaza (H/F)", "Succulent chicken pieces cooked with caramelized double onions, ginger, garlic & whole spices.", "Combos", 180, 245, 245, "chicken_do_pyaza_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_curry_masala", "Chicken Curry Masala (H/F)", "Homestyle spiced Indian chicken curry simmered in a fragrant onion-tomato and garam masala gravy.", "Combos", 240, 320, 320, "chicken_curry_masala_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_handi", "Chicken Handi", "Traditional clay-pot slow-cooked Chicken Handi infused with smoky earthen aroma and whole spices.", "Combos", 265, 265, 265, "chicken_handi", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_kadai", "Chicken Kadai", "Tender chicken wok-tossed with bell peppers, tomatoes & freshly roasted coriander-red chilli kadai masala.", "Combos", 265, 265, 265, "chicken_kadai_solo", isVeg = false, spiceLevel = "Spicy"),
            item("main_chicken_rara", "Chicken Rara", "Punjabi specialty combining succulent chicken pieces and spiced minced chicken keema in a rich gravy.", "Combos", 285, 285, 285, "mutton_rogan_chicken_dehati", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_bharta", "Chicken Bharta", "Kolkata-style shredded tandoori chicken simmered in a creamy, smoky spiced cashew-tomato gravy with boiled egg.", "Combos", 275, 275, 275, "chicken_bharta_solo", isVeg = false),
            item("main_chicken_kasha", "Chicken Kasha (4 Pcs)", "Slow-braised Bengali Chicken Kosha in a dark, caramelized onion, ginger and गरम masala gravy.", "Combos", 275, 275, 275, "chicken_kasha_solo", isVeg = false, spiceLevel = "Spicy"),
            item("main_chicken_afgani_curry", "Chicken Afgani Curry Masala", "Creamy, mildly spiced Mughlai Afghani chicken curry enriched with cashew paste, cream & kasuri methi.", "Combos", 285, 285, 285, "chicken_afgani_curry_solo", isVeg = false, spiceLevel = "Mild"),
            item("main_chicken_dehati", "Chicken Dehati (F/H)", "Authentic Bihar village-style fiery Chicken Dehati cooked with mustard oil, whole garlic pods & roasted spices.", "Combos", 275, 430, 430, "chicken_dehati_closeup", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_murg_musallam", "Chicken Murg Musallam", "Royal feast of whole/large chicken pieces braised in a rich Mughlai almond, saffron, egg & keema masala gravy.", "Combos", 499, 499, 499, "chicken_murg_musallam_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_65_lawabdar", "Chicken 65 Lawabdar", "Crispy spiced Chicken 65 morsels folded into a rich, tangy tomato-butter lawabdar gravy.", "Combos", 275, 275, 275, "chicken_65_lawabdar_solo", isVeg = false, spiceLevel = "Spicy"),
            item("main_chicken_changezi", "Downtown Spl Chicken Changezi", "Old Delhi signature roasted chicken simmered in a rich, tangy cream and roasted spice Changezi gravy.", "Combos", 260, 500, 500, "chicken_changezi_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_chicken_schezwan", "Chicken Schezwan", "Diced chicken wok-tossed in fiery red Sichuan chilli-garlic peppercorn sauce with bell peppers.", "Combos", 245, 245, 245, "chicken_schezwan_main_solo", isVeg = false, spiceLevel = "Spicy"),
            item("main_chicken_garlic", "Chicken Garlic", "Tender chicken chunks stir-fried in a savory brown roasted garlic, soy and spring onion sauce.", "Combos", 240, 240, 240, "chicken_garlic_main_solo", isVeg = false),
            item("main_chicken_mangolian", "Chicken Mangolian", "Crispy chicken strips glazed in a sweet-and-savory Mongolian caramelized soy, ginger & scallion sauce.", "Combos", 245, 245, 245, "chicken_mangolian_main_solo", isVeg = false),

            // --- Mutton Main Course (5 items) ---
            item("main_mutton_handi", "Mutton Handi", "Champaran-style slow-cooked earthen clay pot Mutton Handi with tender goat meat, whole garlic & mustard oil.", "Combos", 375, 375, 375, "mutton_handi_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_mutton_kadai", "Mutton Kadai", "Tender bone-in mutton braised with bell peppers, tomatoes & freshly pounded kadai spices.", "Combos", 375, 375, 375, "mutton_kadai_solo", isVeg = false, spiceLevel = "Spicy"),
            item("main_mutton_rara", "Mutton Rara (Spicy)", "Succulent mutton pieces slow-cooked in a thick, fiery spiced mutton keema gravy.", "Combos", 400, 400, 400, "mutton_rara_lal_maas_closeup", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("main_mutton_korma_spl", "Mutton Korma Spl", "Royal Awadhi-style Mutton Korma slow-braised in browned onion, yogurt, kewra & cardamom gravy.", "Combos", 440, 440, 440, "mutton_korma_spl_solo", isVeg = false, isBestseller = true),
            item("main_downtown_lal_maas", "Downtown Spl Lal Maas", "Fiery Rajasthani royal mutton curry cooked with smoky Mathania red chillies, garlic and desi ghee.", "Combos", 450, 450, 450, "mutton_lal_maas_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),

            // --- Rice, Pulao, Biryani & Dal (24 items) ---
            item("rice_steam_rice", "Steam Rice", "Fluffy long-grain aromatic basmati steamed rice served piping hot.", "Combos", 75, 75, 75, "steam_rice_solo", isVeg = true, spiceLevel = "Mild"),
            item("rice_jeera_rice", "Jeera Rice", "Fragrant basmati rice tempered with royal cumin seeds, desi ghee & fresh coriander leaves.", "Combos", 100, 100, 100, "jeera_fried_rice_closeup", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("rice_veg_fried_rice", "Veg Fried Rice", "Wok-tossed aromatic basmati rice with finely diced carrots, French beans, green peas, sweet corn & spring onions.", "Combos", 149, 149, 149, "veg_fried_rice_solo", isVeg = true, isBestseller = true),
            item("rice_paneer_fried_rice", "Paneer Fried Rice", "Indo-Chinese wok-tossed fried rice loaded with golden roasted paneer cubes and crunchy veggies.", "Combos", 185, 185, 185, "paneer_fried_rice_solo", isVeg = true, isBestseller = true),
            item("rice_chilly_garlic_fried_rice", "Chilly Garlic Fried Rice", "Smoky wok-tossed rice flavored with golden burnt garlic, crushed red chillies & spring onions.", "Combos", 185, 185, 185, "chilly_garlic_fried_rice_solo", isVeg = true, spiceLevel = "Spicy"),
            item("rice_mushroom_onion_fried_rice", "Mushroom Onion Fried Rice", "Fragrant wok-tossed rice with sliced button mushrooms, caramelized onions, black pepper & soy glaze.", "Combos", 185, 185, 185, "mushroom_onion_fried_rice_solo", isVeg = true),
            item("rice_baby_corn_mushroom_fried_rice", "Baby Corn & Mushroom Fried Rice", "Crunchy baby corn and fresh button mushrooms wok-tossed with basmati rice and Indo-Chinese seasonings.", "Combos", 195, 195, 195, "baby_corn_fried_rice_solo", isVeg = true),
            item("rice_chicken_fried_rice", "Chicken Fried Rice", "Classic Indo-Chinese wok-tossed basmati rice with tender diced chicken, egg & spring onions.", "Combos", 199, 199, 199, "chicken_fried_rice_solo", isVeg = false, isBestseller = true),
            item("rice_chicken_egg_fried_rice", "Chicken Egg Fried Rice", "Loaded double-protein fried rice wok-tossed with juicy chicken chunks, scrambled eggs & scallions.", "Combos", 220, 220, 220, "chicken_egg_fried_rice_solo", isVeg = false, isBestseller = true),
            item("rice_green_pea_pulao", "Green Pea Pulao", "Fragrant basmati pulao cooked with sweet green peas, whole cumin, bay leaf, cloves & desi ghee.", "Combos", 165, 165, 165, "green_pea_pulao_solo", isVeg = true, spiceLevel = "Mild"),
            item("rice_sahi_pulao", "Sahi Pulao", "Royal saffron-infused basmati pulao studded with roasted cashews, almonds, raisins & paneer cubes.", "Combos", 200, 200, 200, "sahi_pulao_solo", isVeg = true, spiceLevel = "Mild"),
            item("rice_kashmiri_pulao", "Kashmiri Pulao", "Aromatic mildly sweet Kashmiri basmati pulao garnished with saffron, dry fruits, toasted nuts & fruits.", "Combos", 220, 220, 220, "kashmiri_pulao_solo", isVeg = true, spiceLevel = "Mild"),
            item("biryani_paneer_biryani", "Paneer Biryani", "Layered dum-cooked saffron basmati biryani with marinated tandoori paneer cubes, mint & caramelized onions.", "Combos", 200, 200, 200, "paneer_biryani_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("biryani_egg_biryani", "Egg Biryani", "Fragrant dum basmati rice cooked with spiced roasted boiled eggs, whole garam masala, mint & raita.", "Combos", 240, 240, 240, "egg_biryani_solo", isVeg = false, spiceLevel = "Spicy"),
            item("biryani_chicken_leg_biryani", "Chicken Leg Biryani", "Spiced basmati biryani served with a juicy marinated whole chicken leg piece, birista & raita.", "Combos", 200, 200, 200, "chicken_leg_biryani_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("biryani_chicken_dum_hyderabadi", "Chicken Dum Biryani Hyderabadi", "Authentic Nizami Kacchi-style Hyderabadi Chicken Dum Biryani with saffron, mint, fried onions & raita.", "Combos", 250, 250, 250, "dum_biryani", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("biryani_chicken_dum_kolkata", "Chicken Dum Biryani Kolkata", "Royal Kolkata-style Chicken Dum Biryani with fragrant basmati, tender chicken, golden potato & boiled egg.", "Combos", 250, 250, 250, "chicken_dum_kolkata_solo", isVeg = false, isBestseller = true),
            item("rice_downtown_chef_spl_fried_rice", "Downtown Chef Spl Fried Rice (Red Hot Sauce)", "Signature chef-special veg fried rice prepared in fiery red hot Schezwan garlic sauce with paneer & veggies.", "Combos", 265, 265, 265, "chef_spl_fried_rice_solo", isVeg = true, isBestseller = true, spiceLevel = "Spicy"),
            item("rice_downtown_chef_spl_chicken_fried_rice", "Downtown Chef Spl Chicken Fried Rice (Red Hot Sauce)", "Signature chef-special chicken fried rice tossed in fiery red hot sauce with chicken, egg & bell peppers.", "Combos", 275, 275, 275, "chef_spl_chicken_fried_rice_solo", isVeg = false, isBestseller = true, spiceLevel = "Spicy"),
            item("dal_fry", "Dal Fry", "Comforting yellow toor and moong lentils simmered with onions, tomatoes, ginger, garlic & coriander.", "Combos", 95, 95, 95, "dal_fry_solo", isVeg = true, spiceLevel = "Mild"),
            item("dal_tarka", "Dal Tarka", "Dhaba-style yellow lentils double-tempered in desi ghee with cumin, garlic, asafoetida & whole red chillies.", "Combos", 110, 110, 110, "dal_makhni_jeera_rice", isVeg = true, isBestseller = true),
            item("dal_plain", "Plain Dal", "Homestyle lightly seasoned steamed yellow lentils with turmeric and cumin.", "Combos", 75, 75, 75, "plain_dal_solo", isVeg = true, spiceLevel = "Mild"),
            item("dal_makhni", "Dal Makhni", "Slow-simmered overnight black urad lentils and rajma enriched with white butter, tomato puree & fresh cream.", "Combos", 140, 140, 140, "dal_makhni_closeup", isVeg = true, isBestseller = true),
            item("dal_bukhara", "Dal Bukhara", "Velvety Peshawari-style whole black lentils slow-cooked over charcoal with ginger, tomatoes & dolloped butter.", "Combos", 120, 120, 120, "dal_bukhara_solo", isVeg = true, isBestseller = true),

            // --- Tandoori Breads: Roti, Naan, Paratha & Kulcha (18 items) ---
            item("bread_plain_tawa_roti", "Plain Tawa Roti", "Soft whole-wheat homestyle phulka roti cooked fresh on a hot iron tawa.", "Combos", 10, 10, 10, "plain_tawa_roti_solo", isVeg = true, spiceLevel = "Mild"),
            item("bread_tandoori_roti", "Tandoori Roti", "Crisp whole-wheat flatbread baked against the glowing walls of our clay tandoor.", "Combos", 15, 15, 15, "tandoori_roti_solo", isVeg = true, spiceLevel = "Mild"),
            item("bread_tandoori_butter_roti", "Tandoori Butter Roti", "Clay-oven baked whole-wheat tandoori roti generously brushed with melted butter.", "Combos", 18, 18, 18, "tandoori_butter_roti_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("bread_missi_roti", "Missi Roti", "Traditional Rajasthani/Punjabi spiced gram-flour and whole-wheat flatbread with ajwain, onions & coriander.", "Combos", 45, 45, 45, "missi_roti_solo", isVeg = true),
            item("bread_desi_ghee_roti", "Desi Ghee Roti", "Piping hot tandoori/tawa whole-wheat roti lavishly basted with pure cow desi ghee.", "Combos", 50, 50, 50, "desi_ghee_roti_solo", isVeg = true, spiceLevel = "Mild"),
            item("bread_plain_naan", "Naan", "Soft, pillowy leavened white-flour flatbread baked fresh in the clay tandoor.", "Combos", 45, 45, 45, "plain_naan_solo", isVeg = true, spiceLevel = "Mild"),
            item("bread_butter_naan", "Butter Naan", "Classic clay-oven baked soft tandoori naan glazed with melted creamy butter.", "Combos", 50, 50, 50, "butter_naan_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("bread_garlic_naan", "Garlic Naan", "Leavened tandoori naan topped with minced roasted garlic, fresh coriander & melted butter.", "Combos", 90, 90, 90, "nacho_garlic_bread", isVeg = true, isBestseller = true),
            item("bread_veg_stuff_naan", "Veg Stuff. Naan", "Tandoori naan stuffed with spiced mashed garden vegetables, herbs and melted butter.", "Combos", 90, 90, 90, "veg_stuff_naan_solo", isVeg = true),
            item("bread_kashmiri_naan", "Kashmiri Naan", "Royal sweet tandoori naan stuffed with cashews, almonds, glazed cherries, raisins & khoya.", "Combos", 100, 100, 100, "kashmiri_naan_solo", isVeg = true, spiceLevel = "Mild"),
            item("bread_paneer_stuff_naan", "Paneer Stuff Naan", "Tandoori naan generously stuffed with spiced grated cottage cheese, green chillies & coriander.", "Combos", 100, 100, 100, "paneer_stuff_naan_solo", isVeg = true, isBestseller = true),
            item("bread_aloo_paratha", "Aloo Paratha", "Golden tawa/tandoori flatbread stuffed with spiced mashed potatoes, green chillies, cumin & butter.", "Combos", 50, 50, 50, "aloo_paratha_solo", isVeg = true, isBestseller = true),
            item("bread_paneer_butter_paratha", "Paneer Butter Paratha", "Flaky whole-wheat paratha stuffed with seasoned grated paneer and topped with a slab of white butter.", "Combos", 75, 75, 75, "paneer_butter_paratha_solo", isVeg = true, isBestseller = true),
            item("bread_gobi_paratha", "Gobi Paratha", "Crisp whole-wheat paratha stuffed with spiced grated cauliflower, ginger, ajwain & fresh coriander.", "Combos", 75, 75, 75, "gobi_paratha_solo", isVeg = true),
            item("bread_lachha_paratha", "Lachha Paratha", "Multi-layered flaky whole-wheat tandoori Lachha Paratha brushed with butter.", "Combos", 45, 45, 45, "lachha_aloo_paratha_closeup", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("bread_masala_kulcha", "Masala Kulcha", "Soft tandoori Amritsari kulcha stuffed with spiced potatoes, herbs & crushed coriander seeds.", "Combos", 65, 65, 65, "garlic_naan_kulcha", isVeg = true, isBestseller = true),
            item("bread_onion_masala_kulcha", "Onion Masala Kulcha", "Tandoori kulcha stuffed with caramelized spiced onions, green chillies, amchoor & butter.", "Combos", 75, 75, 75, "onion_masala_kulcha_solo", isVeg = true),
            item("bread_paneer_masala_kulcha", "Paneer Masala Kulcha", "Crisp golden Amritsari kulcha stuffed with spiced cottage cheese, herbs and melted butter.", "Combos", 85, 85, 85, "paneer_masala_kulcha_solo", isVeg = true, isBestseller = true),

            // --- Salad, Raita, Papad & Extra (13 items) ---
            item("salad_green_salad", "Green Salad", "Freshly sliced garden cucumbers, vine tomatoes, carrots, red onions, green chillies & lemon wedges.", "Combos", 70, 70, 70, "green_salad_raita_solo", isVeg = true, spiceLevel = "Mild"),
            item("salad_onion_cucumber", "Onion Cucumber Salad", "Crisp chilled cucumber slices and red onion rings dusted with black salt & lemon juice.", "Combos", 65, 65, 65, "onion_cucumber_salad_solo", isVeg = true, spiceLevel = "Mild"),
            item("salad_fruit_mix", "Fruit Mix Salad", "Bowl of fresh seasonal cut fruits including apple, pineapple, pomegranate, banana & kiwi.", "Combos", 80, 80, 80, "fruit_mix_salad_solo", isVeg = true, spiceLevel = "Mild"),
            item("raita_cucumber", "Cucumber Raita", "Chilled whipped curd mixed with grated fresh cucumber, roasted cumin powder & mint.", "Combos", 65, 65, 65, "cucumber_raita_solo", isVeg = true, spiceLevel = "Mild"),
            item("raita_onion", "Onion Raita", "Creamy whisked yogurt with finely chopped crunchy red onions, green chillies & roasted jeera.", "Combos", 65, 65, 65, "onion_raita_solo", isVeg = true, spiceLevel = "Mild"),
            item("raita_mix_veg", "Mix Veg Raita", "Whisked chilled curd loaded with diced cucumber, tomatoes, onions, carrots, coriander & chaat masala.", "Combos", 70, 70, 70, "mix_veg_raita_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("raita_boondi", "Boondi Raita", "Spiced chilled yogurt with crispy golden gram-flour boondi pearls, roasted cumin & black salt.", "Combos", 70, 70, 70, "boondi_raita_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild"),
            item("papad_roasted", "Roasted Papad", "Crisp fire-roasted urad dal pepper papad.", "Combos", 20, 20, 20, "roasted_papad_solo", isVeg = true, spiceLevel = "Mild"),
            item("papad_fried", "Fried Papad", "Golden deep-fried crunchy urad dal papad.", "Combos", 25, 25, 25, "fried_papad_solo", isVeg = true, spiceLevel = "Mild"),
            item("papad_masala", "Masala Papad", "Crispy papad topped with finely chopped onions, tomatoes, green chillies, coriander & tangy chaat masala.", "Combos", 30, 30, 30, "masala_papad_solo", isVeg = true, isBestseller = true),
            item("extra_mint_chutney", "Small Bowl of Mint Chutney", "Freshly ground green mint, coriander, green chilli, ginger & tangy lemon dip.", "Combos", 25, 25, 25, "mint_chutney_solo", isVeg = true),
            item("extra_onion_rings_chaat", "Onion Rings with Chat Masala", "Crunchy red onion rings tossed with tangy chaat masala, Kashmiri chilli, lemon & coriander.", "Combos", 35, 35, 35, "onion_rings_chaat_solo", isVeg = true),
            item("salad_chef_special_seasoned", "Chef Special Seasoned Salad", "Gourmet salad of paneer cubes, cucumbers, cherry tomatoes, olives & lettuce in herb vinaigrette.", "Combos", 125, 125, 125, "chef_seasoned_salad_solo", isVeg = true, isBestseller = true, spiceLevel = "Mild")
        )
    }
}

package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Formats integer amounts into Indian Rupees (₹ / INR) using Indian numbering conventions.
 */
fun formatInr(amountInr: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"))
    return "₹${formatter.format(amountInr)}"
}

enum class AppRole(val label: String) {
    CUSTOMER("Customer"),
    DELIVERY("Delivery Partner"),
    ADMIN("Store Admin")
}

enum class PizzaSize(val label: String, val slices: String) {
    REGULAR("Regular 7\"", "4 Slices"),
    MEDIUM("Medium 10\"", "6 Slices"),
    LARGE("Large 13\"", "8 Slices")
}

enum class PizzaCategory(
    val id: String,
    val title: String,
    val hindiSubtitle: String,
    val exploreBadge: String,
    val imageKey: String
) {
    VEG_PIZZA("Veg Pizza", "Veg Pizza & Teaser", "Paneer, Mushroom, Farmhouse & Teasers", "FROM ₹89", "peppy_paneer"),
    CHEESE_PIZZA("Cheese Pizza", "Cheese & Kulhad Pizza", "Margherita, Double Cheese & Kulhad", "FROM ₹109", "kulhad_pizza"),
    SPECIAL_PIZZA("Special Pizza", "My Crush Specials", "My Crush Best Seller & Spicy Tango", "BEST SELLER", "crush_special"),
    BURGERS("Burgers", "Burgers, Sandwich & Wrap", "Burger, Sandwich, Wrap & Fries", "FROM ₹69", "grilled_sandwich"),
    GARLIC_BREAD("Garlic Bread", "Sides, Garlic Bread & Starters", "Zingy Parcel, Garlic Bread & Kababs", "FROM ₹49", "zingy_garlic"),
    PASTA("Pasta", "Pasta, Chinese & Main Course", "White/Red Pasta, Chowmein, Paneer & Biryani", "HOT & FRESH", "chilli_noodles"),
    BEVERAGES("Beverages", "Shakes, Pastry & Desserts", "Oreo Shake, Cold Coffee, Pastry & Lava Cake", "FROM ₹30", "shakes_lava"),
    COMBOS("Combos", "Combos & Party Platters", "Family Feast Combos & Tikka Platters", "SAVE ₹250", "feast_combo");

    companion object {
        fun fromCategoryName(name: String): PizzaCategory {
            return entries.firstOrNull { it.id.equals(name, ignoreCase = true) } ?: VEG_PIZZA
        }
    }
}

enum class OrderStatus(
    val code: String,
    val stepIndex: Int,
    val title: String,
    val subtitle: String
) {
    CONFIRMED(
        "CONFIRMED",
        0,
        "Order Confirmed",
        "Your order is received at My Crush Pizza – RN College Chautha Road, Hajipur (+91 95238 41563)."
    ),
    PREPARING(
        "PREPARING",
        1,
        "Preparing",
        "Our chef is freshly preparing and baking your hot order."
    ),
    READY(
        "READY",
        2,
        "Ready",
        "Baked to golden perfection and hot out of the oven."
    ),
    ASSIGNED(
        "ASSIGNED",
        3,
        "Packing",
        "Hygienically packing your hot order with oregano, chilli flakes & dips."
    ),
    PICKED_UP(
        "PICKED_UP",
        4,
        "Reaching Soon",
        "Dispatched from our Hajipur kitchen and reaching your address soon."
    ),
    ON_THE_WAY(
        "ON_THE_WAY",
        4,
        "Reaching Soon",
        "Dispatched from our Hajipur kitchen and reaching your address soon."
    ),
    DELIVERED(
        "DELIVERED",
        5,
        "Delivered",
        "Order delivered! Enjoy your hot meal from My Crush Pizza – Hajipur."
    ),
    CANCELLED(
        "CANCELLED",
        -1,
        "Order Cancelled",
        "Your order was cancelled by My Crush Pizza – Hajipur."
    );

    companion object {
        val trackingSteps: List<OrderStatus> = listOf(
            CONFIRMED,
            PREPARING,
            READY,
            ASSIGNED,
            ON_THE_WAY,
            DELIVERED
        )

        fun fromCode(code: String): OrderStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CONFIRMED
        }
    }
}

enum class WalletBalanceType(val code: String, val label: String) {
    DEPOSIT("DEPOSIT", "Deposit Balance (Added Cash)"),
    BONUS("BONUS", "Bonus Balance (Reward Cash)")
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val defaultAddress: String = "",
    val defaultLat: Double = 25.6892,
    val defaultLng: Double = 85.2145,
    val role: String = "CUSTOMER",
    val vehicleNumber: String = "",
    val depositBalanceInr: Int = 0,
    val bonusBalanceInr: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreConfig(
    @DocumentId val id: String = "",
    val storeName: String = "MY CRUSH PIZZA – HAJIPUR",
    val branchAddress: String = "RN College Chautha Road, Hajipur, Bihar 844101",
    val storePhone: String = "+91 95238 41563",
    val openTime: String = "10:00 AM",
    val closeTime: String = "11:00 PM",
    val isOpen: Boolean = true,
    val deliveryChargeInr: Int = 25,
    val freeDeliveryMinOrderInr: Int = 299,
    val taxPercent: Int = 5,
    val storeLat: Double = 25.68342,
    val storeLng: Double = 85.21804,
    val razorpayEnabled: Boolean = false,
    val razorpayKeyId: String = "",
    val razorpayMerchantName: String = "My Crush Pizza – Hajipur",
    val razorpayPaymentLink: String = "",
    val razorpayMode: String = "LIVE",
    val merchantUpiId: String = "mycrushpizza@upi",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MenuItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Veg Pizza",
    val priceRegularInr: Int = 199,
    val priceMediumInr: Int = 349,
    val priceLargeInr: Int = 499,
    val imageKey: String = "hero_pizza",
    val imageUrl: String = "",
    val isVeg: Boolean = true,
    val isBestseller: Boolean = false,
    val isAvailable: Boolean = true,
    val spiceLevel: String = "Medium",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun priceForSize(size: PizzaSize): Int = when (size) {
        PizzaSize.REGULAR -> priceRegularInr
        PizzaSize.MEDIUM -> priceMediumInr
        PizzaSize.LARGE -> priceLargeInr
    }

    fun supportsMultipleSizes(): Boolean {
        return category in listOf("Veg Pizza", "Cheese Pizza", "Special Pizza") &&
            !id.startsWith("starter_") &&
            (priceRegularInr != priceMediumInr || priceMediumInr != priceLargeInr)
    }

    fun displaySubCategoryLabel(): String = when {
        id.startsWith("teaser_") -> "PIZZA TEASER"
        id.startsWith("veg_") -> "VEG PIZZA"
        id.contains("kulhad_pizza") -> "KULHAD PIZZA"
        id.startsWith("cheese_") -> "CHEESE PIZZA"
        id.startsWith("special_") -> "SPECIAL PIZZA"
        id.startsWith("starter_chicken_") -> "CHICKEN STARTER"
        id.startsWith("starter_spring") || id.startsWith("starter_paneer_hong") ||
            id.startsWith("starter_finger") || id.startsWith("starter_crispy_baby") ||
            id.startsWith("starter_chilly_baby") -> "CHINESE VEG STARTER"
        id.startsWith("starter_") -> "VEG PAKORA STARTER"
        id.startsWith("burger_") -> "BURGER"
        id.startsWith("sandwich_") -> "GRILLED SANDWICH"
        id.startsWith("wrap_") -> "WRAP & ROLL"
        id.startsWith("fries_") -> "FRENCH FRIES"
        id.startsWith("egg_") -> "EGG & OMLET"
        id.contains("zingy") -> "ZINGY PARCEL"
        id.contains("nacho") -> "NACHO CHIPS"
        id.startsWith("garlic_") -> "GARLIC BREAD"
        id.startsWith("pasta_cheese_masala_maggi") || id.startsWith("maggi_") -> "SPECIAL MAGGI"
        id.startsWith("pasta_") -> "ITALIAN PASTA"
        id.startsWith("noodles_") -> "CHOWMEIN & NOODLES"
        id.startsWith("chinese_") -> "CHINESE MAIN & STARTER"
        id.startsWith("pastry_") -> "FRESH PASTRY"
        id.startsWith("shake_") -> "MILKSHAKE & COFFEE"
        id.startsWith("dessert_") -> "SWEET DESSERT"
        id.startsWith("bev_") -> "CHILLED BEVERAGE"
        id.startsWith("soup_") -> "HOT SOUP"
        id.startsWith("main_paneer") || id.startsWith("main_shahi") ||
            id.startsWith("main_kaju") || id.startsWith("main_malai") ||
            id.startsWith("main_mushroom") || id.startsWith("main_mix_veg") ||
            id.startsWith("main_party_paneer") -> "INDIAN VEG MAIN COURSE"
        id.startsWith("main_chicken") -> "NON-VEG CHICKEN MAIN COURSE"
        id.startsWith("main_mutton") || id.startsWith("main_downtown_lal") -> "MUTTON MAIN COURSE"
        id.startsWith("rice_") -> "RICE & PULAO"
        id.startsWith("biryani_") -> "DUM BIRYANI"
        id.startsWith("dal_") -> "TADKA DAL"
        id.startsWith("bread_") -> "TANDOORI BREAD, NAAN & PARATHA"
        id.startsWith("salad_") -> "FRESH SALAD"
        id.startsWith("raita_") -> "CHILLED RAITA"
        id.startsWith("papad_") -> "CRISPY PAPAD"
        id.startsWith("extra_") -> "ACCOMPANIMENTS"
        else -> category.uppercase()
    }

    fun preparationSubtitle(): String = when {
        id.startsWith("bev_") || id.startsWith("shake_") || id.startsWith("pastry_") ||
            id.startsWith("dessert_") || id.startsWith("salad_") || id.startsWith("raita_") ->
            "Served fresh & chilled • My Crush Hajipur"
        supportsMultipleSizes() || id.startsWith("garlic_") || id.startsWith("bread_") ->
            "Freshly baked in Hajipur • Spice: $spiceLevel"
        else ->
            "Prepared hot & fresh in Hajipur • Spice: $spiceLevel"
    }
}

@IgnoreExtraProperties
data class Coupon(
    @DocumentId val id: String = "",
    val code: String = "",
    val title: String = "",
    val description: String = "",
    val discountType: String = "FLAT", // FLAT or PERCENT
    val discountValue: Int = 50,
    val minOrderInr: Int = 249,
    val maxDiscountInr: Int = 100,
    val isActive: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun calculateDiscount(subtotalInr: Int): Int {
        if (!isActive || subtotalInr < minOrderInr) return 0
        return if (discountType.equals("PERCENT", ignoreCase = true)) {
            val raw = (subtotalInr * discountValue) / 100
            raw.coerceAtMost(maxDiscountInr).coerceAtMost(subtotalInr)
        } else {
            discountValue.coerceAtMost(maxDiscountInr).coerceAtMost(subtotalInr)
        }
    }
}

@IgnoreExtraProperties
data class DeliveryPartner(
    @DocumentId val id: String = "",
    val partnerUid: String = "",
    val name: String = "",
    val phone: String = "",
    val pinCode: String = "2468",
    val vehicleNumber: String = "BR-31-M-4821",
    val isActive: Boolean = true,
    val isAvailable: Boolean = true,
    val currentLat: Double = 25.68342,
    val currentLng: Double = 85.21804,
    val activeOrderId: String = "",
    val completedDeliveries: Int = 0,
    val totalEarningsInr: Int = 0,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PizzaOrder(
    @DocumentId val id: String = "",
    val orderNumber: String = "",
    val barcodeValue: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val landmark: String = "",
    val customerLat: Double = 25.6892,
    val customerLng: Double = 85.2145,
    val itemsSummary: String = "",
    val itemsCount: Int = 1,
    val subtotalInr: Int = 0,
    val discountInr: Int = 0,
    val couponCode: String = "",
    val deliveryFeeInr: Int = 0,
    val taxInr: Int = 0,
    val totalInr: Int = 0,
    val depositUsedInr: Int = 0,
    val bonusUsedInr: Int = 0,
    val paymentMethod: String = "UPI",
    val paymentStatus: String = "PAID",
    val status: String = "CONFIRMED",
    val deliveryPartnerId: String = "",
    val deliveryPartnerName: String = "",
    val deliveryPartnerPhone: String = "",
    val deliveryPartnerVehicle: String = "",
    val deliveryLat: Double = 25.68342,
    val deliveryLng: Double = 85.21804,
    val isLocationSharingActive: Boolean = false,
    val estimatedMinutes: Int = 35,
    val distanceKm: Double = 1.5,
    val adminAlertAcknowledged: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val orderStatus: OrderStatus
        get() = OrderStatus.fromCode(status)

    val isTerminal: Boolean
        get() = orderStatus == OrderStatus.DELIVERED || orderStatus == OrderStatus.CANCELLED

    /**
     * Automatically advances an active order by 1 step every 5 minutes along:
     * CONFIRMED -> PREPARING -> READY -> ASSIGNED (Packing) -> ON_THE_WAY (Reaching Soon).
     * Strictly caps at ON_THE_WAY so the final DELIVERED status is ONLY set manually by Admin.
     */
    fun autoProgressedOrderStatus(nowMillis: Long = System.currentTimeMillis()): OrderStatus {
        val current = orderStatus
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) return current
        val normalizedCurrent = if (current == OrderStatus.PICKED_UP) OrderStatus.ON_THE_WAY else current
        val currentIdx = AUTO_PROGRESS_PIPELINE.indexOf(normalizedCurrent)
        if (currentIdx < 0 || currentIdx >= AUTO_PROGRESS_PIPELINE.lastIndex) return normalizedCurrent

        val referenceMillis = (updatedAt ?: createdAt)?.toDate()?.time ?: return normalizedCurrent
        val elapsedMillis = (nowMillis - referenceMillis).coerceAtLeast(0L)
        val stepsToAdvance = (elapsedMillis / AUTO_PROGRESS_STEP_INTERVAL_MS).toInt()
        if (stepsToAdvance <= 0) return normalizedCurrent

        val targetIdx = (currentIdx + stepsToAdvance).coerceAtMost(AUTO_PROGRESS_PIPELINE.lastIndex)
        return AUTO_PROGRESS_PIPELINE[targetIdx]
    }

    /**
     * Returns the next single step to persist in Firestore when at least 5 minutes have elapsed,
     * or null if no auto-advancement is due (never returns DELIVERED).
     */
    fun nextAutoProgressStatus(nowMillis: Long = System.currentTimeMillis()): OrderStatus? {
        val current = orderStatus
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) return null
        val normalizedCurrent = if (current == OrderStatus.PICKED_UP) OrderStatus.ON_THE_WAY else current
        val currentIdx = AUTO_PROGRESS_PIPELINE.indexOf(normalizedCurrent)
        if (currentIdx < 0 || currentIdx >= AUTO_PROGRESS_PIPELINE.lastIndex) return null

        val progressed = autoProgressedOrderStatus(nowMillis)
        return if (progressed != normalizedCurrent) progressed else null
    }

    companion object {
        const val AUTO_PROGRESS_STEP_INTERVAL_MS: Long = 5 * 60 * 1000L

        val AUTO_PROGRESS_PIPELINE: List<OrderStatus> = listOf(
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY,
            OrderStatus.ASSIGNED,
            OrderStatus.ON_THE_WAY
        )
    }
}

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val recipientUid: String = "",
    val orderId: String = "",
    val orderNumber: String = "",
    val title: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val imageKey: String = "",
    val type: String = "ORDER_PLACED",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CartItem(
    val menuItem: MenuItem,
    val size: PizzaSize = PizzaSize.REGULAR,
    val quantity: Int = 1
) {
    val key: String
        get() = "${menuItem.id}_${size.name}"

    val unitPriceInr: Int
        get() = if (menuItem.supportsMultipleSizes()) menuItem.priceForSize(size) else menuItem.priceRegularInr

    val lineTotalInr: Int
        get() = unitPriceInr * quantity
}

data class HajipurLandmark(
    val name: String,
    val addressLine: String,
    val lat: Double,
    val lng: Double
)

object HajipurLocations {
    const val STORE_LOCATION_NAME = "My Crush Pizza Hajipur"
    const val STORE_FULL_ADDRESS = "RN College Chautha Road, Hajipur, Bihar 844101"
    const val STORE_MAPS_SHARE_URL = "https://share.google/ONeglkjGJ5nzSNUSA"
    const val KITCHEN_LAT = 25.68342
    const val KITCHEN_LNG = 85.21804
    const val MAX_DELIVERY_RADIUS_KM = 10.0
    const val DELIVERY_TIME_RANGE_LABEL = "30-45 mins"
    const val DELIVERY_TIME_BADGE_SHORT = "30-45M"
    const val DELIVERY_TIME_NUMBER_RANGE = "30-45"

    val landmarks: List<HajipurLandmark> = listOf(
        HajipurLandmark("My Crush Pizza Hajipur", "RN College Chautha Road, Hajipur, Bihar 844101", 25.68342, 85.21804),
        HajipurLandmark("RN College / Chautha Road", "RN College Chautha Road, Hajipur, Bihar 844101", 25.68342, 85.21804),
        HajipurLandmark("Shahi Colony", "Shahi Colony, Near Dakbunglow Road, Hajipur, Bihar 844101", 25.6886, 85.2132),
        HajipurLandmark("Cinema Road", "Cinema Road Main Market, Hajipur, Bihar 844101", 25.6850, 85.2044),
        HajipurLandmark("Zonal Office Road, Tirhut Division", "Zonal Office Road, Tirhut Division, Hajipur, Bihar 844101", 25.6872, 85.2105),
        HajipurLandmark("Dighikala East", "Dighikala East, Near TRW Enclave & Manoj Villa, Hajipur, Bihar 844102", 25.7045, 85.2260),
        HajipurLandmark("Station Road", "Station Road, Near Hajipur Junction, Hajipur, Bihar 844101", 25.6858, 85.2095),
        HajipurLandmark("Dakbunglow Chowk", "Dakbunglow Road, Near LIC Office, Hajipur, Bihar 844101", 25.6879, 85.2118),
        HajipurLandmark("Gandhi Chowk", "Gandhi Chowk Main Market, Hajipur, Bihar 844101", 25.6836, 85.2062),
        HajipurLandmark("Anjanpeer Chowk", "Anjanpeer Chowk, Lalganj Road, Hajipur, Bihar 844101", 25.6940, 85.2080),
        HajipurLandmark("Ramaashish Chowk", "Ramaashish Chowk, NH-22 Overbridge, Hajipur, Bihar 844101", 25.6982, 85.2215),
        HajipurLandmark("Paswan Chowk", "Paswan Chowk, Industrial Area Road, Hajipur, Bihar 844102", 25.6915, 85.2310),
        HajipurLandmark("Subhash Chowk", "Subhash Chowk, Gudri Bazar, Hajipur, Bihar 844101", 25.6815, 85.2102),
        HajipurLandmark("Yadav Chowk", "Yadav Chowk, Kachahari Road, Hajipur, Bihar 844101", 25.6798, 85.2148),
        HajipurLandmark("Bagmali", "Bagmali Mohalla, Near High School, Hajipur, Bihar 844101", 25.6912, 85.2025),
        HajipurLandmark("Konhara Ghat", "Konhara Ghat Road, Near Sonepur Bridge, Hajipur, Bihar 844101", 25.6762, 85.1984),
        HajipurLandmark("Rajendra Chowk", "Rajendra Chowk, Main Market, Hajipur, Bihar 844101", 25.6844, 85.2055)
    )

    fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val rawKm = earthRadiusKm * c * 1.25 // Road distance factor
        return ((rawKm * 10.0).roundToInt() / 10.0).coerceAtLeast(0.3)
    }

    fun isWithinDeliveryRange(distanceKm: Double): Boolean {
        return distanceKm <= MAX_DELIVERY_RADIUS_KM
    }

    fun isLocationDeliverable(
        customerLat: Double,
        customerLng: Double,
        storeLat: Double = KITCHEN_LAT,
        storeLng: Double = KITCHEN_LNG
    ): Boolean {
        val distKm = calculateDistanceKm(storeLat, storeLng, customerLat, customerLng)
        return isWithinDeliveryRange(distKm)
    }

    fun estimateMinutesForDistance(distanceKm: Double, status: OrderStatus): Int {
        return when (status) {
            OrderStatus.CONFIRMED -> (30 + (distanceKm * 2.5).roundToInt()).coerceIn(30, 45)
            OrderStatus.PREPARING -> (25 + (distanceKm * 2.2).roundToInt()).coerceIn(25, 40)
            OrderStatus.READY, OrderStatus.ASSIGNED -> (18 + (distanceKm * 2.0).roundToInt()).coerceIn(18, 32)
            OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> (8 + (distanceKm * 2.0).roundToInt()).coerceIn(8, 22)
            OrderStatus.DELIVERED, OrderStatus.CANCELLED -> 0
        }
    }
}


package com.example.data.repository

import android.content.Context
import com.mycrushpizza.`in`.R
import com.example.data.model.AppNotification
import com.example.data.model.DeliveryPartner
import com.example.data.model.HajipurLocations
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
import kotlin.random.Random

class OrderAndNotificationRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        landmark: String,
        customerLat: Double,
        customerLng: Double,
        itemsSummary: String,
        itemsCount: Int,
        subtotalInr: Int,
        discountInr: Int,
        couponCode: String,
        deliveryFeeInr: Int,
        taxInr: Int,
        totalInr: Int,
        paymentMethod: String,
        depositUsedInr: Int = 0,
        bonusUsedInr: Int = 0
    ): Result<PizzaOrder> = runCatching {
        val uid = requireUserId()
        val randomCode = Random.nextInt(1000, 9999)
        val orderNumber = "MCP-$randomCode"
        val barcodeValue = orderNumber
        val docRef = db.collection("orders").document()

        val distanceKm = HajipurLocations.calculateDistanceKm(
            HajipurLocations.KITCHEN_LAT,
            HajipurLocations.KITCHEN_LNG,
            customerLat,
            customerLng
        )
        if (!HajipurLocations.isWithinDeliveryRange(distanceKm)) {
            throw IllegalStateException(
                "Delivery Not Available Above 10 km! Your location is $distanceKm km from My Crush Pizza Hajipur (Max 10 km)."
            )
        }
        val estimatedMinutes = HajipurLocations.estimateMinutesForDistance(distanceKm, OrderStatus.CONFIRMED)
        val paymentStatus = if (paymentMethod == "COD" && totalInr > 0) "PENDING" else "PAID"

        val payload = mapOf(
            "orderNumber" to orderNumber,
            "barcodeValue" to barcodeValue,
            "customerId" to uid,
            "customerName" to customerName.ifBlank { "Guest Customer" }.take(100),
            "customerPhone" to customerPhone.take(20),
            "deliveryAddress" to deliveryAddress.take(400),
            "landmark" to landmark.take(150),
            "customerLat" to customerLat,
            "customerLng" to customerLng,
            "itemsSummary" to itemsSummary.take(2000),
            "itemsCount" to itemsCount.coerceAtLeast(1),
            "subtotalInr" to subtotalInr.coerceAtLeast(0),
            "discountInr" to discountInr.coerceAtLeast(0),
            "couponCode" to couponCode.take(30),
            "deliveryFeeInr" to deliveryFeeInr.coerceAtLeast(0),
            "taxInr" to taxInr.coerceAtLeast(0),
            "totalInr" to totalInr.coerceAtLeast(0),
            "depositUsedInr" to depositUsedInr.coerceAtLeast(0),
            "bonusUsedInr" to bonusUsedInr.coerceAtLeast(0),
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus,
            "status" to OrderStatus.CONFIRMED.code,
            "deliveryPartnerId" to "",
            "deliveryPartnerName" to "",
            "deliveryPartnerPhone" to "",
            "deliveryPartnerVehicle" to "",
            "deliveryLat" to HajipurLocations.KITCHEN_LAT,
            "deliveryLng" to HajipurLocations.KITCHEN_LNG,
            "isLocationSharingActive" to false,
            "estimatedMinutes" to estimatedMinutes,
            "distanceKm" to distanceKm,
            "adminAlertAcknowledged" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }

        sendNotification(
            recipientUid = uid,
            orderId = docRef.id,
            orderNumber = orderNumber,
            title = "Order Confirmed ($orderNumber)",
            message = "Your order of ₹$totalInr has been received at My Crush Pizza – Hajipur!",
            type = "ORDER_PLACED"
        )

        val snap = docRef.get().await()
        snap.toObject(
            PizzaOrder::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ) ?: PizzaOrder(
            id = docRef.id,
            orderNumber = orderNumber,
            barcodeValue = barcodeValue,
            customerId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            landmark = landmark,
            customerLat = customerLat,
            customerLng = customerLng,
            itemsSummary = itemsSummary,
            itemsCount = itemsCount,
            subtotalInr = subtotalInr,
            discountInr = discountInr,
            couponCode = couponCode,
            deliveryFeeInr = deliveryFeeInr,
            taxInr = taxInr,
            totalInr = totalInr,
            depositUsedInr = depositUsedInr,
            bonusUsedInr = bonusUsedInr,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            status = OrderStatus.CONFIRMED.code,
            estimatedMinutes = estimatedMinutes,
            distanceKm = distanceKm
        )
    }

    suspend fun getOrderById(orderId: String): Result<PizzaOrder> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(orderId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                throw IllegalStateException("Order not found: $orderId")
            }
            snap.toObject(
                PizzaOrder::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: throw IllegalStateException("Failed to deserialize order $orderId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeCustomerOrders(): Flow<List<PizzaOrder>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("customerId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PizzaOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
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

    suspend fun fetchCustomerOrderHistoryOnce(): Result<List<PizzaOrder>> {
        val path = "orders"
        return try {
            val uid = requireUserId()
            val snapshot = db.collection(path)
                .whereEqualTo("customerId", uid)
                .get()
                .await()
            val orders = snapshot.toObjects(
                PizzaOrder::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            Result.success(orders)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun cancelOwnConfirmedOrder(orderId: String): Result<Unit> {
        var docPath = "orders/$orderId"
        return try {
            requireUserId()
            require(orderId.isNotBlank()) { "Order ID must not be blank" }
            val docRef = db.collection("orders").document(orderId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "status" to OrderStatus.CANCELLED.code,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeAssignedDeliveryOrders(): Flow<List<PizzaOrder>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("deliveryPartnerId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PizzaOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
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

    fun observeAllOrdersForAdmin(): Flow<List<PizzaOrder>> = flow {
        val uid = requireUserId()
        val path = "orders"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PizzaOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
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

    suspend fun acknowledgeAdminOrderAlert(orderId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "adminAlertAcknowledged" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateOrderStatusByAdmin(
        order: PizzaOrder,
        newStatus: OrderStatus
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(order.id)
        val distanceKm = HajipurLocations.calculateDistanceKm(
            order.deliveryLat,
            order.deliveryLng,
            order.customerLat,
            order.customerLng
        )
        val estimatedMinutes = HajipurLocations.estimateMinutesForDistance(distanceKm, newStatus)
        val isLocationSharingActive = newStatus in listOf(
            OrderStatus.ASSIGNED,
            OrderStatus.PICKED_UP,
            OrderStatus.ON_THE_WAY
        )
        val paymentStatus = if (newStatus == OrderStatus.DELIVERED) "PAID" else order.paymentStatus

        try {
            docRef.update(
                mapOf(
                    "status" to newStatus.code,
                    "paymentStatus" to paymentStatus,
                    "isLocationSharingActive" to isLocationSharingActive,
                    "estimatedMinutes" to estimatedMinutes,
                    "distanceKm" to if (newStatus == OrderStatus.DELIVERED) 0.0 else distanceKm,
                    "adminAlertAcknowledged" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }

        val (notifTitle, notifMsg, notifType) = when (newStatus) {
            OrderStatus.PREPARING -> Triple(
                "Order Accepted & Preparing (${order.orderNumber})",
                "Our Hajipur kitchen has accepted your order and started preparing your fresh pizza!",
                "PREPARING"
            )
            OrderStatus.READY -> Triple(
                "Pizza Ready for Dispatch (${order.orderNumber})",
                "Your hot pizza is out of the oven and packed with barcode ${order.barcodeValue}!",
                "READY"
            )
            OrderStatus.ASSIGNED -> Triple(
                "Delivery Boy Assigned (${order.orderNumber})",
                "${order.deliveryPartnerName.ifBlank { "Delivery Partner" }} is assigned to deliver your order.",
                "ASSIGNED"
            )
            OrderStatus.PICKED_UP -> Triple(
                "Order Picked Up (${order.orderNumber})",
                "${order.deliveryPartnerName.ifBlank { "Delivery Partner" }} has picked up your order from RN College Chautha Road.",
                "PICKED_UP"
            )
            OrderStatus.ON_THE_WAY -> Triple(
                "Out for Delivery (${order.orderNumber})",
                "${order.deliveryPartnerName.ifBlank { "Delivery Partner" }} is on the way! Track live GPS on map.",
                "NEARBY"
            )
            OrderStatus.DELIVERED -> Triple(
                "Order Delivered (${order.orderNumber})",
                "Your order has been delivered! Thank you for ordering from My Crush Pizza – Hajipur.",
                "DELIVERED"
            )
            OrderStatus.CANCELLED -> Triple(
                "Order Cancelled (${order.orderNumber})",
                "Your order ${order.orderNumber} has been cancelled by My Crush Pizza – Hajipur.",
                "ORDER_CANCELLED"
            )
            OrderStatus.CONFIRMED -> Triple(
                "Order Confirmed (${order.orderNumber})",
                "Your order is confirmed.",
                "ORDER_PLACED"
            )
        }

        sendNotification(
            recipientUid = order.customerId,
            orderId = order.id,
            orderNumber = order.orderNumber,
            title = notifTitle,
            message = notifMsg,
            type = notifType
        )
    }

    suspend fun assignDeliveryPartnerAndScanBarcode(
        order: PizzaOrder,
        partner: DeliveryPartner,
        targetStatus: OrderStatus = OrderStatus.ASSIGNED
    ): Result<Unit> = runCatching {
        val currentUid = requireUserId()
        val assignedPartnerUid = partner.partnerUid.ifBlank { currentUid }
        val docRef = db.collection("orders").document(order.id)
        val distanceKm = HajipurLocations.calculateDistanceKm(
            partner.currentLat,
            partner.currentLng,
            order.customerLat,
            order.customerLng
        )
        val estimatedMinutes = HajipurLocations.estimateMinutesForDistance(distanceKm, targetStatus)

        try {
            docRef.update(
                mapOf(
                    "status" to targetStatus.code,
                    "deliveryPartnerId" to assignedPartnerUid,
                    "deliveryPartnerName" to partner.name.take(100),
                    "deliveryPartnerPhone" to partner.phone.take(20),
                    "deliveryPartnerVehicle" to partner.vehicleNumber.take(40),
                    "deliveryLat" to partner.currentLat,
                    "deliveryLng" to partner.currentLng,
                    "isLocationSharingActive" to true,
                    "estimatedMinutes" to estimatedMinutes,
                    "distanceKm" to distanceKm,
                    "adminAlertAcknowledged" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }

        sendNotification(
            recipientUid = order.customerId,
            orderId = order.id,
            orderNumber = order.orderNumber,
            title = "Delivery Partner Assigned (${order.orderNumber})",
            message = "${partner.name} (${partner.vehicleNumber}) scanned barcode ${order.barcodeValue} and is assigned to your order!",
            type = "ASSIGNED"
        )
    }

    suspend fun updateDeliveryPartnerOrderStepAndGps(
        order: PizzaOrder,
        newStatus: OrderStatus,
        riderLat: Double,
        riderLng: Double
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(order.id)
        val isDelivered = newStatus == OrderStatus.DELIVERED
        val distanceKm = if (isDelivered) {
            0.0
        } else {
            HajipurLocations.calculateDistanceKm(
                riderLat,
                riderLng,
                order.customerLat,
                order.customerLng
            )
        }
        val estimatedMinutes = if (isDelivered) {
            0
        } else {
            HajipurLocations.estimateMinutesForDistance(distanceKm, newStatus)
        }
        val paymentStatus = if (isDelivered) "PAID" else order.paymentStatus
        val isLocationSharingActive = !isDelivered && newStatus != OrderStatus.CANCELLED

        try {
            docRef.update(
                mapOf(
                    "status" to newStatus.code,
                    "paymentStatus" to paymentStatus,
                    "deliveryLat" to if (isDelivered) order.customerLat else riderLat,
                    "deliveryLng" to if (isDelivered) order.customerLng else riderLng,
                    "isLocationSharingActive" to isLocationSharingActive,
                    "estimatedMinutes" to estimatedMinutes,
                    "distanceKm" to distanceKm,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }

        if (newStatus in listOf(OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY, OrderStatus.DELIVERED)) {
            val (title, msg, type) = when (newStatus) {
                OrderStatus.PICKED_UP -> Triple(
                    "Pizza Picked Up (${order.orderNumber})",
                    "${order.deliveryPartnerName.ifBlank { "Your Rider" }} picked up your pizza from RN College Chautha Road!",
                    "PICKED_UP"
                )
                OrderStatus.ON_THE_WAY -> Triple(
                    "Rider is Nearby (${order.orderNumber})",
                    "${order.deliveryPartnerName.ifBlank { "Your Rider" }} is on the way (${distanceKm} km away, ETA $estimatedMinutes mins).",
                    "NEARBY"
                )
                else -> Triple(
                    "Order Delivered (${order.orderNumber})",
                    "Enjoy your hot pizza! Live GPS sharing has stopped.",
                    "DELIVERED"
                )
            }
            sendNotification(
                recipientUid = order.customerId,
                orderId = order.id,
                orderNumber = order.orderNumber,
                title = title,
                message = msg,
                type = type
            )
        }
    }

    suspend fun sendNotification(
        recipientUid: String,
        orderId: String = "",
        orderNumber: String = "",
        title: String,
        message: String,
        type: String = "PROMO",
        imageUrl: String = "",
        imageKey: String = ""
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("notifications").document()
        val payload = mapOf(
            "recipientUid" to recipientUid.take(128),
            "orderId" to orderId.take(128),
            "orderNumber" to orderNumber.take(40),
            "title" to title.take(150),
            "message" to message.take(600),
            "imageUrl" to imageUrl.take(800),
            "imageKey" to imageKey.take(100),
            "type" to type.take(40),
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeNotificationsForUser(): Flow<List<AppNotification>> = flow {
        val uid = requireUserId()
        val path = "notifications"
        emitAll(
            db.collection(path)
                .whereIn("recipientUid", listOf(uid, "ALL"))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AppNotification::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
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

    suspend fun markNotificationRead(notifId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("notifications").document(notifId)
        try {
            docRef.update("isRead", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    /**
     * Permanently deletes the specified orders from Firestore (Admin-only via Firestore rules).
     * Uses chunked WriteBatch (450 docs max per batch) to safely clear old orders in bulk.
     */
    suspend fun deleteOrdersByAdmin(orderIds: List<String>): Result<Int> = runCatching {
        requireUserId()
        val uniqueIds = orderIds.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (uniqueIds.isEmpty()) return@runCatching 0
        var deletedCount = 0
        for (chunk in uniqueIds.chunked(450)) {
            val batch = db.batch()
            for (id in chunk) {
                batch.delete(db.collection("orders").document(id))
            }
            try {
                batch.commit().await()
                deletedCount += chunk.size
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, "orders")
                throw e
            }
        }
        deletedCount
    }
}

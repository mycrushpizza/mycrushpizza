package com.example.data.repository

import android.content.Context
import com.mycrushpizza.`in`.R
import com.example.data.model.AppRole
import com.example.data.model.DeliveryPartner
import com.example.data.model.HajipurLocations
import com.example.data.model.UserProfile
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
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

class AuthAndUserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun currentUserId(): String? = auth.currentUser?.uid

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    companion object {
        const val ADMIN_EMAIL = "pranavgamer758@gmail.com"
        const val SECOND_ADMIN_EMAIL = "poojajha844101@gmail.com"
        private val ADMIN_PANEL_EMAILS = setOf(
            ADMIN_EMAIL,
            SECOND_ADMIN_EMAIL
        )
        private val ADMIN_EMAILS = setOf(
            ADMIN_EMAIL,
            SECOND_ADMIN_EMAIL,
            "arun1sahu1990@gmail.com"
        )

        fun isBootstrappedAdminPanelEmail(email: String?): Boolean {
            if (email.isNullOrBlank()) return false
            return email.trim().lowercase() in ADMIN_PANEL_EMAILS
        }

        fun isAdminEmail(email: String?): Boolean {
            if (email.isNullOrBlank()) return false
            return email.trim().lowercase() in ADMIN_EMAILS
        }

        fun isAdminUserProfile(profile: UserProfile): Boolean {
            return profile.role.equals(AppRole.ADMIN.name, ignoreCase = true) ||
                isAdminEmail(profile.email)
        }
    }

    fun isBootstrappedAdminEmail(email: String?): Boolean = isBootstrappedAdminPanelEmail(email)

    fun isAdminProfile(profile: UserProfile): Boolean = isAdminUserProfile(profile)

    suspend fun ensureUserProfile(
        displayName: String,
        email: String,
        phone: String = "",
        defaultAddress: String = ""
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val isEmailAdmin = isBootstrappedAdminEmail(email)

        if (isEmailAdmin) {
            val adminRef = db.collection("admins").document(uid)
            try {
                adminRef.set(
                    mapOf(
                        "adminUid" to uid,
                        "email" to email.trim(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (_: Exception) {
                // Non-fatal if admin record already exists or rule rejects non-verified test token
            }
        }

        val existingSnap = try {
            userRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, userRef.path)
            throw e
        }

        val expectedRole = if (isEmailAdmin) AppRole.ADMIN.name else AppRole.CUSTOMER.name

        if (existingSnap.exists()) {
            val existingProfile = existingSnap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: UserProfile(id = uid, userId = uid)
            existingProfile.copy(role = expectedRole)
        } else {
            val initialRole = expectedRole
            val payload = mapOf(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "Hajipur Pizza Lover" }.take(80),
                "email" to email.take(120),
                "phone" to phone.take(20),
                "defaultAddress" to defaultAddress.take(300),
                "defaultLat" to HajipurLocations.KITCHEN_LAT,
                "defaultLng" to HajipurLocations.KITCHEN_LNG,
                "role" to initialRole,
                "vehicleNumber" to "",
                "depositBalanceInr" to 0,
                "bonusBalanceInr" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                userRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, userRef.path)
                throw e
            }
            val saved = userRef.get().await()
            saved.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: UserProfile(
                id = uid,
                userId = uid,
                displayName = displayName.ifBlank { "Hajipur Pizza Lover" },
                email = email,
                phone = phone,
                defaultAddress = defaultAddress,
                role = initialRole,
                depositBalanceInr = 0,
                bonusBalanceInr = 0
            )
        }
    }

    fun observeCurrentUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap ->
                    snap.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
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

    suspend fun updateCustomerAddressAndPhone(
        displayName: String,
        phone: String,
        address: String,
        lat: Double,
        lng: Double
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            ensureUserProfile(
                displayName = displayName,
                email = auth.currentUser?.email ?: "",
                phone = phone,
                defaultAddress = address
            ).getOrThrow()
        }
        val updates = mapOf(
            "displayName" to displayName.ifBlank { "Customer" }.take(80),
            "phone" to phone.take(20),
            "defaultAddress" to address.take(350),
            "defaultLat" to lat,
            "defaultLng" to lng,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    /**
     * Customer self top-up: adds selected payment amount into `depositBalanceInr` (Deposit Wallet).
     */
    suspend fun addCustomerDepositBalance(amountInr: Int): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val cleanAmount = amountInr.coerceIn(1, 100000)
        val docRef = db.collection("users").document(uid)
        val existing = ensureUserProfile(
            displayName = auth.currentUser?.displayName ?: "Hajipur Customer",
            email = auth.currentUser?.email ?: ""
        ).getOrThrow()

        val nextDeposit = (existing.depositBalanceInr + cleanAmount).coerceIn(0, 1000000)
        val updates = mapOf(
            "depositBalanceInr" to nextDeposit,
            "bonusBalanceInr" to existing.bonusBalanceInr.coerceIn(0, 1000000),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
        existing.copy(depositBalanceInr = nextDeposit)
    }

    /**
     * Deducts Deposit Balance and/or Bonus Balance when the customer places an order using wallet funds.
     */
    suspend fun deductWalletBalancesForOrder(
        depositUsedInr: Int,
        bonusUsedInr: Int
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existing = ensureUserProfile(
            displayName = auth.currentUser?.displayName ?: "Hajipur Customer",
            email = auth.currentUser?.email ?: ""
        ).getOrThrow()

        val nextDeposit = (existing.depositBalanceInr - depositUsedInr.coerceAtLeast(0)).coerceAtLeast(0)
        val nextBonus = (existing.bonusBalanceInr - bonusUsedInr.coerceAtLeast(0)).coerceAtLeast(0)
        val updates = mapOf(
            "depositBalanceInr" to nextDeposit,
            "bonusBalanceInr" to nextBonus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
        existing.copy(depositBalanceInr = nextDeposit, bonusBalanceInr = nextBonus)
    }

    /**
     * Store Admin action: Credits any desired amount to a Customer ID's Deposit Balance or Bonus Balance.
     */
    suspend fun adminCreditCustomerWallet(
        targetUserId: String,
        amountInr: Int,
        isBonus: Boolean,
        customerNameHint: String = ""
    ): Result<UserProfile> = runCatching {
        requireUserId()
        val cleanTargetId = targetUserId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        if (cleanTargetId.isBlank()) {
            throw IllegalArgumentException("Customer ID cannot be empty")
        }
        val cleanAmount = amountInr.coerceIn(1, 500000)
        val docRef = db.collection("users").document(cleanTargetId)
        val snap = docRef.get().await()

        if (snap.exists()) {
            val currentProfile = snap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: UserProfile(id = cleanTargetId, userId = cleanTargetId)

            val nextDeposit = if (isBonus) {
                currentProfile.depositBalanceInr
            } else {
                (currentProfile.depositBalanceInr + cleanAmount).coerceIn(0, 1000000)
            }
            val nextBonus = if (isBonus) {
                (currentProfile.bonusBalanceInr + cleanAmount).coerceIn(0, 1000000)
            } else {
                currentProfile.bonusBalanceInr
            }

            try {
                docRef.update(
                    mapOf(
                        "depositBalanceInr" to nextDeposit,
                        "bonusBalanceInr" to nextBonus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }

            currentProfile.copy(
                depositBalanceInr = nextDeposit,
                bonusBalanceInr = nextBonus
            )
        } else {
            val nextDeposit = if (isBonus) 0 else cleanAmount
            val nextBonus = if (isBonus) cleanAmount else 0
            val displayName = customerNameHint.trim().ifBlank { "Customer ($cleanTargetId)" }.take(80)
            val payload = mapOf(
                "userId" to cleanTargetId,
                "displayName" to displayName,
                "email" to "",
                "phone" to "",
                "defaultAddress" to "",
                "defaultLat" to HajipurLocations.KITCHEN_LAT,
                "defaultLng" to HajipurLocations.KITCHEN_LNG,
                "role" to AppRole.CUSTOMER.name,
                "vehicleNumber" to "",
                "depositBalanceInr" to nextDeposit,
                "bonusBalanceInr" to nextBonus,
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
            UserProfile(
                id = cleanTargetId,
                userId = cleanTargetId,
                displayName = displayName,
                role = AppRole.CUSTOMER.name,
                depositBalanceInr = nextDeposit,
                bonusBalanceInr = nextBonus
            )
        }
    }

    /**
     * Store Admin action: Cuts / deducts any desired amount from a Customer ID's Deposit Balance or Bonus Balance.
     */
    suspend fun adminDeductCustomerWallet(
        targetUserId: String,
        amountInr: Int,
        isBonus: Boolean,
        customerNameHint: String = ""
    ): Result<UserProfile> = runCatching {
        requireUserId()
        val cleanTargetId = targetUserId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        if (cleanTargetId.isBlank()) {
            throw IllegalArgumentException("Customer ID cannot be empty")
        }
        val cleanAmount = amountInr.coerceIn(1, 500000)
        val docRef = db.collection("users").document(cleanTargetId)
        val snap = docRef.get().await()

        if (snap.exists()) {
            val currentProfile = snap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: UserProfile(id = cleanTargetId, userId = cleanTargetId)

            val nextDeposit = if (isBonus) {
                currentProfile.depositBalanceInr
            } else {
                (currentProfile.depositBalanceInr - cleanAmount).coerceAtLeast(0)
            }
            val nextBonus = if (isBonus) {
                (currentProfile.bonusBalanceInr - cleanAmount).coerceAtLeast(0)
            } else {
                currentProfile.bonusBalanceInr
            }

            try {
                docRef.update(
                    mapOf(
                        "depositBalanceInr" to nextDeposit,
                        "bonusBalanceInr" to nextBonus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }

            currentProfile.copy(
                depositBalanceInr = nextDeposit,
                bonusBalanceInr = nextBonus
            )
        } else {
            val displayName = customerNameHint.trim().ifBlank { "Customer ($cleanTargetId)" }.take(80)
            UserProfile(
                id = cleanTargetId,
                userId = cleanTargetId,
                displayName = displayName,
                role = AppRole.CUSTOMER.name,
                depositBalanceInr = 0,
                bonusBalanceInr = 0
            )
        }
    }

    suspend fun getUserProfileById(targetUserId: String): Result<UserProfile?> = runCatching {
        requireUserId()
        val docRef = db.collection("users").document(targetUserId)
        try {
            val snap = docRef.get().await()
            snap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeAllUsersForAdmin(): Flow<List<UserProfile>> = flow {
        val uid = requireUserId()
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
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

    suspend fun deleteUsersByAdmin(userIds: List<String>): Result<Int> = runCatching {
        val currentUid = requireUserId()
        val distinctIds = userIds
            .map { it.trim() }
            .filter { it.isNotBlank() && it != currentUid && it.length <= 128 }
            .distinct()
        if (distinctIds.isEmpty()) return@runCatching 0

        var deletedCount = 0
        distinctIds.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { uid ->
                batch.delete(db.collection("users").document(uid))
            }
            try {
                batch.commit().await()
                deletedCount += chunk.size
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.DELETE, "users")
                throw e
            }
        }
        deletedCount
    }

    suspend fun registerOrUpdateDeliveryPartner(
        partnerDocId: String,
        partnerUid: String,
        name: String,
        phone: String,
        pinCode: String,
        vehicleNumber: String,
        isActive: Boolean = true,
        isAvailable: Boolean = true,
        currentLat: Double = HajipurLocations.KITCHEN_LAT,
        currentLng: Double = HajipurLocations.KITCHEN_LNG,
        completedDeliveries: Int = 0,
        totalEarningsInr: Int = 0
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("delivery_partners").document(partnerDocId)
        val payload = mapOf(
            "partnerUid" to partnerUid,
            "name" to name.take(80),
            "phone" to phone.take(15),
            "pinCode" to pinCode.take(10),
            "vehicleNumber" to vehicleNumber.take(30),
            "isActive" to isActive,
            "isAvailable" to isAvailable,
            "currentLat" to currentLat,
            "currentLng" to currentLng,
            "activeOrderId" to "",
            "completedDeliveries" to completedDeliveries,
            "totalEarningsInr" to totalEarningsInr,
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

    suspend fun updateDeliveryPartnerLiveLocation(
        partnerDocId: String,
        lat: Double,
        lng: Double,
        activeOrderId: String,
        isAvailable: Boolean
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("delivery_partners").document(partnerDocId)
        try {
            docRef.update(
                mapOf(
                    "currentLat" to lat,
                    "currentLng" to lng,
                    "activeOrderId" to activeOrderId,
                    "isAvailable" to isAvailable,
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

    fun observeDeliveryPartnersForUser(isAdminUser: Boolean): Flow<List<DeliveryPartner>> = flow {
        val uid = requireUserId()
        val path = "delivery_partners"
        val query = if (isAdminUser) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("partnerUid", uid)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DeliveryPartner::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
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
}

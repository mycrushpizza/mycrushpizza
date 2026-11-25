package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.OrderStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminAlarmActionEvent(
    val orderId: String,
    val orderNumber: String,
    val accepted: Boolean
)

object AdminOrderAlarmEvents {
    private val _events = MutableSharedFlow<AdminAlarmActionEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<AdminAlarmActionEvent> = _events.asSharedFlow()

    fun emitAction(event: AdminAlarmActionEvent) {
        _events.tryEmit(event)
    }
}

/**
 * Handles direct ACCEPT / DECLINE button taps from the Admin Incoming Order Alarm Notification
 * (including from the lock screen or when the phone screen is off).
 */
class AdminOrderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID).orEmpty()
        val orderNumber = intent.getStringExtra(EXTRA_ORDER_NUMBER).orEmpty()
        val customerId = intent.getStringExtra(EXTRA_CUSTOMER_ID).orEmpty()
        if (orderId.isBlank()) return

        val accepted = when (action) {
            ACTION_ACCEPT_ORDER -> true
            ACTION_DECLINE_ORDER -> false
            else -> return
        }

        // 1. Immediately silence the repeating alarm & cancel the ongoing alarm notification
        AdminOrderAlertPlayer.silenceOrderAndStopRinging(orderId)
        NotificationHelper.cancelAdminOrderAlarmNotification(context, orderId)

        // 2. Notify running ViewModel in real time
        AdminOrderAlarmEvents.emitAction(
            AdminAlarmActionEvent(
                orderId = orderId,
                orderNumber = orderNumber,
                accepted = accepted
            )
        )

        // 3. Also persist status directly to Firestore so lockscreen/background action always succeeds
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val uid = try {
                    Firebase.auth.currentUser?.uid
                } catch (_: Exception) {
                    null
                }
                if (!uid.isNullOrBlank()) {
                    val nextStatus = if (accepted) OrderStatus.PREPARING else OrderStatus.CANCELLED
                    val dbId = context.applicationContext.getString(com.mycrushpizza.`in`.R.string.firestore_database_id)
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance(dbId)
                    db.collection("orders")
                        .document(orderId)
                        .update(
                            mapOf(
                                "status" to nextStatus.code,
                                "adminAlertAcknowledged" to true,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .await()

                    if (customerId.isNotBlank()) {
                        db.collection("notifications").add(
                            mapOf(
                                "recipientUid" to customerId,
                                "orderId" to orderId,
                                "orderNumber" to orderNumber,
                                "title" to "${orderNumber.ifBlank { "Order" }}: ${nextStatus.title}",
                                "message" to nextStatus.subtitle,
                                "type" to "STATUS_${nextStatus.code}",
                                "isRead" to false,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    pendingResult.finish()
                } catch (_: Exception) {
                }
            }
        }
    }

    companion object {
        const val ACTION_ACCEPT_ORDER = "com.example.action.ADMIN_ACCEPT_ORDER"
        const val ACTION_DECLINE_ORDER = "com.example.action.ADMIN_DECLINE_ORDER"
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_ORDER_NUMBER = "extra_order_number"
        const val EXTRA_CUSTOMER_ID = "extra_customer_id"
    }
}

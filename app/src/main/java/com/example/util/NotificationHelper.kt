package com.example.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.mycrushpizza.`in`.R
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import kotlin.math.abs
import kotlin.random.Random

object NotificationHelper {
    private const val CHANNEL_ID = "my_crush_pizza_hajipur_orders"
    private const val CHANNEL_NAME = "My Crush Pizza Order Updates"

    private const val ADMIN_ALARM_CHANNEL_ID = "my_crush_pizza_admin_alarm_channel"
    private const val ADMIN_ALARM_CHANNEL_NAME = "Admin Incoming Order Alarm (Rings on Lock Screen)"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val orderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time pizza order status updates (Preparing, Ready, Packing, Reaching Soon)"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(orderChannel)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarmAudioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val adminAlarmChannel = NotificationChannel(
                ADMIN_ALARM_CHANNEL_ID,
                ADMIN_ALARM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loud incoming order alarm for Store Admin with Accept and Decline buttons"
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 500L, 300L, 500L, 300L, 700L)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                if (alarmUri != null) {
                    setSound(alarmUri, alarmAudioAttrs)
                }
            }
            manager.createNotificationChannel(adminAlarmChannel)
        }
    }

    const val MAX_SAME_MESSAGE_PER_CUSTOMER = 2
    private const val PREFS_NAME = "my_crush_notification_caps"

    fun buildMessageCapKey(recipientKey: String, title: String, message: String): String {
        val cleanRecipient = recipientKey.trim().lowercase().ifBlank { "customer" }
        val cleanTitle = title.trim().lowercase()
        val cleanMessage = message.trim().lowercase()
        return "$cleanRecipient|$cleanTitle|$cleanMessage"
    }

    fun showOrderPushNotification(
        context: Context,
        title: String,
        message: String,
        recipientKey: String = "customer",
        notificationDocId: String = ""
    ): Boolean {
        try {
            ensureChannel(context)
            if (!hasNotificationPermission(context)) return false

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (notificationDocId.isNotBlank()) {
                val docKey = "doc_$notificationDocId"
                if (prefs.getBoolean(docKey, false)) {
                    return false
                }
            }

            val capKey = "cnt_${buildMessageCapKey(recipientKey, title, message).hashCode()}"
            val alreadySentCount = prefs.getInt(capKey, 0)
            if (alreadySentCount >= MAX_SAME_MESSAGE_PER_CUSTOMER) {
                return false
            }

            val editor = prefs.edit().putInt(capKey, alreadySentCount + 1)
            if (notificationDocId.isNotBlank()) {
                editor.putBoolean("doc_$notificationDocId", true)
            }
            editor.apply()

            val deterministicId = 20000 + (abs(buildMessageCapKey(recipientKey, title, message).hashCode()) % 50000)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                deterministicId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(deterministicId, notification)
            return true
        } catch (_: Exception) {
            // Safe no-op in headless/test environments
            return false
        }
    }

    private val activeAdminAlarmOrderIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    /**
     * Posts a high-priority full-screen / lock-screen alarm notification for the Store Admin
     * with direct ACCEPT ORDER and DECLINE ORDER action buttons.
     * Strictly deduplicated per orderId so repeated Firestore snapshots do not re-spam the Admin.
     */
    fun showAdminIncomingOrderAlarmNotification(
        context: Context,
        order: PizzaOrder
    ) {
        try {
            ensureChannel(context)
            if (!hasNotificationPermission(context)) return
            if (order.id.isNotBlank() && !activeAdminAlarmOrderIds.add(order.id)) {
                return
            }

            val notificationId = notificationIdForOrder(order.id)

            val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_admin_order_id", order.id)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val acceptIntent = Intent(context, AdminOrderAlarmReceiver::class.java).apply {
                action = AdminOrderAlarmReceiver.ACTION_ACCEPT_ORDER
                putExtra(AdminOrderAlarmReceiver.EXTRA_ORDER_ID, order.id)
                putExtra(AdminOrderAlarmReceiver.EXTRA_ORDER_NUMBER, order.orderNumber)
                putExtra(AdminOrderAlarmReceiver.EXTRA_CUSTOMER_ID, order.customerId)
            }
            val acceptPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val declineIntent = Intent(context, AdminOrderAlarmReceiver::class.java).apply {
                action = AdminOrderAlarmReceiver.ACTION_DECLINE_ORDER
                putExtra(AdminOrderAlarmReceiver.EXTRA_ORDER_ID, order.id)
                putExtra(AdminOrderAlarmReceiver.EXTRA_ORDER_NUMBER, order.orderNumber)
                putExtra(AdminOrderAlarmReceiver.EXTRA_CUSTOMER_ID, order.customerId)
            }
            val declinePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 2,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val customerLabel = order.customerName.ifBlank { "Customer" }
            val title = "🚨 NEW ORDER • ${order.orderNumber} • $customerLabel (${formatInr(order.totalInr)})"
            val body = "$customerLabel (${order.customerPhone}) • ${order.itemsSummary} • Deliver to: ${order.deliveryAddress}"

            val notification = NotificationCompat.Builder(context, ADMIN_ALARM_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(fullScreenPendingIntent)
                .addAction(
                    android.R.drawable.ic_input_add,
                    "ACCEPT ORDER",
                    acceptPendingIntent
                )
                .addAction(
                    android.R.drawable.ic_delete,
                    "DECLINE ORDER",
                    declinePendingIntent
                )
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {
        }
    }

    fun cancelAdminOrderAlarmNotification(context: Context, orderId: String) {
        try {
            activeAdminAlarmOrderIds.remove(orderId)
            NotificationManagerCompat.from(context).cancel(notificationIdForOrder(orderId))
        } catch (_: Exception) {
        }
    }

    private fun notificationIdForOrder(orderId: String): Int {
        return 90000 + (abs(orderId.hashCode()) % 9000)
    }
}

package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.mycrushpizza.`in`.R
import com.example.data.model.OrderStatus
import com.example.data.model.PizzaOrder
import com.example.data.repository.AuthAndUserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Persistent Foreground Service that keeps a real-time Firestore order listener + CPU WakeLock active
 * EXCLUSIVELY when the Admin Gmail (`pranavgamer758@gmail.com`) is signed in on the device.
 *
 * Ensures that even if the app is closed/backgrounded or the phone screen is turned off,
 * any incoming `CONFIRMED` order immediately wakes up the device and triggers the loud
 * continuous alarm tone + lock-screen Accept/Decline notification.
 */
class AdminOrderMonitorService : Service() {

    private var ordersListener: ListenerRegistration? = null
    private var serviceWakeLock: PowerManager.WakeLock? = null
    private var screenWakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureMonitorChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Persistent "Listening for live orders" foreground notification is disabled per Admin request.
        // Live order alerts continue seamlessly inside the app without an ongoing status-bar notification.
        stopMonitorAndSelf()
        return START_NOT_STICKY
    }

    private var lastAlertedOrderId: String? = null

    private fun attachRealtimeOrdersListener() {
        if (ordersListener != null) return
        try {
            val dbId = applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            ordersListener = db.collection("orders")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val currentEmail = try {
                        Firebase.auth.currentUser?.email.orEmpty().trim()
                    } catch (_: Exception) {
                        ""
                    }
                    if (!AuthAndUserRepository.isBootstrappedAdminPanelEmail(currentEmail)) {
                        stopMonitorAndSelf()
                        return@addSnapshotListener
                    }

                    val allOrders = snapshot.toObjects(
                        PizzaOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    val unacknowledgedConfirmed = allOrders.filter {
                        it.orderStatus == OrderStatus.CONFIRMED &&
                            !it.adminAlertAcknowledged &&
                            !AdminOrderAlertPlayer.isOrderSilenced(it.id)
                    }

                    if (unacknowledgedConfirmed.isNotEmpty()) {
                        val newestOrder = unacknowledgedConfirmed.first()
                        if (lastAlertedOrderId != newestOrder.id) {
                            lastAlertedOrderId = newestOrder.id
                            maximizeAlarmStreamVolume()
                            AdminOrderAlertPlayer.getOrCreate(applicationContext).startRinging(newestOrder.id)
                            NotificationHelper.showAdminIncomingOrderAlarmNotification(
                                applicationContext,
                                newestOrder
                            )
                        }
                    } else {
                        lastAlertedOrderId = null
                        AdminOrderAlertPlayer.stopGlobalRinging()
                    }
                }
        } catch (_: Exception) {
        }
    }

    private fun acquireCpuWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            if (serviceWakeLock == null) {
                serviceWakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "mycrushpizza:AdminOrderMonitorServiceWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            }
            if (serviceWakeLock?.isHeld == false) {
                serviceWakeLock?.acquire(4 * 60 * 60 * 1000L) // CPU-only wake lock; screen stays completely OFF
            }
        } catch (_: Exception) {
        }
    }

    private fun wakeScreenForIncomingOrder() {
        // Intentionally disabled so the app never wakes or overrides the lock screen.
    }

    private fun maximizeAlarmStreamVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (maxVol > 0 && currentVol < (maxVol * 0.85).toInt()) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
            }
        } catch (_: Exception) {
        }
    }

    private fun ensureMonitorChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                MONITOR_CHANNEL_ID,
                "Admin Live Order Radar (pranavgamer758@gmail.com)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Admin Order Alarm active even when app is closed or phone screen is off"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildMonitorForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            9910,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MONITOR_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("My Crush Pizza Admin Order Alarm Active")
            .setContentText("Listening for live orders (pranavgamer758@gmail.com) • Screen-off alarm ready")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun stopMonitorAndSelf() {
        try {
            ordersListener?.remove()
        } catch (_: Exception) {
        }
        ordersListener = null
        AdminOrderAlertPlayer.stopGlobalRinging()
        try {
            if (serviceWakeLock?.isHeld == true) {
                serviceWakeLock?.release()
            }
        } catch (_: Exception) {
        }
        try {
            if (screenWakeLock?.isHeld == true) {
                screenWakeLock?.release()
            }
        } catch (_: Exception) {
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        try {
            ordersListener?.remove()
        } catch (_: Exception) {
        }
        ordersListener = null
        super.onDestroy()
    }

    companion object {
        private const val MONITOR_CHANNEL_ID = "my_crush_pizza_admin_monitor_service"
        private const val FOREGROUND_NOTIF_ID = 88401
        private const val ACTION_START_MONITOR = "com.example.action.START_ADMIN_ORDER_MONITOR"
        private const val ACTION_STOP_MONITOR = "com.example.action.STOP_ADMIN_ORDER_MONITOR"

        fun syncAdminMonitorState(context: Context, isSignedIn: Boolean, email: String) {
            // Always stop any leftover foreground monitor service and clear its ongoing notification
            // so the "Listening for live orders" Admin notification never appears.
            stopMonitorAndClearNotification(context)
        }

        fun stopMonitorAndClearNotification(context: Context) {
            try {
                val stopIntent = Intent(context.applicationContext, AdminOrderMonitorService::class.java).apply {
                    action = ACTION_STOP_MONITOR
                }
                context.applicationContext.stopService(stopIntent)
            } catch (_: Exception) {
            }
            try {
                val manager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.cancel(FOREGROUND_NOTIF_ID)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager?.deleteNotificationChannel(MONITOR_CHANNEL_ID)
                }
            } catch (_: Exception) {
            }
        }
    }
}

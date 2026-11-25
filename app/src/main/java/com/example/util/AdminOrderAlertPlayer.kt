package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Plays a continuous repeating high-priority kitchen alarm tone + vibration with a WakeLock
 * when a new order arrives for the Admin Panel — even if the phone screen is turned off or locked —
 * until the Admin clicks Accept, Decline, or DONE (in the app or from the lock-screen notification).
 */
class AdminOrderAlertPlayer(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ringJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    @Volatile
    var isRinging: Boolean = false
        private set

    init {
        activeInstance = this
        allInstances.add(this)
    }

    fun startRinging(orderId: String = "") {
        if (orderId.isNotBlank() && isOrderSilenced(orderId)) {
            stopRinging()
            return
        }
        if (isRinging && ringJob?.isActive == true) return
        isRinging = true
        activeInstance = this

        acquireAlarmWakeLock()
        ensureLoudAlarmVolume()

        ringJob?.cancel()
        ringJob = scope.launch {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Exception) {
            }
            mediaPlayer = null

            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (alarmUri != null && isRinging) {
                    val mp = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setDataSource(context.applicationContext, alarmUri)
                        isLooping = true
                        prepare()
                        start()
                    }
                    mediaPlayer = mp
                }
            } catch (_: Exception) {
                // Fallback to ToneGenerator loop below
            }

            try {
                if (toneGenerator == null && isRinging) {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                }
            } catch (_: Exception) {
                // Ignore in headless unit test environments
            }

            while (isActive && isRinging) {
                if (orderId.isNotBlank() && isOrderSilenced(orderId)) {
                    stopRinging()
                    break
                }
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                    pulseVibration()
                } catch (_: Exception) {
                    // Ignore audio/vibration hardware exceptions in test environments
                }
                delay(1000L)
            }
        }
    }

    private fun acquireAlarmWakeLock() {
        try {
            val pm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (wakeLock == null && pm != null) {
                wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "mycrushpizza:AdminOrderAlarmWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(5 * 60 * 1000L) // CPU-only wake lock while alarm rings; never turns on screen
            }
        } catch (_: Exception) {
        }
    }

    private fun ensureLoudAlarmVolume() {
        try {
            val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val curAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (maxAlarm > 0 && curAlarm < (maxAlarm * 0.85).toInt()) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)
            }
        } catch (_: Exception) {
        }
    }

    private fun pulseVibration() {
        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(450L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(450L)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun stopRinging() {
        isRinging = false
        ringJob?.cancel()
        ringJob = null
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null

        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
        } catch (_: Exception) {
        }
        toneGenerator = null

        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (_: Exception) {
        }

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        @Volatile
        private var activeInstance: AdminOrderAlertPlayer? = null
        private val allInstances = java.util.concurrent.CopyOnWriteArrayList<AdminOrderAlertPlayer>()
        private val silencedOrderIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

        fun getOrCreate(context: Context): AdminOrderAlertPlayer {
            return activeInstance ?: synchronized(this) {
                activeInstance ?: AdminOrderAlertPlayer(context.applicationContext).also {
                    activeInstance = it
                }
            }
        }

        fun isOrderSilenced(orderId: String): Boolean {
            return orderId.isNotBlank() && silencedOrderIds.contains(orderId)
        }

        fun silenceOrderAndStopRinging(orderId: String) {
            if (orderId.isNotBlank()) {
                silencedOrderIds.add(orderId)
            }
            stopGlobalRinging()
        }

        fun silenceOrdersAndStopRinging(orderIds: Collection<String>) {
            orderIds.filter { it.isNotBlank() }.forEach { silencedOrderIds.add(it) }
            stopGlobalRinging()
        }

        fun stopGlobalRinging() {
            activeInstance?.stopRinging()
            allInstances.forEach { player ->
                try {
                    player.stopRinging()
                } catch (_: Exception) {
                }
            }
        }
    }
}

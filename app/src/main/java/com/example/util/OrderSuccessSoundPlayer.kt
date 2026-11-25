package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Plays a cheerful, crisp "Order Placed Successfully" confirmation chime synthesized in-memory
 * (ascending C5 -> E5 -> G5 -> C6 major arpeggio with warm harmonic decay) plus a subtle haptic tap.
 * Works 100% offline with zero external audio asset dependencies.
 */
object OrderSuccessSoundPlayer {

    private const val SAMPLE_RATE = 44100

    fun playOrderPlacedSound(context: Context) {
        triggerGentleSuccessHaptic(context)
        Thread {
            val played = playSynthesizedChime()
            if (!played) {
                playFallbackTone()
            }
        }.apply {
            isDaemon = true
            name = "OrderSuccessChimeThread"
            start()
        }
    }

    private fun triggerGentleSuccessHaptic(context: Context) {
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(65L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(65L)
                }
            }
        }
    }

    private fun playSynthesizedChime(): Boolean {
        return runCatching {
            val totalDurationSec = 0.85
            val numSamples = (SAMPLE_RATE * totalDurationSec).toInt()
            val pcm = ShortArray(numSamples)

            // Crisp, joyful 4-note ascending major chime (C5, E5, G5, C6)
            data class Note(val freqHz: Double, val startSec: Double, val decayRate: Double, val amp: Double)
            val notes = listOf(
                Note(523.25, 0.00, 9.5, 0.34),   // C5
                Note(659.25, 0.11, 9.0, 0.36),   // E5
                Note(783.99, 0.22, 8.0, 0.38),   // G5
                Note(1046.50, 0.34, 5.2, 0.44)   // C6 (bright final bell ring)
            )

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                var sample = 0.0
                for (note in notes) {
                    val dt = t - note.startSec
                    if (dt >= 0.0) {
                        val attack = (dt / 0.008).coerceIn(0.0, 1.0)
                        val env = attack * exp(-note.decayRate * dt)
                        val fundamental = sin(2.0 * PI * note.freqHz * dt)
                        val overtone = 0.28 * sin(2.0 * PI * (note.freqHz * 2.0) * dt)
                        sample += note.amp * env * (fundamental + overtone)
                    }
                }
                val clamped = sample.coerceIn(-0.95, 0.95)
                pcm[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val bufferBytes = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferBytes)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep((totalDurationSec * 1000).toLong() + 120L)
            runCatching {
                track.stop()
                track.release()
            }
            true
        }.getOrDefault(false)
    }

    private fun playFallbackTone() {
        runCatching {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
            Thread.sleep(360L)
            toneGen.release()
        }
    }
}

package com.fiestadecartas.app.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.fiestadecartas.app.data.Prefs

object Feedback {
    private var tone: ToneGenerator? = null

    private fun gen(): ToneGenerator? {
        if (!Prefs.sound) return null
        return try {
            tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 45).also { tone = it }
        } catch (_: Exception) { null }
    }

    fun click() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 45) } }
    fun play() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_ACK, 60) } }
    fun win() { runCatching { gen()?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180) } }
    fun error() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_NACK, 90) } }

    /** Shuffling / dealing tick. */
    fun deal() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_PROMPT, 35) } }

    /** A canto is declared: bright double beep. */
    fun canto() {
        runCatching {
            val g = gen() ?: return
            g.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 90)
            g.startTone(ToneGenerator.TONE_PROP_ACK, 90)
        }
    }

    /** A card slams on the table and captures the pile. */
    fun slam() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_NACK, 120) } }

    fun dice() { runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 45) } }

    /** Short vibration; honours the user setting. */
    fun buzz(context: Context, millis: Long = 35, amplitude: Int = 120) {
        if (!Prefs.vibration) return
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(millis, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(millis)
            }
        }
    }

    /** Pattern used when the table shakes after a canto or a capture. */
    fun rumble(context: Context, strong: Boolean = false) {
        if (!Prefs.vibration) return
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            val pattern = if (strong) longArrayOf(0, 60, 45, 110, 40, 160) else longArrayOf(0, 35, 40, 70)
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}

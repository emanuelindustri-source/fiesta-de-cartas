package com.fiestadecartas.app.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.fiestadecartas.app.R
import com.fiestadecartas.app.data.Prefs

object Feedback {
    private var tone: ToneGenerator? = null
    private var pool: SoundPool? = null
    private val ids = mutableMapOf<Int, Int>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        if (pool != null) return
        runCatching {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val sp = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attributes).build()
            listOf(
                R.raw.sfx_deal, R.raw.sfx_slam, R.raw.sfx_canto, R.raw.sfx_dice,
                R.raw.sfx_win, R.raw.sfx_coin, R.raw.sfx_click, R.raw.sfx_spooky
            ).forEach { res ->
                sp.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) ids[res] = sampleId
                }
                sp.load(appContext, res, 1)
            }
            pool = sp
        }
    }

    private fun playRes(res: Int, volume: Float = 0.9f, rate: Float = 1f): Boolean {
        if (!Prefs.sound) return false
        val p = pool ?: return false
        val id = ids[res] ?: return false
        return runCatching {
            p.play(id, volume, volume, 1, 0, rate)
            true
        }.getOrDefault(false)
    }

    private fun gen(): ToneGenerator? {
        if (!Prefs.sound) return null
        return try {
            tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 45).also { tone = it }
        } catch (_: Exception) { null }
    }

    fun click() {
        if (!playRes(R.raw.sfx_click, 0.55f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 40) }
        }
    }

    fun play() {
        if (!playRes(R.raw.sfx_deal, 0.8f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_ACK, 50) }
        }
    }

    fun deal() = playRes(R.raw.sfx_deal, 0.55f, 0.94f)

    fun slam() {
        if (!playRes(R.raw.sfx_slam, 1f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_NACK, 110) }
        }
    }

    fun canto() {
        if (!playRes(R.raw.sfx_canto, 0.95f)) {
            runCatching {
                val g = gen() ?: return
                g.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 90)
                g.startTone(ToneGenerator.TONE_PROP_ACK, 90)
            }
        }
    }

    fun dice() {
        if (!playRes(R.raw.sfx_dice, 0.9f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50) }
        }
    }

    fun win() {
        if (!playRes(R.raw.sfx_win, 1f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180) }
        }
    }

    fun coin() = playRes(R.raw.sfx_coin, 0.85f)

    fun spooky() = playRes(R.raw.sfx_spooky, 0.7f)

    fun error() {
        if (!playRes(R.raw.sfx_click, 0.5f, 0.8f)) {
            runCatching { gen()?.startTone(ToneGenerator.TONE_PROP_NACK, 90) }
        }
    }

    /** Short vibration; honours the user setting. */
    fun buzz(context: Context, millis: Long = 35, amplitude: Int = 120) {
        if (!Prefs.vibration) return
        runCatching {
            val vibrator = vibrator(context) ?: return
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
            val vibrator = vibrator(context) ?: return
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

    private fun vibrator(context: Context): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

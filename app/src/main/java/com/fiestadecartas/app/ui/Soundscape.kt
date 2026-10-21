package com.fiestadecartas.app.ui

import android.content.Context
import android.media.MediaPlayer
import com.fiestadecartas.app.R
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.ui.theme.Festival

/**
 * Seasonal ambience. Short procedural loops, low volume, looped only while the app is
 * in the foreground and only when sound is enabled.
 */
object Soundscape {
    private var player: MediaPlayer? = null
    private var currentRes: Int = 0

    fun ambientFor(festival: Festival): Int = when (festival) {
        Festival.HALLOWEEN, Festival.SEMANA_SANTA -> R.raw.ambient_halloween
        Festival.CHRISTMAS, Festival.REYES -> R.raw.ambient_christmas
        else -> R.raw.ambient_fiesta
    }

    fun sync(context: Context, festival: Festival) {
        if (!Prefs.sound) {
            stop()
            return
        }
        val res = ambientFor(festival)
        if (player != null && currentRes == res) {
            if (player?.isPlaying == false) runCatching { player?.start() }
            return
        }
        stop()
        runCatching {
            val mp = MediaPlayer.create(context.applicationContext, res) ?: return
            mp.isLooping = true
            mp.setVolume(0.16f, 0.16f)
            mp.start()
            player = mp
            currentRes = res
        }
    }

    fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        currentRes = 0
    }
}

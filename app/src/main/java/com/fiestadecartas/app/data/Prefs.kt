package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat

object Prefs {
    private lateinit var sp: SharedPreferences

    var language by mutableStateOf("system")
        private set
    var themeMode by mutableStateOf("dark")
        private set
    var dynamicColor by mutableStateOf(true)
        private set
    var sound by mutableStateOf(true)
        private set
    var vibration by mutableStateOf(true)
        private set
    var manualFestival by mutableStateOf("auto")
        private set
    var nickname by mutableStateOf("")
        private set
    var adsConsent by mutableStateOf<Boolean?>(null)
        private set
    var wins by mutableStateOf(0)
        private set
    var played by mutableStateOf(0)
        private set

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_prefs", Context.MODE_PRIVATE)
        language = sp.getString("language", "system") ?: "system"
        themeMode = sp.getString("theme_mode", "dark") ?: "dark"
        dynamicColor = sp.getBoolean("dynamic_color", true)
        sound = sp.getBoolean("sound", true)
        vibration = sp.getBoolean("vibration", true)
        manualFestival = sp.getString("manual_festival", "auto") ?: "auto"
        nickname = sp.getString("nickname", "") ?: ""
        adsConsent = if (sp.contains("ads_consent")) sp.getBoolean("ads_consent", false) else null
        wins = sp.getInt("wins", 0)
        played = sp.getInt("played", 0)
    }

    @kotlin.jvm.JvmName("applyLanguage")
    fun setLanguage(tag: String) {
        language = tag
        sp.edit().putString("language", tag).apply()
        val locales = if (tag == "system") LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    @kotlin.jvm.JvmName("applyThemeMode")
    fun setThemeMode(mode: String) { themeMode = mode; sp.edit().putString("theme_mode", mode).apply() }
    @kotlin.jvm.JvmName("applyDynamicColor")
    fun setDynamicColor(enabled: Boolean) { dynamicColor = enabled; sp.edit().putBoolean("dynamic_color", enabled).apply() }
    @kotlin.jvm.JvmName("applySound")
    fun setSound(enabled: Boolean) { sound = enabled; sp.edit().putBoolean("sound", enabled).apply() }
    @kotlin.jvm.JvmName("applyVibration")
    fun setVibration(enabled: Boolean) { vibration = enabled; sp.edit().putBoolean("vibration", enabled).apply() }
    @kotlin.jvm.JvmName("applyManualFestival")
    fun setManualFestival(key: String) { manualFestival = key; sp.edit().putString("manual_festival", key).apply() }
    @kotlin.jvm.JvmName("applyNickname")
    fun setNickname(value: String) { nickname = value.take(18); sp.edit().putString("nickname", nickname).apply() }
    fun setAdsConsent(value: Boolean) { adsConsent = value; sp.edit().putBoolean("ads_consent", value).apply() }
    fun recordResult(won: Boolean) {
        played += 1
        if (won) wins += 1
        sp.edit().putInt("played", played).putInt("wins", wins).apply()
        Profile.addXp(if (won) 90 else 45)
    }
    fun resetStats() {
        wins = 0
        played = 0
        sp.edit().putInt("wins", 0).putInt("played", 0).apply()
        Profile.reset()
        GameStats.clear()
    }
}

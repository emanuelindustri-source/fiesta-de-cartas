package com.fiestadecartas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.ads.BannerAd
import com.fiestadecartas.app.data.DeviceInfo
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.theme.Festival
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun SettingsScreen() {
    val palette = LocalPalette.current
    var nickname by remember { mutableStateOf(Prefs.nickname) }
    FiestaBackground {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = { Nav.go(Screen.Home) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(18.dp))

            Surface(
                onClick = { Nav.go(Screen.Profile) },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    AvatarBubble(Profile.avatar.emoji, Color(Profile.avatar.color), size = 46.dp, active = true)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.level_value, Profile.level) + " · " + stringResource(Profile.rankRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.xp_progress, Profile.xp, Profile.levelCeil),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text("${Profile.avatar.emoji}", fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).background(
                            Brush.linearGradient(listOf(palette.accent, palette.accent2)),
                            RoundedCornerShape(16.dp)
                        ), contentAlignment = Alignment.Center
                    ) { Text("👤", fontSize = 26.sp) }
                    Spacer(Modifier.width(14.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            nickname = it.take(18)
                            Prefs.setNickname(nickname)
                        },
                        label = { Text(stringResource(R.string.nickname)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSection(stringResource(R.string.language), Icons.Rounded.Language) {
                val languages = listOf(
                    "system" to stringResource(R.string.system_language),
                    "es" to "Español",
                    "en" to "English",
                    "pt" to "Português",
                    "it" to "Italiano",
                    "zh" to "中文"
                )
                languages.forEach { (tag, label) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = Prefs.language == tag, onClick = {
                            Prefs.setLanguage(tag)
                        })
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSection(stringResource(R.string.theme_mode), Icons.Rounded.DarkMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "system" to stringResource(R.string.system_theme),
                        "light" to stringResource(R.string.light),
                        "dark" to stringResource(R.string.dark)
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = Prefs.themeMode == mode,
                            onClick = { Prefs.setThemeMode(mode) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.dynamic_color), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.festival_auto_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                    Switch(checked = Prefs.dynamicColor, onCheckedChange = { Prefs.setDynamicColor(it) })
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSection(stringResource(R.string.theme), Icons.Rounded.Celebration) {
                Text(
                    stringResource(R.string.theme_auto) + " · " + stringResource(R.string.festival_auto_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(10.dp))
                val options = listOf(
                    "auto" to "🕒 ${stringResource(R.string.theme_auto)}",
                    "fiesta" to "🎉 ${stringResource(R.string.theme_fiesta)}",
                    "halloween" to "🎃 ${stringResource(R.string.theme_halloween)}",
                    "christmas" to "🎄 ${stringResource(R.string.theme_christmas)}",
                    "reyes" to "👑 ${stringResource(R.string.theme_reyes)}",
                    "valentine" to "❤️ ${stringResource(R.string.theme_valentine)}",
                    "easter" to "🐣 ${stringResource(R.string.theme_easter)}",
                    "semana" to "✝️ ${stringResource(R.string.theme_semana_santa)}"
                )
                options.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (key, label) ->
                            FilterChip(
                                selected = Prefs.manualFestival == key,
                                onClick = { Prefs.setManualFestival(key) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSection(stringResource(R.string.appearance), Icons.Rounded.Tune) {
                SwitchRow(
                    title = stringResource(R.string.sound),
                    subtitle = if (Prefs.sound) stringResource(R.string.sound_on) else stringResource(R.string.sound_off),
                    checked = Prefs.sound,
                    onChange = { Prefs.setSound(it) }
                )
                SwitchRow(
                    title = stringResource(R.string.vibration),
                    subtitle = if (Prefs.vibration) stringResource(R.string.vibration_on) else stringResource(R.string.vibration_off),
                    checked = Prefs.vibration,
                    onChange = { Prefs.setVibration(it) }
                )
                SwitchRow(
                    title = stringResource(R.string.ads_note),
                    subtitle = if (Prefs.adsConsent == true) stringResource(R.string.accept) else stringResource(R.string.reject),
                    checked = Prefs.adsConsent == true,
                    onChange = { Prefs.setAdsConsent(it) }
                )
                Spacer(Modifier.height(8.dp))
                val device = DeviceInfo.detect(LocalContext.current)
                Text(
                    stringResource(R.string.device) + ": " + stringResource(device.labelRes) +
                        " · " + stringResource(R.string.device_detected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(16.dp))
            SettingsSection(stringResource(R.string.stats), Icons.Rounded.Insights) {
                Row {
                    StatCard(stringResource(R.string.played), "${Prefs.played}", Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    StatCard(stringResource(R.string.wins), "${Prefs.wins}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { Prefs.resetStats() }) {
                    Text(stringResource(R.string.reset_stats))
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.share_app), Modifier.weight(1f)) { Nav.go(Screen.Share) }
                SecondaryButton(stringResource(R.string.about), Modifier.weight(1f)) { Nav.go(Screen.About) }
            }
            Spacer(Modifier.height(10.dp))
            SecondaryButton(stringResource(R.string.rate_app), Modifier.fillMaxWidth()) { Nav.go(Screen.Review) }
            Spacer(Modifier.height(12.dp))
            Text(
                "${stringResource(R.string.version)} ${com.fiestadecartas.app.BuildConfig.VERSION_NAME} · 🃏",
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            BannerAd()
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    GlassPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

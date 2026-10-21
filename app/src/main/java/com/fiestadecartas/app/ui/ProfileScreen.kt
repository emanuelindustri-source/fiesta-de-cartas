package com.fiestadecartas.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.Avatars
import com.fiestadecartas.app.data.Cosmetics
import com.fiestadecartas.app.data.DeviceInfo
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val palette = LocalPalette.current
    val avatar = Profile.avatar
    val level = Profile.level
    val device = DeviceInfo.detect(context)

    GameScaffold(
        title = stringResource(R.string.profile),
        subtitle = stringResource(R.string.profile_subtitle),
        onBack = { Nav.go(Screen.Home) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarBubble(avatar.emoji, Color(avatar.color), size = 72.dp, active = true)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.level_value, level),
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.accent
                        )
                        Text(
                            stringResource(Profile.rankRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.xp_progress, Profile.xp, Profile.levelCeil),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { Profile.progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = palette.accent,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (level >= 100) stringResource(R.string.max_level)
                    else stringResource(R.string.xp_next, Profile.xpToNext, level + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinView(size = 30.dp)
                Spacer(Modifier.width(8.dp))
                Text("${Profile.coins}", style = MaterialTheme.typography.headlineMedium, color = palette.accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.coins), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                StatChip(stringResource(R.string.level_value, level), color = palette.accent2)
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.choose_avatar), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Avatars.all.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { option ->
                        val selected = option.id == Profile.avatarId
                        val owned = Cosmetics.isAvatarOwned(option.id)
                        androidx.compose.material3.Surface(
                            onClick = {
                                if (owned) {
                                    Profile.setAvatar(option.id)
                                    Feedback.click()
                                } else if (Cosmetics.buyAvatar(option)) {
                                    Feedback.coin()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.coins_earned, option.price),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, R.string.not_enough_coins, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) Color(option.color).copy(alpha = 0.20f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (selected) 2.dp else 1.dp,
                                if (selected) Color(option.color) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarBubble(option.emoji, Color(option.color), size = 36.dp, active = selected)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        stringResource(option.nameRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (!owned) {
                                        Text(
                                            "${option.price} ${stringResource(R.string.coins)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinView(size = 30.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.practice_chips), style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${Profile.chips}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.accent2
                        )
                    }
                    Text(
                        stringResource(R.string.device) + ": " + stringResource(device.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.fichas_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.shop_title), Modifier.weight(1f)) { Nav.go(Screen.Shop) }
                SecondaryButton(stringResource(R.string.stats_title), Modifier.weight(1f)) { Nav.go(Screen.Stats) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.achievements_title), Modifier.weight(1f)) { Nav.go(Screen.Progress) }
                SecondaryButton(stringResource(R.string.rate_app), Modifier.weight(1f)) { Nav.go(Screen.Review) }
            }
            Spacer(Modifier.height(8.dp))
            SecondaryButton(stringResource(R.string.settings), Modifier.fillMaxWidth()) { Nav.go(Screen.Settings) }
            Spacer(Modifier.height(30.dp))
        }
    }
}

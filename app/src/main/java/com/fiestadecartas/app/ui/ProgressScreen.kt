package com.fiestadecartas.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.Achievements
import com.fiestadecartas.app.data.DailyMissions
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    val palette = LocalPalette.current
    GameScaffold(
        title = stringResource(R.string.achievements_title),
        subtitle = stringResource(R.string.missions_subtitle),
        onBack = { Nav.go(Screen.Profile) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinView(size = 30.dp)
                Spacer(Modifier.width(10.dp))
                Text("${Profile.coins}", style = MaterialTheme.typography.headlineMedium, color = palette.accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.coins), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                StatChip(stringResource(R.string.level_value, Profile.level), color = palette.accent2)
            }
            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.missions_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.missions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(8.dp))
            DailyMissions.missions.forEach { mission ->
                val progress = DailyMissions.progressOf(mission)
                val complete = DailyMissions.isComplete(mission)
                val claimed = DailyMissions.isClaimed(mission)
                GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mission.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(mission.titleRes), style = MaterialTheme.typography.titleMedium)
                            Text(
                                "$progress / ${mission.target} · +${mission.reward} ${stringResource(R.string.coins)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { (progress.toFloat() / mission.target).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = palette.accent,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (claimed) {
                            Text(stringResource(R.string.claimed), style = MaterialTheme.typography.labelSmall, color = palette.accent2)
                        } else if (complete) {
                            TextButton(onClick = {
                                if (DailyMissions.claim(mission)) {
                                    Feedback.coin()
                                    Toast.makeText(context, context.getString(R.string.coins_earned, mission.reward), Toast.LENGTH_SHORT).show()
                                }
                            }) { Text(stringResource(R.string.claim)) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.achievements_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Achievements.all.forEach { achievement ->
                val progress = Achievements.progress(achievement)
                val complete = progress >= achievement.target
                val claimed = Achievements.isClaimed(achievement.id)
                GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(achievement.icon, fontSize = 26.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(achievement.titleRes), fontWeight = FontWeight.Bold)
                            Text(
                                "${progress.coerceAtMost(achievement.target)} / ${achievement.target} · +${achievement.reward} ${stringResource(R.string.coins)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { (progress.toFloat() / achievement.target).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = palette.accent2,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (claimed) {
                            Text(stringResource(R.string.claimed), style = MaterialTheme.typography.labelSmall, color = palette.accent2)
                        } else if (complete) {
                            TextButton(onClick = {
                                if (Achievements.claim(achievement)) {
                                    Feedback.coin()
                                    Toast.makeText(context, context.getString(R.string.coins_earned, achievement.reward), Toast.LENGTH_SHORT).show()
                                }
                            }) { Text(stringResource(R.string.claim)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

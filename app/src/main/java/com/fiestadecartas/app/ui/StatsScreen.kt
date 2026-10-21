package com.fiestadecartas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.ReplaySelection
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.GameStats
import com.fiestadecartas.app.data.MatchRecord
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun gameName(id: String): String = id

@Composable
fun StatsScreen() {
    val palette = LocalPalette.current
    GameScaffold(
        title = stringResource(R.string.stats_title),
        subtitle = stringResource(R.string.stats_subtitle),
        onBack = { Nav.go(Screen.Profile) }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            GameStats.games.forEach { (id, stat) ->
                val game = GAMES.firstOrNull { it.id == id }
                if (game != null) {
                    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(game.emoji, fontSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(game.nameRes), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${stringResource(R.string.games_played)} ${stat.played} · ${stringResource(R.string.wins)} ${stat.wins}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${stringResource(R.string.best_score)} ${stat.bestScore}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.accent
                                )
                            }
                            val rate = if (stat.played == 0) 0f else stat.wins.toFloat() / stat.played
                            Text(
                                "${(rate * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.accent2
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.recent_matches), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (GameStats.recent.isEmpty()) {
                Text(stringResource(R.string.no_matches), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            } else {
                GameStats.recent.forEach { record ->
                    MatchRow(record)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MatchRow(record: MatchRecord) {
    val date = remember(record.timestamp) {
        SimpleDateFormat("d MMM · HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    }
    val game = GAMES.firstOrNull { it.id == record.gameId }
    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(game?.emoji ?: "🎴", fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    (game?.let { stringResource(it.nameRes) } ?: record.gameId) +
                        " · " + if (record.won) stringResource(R.string.wins) else stringResource(R.string.game_over),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$date · ${record.mode} · ${record.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (record.messages.isNotEmpty() || record.seed != 0L) {
                TextButton(onClick = {
                    ReplaySelection.record = record
                    Nav.go(Screen.Replay)
                }) { Text(stringResource(R.string.replay)) }
            }
        }
    }
}

@Composable
fun ReplayScreen() {
    val record = ReplaySelection.record
    val palette = LocalPalette.current
    if (record == null) {
        GameScaffold(
            title = stringResource(R.string.replay_title),
            onBack = { Nav.go(Screen.Stats) }
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_matches))
            }
        }
        return
    }
    var index by remember(record) { mutableIntStateOf(0) }
    var playing by remember(record) { mutableStateOf(false) }
    val total = record.messages.size
    LaunchedEffect(playing, index, total) {
        if (playing && index < total) {
            delay(900)
            index = (index + 1).coerceAtMost(total)
            if (index >= total) playing = false
        }
    }
    val game = GAMES.firstOrNull { it.id == record.gameId }
    GameScaffold(
        title = stringResource(R.string.replay_title),
        subtitle = game?.let { stringResource(it.nameRes) } ?: record.gameId,
        onBack = { Nav.go(Screen.Stats) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.match_summary),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${record.playerNames.joinToString(" · ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${stringResource(R.string.replay_seed)}: ${record.seed} · ${record.mode} · ${record.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else index.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = palette.accent,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                Spacer(Modifier.height(6.dp))
                Text("$index / $total", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(16.dp))
            val visible = record.messages.take(index)
            if (visible.isEmpty()) {
                Text(
                    stringResource(R.string.no_history),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            } else {
                visible.takeLast(5).forEachIndexed { i, message ->
                    Text(
                        message,
                        style = if (i == visible.takeLast(5).lastIndex) MaterialTheme.typography.titleMedium
                        else MaterialTheme.typography.bodyMedium,
                        color = if (i == visible.takeLast(5).lastIndex) palette.accent
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { playing = !playing }) {
                    Text(stringResource(if (playing) R.string.replay_pause else R.string.replay_play))
                }
                TextButton(onClick = {
                    if (index < total) index += 1
                }) { Text(stringResource(R.string.replay_step)) }
                TextButton(onClick = { index = total; playing = false }) {
                    Text(stringResource(R.string.close))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

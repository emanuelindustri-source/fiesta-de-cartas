package com.fiestadecartas.app.game.kemix

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.fiestadecartas.app.ads.Interstitial
import com.fiestadecartas.app.data.Avatars
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.KemixCardView
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay

private val KEMIX_SEAT_COLORS = listOf(
    Color(0xFFFFB703), Color(0xFF4CC9F0), Color(0xFFFF6B9D), Color(0xFF2EC4B6)
)

@Composable
fun KemixScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val localSeed = remember { System.currentTimeMillis() }
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var recorded by remember(restart) { mutableStateOf(false) }

    val myName = Prefs.nickname.ifBlank { stringResource(R.string.you) }
    val names = remember(myName) { listOf(myName, "Bot 1", "Bot 2", "Bot 3") }
    val game = remember(localSeed, restart) {
        KemixGame(names, setOf(1, 2, 3), localSeed + restart * 53L)
    }
    val canAct = game.phase == KemixPhase.PLAYING && game.current == 0 && !game.pendingHide

    LaunchedEffect(revision, game.current, game.phase, game.pendingHide) {
        if (game.phase == KemixPhase.PLAYING && game.isBotTurn() && !game.pendingHide) {
            if (game.firstFlip < 0) {
                delay(750)
                val pick = game.botFirstPick()
                if (pick >= 0 && game.flip(pick)) {
                    Feedback.click()
                    revision++
                }
            } else {
                delay(800)
                val pick = game.botSecondPick(game.firstFlip)
                if (pick >= 0) {
                    val matched = game.cards[pick].family == game.cards[game.firstFlip].family
                    if (game.flip(pick)) {
                        if (matched) Feedback.play() else Feedback.error()
                        revision++
                    }
                }
            }
        }
    }
    LaunchedEffect(game.pendingHide) {
        if (game.pendingHide) {
            delay(1100)
            game.resolveMismatch()
            revision++
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == KemixPhase.OVER && !recorded) {
            recorded = true
            Prefs.recordResult(game.winner == 0)
            if (game.winner == 0) Feedback.win() else Feedback.error()
            Interstitial.maybeShow(activity)
        }
    }

    val tick = revision
    GameScaffold(
        title = stringResource(R.string.kemix_name),
        subtitle = stringResource(R.string.play_bots),
        onBack = { Nav.go(Screen.Home) },
        actions = {
            IconButton(onClick = { showRules = true }) { Icon(Icons.Rounded.Info, null) }
            IconButton(onClick = { restart++; Feedback.click() }) { Icon(Icons.Rounded.Refresh, null) }
        }
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                names.forEachIndexed { i, name ->
                    val av = if (i == 0) Profile.avatar else Avatars.all[(i + 1) % Avatars.all.size]
                    PlayerBadge(
                        name = name,
                        points = game.pairs[i],
                        active = game.current == i && game.phase == KemixPhase.PLAYING,
                        color = KEMIX_SEAT_COLORS[i % KEMIX_SEAT_COLORS.size],
                        avatarEmoji = av.emoji,
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("${stringResource(R.string.pairs)}: ${game.pairs[0]}", color = palette.accent)
                Spacer(Modifier.width(6.dp))
                StatChip("${stringResource(R.string.cards_left, 12 - game.pairs.sum())}", color = palette.accent2)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    game.phase == KemixPhase.OVER -> stringResource(R.string.game_over)
                    game.current == 0 -> stringResource(R.string.your_turn)
                    else -> stringResource(R.string.turn_of, names.getOrElse(game.current) { "" })
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (game.current == 0) palette.accent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Text(
                game.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(10.dp))

            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val gap = 6.dp
                val cell = (maxWidth - gap * 3) / 4
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(6) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            repeat(4) { col ->
                                val index = row * 4 + col
                                val card = game.cards[index]
                                val faceUp = index in game.revealed || game.matchedBy[index] >= 0
                                val matched = game.matchedBy[index] >= 0
                                Box(
                                    Modifier.weight(1f).alpha(if (matched) 0.85f else 1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KemixCardView(
                                        card = card,
                                        width = cell,
                                        faceUp = faceUp,
                                        onClick = {
                                            if (!canAct || faceUp) return@KemixCardView
                                            val matchedNow = game.firstFlip >= 0 &&
                                                game.cards[game.firstFlip].family == card.family
                                            if (game.flip(index)) {
                                                if (matchedNow) Feedback.play() else Feedback.click()
                                                revision++
                                            }
                                        }
                                    )
                                    if (matched) {
                                        val seat = game.matchedBy[index]
                                        Box(
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp)
                                                .background(
                                                    KEMIX_SEAT_COLORS[seat % KEMIX_SEAT_COLORS.size],
                                                    CircleShape
                                                )
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "${seat + 1}",
                                                color = Color(0xFF101010),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.memory_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
        }
        ConfettiOverlay(visible = game.phase == KemixPhase.OVER && game.winner == 0)
    }

    if (game.phase == KemixPhase.OVER) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    if (game.winner == 0) stringResource(R.string.winner)
                    else stringResource(R.string.game_over),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 44.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        game.message,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    names.forEachIndexed { i, name ->
                        Text(
                            "$name · ${game.pairs[i]} ${stringResource(R.string.pairs)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(stringResource(R.string.play_again)) {
                    restart++
                    Feedback.click()
                }
            }
        )
    }

    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.kemix_name)) },
            text = { Text(stringResource(R.string.rules_kemix)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
}

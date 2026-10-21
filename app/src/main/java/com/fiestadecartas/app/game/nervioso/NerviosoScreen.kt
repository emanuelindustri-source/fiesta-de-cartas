package com.fiestadecartas.app.game.nervioso

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.SpanishCardView
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val NERVIOSO_SEAT_COLORS = listOf(
    Color(0xFFFFB703), Color(0xFF4CC9F0), Color(0xFFFF6B9D), Color(0xFF2EC4B6)
)

@Composable
fun NerviosoScreen() {
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
    val bots = remember { setOf(1, 2, 3) }
    val game = remember(localSeed, restart) {
        NerviosoGame(names, bots, localSeed + restart * 89L, GameConfig.nerviosoStartCards)
    }
    val myHand = game.hands[0]
    val canPlay = game.phase == NerviosoPhase.TURN && game.current == 0
    val canSlap = game.phase == NerviosoPhase.SLAP && 0 !in game.slaps

    LaunchedEffect(revision, game.current, game.phase) {
        if (game.phase == NerviosoPhase.TURN && game.isBotTurn()) {
            delay(950)
            val seat = game.current
            val idx = game.botPick(seat)
            if (idx >= 0) {
                game.playCard(seat, idx)
                Feedback.click()
                revision++
            }
        }
    }
    LaunchedEffect(game.phase, game.slapRound) {
        if (game.phase == NerviosoPhase.SLAP) {
            bots.forEachIndexed { order, seat ->
                launch {
                    delay(game.botSlapDelay(seat) + order * 140L)
                    if (game.phase == NerviosoPhase.SLAP && seat !in game.slaps) {
                        game.slap(seat)
                        Feedback.click()
                        revision++
                    }
                }
            }
            launch {
                delay(1700)
                if (game.phase == NerviosoPhase.SLAP && 0 !in game.slaps) {
                    game.slap(0)
                    revision++
                }
            }
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == NerviosoPhase.OVER && !recorded) {
            recorded = true
            Prefs.recordResult(game.winner == 0)
            if (game.winner == 0) Feedback.win() else Feedback.error()
            Interstitial.maybeShow(activity)
        }
    }

    val tick = revision
    GameScaffold(
        title = stringResource(R.string.nervioso_name),
        subtitle = stringResource(R.string.play_bots),
        onBack = { Nav.go(Screen.Home) },
        actions = {
            IconButton(onClick = { showRules = true }) { Icon(Icons.Rounded.Info, null) }
            IconButton(onClick = { restart++; Feedback.click() }) { Icon(Icons.Rounded.Refresh, null) }
        }
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
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
                        points = game.hands[i].size,
                        active = game.current == i && game.phase != NerviosoPhase.OVER,
                        color = NERVIOSO_SEAT_COLORS[i % NERVIOSO_SEAT_COLORS.size],
                        avatarEmoji = av.emoji,
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip(
                    "${stringResource(R.string.nervioso_pile, game.pile.size)}",
                    color = palette.accent
                )
                Spacer(Modifier.width(6.dp))
                StatChip(
                    if (game.current == 0) stringResource(R.string.your_turn)
                    else stringResource(R.string.turn_of, names.getOrElse(game.current) { "" }),
                    color = NERVIOSO_SEAT_COLORS[game.current % 4]
                )
            }
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.nervioso_count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier.size(78.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(palette.accent.copy(alpha = 0.55f), palette.accent.copy(alpha = 0.12f))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${game.announced}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        SpanishCardView(
                            card = game.pile.lastOrNull(),
                            width = 76.dp,
                            faceUp = game.pile.isNotEmpty()
                        )
                        if (game.lastCard != null) {
                            Text(
                                "${game.lastCard!!.rank}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        "${game.pile.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                game.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (game.phase == NerviosoPhase.SLAP) palette.accent2
                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(10.dp))

            if (game.phase == NerviosoPhase.OVER) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text(
                        "🏆 ${names.getOrElse(game.winner) { "" }}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.nervioso_win),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(10.dp))
                    PrimaryButton(stringResource(R.string.play_again), Modifier.fillMaxWidth()) {
                        restart++
                        Feedback.click()
                    }
                }
            } else {
                Text(
                    stringResource(R.string.your_cards) + " · ${myHand.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    myHand.forEachIndexed { index, card ->
                        val ok = canPlay && card.rank == game.announced
                        Box(Modifier.alpha(if (canPlay) 1f else 0.55f)) {
                            SpanishCardView(
                                card = card,
                                width = 64.dp,
                                highlighted = ok,
                                onClick = {
                                    if (!canPlay) return@SpanishCardView
                                    val play = game.playCard(0, index) ?: return@SpanishCardView
                                    if (play.match) Feedback.play() else Feedback.click()
                                    revision++
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (game.phase == NerviosoPhase.SLAP) {
                    Button(
                        onClick = {
                            val loser = game.slap(0)
                            if (loser == 0) Feedback.error() else Feedback.play()
                            revision++
                        },
                        enabled = canSlap,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent2,
                            contentColor = Color(0xFF101010)
                        )
                    ) {
                        Text(
                            "✋ ${stringResource(R.string.slap)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.nervioso_slap_fast),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                } else {
                    Text(
                        if (canPlay) stringResource(R.string.tap_card) else stringResource(R.string.wait),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        ConfettiOverlay(visible = game.phase == NerviosoPhase.OVER && game.winner == 0)
    }

    if (game.phase == NerviosoPhase.OVER && !recorded) {
        // dialog is shown by the panel; kept for symmetry with other screens
    }

    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.nervioso_name)) },
            text = { Text(stringResource(R.string.rules_nervioso)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
}

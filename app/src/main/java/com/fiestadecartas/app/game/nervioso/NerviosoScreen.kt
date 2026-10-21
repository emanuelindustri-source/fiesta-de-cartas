package com.fiestadecartas.app.game.nervioso

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
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
import com.fiestadecartas.app.data.BotSettings
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.MatchHistoryDialog
import com.fiestadecartas.app.ui.MatchHistoryStrip
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.SpanishCardView
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.rememberMatchHistory
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val NERVIOSO_SEAT_COLORS = listOf(
    Color(0xFFFFB703), Color(0xFF4CC9F0), Color(0xFFFF6B9D), Color(0xFF2EC4B6)
)

@Composable
fun NerviosoScreen(lan: Boolean = false, hotseat: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val isLan = lan && LanSession.role != "none"
    val localHotseat = hotseat && !isLan
    val mySeat = if (isLan) LanSession.mySeat else 0
    val playersCount = if (isLan) LanSession.players.coerceIn(2, 4) else 4
    val localSeed = remember { System.currentTimeMillis() }
    val seed = if (isLan) LanSession.seed else localSeed
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var recorded by remember(restart) { mutableStateOf(false) }
    var pendingSlap by remember { mutableStateOf(false) }

    val myName = Prefs.nickname.ifBlank { stringResource(R.string.you) }
    val playerLabels = (1..playersCount).map { stringResource(R.string.player_number, it) }
    val lanHostLabel = LanSession.hostName.ifBlank { stringResource(R.string.host) }
    val lanGuestLabel = LanSession.guestName.ifBlank { stringResource(R.string.guest) }
    val names = remember(myName, isLan, localHotseat, playersCount, playerLabels, lanHostLabel, lanGuestLabel) {
        val list = mutableListOf<String>()
        if (isLan) {
            list += lanHostLabel
            list += lanGuestLabel
        } else if (localHotseat) {
            list += playerLabels
        } else list += myName
        var b = 1
        while (list.size < playersCount) { list += "Bot $b"; b++ }
        list
    }
    val isHost = !isLan || LanSession.role == "host"
    val bots = remember(isLan, localHotseat, playersCount) {
        when {
            isLan -> if (isHost) (2 until playersCount).toSet() else emptySet()
            localHotseat -> emptySet()
            else -> (1 until playersCount).toSet()
        }
    }
    val game = remember(seed, restart, playersCount, localHotseat, bots) {
        NerviosoGame(names, bots, seed + restart * 89L, GameConfig.nerviosoStartCards)
    }
    val activeSeat = if (localHotseat) game.current else mySeat
    val history = rememberMatchHistory(game.message)
    var showHistory by remember { mutableStateOf(false) }
    val myHand = game.hands.getOrNull(activeSeat).orEmpty()
    val canPlay = game.phase == NerviosoPhase.TURN && game.current == activeSeat
    val nextSlapSeat = (0 until names.size).firstOrNull { it !in game.slaps } ?: -1
    val canSlap = game.phase == NerviosoPhase.SLAP && when {
        isLan && !isHost -> !pendingSlap && mySeat !in game.slaps
        localHotseat -> nextSlapSeat >= 0
        else -> activeSeat !in game.slaps
    }

    fun applyPlay(seat: Int, index: Int, send: Boolean) {
        val play = game.playCard(seat, index) ?: return
        if (play.match) Feedback.play() else Feedback.click()
        revision++
        if (send && isLan) {
            if (LanSession.role == "host") LanSession.sendAction("PLAY:$seat:$index")
            else LanSession.sendAction("PLAY:$index")
        }
    }

    fun applySlap(seat: Int, send: Boolean) {
        if (seat !in names.indices || seat in game.slaps) return
        val loser = game.slap(seat)
        if (loser >= 0 || seat in game.slaps) revision++
        if (loser == seat) Feedback.error() else Feedback.play()
        if (send && isLan && LanSession.role == "host") LanSession.sendAction("SLAP:$seat")
    }

    fun doSlap() {
        if (!canSlap) return
        val seat = if (localHotseat) nextSlapSeat else activeSeat
        if (seat < 0) return
        if (isLan && LanSession.role == "guest") {
            pendingSlap = true
            LanSession.sendAction("SLAP")
            return
        }
        applySlap(seat, send = true)
    }

    fun remoteAction(action: String) {
        val parts = action.split(":")
        when (parts.getOrNull(0)) {
            "PLAY" -> {
                val a = parts.getOrNull(1)?.toIntOrNull()
                val b = parts.getOrNull(2)?.toIntOrNull()
                if (a != null && b != null) {
                    applyPlay(a, b, send = false)
                } else if (a != null) {
                    applyPlay(1, a, send = false)
                }
            }
            "SLAP" -> {
                val seat = parts.getOrNull(1)?.toIntOrNull() ?: 1
                applySlap(seat, send = false)
                if (isLan && LanSession.role == "host") LanSession.sendAction("SLAP:$seat")
            }
            "RESET" -> { restart++; revision++ }
        }
    }

    DisposableEffect(Unit) {
        LanSession.onRemoteAction = { action -> remoteAction(action) }
        onDispose { LanSession.onRemoteAction = null }
    }

    LaunchedEffect(revision, game.current, game.phase, game.isBotTurn()) {
        if (game.phase == NerviosoPhase.TURN && game.isBotTurn() && isHost) {
            delay(950)
            val seat = game.current
            val idx = game.botPick(seat, BotSettings.difficulty)
            if (idx >= 0) {
                game.playCard(seat, idx)
                Feedback.click()
                revision++
                if (isLan) LanSession.sendAction("PLAY:$seat:$idx")
            }
        }
    }
    LaunchedEffect(game.phase, game.slapRound, isHost) {
        if (game.phase == NerviosoPhase.SLAP && isHost) {
            bots.forEachIndexed { order, seat ->
                launch {
                    delay(game.botSlapDelay(seat, BotSettings.difficulty) + order * 140L)
                    if (game.phase == NerviosoPhase.SLAP && seat !in game.slaps) {
                        game.slap(seat)
                        Feedback.click()
                        revision++
                        if (isLan) LanSession.sendAction("SLAP:$seat")
                    }
                }
            }
            launch {
                delay(1700)
                if (game.phase == NerviosoPhase.SLAP && 0 !in game.slaps) {
                    game.slap(0)
                    revision++
                    if (isLan) LanSession.sendAction("SLAP:0")
                }
            }
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase != NerviosoPhase.SLAP) pendingSlap = false
        if (game.phase == NerviosoPhase.OVER && !recorded) {
            recorded = true
            Prefs.recordResult(game.winner == mySeat)
            if (game.winner == activeSeat) Feedback.win() else Feedback.error()
            Interstitial.maybeShow(activity)
        }
    }

    val tick = revision
    GameScaffold(
        title = stringResource(R.string.nervioso_name),
        subtitle = when {
            isLan -> "WiFi · ${stringResource(R.string.room_code)} ${LanSession.code}"
            localHotseat -> stringResource(R.string.play_hotseat)
            else -> stringResource(R.string.play_bots)
        },
        onBack = {
            if (isLan) LanSession.stop()
            Nav.go(Screen.Home)
        },
        actions = {
            IconButton(onClick = { showHistory = true }) { Icon(Icons.Rounded.History, null) }
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
                    val av = if (i == (if (isLan) mySeat else 0)) Profile.avatar else Avatars.all[(i + 1) % Avatars.all.size]
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
                    if (game.current == activeSeat && !localHotseat) stringResource(R.string.your_turn)
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
                        if (isLan) {
                            LanSession.stop()
                            Nav.go(Screen.Home)
                        } else {
                            restart++
                        }
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
                                    applyPlay(activeSeat, index, send = true)
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (game.phase == NerviosoPhase.SLAP) {
                    Button(
                        onClick = {
                            doSlap()
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
            Spacer(Modifier.height(8.dp))
            MatchHistoryStrip(history, onOpen = { showHistory = true })
            Spacer(Modifier.height(10.dp))
        }
        ConfettiOverlay(visible = game.phase == NerviosoPhase.OVER && game.winner == activeSeat)
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
    if (showHistory) {
        MatchHistoryDialog(history, onDismiss = { showHistory = false })
    }
}

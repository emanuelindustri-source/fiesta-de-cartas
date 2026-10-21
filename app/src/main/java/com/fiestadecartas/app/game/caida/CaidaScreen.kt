package com.fiestadecartas.app.game.caida

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
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
import com.fiestadecartas.app.ui.AvatarBubble
import com.fiestadecartas.app.ui.CardSlot
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.MatchHistoryDialog
import com.fiestadecartas.app.ui.MatchHistoryStrip
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.Reveal
import com.fiestadecartas.app.ui.SecondaryButton
import com.fiestadecartas.app.ui.SpanishCardView
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.rememberMatchHistory
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay

@Composable
fun CaidaScreen(lan: Boolean = false, hotseat: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val isLan = lan && LanSession.role != "none"
    val localHotseat = hotseat && !isLan
    val mySeat = if (isLan) LanSession.mySeat else 0
    val playersCount = if (isLan) LanSession.players.coerceIn(2, 4) else GameConfig.caidaPlayers
    val target = GameConfig.caidaTarget
    val localSeed = remember { System.currentTimeMillis() }
    val seed = if (isLan) LanSession.seed else localSeed
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var recorded by remember { mutableStateOf(false) }
    var banner by remember { mutableStateOf("") }
    var dealt by remember { mutableIntStateOf(0) }
    var flyCard by remember { mutableStateOf<com.fiestadecartas.app.game.SpanishCard?>(null) }
    val reactions = remember { mutableStateMapOf<Int, String>() }
    val shake = remember { Animatable(0f) }
    val fly = remember { Animatable(0f) }
    val cantoLabel = stringResource(R.string.canto_banner)
    val caidaLabel = stringResource(R.string.caida_banner)

    val youLabel = stringResource(R.string.you)
    val hostLabel = stringResource(R.string.host)
    val guestLabel = stringResource(R.string.guest)
    val playerLabels = (1..playersCount).map { stringResource(R.string.player_number, it) }
    val names = remember(playersCount, LanSession.hostName, LanSession.guestName, isLan, youLabel, hostLabel, guestLabel, localHotseat, playerLabels) {
        val list = mutableListOf<String>()
        if (isLan) {
            list += LanSession.hostName.takeIf { it.isNotBlank() && it != "Anfitrión" } ?: hostLabel
            list += LanSession.guestName.takeIf { it.isNotBlank() && it != "Invitado" } ?: guestLabel
        } else if (localHotseat) {
            list += playerLabels
        } else {
            list += youLabel
        }
        while (list.size < playersCount) list += "Bot ${list.size}"
        list
    }
    val botSeats = remember(playersCount, isLan, localHotseat) {
        when {
            isLan -> (2 until playersCount).toSet()
            localHotseat -> emptySet()
            else -> (1 until playersCount).toSet()
        }
    }
    val game = remember(seed, restart, playersCount, target, localHotseat, mySeat) {
        CaidaGame(names, botSeats, seed + restart * 97L, mySeat, target)
    }
    val history = rememberMatchHistory(game.message)
    var showHistory by remember { mutableStateOf(false) }
    val isHost = !isLan || LanSession.role == "host"
    val seatColors = listOf(Color(0xFFE63946), Color(0xFF2A9D8F), Color(0xFFFFB703), Color(0xFF3A86FF))
    fun avatarFor(seat: Int) = if (!isLan) {
        if (seat == 0) Profile.avatar else Avatars.all[(seat + 1) % Avatars.all.size]
    } else {
        if (seat == mySeat) Profile.avatar else Avatars.all[(mySeat + 1 + seat) % Avatars.all.size]
    }
    val activeSeat = if (localHotseat) game.current else mySeat

    fun localAction(action: String, block: () -> Unit) {
        block()
        revision++
        if (isLan) LanSession.sendAction(action)
    }

    fun remoteAction(action: String) {
        val p = action.split(":")
        when (p.getOrNull(0)) {
            "PLAY" -> p.getOrNull(1)?.toIntOrNull()?.let { game.playCard(it) }
            "NEXT" -> game.nextRound()
        }
        revision++
    }

    DisposableEffect(Unit) {
        LanSession.onRemoteAction = { action -> remoteAction(action) }
        onDispose { LanSession.onRemoteAction = null }
    }

    LaunchedEffect(revision, game.current, game.phase) {
        if (game.phase == CaidaPhase.PLAYING && game.isBotTurn() && isHost) {
            delay(850)
            val idx = game.botMove(BotSettings.difficulty)
            if (idx >= 0) {
                game.playCard(idx)
                revision++
                if (isLan) LanSession.sendAction("PLAY:$idx")
            }
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == CaidaPhase.OVER && !recorded) {
            recorded = true
            Prefs.recordResult(game.winner == mySeat)
            Feedback.win()
            Interstitial.maybeShow(activity)
        }
    }
    LaunchedEffect(game, game.dealTick) {
        val count = game.table.size
        dealt = 0
        reactions.clear()
        banner = ""
        repeat(count) {
            delay(75)
            dealt = it + 1
            Feedback.deal()
        }
        if (game.lastCantoSeat >= 0 && game.phase != CaidaPhase.OVER) {
            banner = cantoLabel
            reactions[game.lastCantoSeat] = "😲"
            Feedback.canto()
            Feedback.rumble(context)
            delay(1500)
            reactions.remove(game.lastCantoSeat)
            if (banner == cantoLabel) banner = ""
        }
    }
    LaunchedEffect(game, game.eventTick) {
        val seat = game.lastPlayedSeat
        val capture = game.lastCaptureBy
        if (game.lastPlayed == null && capture < 0) return@LaunchedEffect
        reactions.clear()
        banner = ""
        if (capture >= 0) {
            banner = caidaLabel
            reactions[capture] = "🔥"
            Feedback.slam()
            Feedback.rumble(context, strong = true)
            shake.snapTo(0f)
            shake.animateTo(
                0f,
                keyframes {
                    durationMillis = 430
                    -9f at 40
                    8f at 90
                    -6f at 150
                    5f at 210
                    -3f at 280
                    0f at 430
                }
            )
        } else if (seat >= 0) {
            reactions[seat] = "😮"
            Feedback.play()
            Feedback.buzz(context, 18)
        }
        val card = game.lastPlayed
        if (card != null) {
            flyCard = card
            fly.snapTo(0f)
            fly.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
            delay(140)
            flyCard = null
        }
        dealt = game.table.size
        delay(1500)
        reactions.clear()
        if (banner == caidaLabel) banner = ""
    }

    // Read revision so every mutation recomposes.
    val tick = revision

    GameScaffold(
        title = stringResource(R.string.caida_name),
        subtitle = if (isLan) "WiFi · ${stringResource(R.string.room_code)} ${LanSession.code}" else stringResource(R.string.play_bots),
        onBack = {
            if (isLan) LanSession.stop()
            Nav.go(Screen.Home)
        },
        actions = {
            IconButton(onClick = { showHistory = true }) { Icon(Icons.Rounded.History, null) }
            IconButton(onClick = { showRules = true }) { Icon(Icons.Rounded.Info, null) }
            if (!isLan) {
                IconButton(onClick = { Nav.go(Screen.Lobby("caida")) }) {
                    Icon(Icons.Rounded.Wifi, null, tint = palette.accent2)
                }
                IconButton(onClick = {
                    restart++
                    Feedback.click()
                }) { Icon(Icons.Rounded.Refresh, null) }
            }
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
                    val av = avatarFor(i)
                    PlayerBadge(
                        name = name,
                        points = game.points[i],
                        active = game.current == i && game.phase == CaidaPhase.PLAYING,
                        color = seatColors[i % 4],
                        avatarEmoji = av.emoji,
                        reaction = reactions[i],
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StatChip("🃏 ${game.deck.size}", color = palette.accent)
                Spacer(Modifier.width(6.dp))
                StatChip("🏆 ${game.target}", color = palette.accent2)
                Spacer(Modifier.weight(1f))
                if (isLan) StatChip(if (isHost) stringResource(R.string.host) else stringResource(R.string.guest))
            }

            Spacer(Modifier.weight(0.35f))
            Text(
                when {
                    game.phase == CaidaPhase.OVER -> stringResource(R.string.game_over)
                    game.phase == CaidaPhase.ROUND_OVER -> stringResource(R.string.round) + " ${game.round} · ${stringResource(R.string.ended)}"
                    !localHotseat && game.current == mySeat -> stringResource(R.string.your_turn)
                    else -> stringResource(R.string.turn_of, names.getOrElse(game.current) { "" })
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (game.current == activeSeat) palette.accent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                game.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(12.dp))

            // Table
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF0E5A3A).copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD8A93B).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().height(190.dp).offset(x = shake.value.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(12.dp)) {
                    Text(
                        stringResource(R.string.table),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Reveal(
                        visible = banner.isNotEmpty(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            banner,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFFFFD166),
                            modifier = Modifier
                                .background(Color(0xCC1A1208), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                    Row(
                        Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy((-18).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        game.table.take(dealt).takeLast(5).forEach { c ->
                            key(c.id) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = scaleIn(spring(dampingRatio = 0.5f, stiffness = 500f)) + fadeIn(tween(120))
                                ) {
                                    SpanishCardView(c, width = 62.dp)
                                }
                            }
                        }
                        if (game.table.isEmpty()) {
                            CardSlot("—", width = 56.dp)
                        }
                    }
                    if (dealt < game.table.size) {
                        Text(
                            stringResource(R.string.dealing),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                    val flying = flyCard
                    if (flying != null) {
                        Box(
                            Modifier.align(Alignment.Center).graphicsLayer {
                                val t = fly.value
                                translationY = (1f - t) * 170.dp.toPx()
                                rotationZ = (1f - t) * -24f
                                scaleX = 1f + (1f - t) * 0.4f
                                scaleY = 1f + (1f - t) * 0.4f
                                alpha = 0.25f + t * 0.75f
                            }
                        ) {
                            SpanishCardView(flying, width = 62.dp)
                        }
                    }
                    Row(
                        Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${game.table.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(stringResource(R.string.cards_on_table_label), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(width = 44.dp, height = 64.dp)) {
                            SpanishCardView(null, width = 44.dp, faceUp = false)
                        }
                    }
                    if (game.animatingCapture) {
                        LaunchedEffect(Unit) {
                            delay(500)
                            game.finishCaptureAnimation()
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.4f))
            if (game.phase == CaidaPhase.ROUND_OVER || game.phase == CaidaPhase.OVER) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.points) + ": " + names.indices.joinToString(" · ") { "${names[it]} ${game.points[it]}" },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (game.phase == CaidaPhase.ROUND_OVER) {
                            PrimaryButton(stringResource(R.string.next_round), Modifier.weight(1f)) {
                                localAction("NEXT") { game.nextRound() }
                            }
                        }
                        SecondaryButton(stringResource(R.string.play_again), Modifier.weight(1f)) {
                            if (!isLan) {
                                restart++
                            } else {
                                Nav.go(Screen.Home)
                            }
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.your_cards),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    game.hands[activeSeat.coerceIn(0, names.size - 1)].forEachIndexed { index, card ->
                        key(card.id) {
                            var drag by remember { mutableFloatStateOf(0f) }
                            val canPlay = game.current == activeSeat && game.phase == CaidaPhase.PLAYING
                            Box(
                                Modifier
                                    .graphicsLayer {
                                        translationY = drag
                                        rotationZ = drag / 26f
                                    }
                                    .then(
                                        if (canPlay) Modifier.pointerInput(game.current, game.phase, index) {
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, amount ->
                                                    change.consume()
                                                    drag = (drag + amount).coerceIn(-150f, 0f)
                                                },
                                                onDragEnd = {
                                                    if (drag < -64f) {
                                                        Feedback.play()
                                                        localAction("PLAY:$index") { game.playCard(index) }
                                                    }
                                                    drag = 0f
                                                },
                                                onDragCancel = { drag = 0f }
                                            )
                                        } else Modifier
                                    )
                            ) {
                                Reveal(visible = true) {
                                    SpanishCardView(
                                        card = card,
                                        width = 70.dp,
                                        highlighted = canPlay,
                                        onClick = {
                                            if (canPlay) {
                                                Feedback.play()
                                                localAction("PLAY:$index") { game.playCard(index) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (game.hands.getOrNull(activeSeat)?.isEmpty() == true) {
                        Text(stringResource(R.string.wait), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (game.current == activeSeat) stringResource(R.string.slide_to_play) else stringResource(R.string.wait),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                )
            }
            Spacer(Modifier.height(10.dp))
            MatchHistoryStrip(history, onOpen = { showHistory = true })
            Spacer(Modifier.height(4.dp))
            Text(
                game.log.lastOrNull().orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.accent,
                maxLines = 1
            )
            Spacer(Modifier.height(10.dp))
        }
        ConfettiOverlay(visible = game.phase == CaidaPhase.OVER)
    }

    if (game.phase == CaidaPhase.OVER && game.winner >= 0) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("${festivalEmoji()} ${stringResource(R.string.winner)}") },
            text = { Text("${names.getOrElse(game.winner) { "" }} · ${if (game.winner in names.indices) game.points[game.winner] else 0} ${stringResource(R.string.points)}") },
            confirmButton = {
                TextButton(onClick = {
                    if (!isLan) restart++ else Nav.go(Screen.Home)
                }) { Text(stringResource(R.string.play_again)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (isLan) LanSession.stop()
                    Nav.go(Screen.Home)
                }) { Text(stringResource(R.string.menu)) }
            }
        )
    }
    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.caida_name)) },
            text = { Text(stringResource(R.string.rules_caida)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
    if (showHistory) {
        MatchHistoryDialog(history, onDismiss = { showHistory = false })
    }
}

private fun festivalEmoji(): String = "🏆"

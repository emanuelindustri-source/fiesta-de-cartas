package com.fiestadecartas.app.game.fiesta

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.fiestadecartas.app.data.Cosmetics
import com.fiestadecartas.app.data.MatchTracker
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.game.FiestaCard
import com.fiestadecartas.app.game.FiestaColor
import com.fiestadecartas.app.game.FiestaKind
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.FiestaCardView
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.MatchHistoryDialog
import com.fiestadecartas.app.ui.MatchHistoryStrip
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.SecondaryButton
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.rememberMatchHistory
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay

private val FIESTA_COLORS = listOf(
    FiestaColor.RED to Color(0xFFE63946),
    FiestaColor.YELLOW to Color(0xFFFFB703),
    FiestaColor.GREEN to Color(0xFF2A9D8F),
    FiestaColor.BLUE to Color(0xFF3A86FF)
)

fun colorOf(c: FiestaColor): Color = FIESTA_COLORS.firstOrNull { it.first == c }?.second ?: Color(0xFF151515)

@Composable
fun FiestaScreen(lan: Boolean = false, hotseat: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val isLan = lan && LanSession.role != "none"
    val localHotseat = hotseat && !isLan
    val mySeat = if (isLan) LanSession.mySeat else 0
    val playersCount = if (isLan) LanSession.players.coerceIn(2, 4) else GameConfig.fiestaPlayers
    val target = GameConfig.fiestaTarget
    val localSeed = remember { System.currentTimeMillis() }
    val seed = if (isLan) LanSession.seed else localSeed
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var wildCard by remember { mutableStateOf<FiestaCard?>(null) }
    var recorded by remember { mutableStateOf(false) }

    val youLabel = stringResource(R.string.you)
    val hostLabel = stringResource(R.string.host)
    val guestLabel = stringResource(R.string.guest)
    val playerLabels = (1..playersCount).map { stringResource(R.string.player_number, it) }
    val names = remember(LanSession.hostName, LanSession.guestName, isLan, youLabel, hostLabel, guestLabel, localHotseat, playersCount, playerLabels) {
        val list = mutableListOf<String>()
        if (isLan) {
            list += LanSession.hostName.takeIf { it.isNotBlank() && it != "Anfitrión" } ?: hostLabel
            list += LanSession.guestName.takeIf { it.isNotBlank() && it != "Invitado" } ?: guestLabel
        } else if (localHotseat) {
            list += playerLabels
        } else list += youLabel
        while (list.size < playersCount) list += "Bot ${list.size}"
        list
    }
    val botSeats = remember(isLan, playersCount, localHotseat) {
        when {
            isLan -> emptySet()
            localHotseat -> emptySet()
            else -> (1 until playersCount).toSet()
        }
    }
    val game = remember(seed, restart, playersCount, target, localHotseat) {
        FiestaGame(names, botSeats, seed + restart * 71L, target)
    }
    val history = rememberMatchHistory(game.message)
    var showHistory by remember { mutableStateOf(false) }
    val fly = remember { Animatable(0f) }
    var flyCard by remember { mutableStateOf<FiestaCard?>(null) }
    LaunchedEffect(game.eventTick) {
        val card = game.lastPlayed ?: return@LaunchedEffect
        flyCard = card
        fly.snapTo(0f)
        fly.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        flyCard = null
    }
    val activeSeat = if (localHotseat) game.current else mySeat
    val isHost = !isLan || LanSession.role == "host"
    val myHand = game.hands.getOrNull(activeSeat).orEmpty()
    val canAct = game.phase == FiestaPhase.PLAYING && game.current == activeSeat

    fun applyLocal(action: String, block: () -> Unit) {
        block()
        revision++
        if (isLan) LanSession.sendAction(action)
    }

    fun remoteAction(action: String) {
        val p = action.split("|")
        when (p.getOrNull(0)) {
            "P" -> {
                val id = p.getOrNull(1)?.toIntOrNull()
                val col = p.getOrNull(2)?.toIntOrNull()?.let { FiestaColor.entries.getOrNull(it) }
                if (id != null) game.play(id, col)
            }
            "D" -> game.drawAndMaybePass()
            "S" -> game.passTurn()
            "N" -> game.nextRound()
        }
        revision++
    }

    DisposableEffect(Unit) {
        LanSession.onRemoteAction = { remoteAction(it) }
        onDispose { LanSession.onRemoteAction = null }
    }

    LaunchedEffect(revision, game.current, game.phase) {
        if (game.phase == FiestaPhase.PLAYING && game.isBotTurn() && isHost) {
            delay(900)
            val action = game.botStep(BotSettings.difficulty)
            if (action != null) {
                revision++
                if (isLan) LanSession.sendAction(action)
            }
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == FiestaPhase.OVER && !recorded) {
            recorded = true
            val mode = when {
                isLan -> "wifi"
                localHotseat -> "hotseat"
                else -> "bots"
            }
            MatchTracker.record(
                gameId = "fiesta",
                won = game.winner == mySeat,
                score = game.scores.getOrElse(mySeat) { 0 },
                mode = mode,
                players = names.size,
                seed = seed + restart * 71L,
                names = names,
                messages = history.toList()
            )
            Feedback.win()
            Interstitial.maybeShow(activity)
        }
    }
    val tick = revision

    GameScaffold(
        title = stringResource(R.string.fiesta_name),
        subtitle = if (isLan) "WiFi · ${LanSession.code}" else stringResource(R.string.play_bots),
        onBack = {
            if (isLan) LanSession.stop()
            Nav.go(Screen.Home)
        },
        actions = {
            IconButton(onClick = { showHistory = true }) { Icon(Icons.Rounded.History, null) }
            IconButton(onClick = { showRules = true }) { Icon(Icons.Rounded.Info, null) }
            if (!isLan) {
                IconButton(onClick = { Nav.go(Screen.Lobby("fiesta")) }) {
                    Icon(Icons.Rounded.Wifi, null, tint = palette.accent2)
                }
                IconButton(onClick = { restart++; Feedback.click() }) { Icon(Icons.Rounded.Refresh, null) }
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
                    val av = if (i == mySeat) Profile.avatar else Avatars.all[(i + 1) % Avatars.all.size]
                    PlayerBadge(
                        name = name,
                        points = game.hands[i].size,
                        active = game.current == i && game.phase == FiestaPhase.PLAYING,
                        color = FIESTA_COLORS[i % 4].second,
                        avatarEmoji = av.emoji,
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("🎯 ${game.scores[activeSeat]}", color = palette.accent)
                Spacer(Modifier.width(6.dp))
                StatChip("🏆 $target", color = palette.accent2)
                Spacer(Modifier.width(6.dp))
                if (isLan) StatChip(if (isHost) stringResource(R.string.host) else stringResource(R.string.guest))
            }
            Spacer(Modifier.weight(0.3f))

            Text(
                when {
                    game.phase == FiestaPhase.OVER -> stringResource(R.string.game_over)
                    game.current == activeSeat && !localHotseat -> stringResource(R.string.your_turn)
                    else -> stringResource(R.string.turn_of, names.getOrElse(game.current) { "" })
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (game.current == activeSeat) palette.accent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                game.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(12.dp))

            // Center table
            Box(
                Modifier.fillMaxWidth().height(210.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                colorOf(game.color).copy(alpha = 0.32f),
                                Color(Cosmetics.felt.color).copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        FiestaCardView(null, width = 78.dp, faceUp = false)
                        Text(
                            "${game.deck.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Box(contentAlignment = Alignment.Center) {
                        FiestaCardView(game.top(), width = 88.dp)
                        val flying = flyCard
                        if (flying != null) {
                            Box(
                                Modifier.graphicsLayer {
                                    val t = fly.value
                                    translationY = (1f - t) * 190.dp.toPx()
                                    rotationZ = (1f - t) * -18f
                                    scaleX = 1f + (1f - t) * 0.28f
                                    scaleY = 1f + (1f - t) * 0.28f
                                    alpha = 0.2f + t * 0.8f
                                }
                            ) {
                                FiestaCardView(flying, width = 88.dp)
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.choose_color), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                Spacer(Modifier.width(8.dp))
                FIESTA_COLORS.forEach { (c, col) ->
                    Box(
                        Modifier.size(if (game.color == c) 20.dp else 14.dp)
                            .background(col, CircleShape)
                            .alpha(if (game.color == c) 1f else 0.35f)
                    )
                    Spacer(Modifier.width(5.dp))
                }
            }

            Spacer(Modifier.weight(0.3f))
            if (game.phase != FiestaPhase.PLAYING) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    val winnerName = names.getOrElse(game.winner) { "" }
                    Text(
                        "🏆 $winnerName · ${game.scores.getOrElse(game.winner) { 0 }} ${stringResource(R.string.points)}",
                        Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (game.phase == FiestaPhase.ROUND_OVER && (!isLan || isHost)) {
                            PrimaryButton(stringResource(R.string.next_round), Modifier.weight(1f)) {
                                applyLocal("N") { game.nextRound() }
                            }
                        }
                        if (!isLan) {
                            SecondaryButton(stringResource(R.string.play_again), Modifier.weight(1f)) {
                                restart++
                            }
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.your_cards) + " · ${myHand.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    myHand.forEach { card ->
                        val ok = canAct && game.playable(card)
                        Box(Modifier.alpha(if (canAct && !ok) 0.45f else 1f)) {
                            FiestaCardView(
                                card = card,
                                width = 62.dp,
                                highlighted = ok,
                                onClick = {
                                    if (!canAct) return@FiestaCardView
                                    if (!game.playable(card)) {
                                        Feedback.error()
                                        return@FiestaCardView
                                    }
                                    if (card.color == FiestaColor.WILD) {
                                        wildCard = card
                                    } else {
                                        Feedback.play()
                                        applyLocal("P|${card.id}|${card.color.ordinal}") {
                                            game.play(card.id, card.color)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(stringResource(R.string.draw), Modifier.weight(1f), enabled = canAct && !game.hasDrawn) {
                        Feedback.click()
                        applyLocal("D") { game.drawAndMaybePass() }
                    }
                    SecondaryButton(stringResource(R.string.pass), Modifier.weight(1f), enabled = canAct && game.hasDrawn) {
                        applyLocal("S") { game.passTurn() }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
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
        ConfettiOverlay(visible = game.phase == FiestaPhase.OVER)
    }

    wildCard?.let { card ->
        AlertDialog(
            onDismissRequest = { wildCard = null },
            title = { Text(stringResource(R.string.choose_color)) },
            text = {
                Column {
                    FIESTA_COLORS.forEach { (c, col) ->
                        Surface(
                            onClick = {
                                Feedback.play()
                                applyLocal("P|${card.id}|${c.ordinal}") { game.play(card.id, c) }
                                wildCard = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = col.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(26.dp).background(col, CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    when (c) {
                                        FiestaColor.RED -> stringResource(R.string.red)
                                        FiestaColor.YELLOW -> stringResource(R.string.yellow)
                                        FiestaColor.GREEN -> stringResource(R.string.green)
                                        FiestaColor.BLUE -> stringResource(R.string.blue)
                                        else -> stringResource(R.string.wild)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (game.phase == FiestaPhase.OVER && game.winner >= 0) {
        // celebration handled by overlay
    }
    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.fiesta_name)) },
            text = { Text(stringResource(R.string.rules_fiesta)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
    if (showHistory) {
        MatchHistoryDialog(history, onDismiss = { showHistory = false })
    }
}

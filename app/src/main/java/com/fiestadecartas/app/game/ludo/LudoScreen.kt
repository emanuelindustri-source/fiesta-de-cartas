package com.fiestadecartas.app.game.ludo

import android.app.Activity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.DiceView
import com.fiestadecartas.app.ui.Feedback
import com.fiestadecartas.app.ui.GameScaffold
import com.fiestadecartas.app.ui.GlassPanel
import com.fiestadecartas.app.ui.PlayerBadge
import com.fiestadecartas.app.ui.PrimaryButton
import com.fiestadecartas.app.ui.SecondaryButton
import com.fiestadecartas.app.ui.StatChip
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sqrt

private val PLAYER_COLORS = listOf(
    Color(0xFFE63946), Color(0xFF2A9D8F), Color(0xFFF4B400), Color(0xFF3A86FF)
)

@Composable
fun LudoScreen(lan: Boolean = false, hotseat: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val isLan = lan && LanSession.role != "none"
    val localHotseat = hotseat && !isLan
    val mySeat = if (isLan) LanSession.mySeat else 0
    val playersCount = 4
    val localSeed = remember { System.currentTimeMillis() }
    val seed = if (isLan) LanSession.seed else localSeed
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var recorded by remember { mutableStateOf(false) }
    var rolling by remember { mutableStateOf(false) }
    LaunchedEffect(rolling) {
        if (rolling) {
            delay(420)
            rolling = false
        }
    }

    val youLabel = stringResource(R.string.you)
    val hostLabel = stringResource(R.string.host)
    val guestLabel = stringResource(R.string.guest)
    val playerLabels = (1..4).map { stringResource(R.string.player_number, it) }
    val names = remember(LanSession.hostName, LanSession.guestName, isLan, youLabel, hostLabel, guestLabel, localHotseat, playerLabels) {
        val list = mutableListOf<String>()
        if (isLan) {
            list += LanSession.hostName.takeIf { it.isNotBlank() && it != "Anfitrión" } ?: hostLabel
            list += LanSession.guestName.takeIf { it.isNotBlank() && it != "Invitado" } ?: guestLabel
        } else if (localHotseat) {
            list += playerLabels
        } else list += youLabel
        while (list.size < 4) list += "Bot ${list.size}"
        list
    }
    val botSeats = remember(isLan, localHotseat) {
        when {
            isLan -> setOf(2, 3)
            localHotseat -> emptySet()
            else -> setOf(1, 2, 3)
        }
    }
    val game = remember(seed, restart, localHotseat) {
        LudoGame(names, botSeats, seed + restart * 131L, GameConfig.ludoHomeTokens)
    }
    LaunchedEffect(game.dice, game.rolled) {
        if (game.rolled && game.phase == LudoPhase.PLAYING) {
            Feedback.dice()
            Feedback.buzz(context, 22)
        }
    }
    val isHost = !isLan || LanSession.role == "host"
    val activeSeat = if (localHotseat) game.current else mySeat
    val movable = if (game.rolled && game.current == activeSeat) game.legalMoves() else emptyList()

    fun sendAndApply(action: String, block: () -> Unit) {
        block()
        revision++
        if (isLan) LanSession.sendAction(action)
    }

    fun remoteAction(action: String) {
        when (action.substringBefore(':')) {
            "R" -> game.roll()
            "M" -> action.substringAfter(':').toIntOrNull()?.let { game.move(it) }
            "P" -> game.pass()
        }
        revision++
    }

    DisposableEffect(Unit) {
        LanSession.onRemoteAction = { remoteAction(it) }
        onDispose { LanSession.onRemoteAction = null }
    }

    LaunchedEffect(revision, game.current, game.rolled, game.phase) {
        if (game.phase == LudoPhase.PLAYING && game.current in botSeats && isHost) {
            delay(if (!game.rolled) 650L else 800L)
            if (!game.rolled) {
                val before = game.current
                game.roll()
                revision++
                if (isLan) LanSession.sendAction("R")
                if (game.current != before && game.rolled && game.legalMoves().isEmpty()) {
                    // pass will be handled on the next loop
                }
            } else {
                val moves = game.legalMoves()
                if (moves.isEmpty()) {
                    game.pass(); revision++
                    if (isLan) LanSession.sendAction("P")
                } else {
                    val token = game.botPick()
                    game.move(token); revision++
                    if (isLan) LanSession.sendAction("M:$token")
                }
            }
        }
    }

    LaunchedEffect(game.phase) {
        if (game.phase == LudoPhase.OVER && !recorded) {
            recorded = true
            Prefs.recordResult(game.winner == mySeat)
            Feedback.win()
            Interstitial.maybeShow(activity)
        }
    }

    // touch revision to subscribe
    val tick = revision

    GameScaffold(
        title = stringResource(R.string.ludo_name),
        subtitle = if (isLan) "WiFi · ${LanSession.code}" else stringResource(R.string.play_bots),
        onBack = {
            if (isLan) LanSession.stop()
            Nav.go(Screen.Home)
        },
        actions = {
            IconButton(onClick = { showRules = true }) { Icon(Icons.Rounded.Info, null) }
            if (!isLan) {
                IconButton(onClick = { Nav.go(Screen.Lobby("ludo")) }) {
                    Icon(Icons.Rounded.Wifi, null, tint = palette.accent2)
                }
                IconButton(onClick = { restart++; Feedback.click() }) { Icon(Icons.Rounded.Refresh, null) }
            }
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
                    val av = if (i == mySeat) Profile.avatar else Avatars.all[(i + 1) % Avatars.all.size]
                    PlayerBadge(
                        name = name,
                        points = game.homeCount(i),
                        active = game.current == i && game.phase == LudoPhase.PLAYING,
                        color = PLAYER_COLORS[i],
                        avatarEmoji = av.emoji,
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LudoBoard(
                    game = game,
                    movable = movable,
                    mySeat = activeSeat,
                    onToken = { t ->
                        Feedback.play()
                        sendAndApply("M:$t") { game.move(t) }
                    },
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        DiceView(game.dice, size = 58.dp, rolling = rolling)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                game.phase == LudoPhase.OVER -> stringResource(R.string.game_over)
                                game.current == activeSeat && !localHotseat -> stringResource(R.string.your_turn)
                                else -> stringResource(R.string.turn_of, names[game.current])
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (game.current == activeSeat) palette.accent else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            game.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2
                        )
                        if (game.lastCapture) StatChip(stringResource(R.string.captured), color = Color(0xFFE63946))
                    }
                    if (game.phase != LudoPhase.OVER && game.current == activeSeat) {
                        if (!game.rolled) {
                            PrimaryButton(stringResource(R.string.roll)) {
                                Feedback.click()
                                rolling = true
                                sendAndApply("R") { game.roll() }
                            }
                        } else if (movable.isEmpty()) {
                            PrimaryButton(stringResource(R.string.pass)) {
                                sendAndApply("P") { game.pass() }
                            }
                        } else {
                            StatChip(stringResource(R.string.tap_token), color = palette.accent)
                        }
                    }
                }
                if (game.rolled && movable.isNotEmpty() && game.current == activeSeat) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        movable.forEach { t ->
                            AssistChip(
                                onClick = {
                                    Feedback.play()
                                    sendAndApply("M:$t") { game.move(t) }
                                },
                                label = { Text("${stringResource(R.string.play)} ${t + 1}") }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        ConfettiOverlay(visible = game.phase == LudoPhase.OVER)
    }

    if (game.phase == LudoPhase.OVER && game.winner >= 0) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("🏆 ${stringResource(R.string.winner)}") },
            text = { Text("${names[game.winner]} · ${game.homeCount(game.winner)}/4") },
            confirmButton = {
                TextButton(onClick = { if (!isLan) restart++ else Nav.go(Screen.Home) }) {
                    Text(stringResource(R.string.play_again))
                }
            },
            dismissButton = {
                TextButton(onClick = { if (isLan) LanSession.stop(); Nav.go(Screen.Home) }) {
                    Text(stringResource(R.string.menu))
                }
            }
        )
    }
    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.ludo_name)) },
            text = { Text(stringResource(R.string.rules_ludo)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
}

@Composable
private fun LudoBoard(game: LudoGame, movable: List<Int>, mySeat: Int, onToken: (Int) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.45f, 1f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "p"
    )
    val cream = Color(0xFFFFF7E8)
    val track = Color(0xFFF2E7D0)

    Canvas(
        modifier.pointerInput(movable, game.rolled, game.current) {
            detectTapGestures { tap ->
                if (movable.isEmpty() || game.current != mySeat) return@detectTapGestures
                val cell = minOf(size.width, size.height) / 15f
                movable.forEach { token ->
                    val c = tokenCenter(game, mySeat, token, cell)
                    val dx = tap.x - c.x
                    val dy = tap.y - c.y
                    if (sqrt(dx * dx + dy * dy) < cell * 0.80f) {
                        onToken(token)
                    }
                }
            }
        }
    ) {
        val cell = size.minDimension / 15f
        val boardSize = cell * 15f
        val offset = Offset((size.width - boardSize) / 2f, (size.height - boardSize) / 2f)

        // Board shadow and carved wooden frame.
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.32f),
            topLeft = Offset(cell * 0.18f, cell * 0.24f),
            size = Size(boardSize, boardSize),
            cornerRadius = CornerRadius(cell * 0.85f)
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF3A2410), Color(0xFF8A5A2B), Color(0xFF5A3414), Color(0xFF2A1608)),
                start = offset,
                end = Offset(offset.x + boardSize, offset.y + boardSize)
            ),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cell * 0.8f)
        )
        drawRoundRect(
            color = Color(0xFFD8A93B).copy(alpha = 0.55f),
            topLeft = Offset(cell * 0.10f, cell * 0.10f),
            size = Size(size.width - cell * 0.20f, size.height - cell * 0.20f),
            cornerRadius = CornerRadius(cell * 0.72f),
            style = Stroke(width = cell * 0.055f)
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFFFFFDF5), Color(0xFFF8EFDD), Color(0xFFE9D9B8)),
                start = offset,
                end = Offset(offset.x + boardSize, offset.y + boardSize)
            ),
            topLeft = offset,
            size = Size(boardSize, boardSize),
            cornerRadius = CornerRadius(cell * 0.45f)
        )
        drawRoundRect(
            color = Color(0xFF7A5A2E).copy(alpha = 0.45f),
            topLeft = offset,
            size = Size(boardSize, boardSize),
            cornerRadius = CornerRadius(cell * 0.45f),
            style = Stroke(width = cell * 0.045f)
        )

        // Yards
        for (p in 0..3) {
            val o = LUDO_YARD_ORIGIN[p]
            val topLeft = Offset(offset.x + o.x * cell, offset.y + o.y * cell)
            drawRoundRect(
                color = PLAYER_COLORS[p].copy(alpha = 0.92f),
                topLeft = topLeft,
                size = Size(cell * 6f, cell * 6f),
                cornerRadius = CornerRadius(cell * 0.5f)
            )
            drawRoundRect(
                color = cream,
                topLeft = Offset(topLeft.x + cell * 0.55f, topLeft.y + cell * 0.55f),
                size = Size(cell * 4.9f, cell * 4.9f),
                cornerRadius = CornerRadius(cell * 0.4f)
            )
            for (t in 0..3) {
                val yc = game.yardCell(p, t)
                drawCircle(
                    color = PLAYER_COLORS[p].copy(alpha = 0.18f),
                    radius = cell * 0.52f,
                    center = Offset(offset.x + (yc.x + 0.5f) * cell, offset.y + (yc.y + 0.5f) * cell)
                )
                drawCircle(
                    color = PLAYER_COLORS[p].copy(alpha = 0.45f),
                    radius = cell * 0.44f,
                    center = Offset(offset.x + (yc.x + 0.5f) * cell, offset.y + (yc.y + 0.5f) * cell),
                    style = Stroke(width = cell * 0.05f)
                )
            }
        }

        // Ring
        LUDO_PATH.forEachIndexed { index, c ->
            val topLeft = Offset(offset.x + c.x * cell + cell * 0.06f, offset.y + c.y * cell + cell * 0.06f)
            val isStart = index in LUDO_STARTS
            val startPlayer = LUDO_STARTS.indexOf(index)
            val fill = when {
                isStart -> PLAYER_COLORS[startPlayer].copy(alpha = 0.75f)
                index in LUDO_SAFE -> Color(0xFFE8DCC4)
                else -> track
            }
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(topLeft.x + cell * 0.045f, topLeft.y + cell * 0.06f),
                size = Size(cell * 0.88f, cell * 0.88f),
                cornerRadius = CornerRadius(cell * 0.18f)
            )
            drawRoundRect(
                color = fill,
                topLeft = topLeft,
                size = Size(cell * 0.88f, cell * 0.88f),
                cornerRadius = CornerRadius(cell * 0.18f)
            )
            drawRoundRect(
                color = Color(0xFF8C7B5F).copy(alpha = 0.35f),
                topLeft = topLeft,
                size = Size(cell * 0.88f, cell * 0.88f),
                cornerRadius = CornerRadius(cell * 0.18f),
                style = Stroke(width = cell * 0.04f)
            )
            if (index in LUDO_SAFE && !isStart) {
                val star = starPath(
                    Offset(topLeft.x + cell * 0.44f, topLeft.y + cell * 0.44f),
                    cell * 0.20f
                )
                drawPath(star, Color(0xFF8C7B5F).copy(alpha = 0.45f))
                drawPath(star, Color(0xFFFFF6E2).copy(alpha = 0.55f), style = Stroke(width = cell * 0.025f))
            }
            if (isStart) {
                drawCircle(
                    Color.White.copy(alpha = 0.35f),
                    radius = cell * 0.16f,
                    center = Offset(topLeft.x + cell * 0.44f, topLeft.y + cell * 0.44f),
                    style = Stroke(width = cell * 0.035f)
                )
            }
        }

        // Home lanes
        for (p in 0..3) {
            LUDO_LANES[p].forEachIndexed { i, c ->
                val topLeft = Offset(offset.x + c.x * cell + cell * 0.07f, offset.y + c.y * cell + cell * 0.07f)
                drawRoundRect(
                    color = PLAYER_COLORS[p].copy(alpha = 0.35f + i * 0.08f),
                    topLeft = topLeft,
                    size = Size(cell * 0.86f, cell * 0.86f),
                    cornerRadius = CornerRadius(cell * 0.18f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.16f),
                    topLeft = Offset(topLeft.x + cell * 0.10f, topLeft.y + cell * 0.10f),
                    size = Size(cell * 0.66f, cell * 0.20f),
                    cornerRadius = CornerRadius(cell * 0.09f)
                )
                drawRoundRect(
                    Color(0xFF8C7B5F).copy(alpha = 0.25f),
                    topLeft = topLeft,
                    size = Size(cell * 0.86f, cell * 0.86f),
                    cornerRadius = CornerRadius(cell * 0.18f),
                    style = Stroke(width = cell * 0.035f)
                )
            }
        }

        // Center triangles
        val c0 = Offset(offset.x + cell * 6f, offset.y + cell * 6f)
        val c1 = Offset(offset.x + cell * 9f, offset.y + cell * 9f)
        val ctr = Offset(offset.x + cell * 7.5f, offset.y + cell * 7.5f)
        fun triangle(a: Offset, b: Offset, color: Color) {
            val path = Path().apply {
                moveTo(ctr.x, ctr.y); lineTo(a.x, a.y); lineTo(b.x, b.y); close()
            }
            drawPath(path, color)
        }
        triangle(Offset(c0.x, c0.y), Offset(c0.x, c1.y), PLAYER_COLORS[1].copy(alpha = 0.8f))
        triangle(Offset(c0.x, c0.y), Offset(c1.x, c0.y), PLAYER_COLORS[2].copy(alpha = 0.8f))
        triangle(Offset(c1.x, c0.y), Offset(c1.x, c1.y), PLAYER_COLORS[3].copy(alpha = 0.8f))
        triangle(Offset(c0.x, c1.y), Offset(c1.x, c1.y), PLAYER_COLORS[0].copy(alpha = 0.8f))
        drawRect(
            color = Color(0xFFD8A93B),
            topLeft = c0,
            size = Size(cell * 3f, cell * 3f),
            style = Stroke(width = cell * 0.08f)
        )
        drawCircle(
            Color(0xFFD8A93B).copy(alpha = 0.45f),
            radius = cell * 1.30f,
            center = ctr,
            style = Stroke(width = cell * 0.045f)
        )
        drawCircle(
            Color.White.copy(alpha = 0.18f),
            radius = cell * 0.95f,
            center = ctr,
            style = Stroke(width = cell * 0.035f)
        )
        drawPath(starPath(ctr, cell * 0.58f), Color(0xFFD8A93B).copy(alpha = 0.65f))
        drawPath(starPath(ctr, cell * 0.58f), Color(0xFFFFF6E2).copy(alpha = 0.45f), style = Stroke(width = cell * 0.03f))

        // Tokens with a small 3D finish.
        for (p in 0..3) {
            for (t in 0..3) {
                val c = tokenCenter(game, p, t, cell, offset)
                val isMovable = p == mySeat && game.current == mySeat && t in movable
                if (isMovable) {
                    drawCircle(
                        color = palette.accent.copy(alpha = 0.25f + 0.35f * pulse),
                        radius = cell * (0.62f + 0.10f * pulse),
                        center = c
                    )
                }
                val isHome = game.tokens[p][t] == 56
                drawCircle(Color.Black.copy(alpha = 0.24f), radius = cell * 0.42f, center = Offset(c.x + cell * 0.055f, c.y + cell * 0.075f))
                drawCircle(Color(0xFFFFF6E2), radius = cell * 0.41f, center = c)
                drawCircle(Color(0xFF8C7B5F).copy(alpha = 0.35f), radius = cell * 0.41f, center = c, style = Stroke(width = cell * 0.035f))
                drawCircle(
                    Brush.radialGradient(
                        listOf(PLAYER_COLORS[p].copy(alpha = 0.98f), PLAYER_COLORS[p].copy(alpha = 0.72f)),
                        center = Offset(c.x - cell * 0.10f, c.y - cell * 0.12f),
                        radius = cell * 0.40f
                    ),
                    radius = cell * 0.34f,
                    center = c
                )
                drawCircle(Color.White.copy(alpha = 0.55f), radius = cell * 0.10f, center = Offset(c.x - cell * 0.10f, c.y - cell * 0.12f))
                if (isHome) {
                    drawPath(starPath(c, cell * 0.16f), Color(0xFFFFF6E2).copy(alpha = 0.95f))
                }
            }
        }
    }
}

private fun tokenCenter(game: LudoGame, player: Int, token: Int, cell: Float, offset: Offset = Offset.Zero): Offset {
    val pos = game.tokens[player][token]
    if (pos < 0) {
        val yc = game.yardCell(player, token)
        return Offset(offset.x + (yc.x + 0.5f) * cell, offset.y + (yc.y + 0.5f) * cell)
    }
    if (pos <= 50) {
        val c = LUDO_PATH[(LUDO_STARTS[player] + pos) % 52]
        return Offset(offset.x + (c.x + 0.5f) * cell, offset.y + (c.y + 0.5f) * cell)
    }
    if (pos <= 55) {
        val c = LUDO_LANES[player][pos - 51]
        return Offset(offset.x + (c.x + 0.5f) * cell, offset.y + (c.y + 0.5f) * cell)
    }
    // Home: cluster inside the player's triangle.
    val base = when (player) {
        0 -> Offset(7.5f, 8.35f)
        1 -> Offset(6.65f, 7.5f)
        2 -> Offset(7.5f, 6.65f)
        else -> Offset(8.35f, 7.5f)
    }
    val dx = when (token) { 0 -> -0.14f; 1 -> 0.14f; 2 -> -0.14f; else -> 0.14f }
    val dy = when (token) { 0 -> -0.10f; 1 -> -0.10f; 2 -> 0.16f; else -> 0.16f }
    return Offset(offset.x + (base.x + dx) * cell, offset.y + (base.y + dy) * cell)
}

private fun Float.pow2() = this.pow(2)

private fun starPath(center: Offset, radius: Float): Path {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.toRadians((-90 + i * 36).toDouble())
        val r = if (i % 2 == 0) radius else radius * 0.44f
        val x = center.x + (kotlin.math.cos(angle) * r).toFloat()
        val y = center.y + (kotlin.math.sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
package com.fiestadecartas.app.game.domino

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.fiestadecartas.app.data.Cosmetics
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.MatchTracker
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.ConfettiOverlay
import com.fiestadecartas.app.ui.DominoTileView
import com.fiestadecartas.app.ui.Feedback
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

private val DOMINO_COLORS = listOf(
    Color(0xFFC9962B), Color(0xFF2A9D8F), Color(0xFFE63946), Color(0xFF3A86FF)
)

@Composable
fun DominoScreen(hotseat: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    val palette = LocalPalette.current
    val playersCount = GameConfig.dominoPlayers
    val target = GameConfig.dominoTarget
    val localSeed = remember { System.currentTimeMillis() }
    var restart by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var showRules by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(-1) }
    var recorded by remember(restart) { mutableStateOf(false) }

    val myName = Prefs.nickname.ifBlank { stringResource(R.string.you) }
    val playerLabels = (1..playersCount).map { stringResource(R.string.player_number, it) }
    val names = remember(myName, hotseat, playersCount, playerLabels) {
        val list = mutableListOf<String>()
        if (hotseat) list += playerLabels else list += myName
        var bot = 1
        while (list.size < playersCount) { list += "Bot $bot"; bot++ }
        list
    }
    val bots = remember(hotseat, playersCount) {
        if (hotseat) emptySet() else (1 until playersCount).toSet()
    }
    val game = remember(localSeed, restart, playersCount, target, hotseat) {
        DominoGame(names, bots, localSeed + restart * 61L, target)
    }
    val history = rememberMatchHistory(game.message)
    val activeSeat = if (hotseat) game.current else 0
    val canAct = game.phase == DominoPhase.PLAYING && game.current == activeSeat
    val myHand = game.hands.getOrElse(activeSeat) { emptyList() }
    val playable = myHand.indices.filter { game.canPlay(myHand[it]) }

    fun playTile(index: Int, side: String) {
        if (!canAct || index !in myHand.indices) return
        if (game.play(index, side)) {
            Feedback.play()
            selected = -1
            revision++
        } else {
            Feedback.error()
        }
    }

    LaunchedEffect(revision, game.current, game.phase) {
        if (game.phase == DominoPhase.PLAYING && game.isBotTurn()) {
            delay(650)
            game.botMove(BotSettings.difficulty)
            revision++
        }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == DominoPhase.OVER && !recorded) {
            recorded = true
            MatchTracker.record(
                gameId = "domino",
                won = game.winner == 0,
                score = game.points.getOrElse(0) { 0 },
                mode = if (hotseat) "hotseat" else "bots",
                players = names.size,
                seed = localSeed + restart * 61L,
                names = names,
                messages = history.toList()
            )
            Feedback.win()
            Interstitial.maybeShow(activity)
        }
    }

    val tick = revision
    GameScaffold(
        title = stringResource(R.string.domino_name),
        subtitle = if (hotseat) stringResource(R.string.play_hotseat) else stringResource(R.string.play_bots),
        onBack = { Nav.go(Screen.Home) },
        actions = {
            IconButton(onClick = { showHistory = true }) { Icon(Icons.Rounded.History, null) }
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
                        points = game.points[i],
                        active = game.current == i && game.phase == DominoPhase.PLAYING,
                        color = DOMINO_COLORS[i % DOMINO_COLORS.size],
                        avatarEmoji = av.emoji,
                        avatarColor = Color(av.color),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("${stringResource(R.string.domino_points)} ${game.points[activeSeat]}/$target", color = palette.accent)
                Spacer(Modifier.width(6.dp))
                StatChip("${stringResource(R.string.domino_boneyard)} ${game.boneyard.size}", color = palette.accent2)
                Spacer(Modifier.weight(1f))
                if (hotseat) StatChip(stringResource(R.string.play_hotseat), color = palette.accent3)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (game.phase == DominoPhase.PLAYING && game.current == activeSeat) stringResource(R.string.your_turn)
                else stringResource(R.string.turn_of, names.getOrElse(game.current) { "" }),
                style = MaterialTheme.typography.titleMedium,
                color = if (game.current == activeSeat) palette.accent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Text(
                game.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(Cosmetics.felt.color).copy(alpha = 0.78f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(Cosmetics.felt.accent).copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth().height(170.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(10.dp)) {
                    if (game.chain.isEmpty()) {
                        Text(
                            "—",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val scroll = rememberScrollState()
                        LaunchedEffect(game.chain.size) {
                            scroll.animateScrollTo(scroll.maxValue)
                        }
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(scroll),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            game.chain.forEach { played ->
                                DominoTileView(DominoTile(played.left, played.right), width = 58.dp)
                            }
                        }
                    }
                    Row(
                        Modifier.align(Alignment.BottomStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${game.leftEnd}",
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.domino_left), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    }
                    Row(
                        Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.domino_right),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${game.rightEnd}",
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (game.phase == DominoPhase.ROUND_OVER || game.phase == DominoPhase.OVER) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text(game.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        names.indices.joinToString(" · ") { "${names[it]} ${game.points[it]}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (game.phase == DominoPhase.ROUND_OVER) {
                            PrimaryButton(stringResource(R.string.next_round), Modifier.weight(1f)) {
                                game.nextRound()
                                revision++
                            }
                        }
                        SecondaryButton(stringResource(R.string.play_again), Modifier.weight(1f)) {
                            restart++
                        }
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    myHand.forEachIndexed { index, tile ->
                        Box(Modifier.alpha(if (game.canPlay(tile) && canAct) 1f else 0.55f)) {
                            DominoTileView(
                                tile = tile,
                                width = 64.dp,
                                highlighted = index == selected,
                                onClick = {
                                    if (!canAct) return@DominoTileView
                                    if (!game.canPlay(tile)) {
                                        Feedback.error()
                                        return@DominoTileView
                                    }
                                    val left = game.canPlayLeft(tile)
                                    val right = game.canPlayRight(tile)
                                    when {
                                        game.chain.isEmpty() -> playTile(index, "R")
                                        left && right -> selected = index
                                        left -> playTile(index, "L")
                                        else -> playTile(index, "R")
                                    }
                                }
                            )
                        }
                    }
                }
                if (selected in myHand.indices) {
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.domino_choose_side), style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(stringResource(R.string.domino_left)) { playTile(selected, "L") }
                        SecondaryButton(stringResource(R.string.domino_right)) { playTile(selected, "R") }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (canAct && playable.isEmpty() && game.boneyard.isNotEmpty()) {
                        SecondaryButton(stringResource(R.string.domino_draw)) {
                            game.draw()
                            revision++
                        }
                    }
                    if (canAct && playable.isEmpty() && game.boneyard.isEmpty()) {
                        SecondaryButton(stringResource(R.string.domino_pass)) {
                            game.pass()
                            revision++
                        }
                    }
                    Text(
                        if (canAct) stringResource(R.string.tap_card) else stringResource(R.string.wait),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            MatchHistoryStrip(history, onOpen = { showHistory = true })
            Spacer(Modifier.height(8.dp))
        }
        ConfettiOverlay(visible = game.phase == DominoPhase.OVER && game.winner == activeSeat)
    }

    if (game.phase == DominoPhase.OVER) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("🏆 ${stringResource(R.string.winner)}") },
            text = {
                Text(
                    "${names.getOrElse(game.winner) { "" }} · ${game.points.getOrElse(game.winner) { 0 }} ${stringResource(R.string.domino_points)}"
                )
            },
            confirmButton = {
                TextButton(onClick = { restart++ }) { Text(stringResource(R.string.play_again)) }
            },
            dismissButton = {
                TextButton(onClick = { Nav.go(Screen.Home) }) { Text(stringResource(R.string.menu)) }
            }
        )
    }
    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text(stringResource(R.string.domino_name)) },
            text = { Text(stringResource(R.string.rules_domino)) },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text(stringResource(R.string.got_it)) } }
        )
    }
    if (showHistory) {
        MatchHistoryDialog(history, onDismiss = { showHistory = false })
    }
}

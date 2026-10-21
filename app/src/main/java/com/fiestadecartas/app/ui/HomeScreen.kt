package com.fiestadecartas.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.ads.BannerAd
import com.fiestadecartas.app.data.BotDifficulty
import com.fiestadecartas.app.data.BotSettings
import com.fiestadecartas.app.data.DeviceInfo
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.theme.Festival
import com.fiestadecartas.app.ui.theme.LocalFestival
import com.fiestadecartas.app.ui.theme.LocalPalette
import com.fiestadecartas.app.ui.theme.paletteFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GameEntry(
    val id: String,
    val nameRes: Int,
    val descRes: Int,
    val emoji: String,
    val accent: Color,
    val lan: Boolean,
    val rulesRes: Int
)

val GAMES = listOf(
    GameEntry("caida", R.string.caida_name, R.string.caida_desc, "🃏", Color(0xFFE63946), true, R.string.rules_caida),
    GameEntry("ludo", R.string.ludo_name, R.string.ludo_desc, "🎲", Color(0xFF2A9D8F), true, R.string.rules_ludo),
    GameEntry("fiesta", R.string.fiesta_name, R.string.fiesta_desc, "🎨", Color(0xFFFFB703), true, R.string.rules_fiesta),
    GameEntry("kemix", R.string.kemix_name, R.string.kemix_desc, "🪷", Color(0xFFFF6B9D), true, R.string.rules_kemix),
    GameEntry("nervioso", R.string.nervioso_name, R.string.nervioso_desc, "✋", Color(0xFF8E7DBE), true, R.string.rules_nervioso)
)

@Composable
fun HomeScreen() {
    val festival = LocalFestival.current
    val palette = LocalPalette.current
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val contentWidth = DeviceInfo.contentWidth(context)
    val isTv = DeviceInfo.isTelevision(context)
    val firstFocus = remember { FocusRequester() }
    var rulesFor by remember { mutableStateOf<GameEntry?>(null) }
    var modeFor by remember { mutableStateOf<GameEntry?>(null) }
    LaunchedEffect(isTv) {
        if (isTv) runCatching { firstFocus.requestFocus() }
    }
    LaunchedEffect(Profile.levelUpTo) {
        val reached = Profile.levelUpTo
        if (reached > 0) {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.level_up, reached),
                android.widget.Toast.LENGTH_LONG
            ).show()
            Feedback.win()
            Profile.clearLevelUp()
        }
    }
    val dateText = remember {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMM")
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }
    FiestaBackground {
        Column(
            Modifier.fillMaxHeight().widthIn(max = contentWidth).fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(scroll).statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 46.dp, seasonal = festival.emoji)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dateText.replaceFirstChar { it.uppercase() } + " · " + festival.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconPill(Icons.Rounded.CameraAlt) { Nav.go(Screen.Scanner) }
                Spacer(Modifier.width(8.dp))
                ProfilePill { Nav.go(Screen.Profile) }
                Spacer(Modifier.width(8.dp))
                IconPill(Icons.Rounded.Settings) { Nav.go(Screen.Settings) }
            }

            Spacer(Modifier.height(18.dp))
            FestivalHero(festival) { Nav.go(Screen.Legends) }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.choose_game), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.weight(1f))
                StatChip(stringResource(R.string.games) + " · ${GAMES.size}")
            }
            Spacer(Modifier.height(12.dp))
            GAMES.forEachIndexed { index, game ->
                GameCard(
                    game = game,
                    modifier = if (index == 0) Modifier.focusRequester(firstFocus) else Modifier
                ) {
                    if (game.lan) modeFor = game else modeFor = game
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(10.dp))
            ShareBanner { Nav.go(Screen.Share) }
            Spacer(Modifier.height(16.dp))
            BannerAd(Modifier.padding(vertical = 4.dp))
            Text(
                stringResource(R.string.made_in),
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(30.dp))
        }
    }
    rulesFor?.let { game ->
        AlertDialog(
            onDismissRequest = { rulesFor = null },
            title = { Text(stringResource(game.nameRes) + " · " + stringResource(R.string.rules)) },
            text = { Text(stringResource(game.rulesRes)) },
            confirmButton = { TextButton(onClick = { rulesFor = null }) { Text(stringResource(R.string.got_it)) } }
        )
    }
    modeFor?.let { game ->
        MatchSetupDialog(
            game = game,
            onDismiss = { modeFor = null },
            onBots = {
                modeFor = null
                Nav.go(Screen.Game(game.id))
            },
            onFriends = {
                modeFor = null
                Nav.go(Screen.Lobby(game.id))
            },
            onHotseat = {
                modeFor = null
                Nav.go(Screen.Game(game.id, hotseat = true))
            }
        )
    }
}

@Composable
private fun ProfilePill(onClick: () -> Unit) {
    val profile = Profile.avatar
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(profile.emoji, fontSize = 22.sp)
            Box(
                Modifier.align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    "${Profile.level}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MatchSetupDialog(
    game: GameEntry,
    onDismiss: () -> Unit,
    onBots: () -> Unit,
    onFriends: () -> Unit,
    onHotseat: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().heightIn(max = 660.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(stringResource(game.nameRes), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(R.string.configure),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    when (game.id) {
                        "caida" -> {
                            RuleChips(
                                label = stringResource(R.string.players),
                                options = listOf(2, 3, 4),
                                selected = GameConfig.caidaPlayers,
                                onSelect = { GameConfig.updateCaidaPlayers(it) }
                            )
                            RuleChips(
                                label = stringResource(R.string.target_points),
                                options = listOf(24, 30, 50),
                                selected = GameConfig.caidaTarget,
                                onSelect = { GameConfig.updateCaidaTarget(it) }
                            )
                        }
                        "fiesta" -> {
                            RuleChips(
                                label = stringResource(R.string.players),
                                options = listOf(2, 3, 4),
                                selected = GameConfig.fiestaPlayers,
                                onSelect = { GameConfig.updateFiestaPlayers(it) }
                            )
                            RuleChips(
                                label = stringResource(R.string.target_points),
                                options = listOf(200, 300, 500),
                                selected = GameConfig.fiestaTarget,
                                onSelect = { GameConfig.updateFiestaTarget(it) }
                            )
                        }
                        "ludo" -> {
                            Text(
                                stringResource(R.string.players) + ": 4",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            RuleChips(
                                label = stringResource(R.string.tokens_to_home),
                                options = listOf(2, 3, 4),
                                selected = GameConfig.ludoHomeTokens,
                                onSelect = { GameConfig.updateLudoHomeTokens(it) }
                            )
                        }
                        "nervioso" -> RuleChips(
                            label = stringResource(R.string.start_cards),
                            options = listOf(5, 7, 9),
                            selected = GameConfig.nerviosoStartCards,
                            onSelect = { GameConfig.updateNerviosoStartCards(it) }
                        )
                        else -> Text(
                            "Kemix · 6 parejas · 4 jugadores",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    DifficultyPicker()
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(stringResource(R.string.play_bots), Modifier.fillMaxWidth(), onClick = onBots)
                if (game.lan) {
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(stringResource(R.string.play_friends), Modifier.fillMaxWidth(), onClick = onFriends)
                }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(stringResource(R.string.play_hotseat), Modifier.fillMaxWidth(), onClick = onHotseat)
            }
        }
    }
}

@Composable
private fun DifficultyPicker() {
    Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BotDifficulty.entries.forEach { difficulty ->
            FilterChip(
                selected = BotSettings.difficulty == difficulty,
                onClick = {
                    BotSettings.set(difficulty)
                    Feedback.click()
                },
                label = {
                    Text(
                        stringResource(
                            when (difficulty) {
                                BotDifficulty.RELAXED -> R.string.difficulty_easy
                                BotDifficulty.BALANCED -> R.string.difficulty_normal
                                BotDifficulty.SHARP -> R.string.difficulty_hard
                            }
                        ),
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
    Text(
        stringResource(R.string.difficulty_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    )
}

@Composable
private fun RuleChips(
    label: String,
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = {
                    onSelect(value)
                    Feedback.click()
                },
                label = { Text("$value") }
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun IconPill(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun FestivalHero(festival: Festival, onLegends: () -> Unit) {
    val palette = LocalPalette.current
    val (next, days) = remember { Festival.daysToNext() }
    val infinite = rememberInfiniteTransition(label = "hero")
    val float by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "float"
    )
    val titleRes = when (festival) {
        Festival.FIESTA -> R.string.fest_title_fiesta
        Festival.HALLOWEEN -> R.string.fest_title_halloween
        Festival.CHRISTMAS -> R.string.fest_title_christmas
        Festival.REYES -> R.string.fest_title_reyes
        Festival.VALENTINE -> R.string.fest_title_valentine
        Festival.EASTER -> R.string.fest_title_easter
        Festival.SEMANA_SANTA -> R.string.fest_title_semana
    }
    val storyRes = when (festival) {
        Festival.FIESTA -> R.string.fest_story_fiesta
        Festival.HALLOWEEN -> R.string.fest_story_halloween
        Festival.CHRISTMAS -> R.string.fest_story_christmas
        Festival.REYES -> R.string.fest_story_reyes
        Festival.VALENTINE -> R.string.fest_story_valentine
        Festival.EASTER -> R.string.fest_story_easter
        Festival.SEMANA_SANTA -> R.string.fest_story_semana
    }
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            palette.accent.copy(alpha = 0.30f),
                            palette.accent2.copy(alpha = 0.19f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(palette.accent.copy(alpha = 0.10f), radius = size.width * 0.45f, center = Offset(size.width * 0.92f, size.height * 0.1f))
                drawCircle(palette.accent2.copy(alpha = 0.10f), radius = size.width * 0.3f, center = Offset(size.width * 0.05f, size.height * 0.95f))
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        festival.emoji,
                        fontSize = 52.sp,
                        modifier = Modifier.scale(1f + float * 0.06f)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        StatChip(stringResource(R.string.season_now), color = palette.accent)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            stringResource(titleRes),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(storyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatChip("${next.emoji} ${stringResource(R.string.next_fest)} · ${stringResource(R.string.days_left, days)}")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onLegends) {
                        Text(stringResource(R.string.see_legends), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: GameEntry, modifier: Modifier = Modifier, onPlay: () -> Unit) {
    val festival = LocalFestival.current
    val accents = when (game.id) {
        "caida" -> listOf(Color(0xFFE63946), Color(0xFFFFB703))
        "ludo" -> listOf(Color(0xFF2A9D8F), Color(0xFF3A86FF))
        "fiesta" -> listOf(Color(0xFFFFB703), Color(0xFFEF476F))
        "kemix" -> listOf(Color(0xFFFF6B9D), Color(0xFF8E7DBE))
        else -> listOf(Color(0xFF8E7DBE), Color(0xFF6FD8D0))
    }
    Surface(
        onClick = onPlay,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(76.dp)
                        .background(Brush.linearGradient(accents), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    GameIllustration(game.id, Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(game.nameRes),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (game.lan) {
                            Spacer(Modifier.width(6.dp))
                            StatChip(stringResource(R.string.wifi_badge), color = accents.first())
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        stringResource(game.descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatChip("🎮 ${stringResource(R.string.play_bots)}", color = accents.first())
                        if (game.lan) {
                            StatChip("📡 ${stringResource(R.string.online)}", color = accents.last())
                        }
                    }
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    }
    if (game.lan) {
        // The WiFi chip opens the lobby; the card itself starts a local match.
    }
}

@Composable
fun GameIllustration(id: String, modifier: Modifier = Modifier) {
    when (id) {
        "caida" -> Box(modifier, contentAlignment = Alignment.Center) {
            Box(Modifier.offset(x = (-8).dp, y = 2.dp).rotate(-14f)) { SpanishCardView(null, width = 34.dp, faceUp = false) }
            Box(Modifier.offset(x = 8.dp, y = (-2).dp).rotate(10f)) {
                SpanishCardView(
                    com.fiestadecartas.app.game.SpanishCard(12, com.fiestadecartas.app.game.SpanishSuit.OROS),
                    width = 34.dp
                )
            }
        }
        "ludo" -> Canvas(modifier.padding(10.dp)) {
            val s = size.minDimension / 5f
            val colors = listOf(Color(0xFFE63946), Color(0xFF2A9D8F), Color(0xFFFFD166), Color(0xFF3A86FF))
            colors.forEachIndexed { i, c ->
                val x = (i % 2) * s * 3.2f
                val y = (i / 2) * s * 3.2f
                drawRoundRect(c.copy(alpha = 0.85f), Offset(x, y), Size(s * 1.8f, s * 1.8f), androidx.compose.ui.geometry.CornerRadius(s * 0.3f))
                drawCircle(Color.White, s * 0.42f, Offset(x + s * 0.9f, y + s * 0.9f))
                drawCircle(c, s * 0.26f, Offset(x + s * 0.9f, y + s * 0.9f))
            }
        }
        "fiesta" -> Box(modifier, contentAlignment = Alignment.Center) {
            Box(Modifier.offset(x = (-14).dp).rotate(-18f)) {
                FiestaCardView(com.fiestadecartas.app.game.FiestaCard(1, com.fiestadecartas.app.game.FiestaColor.GREEN, com.fiestadecartas.app.game.FiestaKind.NUMBER, 7), width = 30.dp)
            }
            Box(Modifier.offset(x = 12.dp, y = 2.dp).rotate(12f)) {
                FiestaCardView(com.fiestadecartas.app.game.FiestaCard(2, com.fiestadecartas.app.game.FiestaColor.RED, com.fiestadecartas.app.game.FiestaKind.DRAW2), width = 30.dp)
            }
        }
        "kemix" -> Canvas(modifier.padding(9.dp)) {
            val emojis = listOf("☀️", "🌙", "⭐", "🌸")
            // Simple decorative diamonds; the emoji layer is added below in the card list.
            val s = size.minDimension / 2.6f
            listOf(0.2f to 0.2f, 0.75f to 0.2f, 0.2f to 0.75f, 0.75f to 0.75f).forEachIndexed { i, (x, y) ->
                drawRoundRect(Color.White.copy(alpha = 0.22f), Offset(size.width * x - s / 2, size.height * y - s / 2), Size(s, s), androidx.compose.ui.geometry.CornerRadius(6f))
            }
        }
        else -> Box(modifier, contentAlignment = Alignment.Center) {
            Box(Modifier.offset(x = (-6).dp, y = 4.dp).rotate(-12f)) {
                FiestaCardView(null, width = 30.dp, faceUp = false)
            }
            Text("✋", fontSize = 28.sp, modifier = Modifier.offset(x = 12.dp, y = (-6).dp))
        }
    }
}

@Composable
private fun ShareBanner(onClick: () -> Unit) {
    val palette = LocalPalette.current
    val festival = LocalFestival.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = palette.accent2.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.accent2.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📡", fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.share_app), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.share_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Rounded.QrCode2, null, tint = palette.accent2)
        }
    }
}

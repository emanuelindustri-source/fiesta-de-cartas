package com.fiestadecartas.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.ui.theme.LocalFestival
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FiestaBackground(content: @Composable BoxScope.() -> Unit) {
    val palette = LocalPalette.current
    val festival = LocalFestival.current
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val emojis = when (festival.key) {
        "halloween" -> listOf("🎃", "🦇", "🌙", "💀", "🕯️")
        "christmas" -> listOf("🎄", "⭐", "🎁", "🔔", "🌟")
        "reyes" -> listOf("👑", "⭐", "🐪", "🎁", "✨")
        "valentine" -> listOf("❤️", "🌹", "💌", "✨", "💘")
        "easter" -> listOf("🐣", "🌷", "🦋", "🥚", "🌼")
        "semana" -> listOf("✝️", "🕯️", "🌿", "🙏", "⛪")
        else -> listOf("🎉", "🃏", "🌴", "🎶", "✨")
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.accent.copy(alpha = if (dark) 0.20f else 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(120f, 80f),
                    radius = 900f
                )
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        palette.accent2.copy(alpha = if (dark) 0.10f else 0.07f)
                    )
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 64.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = palette.accent.copy(alpha = if (dark) 0.035f else 0.05f),
                        radius = 2f,
                        center = Offset(x, y)
                    )
                    y += step
                }
                x += step
            }
        }
        emojis.forEachIndexed { i, e ->
            val x = (0.12f + 0.19f * i) * 1000f
            val y = (0.08f + 0.17f * ((i * 3) % 5)) * 1600f
            Text(
                e,
                fontSize = (34 + (i % 3) * 10).sp,
                modifier = Modifier
                    .offset(x = (x / 3.2f).dp, y = (y / 3.6f).dp)
                    .rotate(i * 17f)
                    .alpha(if (dark) 0.10f else 0.13f)
            )
        }
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    corner: Int = 24,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(corner.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.72f else 0.82f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = 500f), label = "scale")
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier.scale(scale).heightIn(min = 54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
    ) {
        if (icon != null) { icon(); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
fun GameScaffold(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    FiestaBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                    if (subtitle != null) Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            }
            Box(Modifier.weight(1f), content = content)
        }
    }
}

@Composable
fun StatChip(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = (color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.16f)
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color ?: MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun Reveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(spring(dampingRatio = 0.55f, stiffness = 520f)) + fadeIn(tween(140))
    ) { content() }
}

@Composable
fun AvatarBubble(
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    active: Boolean = false
) {
    val infinite = rememberInfiniteTransition(label = "avatar")
    val bob by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "avatarBob"
    )
    Box(
        modifier
            .size(size)
            .scale(if (active) 1.04f + bob * 0.05f else 1f)
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.16f))
                ),
                CircleShape
            )
            .border(
                if (active) 2.dp else 1.dp,
                if (active) color else color.copy(alpha = 0.35f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.55f).sp)
    }
}

@Composable
fun PlayerBadge(
    name: String,
    points: Int? = null,
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    avatarEmoji: String? = null,
    reaction: String? = null,
    avatarColor: Color = color
) {
    val border by animateColorAsState(
        if (active) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        tween(300), label = "border"
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(width = if (active) 2.dp else 1.dp, color = border)
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (avatarEmoji != null) {
                    AvatarBubble(avatarEmoji, avatarColor, size = 34.dp, active = active)
                } else {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                }
                Reveal(
                    visible = reaction != null,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp)
                ) {
                    Text(reaction.orEmpty(), fontSize = 16.sp)
                }
            }
            Spacer(Modifier.width(7.dp))
            Column {
                Text(name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                if (points != null) Text(
                    "$points", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ConfettiOverlay(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    val palette = LocalPalette.current
    val colors = listOf(palette.accent, palette.accent2, palette.accent3, Color.White)
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2200)) }
    Canvas(modifier.fillMaxSize()) {
        repeat(70) { i ->
            val x = (i * 53 % 100) / 100f * size.width
            val speed = 0.6f + (i % 7) * 0.08f
            val y = (progress.value * speed) * size.height * 1.2f - 60f
            val angle = progress.value * 720f + i * 13
            rotate(angle, Offset(x, y)) {
                drawRect(
                    colors[i % colors.size],
                    topLeft = Offset(x, y),
                    size = Size(7f + (i % 4) * 3f, 12f + (i % 3) * 5f)
                )
            }
        }
    }
}

fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

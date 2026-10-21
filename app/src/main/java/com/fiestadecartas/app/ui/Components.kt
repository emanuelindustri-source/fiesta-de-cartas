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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.ui.theme.Festival
import com.fiestadecartas.app.ui.theme.LocalFestival
import com.fiestadecartas.app.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FiestaBackground(content: @Composable BoxScope.() -> Unit) {
    val palette = LocalPalette.current
    val festival = LocalFestival.current
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var phase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(45)
            phase = (phase + 0.0018f) % 1f
        }
    }
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
            drawSeasonalScene(festival, palette, phase, dark)
        }
        emojis.forEachIndexed { i, e ->
            val x = (0.12f + 0.19f * i) * 1000f
            val y = (0.08f + 0.17f * ((i * 3) % 5)) * 1600f
            Text(
                e,
                fontSize = (34 + (i % 3) * 10).sp,
                modifier = Modifier
                    .offset(
                        x = (x / 3.2f).dp,
                        y = (y / 3.6f + sin(phase * 6.283f + i) * 8f).dp
                    )
                    .rotate(i * 17f + sin(phase * 6.283f + i * 2f) * 7f)
                    .alpha(if (dark) 0.10f else 0.13f)
            )
        }
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeasonalScene(
    festival: Festival,
    palette: com.fiestadecartas.app.ui.theme.FestivalPalette,
    phase: Float,
    dark: Boolean
) {
    val w = size.width
    val h = size.height
    val alpha = if (dark) 1f else 0.7f
    val tau = 6.2831855f

    // Drifting fog at the bottom, present in every season.
    for (layer in 0..1) {
        val base = h * (0.74f + layer * 0.09f)
        val amp = 26f + layer * 18f
        val path = Path()
        path.moveTo(0f, h)
        path.lineTo(0f, base)
        var px = 0f
        while (px <= w) {
            val py = base + sin(px / (170f + layer * 40f) + phase * tau * (1.2f + layer * 0.4f)) * amp
            path.lineTo(px, py)
            px += 24f
        }
        path.lineTo(w, h)
        path.close()
        drawPath(path, color = palette.accent2.copy(alpha = 0.055f * alpha))
    }

    when (festival) {
        Festival.HALLOWEEN -> {
            // Moon with a soft halo.
            val moon = Offset(w * 0.83f, h * 0.17f)
            drawCircle(Color(0xFFFFB703).copy(alpha = 0.10f * alpha), w * 0.24f, moon)
            drawCircle(Color(0xFFFFE0A3).copy(alpha = 0.55f * alpha), w * 0.095f, moon)
            drawCircle(Color(0xFFE8B86A).copy(alpha = 0.22f * alpha), w * 0.022f, moon + Offset(-18f, 14f))
            drawCircle(Color(0xFFE8B86A).copy(alpha = 0.18f * alpha), w * 0.014f, moon + Offset(22f, -12f))
            drawCircle(Color(0xFFE8B86A).copy(alpha = 0.16f * alpha), w * 0.010f, moon + Offset(8f, 30f))

            // Layered purple hills and a graveyard silhouette.
            val hill1 = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.08f, h * 0.86f, w * 0.28f, h * 0.94f, w * 0.44f, h * 0.87f)
                cubicTo(w * 0.62f, h * 0.79f, w * 0.80f, h * 0.92f, w, h * 0.85f)
                lineTo(w, h)
                close()
            }
            drawPath(hill1, Color(0xFF1A1030).copy(alpha = 0.55f * alpha))
            val hill2 = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.18f, h * 0.93f, w * 0.38f, h * 0.89f, w * 0.58f, h * 0.95f)
                cubicTo(w * 0.76f, h * 0.99f, w * 0.88f, h * 0.92f, w, h * 0.96f)
                lineTo(w, h)
                close()
            }
            drawPath(hill2, Color(0xFF0C0716).copy(alpha = 0.55f * alpha))

            // Bare trees.
            drawBareTree(Offset(w * 0.07f, h * 0.97f), 120f, -1.35f, 4, Color(0xFF08050F).copy(alpha = 0.72f * alpha))
            drawBareTree(Offset(w * 0.94f, h * 0.99f), 150f, -1.80f, 4, Color(0xFF08050F).copy(alpha = 0.65f * alpha))

            // Gravestones and pumpkins.
            for (i in 0..2) {
                val gx = w * (0.14f + i * 0.075f)
                val gy = h * (0.94f - i * 0.012f)
                drawRoundRect(
                    Color(0xFF241A35).copy(alpha = 0.8f * alpha),
                    topLeft = Offset(gx, gy - 34f),
                    size = Size(24f, 34f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawLine(
                    Color(0xFF4A3B5E).copy(alpha = 0.7f * alpha),
                    Offset(gx + 12f, gy - 28f), Offset(gx + 12f, gy - 12f), 2f
                )
                drawLine(
                    Color(0xFF4A3B5E).copy(alpha = 0.7f * alpha),
                    Offset(gx + 5f, gy - 22f), Offset(gx + 19f, gy - 22f), 2f
                )
            }
            for (i in 0..1) {
                val px = w * (0.90f - i * 0.06f)
                val py = h * (0.96f + i * 0.01f)
                drawOval(
                    Color(0xFFFF7A00).copy(alpha = 0.38f * alpha),
                    topLeft = Offset(px, py - 22f),
                    size = Size(28f + i * 8f, 20f + i * 4f)
                )
                drawLine(
                    Color(0xFF2A6F3E).copy(alpha = 0.7f * alpha),
                    Offset(px + 14f, py - 22f), Offset(px + 16f, py - 30f), 2.5f
                )
            }

            // Cobwebs in the top corners.
            drawCobweb(Offset(0f, 0f), w * 0.20f, Color(0xFFB9A8D0).copy(alpha = 0.20f * alpha))
            drawCobweb(Offset(w, 0f), w * 0.16f, Color(0xFFB9A8D0).copy(alpha = 0.16f * alpha))

            // Bats.
            repeat(4) { i ->
                val bx = ((phase * (1.1f + i * 0.17f) + i * 0.27f) % 1f) * w
                val by = h * (0.13f + 0.06f * i) + sin(phase * tau * 3 + i) * 12f
                val wing = 10f + (i % 2) * 5f
                val bat = Path().apply {
                    moveTo(bx - wing, by)
                    quadraticBezierTo(bx - wing * 0.35f, by - wing * 0.8f, bx, by)
                    quadraticBezierTo(bx + wing * 0.35f, by - wing * 0.8f, bx + wing, by)
                }
                drawPath(bat, Color(0xFF0A0612).copy(alpha = 0.8f * alpha), style = Stroke(width = 2.2f))
            }

            // Embers drifting up.
            repeat(22) { i ->
                val seed = (i * 47 % 100) / 100f
                val x = seed * w + sin(phase * tau * 2 + i) * 18f
                val y = h * (0.72f - ((phase * (0.35f + (i % 5) * 0.05f) + i * 0.11f) % 0.5f))
                drawCircle(
                    Color(0xFFFFB703).copy(alpha = (0.10f + (i % 3) * 0.05f) * alpha),
                    radius = 1.6f + (i % 3),
                    center = Offset(x, y)
                )
            }
        }
        Festival.CHRISTMAS, Festival.REYES -> {
            repeat(36) { i ->
                val x = ((i * 41 % 100) / 100f) * w + sin(phase * tau + i) * 14f
                val y = ((phase * (0.3f + (i % 4) * 0.07f) + i * 0.13f) % 1f) * h
                drawCircle(Color.White.copy(alpha = (0.16f + (i % 3) * 0.07f) * alpha), 1.6f + (i % 3), Offset(x, y))
            }
            val hills = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.22f, h * 0.86f, w * 0.38f, h * 0.95f, w * 0.6f, h * 0.88f)
                cubicTo(w * 0.78f, h * 0.82f, w * 0.9f, h * 0.93f, w, h * 0.9f)
                lineTo(w, h)
                close()
            }
            drawPath(hills, Color(0xFFD9F2FF).copy(alpha = 0.10f * alpha))
            for (i in 0..3) {
                val tx = w * (0.08f + i * 0.27f)
                val ty = h * 0.985f
                val tree = Path().apply {
                    moveTo(tx, ty - 72f)
                    lineTo(tx - 32f, ty)
                    lineTo(tx + 32f, ty)
                    close()
                }
                drawPath(tree, Color(0xFF0B3A2C).copy(alpha = 0.42f * alpha))
                drawPath(tree, Color(0xFF2A9D8F).copy(alpha = 0.18f * alpha), style = Stroke(1.5f))
            }
        }
        Festival.VALENTINE -> {
            repeat(18) { i ->
                val x = ((i * 53 % 100) / 100f) * w + sin(phase * tau + i) * 20f
                val y = ((phase * (0.22f + (i % 4) * 0.05f) + i * 0.15f) % 1f) * h
                drawHeart(Offset(x, y), 6f + (i % 3) * 3f, Color(0xFFFF4D6D).copy(alpha = 0.18f * alpha))
            }
        }
        Festival.EASTER -> {
            repeat(24) { i ->
                val x = ((i * 37 % 100) / 100f) * w + sin(phase * tau + i) * 24f
                val y = ((phase * (0.18f + (i % 4) * 0.05f) + i * 0.12f) % 1f) * h
                drawCircle(
                    Color(0xFFCDB4DB).copy(alpha = 0.16f * alpha),
                    radius = 3f + (i % 3),
                    center = Offset(x, y)
                )
            }
        }
        Festival.SEMANA_SANTA -> {
            repeat(16) { i ->
                val x = ((i * 61 % 100) / 100f) * w
                val y = ((phase * 0.12f + i * 0.1f) % 1f) * h
                drawCircle(Color(0xFFD4AF37).copy(alpha = 0.12f * alpha), 2f + (i % 2), Offset(x, y))
            }
        }
        Festival.FIESTA -> {
            repeat(20) { i ->
                val x = ((i * 43 % 100) / 100f) * w + sin(phase * tau + i) * 16f
                val y = ((phase * (0.2f + (i % 4) * 0.04f) + i * 0.16f) % 1f) * h
                drawCircle(
                    listOf(palette.accent, palette.accent2, palette.accent3)[i % 3].copy(alpha = 0.13f * alpha),
                    radius = 2f + (i % 3),
                    center = Offset(x, y)
                )
            }
        }
    }

    // Thin ornamental border, top and bottom.
    drawLine(
        palette.accent.copy(alpha = 0.18f * alpha),
        Offset(w * 0.06f, 18f), Offset(w * 0.94f, 18f), 1f
    )
    drawLine(
        palette.accent.copy(alpha = 0.12f * alpha),
        Offset(w * 0.06f, h - 18f), Offset(w * 0.94f, h - 18f), 1f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBareTree(
    origin: Offset,
    length: Float,
    angle: Float,
    depth: Int,
    color: Color
) {
    if (depth <= 0 || length < 6f) return
    val end = Offset(
        origin.x + kotlin.math.cos(angle) * length,
        origin.y + kotlin.math.sin(angle) * length
    )
    drawLine(color, origin, end, strokeWidth = (depth * 1.7f).coerceAtLeast(1f))
    val spread = 0.48f
    drawBareTree(end, length * 0.68f, angle - spread, depth - 1, color)
    drawBareTree(end, length * 0.62f, angle + spread * 0.8f, depth - 1, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCobweb(
    origin: Offset,
    radius: Float,
    color: Color
) {
    val spokes = 5
    for (i in 0..spokes) {
        val a = (Math.PI / 2) * (i.toDouble() / spokes)
        val end = Offset(
            origin.x + (kotlin.math.cos(a) * radius).toFloat(),
            origin.y + (kotlin.math.sin(a) * radius).toFloat()
        )
        drawLine(color, origin, end, 1f)
    }
    for (ring in 1..3) {
        val r = radius * ring / 3.4f
        val path = Path()
        for (i in 0..spokes) {
            val a = (Math.PI / 2) * (i.toDouble() / spokes)
            val p = Offset(
                origin.x + (kotlin.math.cos(a) * r).toFloat(),
                origin.y + (kotlin.math.sin(a) * r).toFloat()
            )
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        drawPath(path, color, style = Stroke(0.9f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y + radius * 0.8f)
        cubicTo(
            center.x - radius * 1.6f, center.y - radius * 0.2f,
            center.x - radius * 0.7f, center.y - radius * 1.2f,
            center.x, center.y - radius * 0.35f
        )
        cubicTo(
            center.x + radius * 0.7f, center.y - radius * 1.2f,
            center.x + radius * 1.6f, center.y - radius * 0.2f,
            center.x, center.y + radius * 0.8f
        )
        close()
    }
    drawPath(path, color)
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
    val festival = LocalFestival.current
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(festival.emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(title, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                    }
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
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    seasonal: String? = null
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            drawRoundRect(
                Color(0xFF0E1B2A),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.28f)
            )
            drawRoundRect(
                Color(0xFFD8A93B).copy(alpha = 0.55f),
                topLeft = Offset(w * 0.04f, h * 0.04f),
                size = Size(w * 0.92f, h * 0.92f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.25f),
                style = Stroke(width = w * 0.025f)
            )
            rotate(-17f, Offset(w * 0.40f, h * 0.55f)) {
                drawRoundRect(
                    Color(0xFF2EC4B6),
                    topLeft = Offset(w * 0.24f, h * 0.27f),
                    size = Size(w * 0.24f, h * 0.47f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.045f)
                )
                drawRoundRect(
                    Color(0xFFFFF6E2),
                    topLeft = Offset(w * 0.28f, h * 0.31f),
                    size = Size(w * 0.16f, h * 0.39f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f)
                )
            }
            rotate(17f, Offset(w * 0.60f, h * 0.55f)) {
                drawRoundRect(
                    Color(0xFFE63946),
                    topLeft = Offset(w * 0.52f, h * 0.27f),
                    size = Size(w * 0.24f, h * 0.47f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.045f)
                )
                drawRoundRect(
                    Color(0xFFFFF6E2),
                    topLeft = Offset(w * 0.56f, h * 0.31f),
                    size = Size(w * 0.16f, h * 0.39f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f)
                )
            }
            drawRoundRect(
                Color(0xFFFFB703),
                topLeft = Offset(w * 0.35f, h * 0.20f),
                size = Size(w * 0.30f, h * 0.60f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f)
            )
            drawRoundRect(
                Color(0xFFFFF6E2),
                topLeft = Offset(w * 0.40f, h * 0.25f),
                size = Size(w * 0.20f, h * 0.50f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.035f)
            )
            drawCircle(Color(0xFFC9962B), radius = w * 0.09f, center = Offset(w * 0.50f, h * 0.47f))
            drawCircle(Color(0xFFFFF6E2), radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.47f))
            val star = Path().apply {
                for (i in 0 until 10) {
                    val a = Math.toRadians((-90 + i * 36).toDouble())
                    val r = if (i % 2 == 0) w * 0.10f else w * 0.045f
                    val x = w * 0.50f + (kotlin.math.cos(a) * r).toFloat()
                    val y = h * 0.68f + (kotlin.math.sin(a) * r).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(star, Color(0xFFFFB703))
        }
        if (seasonal != null) {
            Text(
                seasonal,
                fontSize = (size.value * 0.30f).sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
            )
        }
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

package com.fiestadecartas.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.game.FiestaCard
import com.fiestadecartas.app.game.FiestaColor
import com.fiestadecartas.app.game.FiestaKind
import com.fiestadecartas.app.game.KEMIX_FAMILIES
import com.fiestadecartas.app.game.KEMIX_COLORS
import com.fiestadecartas.app.game.KEMIX_EMOJI
import com.fiestadecartas.app.game.KemixCard
import com.fiestadecartas.app.game.SpanishCard
import com.fiestadecartas.app.game.SpanishSuit
import kotlin.math.min

private val CardCream = Color(0xFFFFF6E2)
private val CardInk = Color(0xFF221A10)
private val CardBackNavy = Color(0xFF101E33)
private val CardGold = Color(0xFFD8A93B)

@Composable
fun SpanishCardView(
    card: SpanishCard?,
    modifier: Modifier = Modifier,
    width: Dp = 76.dp,
    faceUp: Boolean = true,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Box(
        modifier
            .width(width)
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape((width.value * 0.10f).dp))
            .then(clickMod)
    ) {
        if (!faceUp || card == null) SpanishCardBack(width)
        else {
            val suit = card.suit
            Box(
                Modifier.fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(CardCream, Color(0xFFFFEBC7)))
                    )
                    .border(
                        if (highlighted) 3.dp else 1.5.dp,
                        if (highlighted) MaterialTheme.colorScheme.primary else suit.color.copy(alpha = 0.55f),
                        RoundedCornerShape((width.value * 0.10f).dp)
                    )
                    .padding((width.value * 0.08f).dp)
            ) {
                Box(Modifier.fillMaxSize().border(0.8.dp, suit.color.copy(alpha = 0.35f), RoundedCornerShape(6.dp)))
                Text(
                    "${card.rank}",
                    color = suit.color,
                    fontSize = (width.value * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 3.dp, top = 1.dp)
                )
                Text(
                    "${card.rank}",
                    color = suit.color,
                    fontSize = (width.value * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 3.dp, bottom = 1.dp).rotate(180f)
                )
                Box(Modifier.align(Alignment.Center).fillMaxSize(0.58f)) {
                    Canvas(Modifier.fillMaxSize()) { drawSuit(suit, card.rank) }
                }
                Canvas(
                    Modifier.align(Alignment.TopStart)
                        .padding(start = 5.dp, top = (width.value * 0.27f).dp)
                        .size((width.value * 0.15f).dp)
                ) { drawSuit(suit, card.rank) }
                Canvas(
                    Modifier.align(Alignment.BottomEnd)
                        .padding(end = 5.dp, bottom = (width.value * 0.16f).dp)
                        .size((width.value * 0.15f).dp)
                        .rotate(180f)
                ) { drawSuit(suit, card.rank) }
                if (card.rank >= 10) {
                    Text(
                        when (card.rank) { 10 -> "SOTA"; 11 -> "CABALLO"; else -> "REY" },
                        color = suit.color.copy(alpha = 0.85f),
                        fontSize = (width.value * 0.085f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpanishCardBack(width: Dp = 76.dp) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(CardBackNavy, Color(0xFF1D3557))))
            .border(1.5.dp, CardGold.copy(alpha = 0.7f), RoundedCornerShape((width.value * 0.10f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / 5f
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    val p = Path().apply {
                        moveTo(x, y - step * 0.26f); lineTo(x + step * 0.26f, y)
                        lineTo(x, y + step * 0.26f); lineTo(x - step * 0.26f, y); close()
                    }
                    drawPath(p, color = CardGold.copy(alpha = 0.28f), style = Stroke(width = 1.2f))
                    y += step
                }
                x += step
            }
            drawCircle(CardGold.copy(alpha = 0.16f), radius = size.width * 0.30f, center = center)
            drawCircle(CardGold.copy(alpha = 0.55f), radius = size.width * 0.30f, center = center, style = Stroke(width = 2f))
            drawCircle(CardGold.copy(alpha = 0.35f), radius = size.width * 0.25f, center = center, style = Stroke(width = 1f))
        }
        Text(
            "FC",
            color = CardGold,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = (width.value * 0.26f).sp
        )
    }
}

private fun DrawScope.drawSuit(suit: SpanishSuit, rank: Int) {
    val w = size.width
    val h = size.height
    val c = center
    val color = suit.color
    when (suit) {
        SpanishSuit.OROS -> {
            drawCircle(color.copy(alpha = 0.12f), radius = w * 0.46f, center = c)
            drawCircle(color, radius = w * 0.38f, center = c, style = Stroke(width = w * 0.06f))
            drawCircle(color.copy(alpha = 0.55f), radius = w * 0.27f, center = c, style = Stroke(width = w * 0.035f))
            drawCircle(color, radius = w * 0.10f, center = c)
            repeat(12) { i ->
                val a = Math.toRadians(i * 30.0)
                val r1 = w * 0.42f
                val r2 = w * 0.49f
                drawLine(
                    color,
                    Offset(c.x + (cos(a) * r1.toDouble()).toFloat(), c.y + (sin(a) * r1.toDouble()).toFloat()),
                    Offset(c.x + (cos(a) * r2.toDouble()).toFloat(), c.y + (sin(a) * r2.toDouble()).toFloat()),
                    strokeWidth = w * 0.025f
                )
            }
        }
        SpanishSuit.COPAS -> {
            val body = Path().apply {
                moveTo(c.x - w * 0.28f, c.y - h * 0.26f)
                lineTo(c.x + w * 0.28f, c.y - h * 0.26f)
                cubicTo(c.x + w * 0.3f, c.y + h * 0.02f, c.x + w * 0.10f, c.y + h * 0.14f, c.x, c.y + h * 0.16f)
                cubicTo(c.x - w * 0.10f, c.y + h * 0.14f, c.x - w * 0.3f, c.y + h * 0.02f, c.x - w * 0.28f, c.y - h * 0.26f)
                close()
            }
            drawPath(body, color)
            drawOval(color, topLeft = Offset(c.x - w * 0.05f, c.y + h * 0.12f), size = Size(w * 0.10f, h * 0.16f))
            drawRoundRect(color, topLeft = Offset(c.x - w * 0.22f, c.y + h * 0.30f), size = Size(w * 0.44f, h * 0.07f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
            drawArc(Color.White.copy(alpha = 0.35f), 200f, 70f, false, topLeft = Offset(c.x - w * 0.22f, c.y - h * 0.22f), size = Size(w * 0.18f, h * 0.26f), style = Stroke(width = w * 0.04f))
        }
        SpanishSuit.ESPADAS -> {
            drawLine(color, Offset(c.x, c.y - h * 0.45f), Offset(c.x, c.y + h * 0.28f), strokeWidth = w * 0.07f)
            val tip = Path().apply {
                moveTo(c.x, c.y - h * 0.52f); lineTo(c.x - w * 0.08f, c.y - h * 0.40f)
                lineTo(c.x + w * 0.08f, c.y - h * 0.40f); close()
            }
            drawPath(tip, color)
            drawLine(color, Offset(c.x - w * 0.26f, c.y + h * 0.10f), Offset(c.x + w * 0.26f, c.y + h * 0.10f), strokeWidth = w * 0.055f)
            drawLine(color, Offset(c.x, c.y + h * 0.28f), Offset(c.x, c.y + h * 0.40f), strokeWidth = w * 0.05f)
            drawCircle(color, radius = w * 0.06f, center = Offset(c.x, c.y + h * 0.43f))
        }
        SpanishSuit.BASTOS -> {
            drawLine(color, Offset(c.x - w * 0.24f, c.y + h * 0.36f), Offset(c.x + w * 0.24f, c.y - h * 0.36f), strokeWidth = w * 0.11f)
            drawLine(color, Offset(c.x - w * 0.22f, c.y - h * 0.22f), Offset(c.x + w * 0.22f, c.y + h * 0.22f), strokeWidth = w * 0.08f)
            listOf(-0.16f, 0f, 0.16f).forEach { t ->
                drawCircle(color, radius = w * 0.055f, center = Offset(c.x + w * t, c.y - h * t * 1.4f))
            }
            drawCircle(color.copy(alpha = 0.6f), radius = w * 0.30f, center = c, style = Stroke(width = 2f))
        }
    }
    if (rank >= 10) {
        val crownY = c.y - h * 0.40f
        val crown = Path().apply {
            moveTo(c.x - w * 0.20f, crownY + h * 0.08f)
            lineTo(c.x - w * 0.16f, crownY - h * 0.06f)
            lineTo(c.x - w * 0.05f, crownY + h * 0.02f)
            lineTo(c.x, crownY - h * 0.10f)
            lineTo(c.x + w * 0.05f, crownY + h * 0.02f)
            lineTo(c.x + w * 0.16f, crownY - h * 0.06f)
            lineTo(c.x + w * 0.20f, crownY + h * 0.08f)
            close()
        }
        drawPath(crown, CardGold)
    }
}

@Composable
fun FiestaCardView(
    card: FiestaCard?,
    modifier: Modifier = Modifier,
    width: Dp = 76.dp,
    faceUp: Boolean = true,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    val shape = RoundedCornerShape((width.value * 0.14f).dp)
    Box(modifier.width(width).aspectRatio(0.66f).clip(shape).then(clickMod)) {
        if (!faceUp || card == null) {
            Box(
                Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF191919), Color(0xFF3A0A12))))
                    .border(2.dp, Color.White.copy(alpha = 0.18f), shape),
                contentAlignment = Alignment.Center
            ) {
                Text("F", color = Color(0xFFFFD166), fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black, fontSize = (width.value * 0.5f).sp)
            }
            return
        }
        val base = when (card.color) {
            FiestaColor.RED -> Color(0xFFE63946)
            FiestaColor.YELLOW -> Color(0xFFFFB703)
            FiestaColor.GREEN -> Color(0xFF2A9D8F)
            FiestaColor.BLUE -> Color(0xFF3A86FF)
            FiestaColor.WILD -> Color(0xFF151515)
        }
        val onColor = if (card.color == FiestaColor.YELLOW) Color(0xFF231A00) else Color.White
        Box(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(base, base.copy(alpha = 0.82f))))
                .border(if (highlighted) 3.dp else 1.5.dp, if (highlighted) Color.White else Color.Black.copy(alpha = 0.15f), shape)
        ) {
            if (card.color == FiestaColor.WILD) {
                listOf(
                    Color(0xFFE63946) to Alignment.TopStart,
                    Color(0xFFFFB703) to Alignment.TopEnd,
                    Color(0xFF2A9D8F) to Alignment.BottomStart,
                    Color(0xFF3A86FF) to Alignment.BottomEnd
                ).forEach { (col, align) ->
                    Box(Modifier.fillMaxSize(0.52f).align(align).background(col))
                }
            }
            Canvas(Modifier.fillMaxSize()) {
                drawOval(
                    Color.White.copy(alpha = if (card.color == FiestaColor.WILD) 0.94f else 0.92f),
                    topLeft = Offset(size.width * 0.10f, size.height * 0.14f),
                    size = Size(size.width * 0.80f, size.height * 0.72f)
                )
            }
            val centerLabel = when (card.kind) {
                FiestaKind.NUMBER -> "${card.number}"
                FiestaKind.SKIP -> "⊘"
                FiestaKind.REVERSE -> "⇄"
                FiestaKind.DRAW2 -> "+2"
                FiestaKind.WILD -> "★"
                FiestaKind.WILD4 -> "+4"
            }
            Text(
                centerLabel,
                color = if (card.kind == FiestaKind.WILD || card.kind == FiestaKind.WILD4) Color(0xFF151515) else base,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = (width.value * if (card.kind == FiestaKind.NUMBER) 0.46f else 0.30f).sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                when (card.kind) {
                    FiestaKind.NUMBER -> "${card.number}"
                    FiestaKind.SKIP -> "S"
                    FiestaKind.REVERSE -> "R"
                    FiestaKind.DRAW2 -> "+2"
                    FiestaKind.WILD -> "W"
                    FiestaKind.WILD4 -> "+4"
                },
                color = onColor, fontWeight = FontWeight.Black,
                fontSize = (width.value * 0.17f).sp,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp, top = 2.dp)
            )
            Text(
                when (card.kind) {
                    FiestaKind.NUMBER -> "${card.number}"
                    FiestaKind.SKIP -> "S"
                    FiestaKind.REVERSE -> "R"
                    FiestaKind.DRAW2 -> "+2"
                    FiestaKind.WILD -> "W"
                    FiestaKind.WILD4 -> "+4"
                },
                color = onColor, fontWeight = FontWeight.Black,
                fontSize = (width.value * 0.17f).sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 2.dp).rotate(180f)
            )
        }
    }
}

@Composable
fun KemixCardView(
    card: KemixCard?,
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
    faceUp: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    val shape = RoundedCornerShape((width.value * 0.16f).dp)
    Box(modifier.width(width).aspectRatio(0.72f).clip(shape).then(clickMod)) {
        if (!faceUp || card == null) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF153243), Color(0xFF0B1D26)))
                ).border(1.5.dp, Color(0xFF6FD8D0).copy(alpha = 0.55f), shape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    repeat(4) { i ->
                        drawCircle(
                            Color(0xFF6FD8D0).copy(alpha = 0.08f),
                            radius = size.width * (0.16f + i * 0.09f),
                            center = center, style = Stroke(width = 2f)
                        )
                    }
                }
                Text("?", color = Color(0xFF6FD8D0), fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif, fontSize = (width.value * 0.4f).sp)
            }
            return
        }
        val col = KEMIX_COLORS[card.family % KEMIX_COLORS.size]
        Box(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(col, col.copy(alpha = 0.65f), Color(0xFF1B1B1B))))
                .border(1.5.dp, Color.White.copy(alpha = 0.22f), shape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                repeat(3) { i ->
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = size.width * (0.18f + i * 0.14f), center = center, style = Stroke(width = 1.5f))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(KEMIX_EMOJI[card.family % KEMIX_EMOJI.size], fontSize = (width.value * 0.38f).sp)
                Text(
                    KEMIX_FAMILIES[card.family % KEMIX_FAMILIES.size].uppercase(),
                    color = Color.White, fontWeight = FontWeight.Bold,
                    fontSize = (width.value * 0.095f).sp, letterSpacing = 1.sp
                )
            }
            Text(
                "4/${card.family + 1}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = (width.value * 0.09f).sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            )
        }
    }
}

@Composable
fun DiceView(value: Int, modifier: Modifier = Modifier, size: Dp = 76.dp, rolling: Boolean = false) {
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "dice")
    val wobble by infinite.animateFloat(
        -12f, 12f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(90),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "wobble"
    )
    val bounce by infinite.animateFloat(
        1f, 1.14f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(140),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val rotation = if (rolling) wobble else 0f
    Box(
        modifier.size(size).rotate(rotation)
            .scale(if (rolling) bounce else 1f)
            .clip(RoundedCornerShape((size.value * 0.22f).dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFFBF0), Color(0xFFF0E2C8))))
            .border(2.dp, Color(0xFFD8A93B), RoundedCornerShape((size.value * 0.22f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.minDimension
            val r = s * 0.085f
            val dot = Color(0xFF1D3557)
            val positions = when (value) {
                1 -> listOf(0.5f to 0.5f)
                2 -> listOf(0.28f to 0.28f, 0.72f to 0.72f)
                3 -> listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f)
                4 -> listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f)
                5 -> listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.5f to 0.5f, 0.28f to 0.72f, 0.72f to 0.72f)
                else -> listOf(0.27f to 0.27f, 0.73f to 0.27f, 0.27f to 0.5f, 0.73f to 0.5f, 0.27f to 0.73f, 0.73f to 0.73f)
            }
            positions.forEach { (x, y) -> drawCircle(dot, r, Offset(s * x, s * y)) }
        }
    }
}

@Composable
fun CardSlot(text: String, modifier: Modifier = Modifier, width: Dp = 76.dp) {
    Box(
        modifier.width(width).aspectRatio(0.68f)
            .clip(RoundedCornerShape((width.value * 0.1f).dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), RoundedCornerShape((width.value * 0.1f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

private fun cos(a: Double) = kotlin.math.cos(a)
private fun sin(a: Double) = kotlin.math.sin(a)
private fun min(a: Float, b: Float) = kotlin.math.min(a, b)

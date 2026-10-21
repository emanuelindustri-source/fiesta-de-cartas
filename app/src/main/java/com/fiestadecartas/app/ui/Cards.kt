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
import androidx.compose.ui.draw.shadow
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
    val shape = RoundedCornerShape((width.value * 0.10f).dp)
    Box(
        modifier
            .width(width)
            .aspectRatio(0.66f)
            .shadow(if (highlighted) 12.dp else 5.dp, shape, clip = false)
            .clip(shape)
            .then(clickMod)
    ) {
        if (!faceUp || card == null) SpanishCardBack(width)
        else SpanishCardFace(card, width, highlighted)
    }
}

@Composable
private fun SpanishCardFace(card: SpanishCard, width: Dp, highlighted: Boolean) {
    val suit = card.suit
    val shape = RoundedCornerShape((width.value * 0.10f).dp)
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFBF0), Color(0xFFFFF1D4), Color(0xFFF6E3BC))
                )
            )
            .border(
                if (highlighted) 3.dp else 2.dp,
                if (highlighted) MaterialTheme.colorScheme.primary else suit.color.copy(alpha = 0.72f),
                shape
            )
            .padding((width.value * 0.075f).dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Aged paper grain: deterministic, cheap and never random per frame.
            repeat(34) { i ->
                val x = ((i * 43 + 11) % 100) / 100f * w
                val y = ((i * 67 + 7) % 100) / 100f * h
                drawCircle(
                    Color(0xFF8A6A3D).copy(alpha = 0.035f),
                    radius = 0.8f + (i % 3) * 0.5f,
                    center = Offset(x, y)
                )
            }
            drawRoundRect(
                CardGold.copy(alpha = 0.42f),
                topLeft = Offset(3f, 3f),
                size = Size(w - 6f, h - 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f),
                style = Stroke(width = 1.1f)
            )
            // Filigree corners.
            val corner = w * 0.12f
            listOf(
                Offset(4f, 4f) to 1f,
                Offset(w - 4f, 4f) to -1f,
                Offset(4f, h - 4f) to 1f,
                Offset(w - 4f, h - 4f) to -1f
            ).forEach { (origin, dir) ->
                drawArc(
                    suit.color.copy(alpha = 0.30f),
                    startAngle = if (dir > 0) 0f else 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(origin.x - (if (dir > 0) 0f else corner), origin.y - (if (dir > 0) 0f else corner)),
                    size = Size(corner, corner),
                    style = Stroke(width = 1f)
                )
            }
        }

        Text(
            "${card.rank}",
            color = suit.color,
            fontSize = (width.value * 0.21f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 0.dp)
        )
        Text(
            "${card.rank}",
            color = suit.color,
            fontSize = (width.value * 0.21f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp, bottom = 0.dp).rotate(180f)
        )

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (card.rank >= 10) {
                drawCourt(suit, card.rank, Offset(w * 0.5f, h * 0.51f), w * 0.76f)
            } else {
                drawPips(suit, card.rank, w, h)
            }
        }

        Canvas(
            Modifier.align(Alignment.TopStart)
                .padding(start = 4.dp, top = (width.value * 0.26f).dp)
                .size((width.value * 0.14f).dp)
        ) { drawSuit(suit, card.rank, crown = false) }
        Canvas(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = (width.value * 0.16f).dp)
                .size((width.value * 0.14f).dp)
                .rotate(180f)
        ) { drawSuit(suit, card.rank, crown = false) }

        if (card.rank >= 10) {
            Text(
                when (card.rank) { 10 -> "SOTA"; 11 -> "CABALLO"; else -> "REY" },
                color = suit.color.copy(alpha = 0.9f),
                fontSize = (width.value * 0.072f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPips(
    suit: SpanishSuit,
    rank: Int,
    w: Float,
    h: Float
) {
    val left = w * 0.30f
    val right = w * 0.70f
    val mid = w * 0.50f
    val top = h * 0.30f
    val centerY = h * 0.51f
    val bottom = h * 0.72f
    val unit = w * 0.19f
    val positions: List<Pair<Float, Float>> = when (rank) {
        1 -> listOf(mid to centerY)
        2 -> listOf(mid to top, mid to bottom)
        3 -> listOf(mid to top, mid to centerY, mid to bottom)
        4 -> listOf(left to top, right to top, left to bottom, right to bottom)
        5 -> listOf(left to top, right to top, mid to centerY, left to bottom, right to bottom)
        6 -> listOf(left to top, right to top, left to centerY, right to centerY, left to bottom, right to bottom)
        7 -> listOf(left to top, right to top, left to centerY, right to centerY, left to bottom, right to bottom, mid to (h * 0.42f))
        else -> listOf(mid to centerY)
    }
    val scale = if (rank == 1) unit * 1.75f else unit
    positions.forEach { (x, y) ->
        drawSuit(suit, rank, at = Offset(x, y), unit = scale, crown = false)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCourt(
    suit: SpanishSuit,
    rank: Int,
    center: Offset,
    unit: Float
) {
    val c = center
    val ink = Color(0xFF241A12)
    val skin = Color(0xFFE8C39E)
    val gold = Color(0xFFD8A93B)
    val robe = when (suit) {
        SpanishSuit.OROS -> Color(0xFF7A4E12)
        SpanishSuit.COPAS -> Color(0xFF8E1B2E)
        SpanishSuit.ESPADAS -> Color(0xFF1D3557)
        SpanishSuit.BASTOS -> Color(0xFF1F5A38)
    }

    // Robe / shoulders.
    val robePath = Path().apply {
        moveTo(c.x - unit * 0.34f, c.y + unit * 0.48f)
        cubicTo(
            c.x - unit * 0.30f, c.y + unit * 0.14f,
            c.x - unit * 0.12f, c.y + unit * 0.06f,
            c.x, c.y + unit * 0.06f
        )
        cubicTo(
            c.x + unit * 0.12f, c.y + unit * 0.06f,
            c.x + unit * 0.30f, c.y + unit * 0.14f,
            c.x + unit * 0.34f, c.y + unit * 0.48f
        )
        close()
    }
    drawPath(robePath, robe)
    drawPath(robePath, gold.copy(alpha = 0.35f), style = Stroke(unit * 0.02f))

    // Neck and head.
    drawRoundRect(
        skin,
        topLeft = Offset(c.x - unit * 0.045f, c.y - unit * 0.10f),
        size = Size(unit * 0.09f, unit * 0.12f)
    )
    drawCircle(skin, unit * 0.145f, Offset(c.x, c.y - unit * 0.18f))
    drawCircle(ink.copy(alpha = 0.75f), unit * 0.145f, Offset(c.x, c.y - unit * 0.18f), style = Stroke(unit * 0.012f))

    when (rank) {
        12 -> {
            // Crown.
            val crownY = c.y - unit * 0.34f
            val crown = Path().apply {
                moveTo(c.x - unit * 0.20f, crownY + unit * 0.08f)
                lineTo(c.x - unit * 0.16f, crownY - unit * 0.07f)
                lineTo(c.x - unit * 0.05f, crownY + unit * 0.01f)
                lineTo(c.x, crownY - unit * 0.12f)
                lineTo(c.x + unit * 0.05f, crownY + unit * 0.01f)
                lineTo(c.x + unit * 0.16f, crownY - unit * 0.07f)
                lineTo(c.x + unit * 0.20f, crownY + unit * 0.08f)
                close()
            }
            drawPath(crown, gold)
            drawCircle(Color(0xFFE63946), unit * 0.022f, Offset(c.x, crownY - unit * 0.01f))
            // Beard.
            val beard = Path().apply {
                moveTo(c.x - unit * 0.10f, c.y - unit * 0.13f)
                quadraticBezierTo(c.x, c.y + unit * 0.06f, c.x + unit * 0.10f, c.y - unit * 0.13f)
                close()
            }
            drawPath(beard, Color(0xFFEDE3D0))
        }
        11 -> {
            // Horse head in profile with bridle.
            val horse = Path().apply {
                moveTo(c.x - unit * 0.22f, c.y + unit * 0.30f)
                lineTo(c.x - unit * 0.12f, c.y - unit * 0.06f)
                lineTo(c.x - unit * 0.20f, c.y - unit * 0.24f)
                lineTo(c.x - unit * 0.07f, c.y - unit * 0.22f)
                lineTo(c.x + unit * 0.02f, c.y - unit * 0.34f)
                lineTo(c.x + unit * 0.08f, c.y - unit * 0.18f)
                cubicTo(
                    c.x + unit * 0.32f, c.y - unit * 0.10f,
                    c.x + unit * 0.30f, c.y + unit * 0.10f,
                    c.x + unit * 0.12f, c.y + unit * 0.18f
                )
                lineTo(c.x + unit * 0.20f, c.y + unit * 0.34f)
                close()
            }
            drawPath(horse, Color(0xFF8B5A2B))
            drawPath(horse, ink.copy(alpha = 0.5f), style = Stroke(unit * 0.015f))
            drawCircle(ink, unit * 0.018f, Offset(c.x + unit * 0.06f, c.y - unit * 0.12f))
            drawLine(gold, Offset(c.x + unit * 0.04f, c.y - unit * 0.05f), Offset(c.x + unit * 0.20f, c.y + unit * 0.06f), unit * 0.02f)
            drawLine(gold, Offset(c.x - unit * 0.12f, c.y + unit * 0.02f), Offset(c.x + unit * 0.14f, c.y + unit * 0.01f), unit * 0.018f)
            // Mane.
            repeat(4) { i ->
                drawLine(
                    ink.copy(alpha = 0.7f),
                    Offset(c.x - unit * 0.08f + i * unit * 0.02f, c.y - unit * 0.25f),
                    Offset(c.x - unit * 0.12f + i * unit * 0.02f, c.y - unit * 0.32f),
                    unit * 0.018f
                )
            }
        }
        else -> {
            // Page / jack with a feathered cap and collar.
            val cap = Path().apply {
                moveTo(c.x - unit * 0.18f, c.y - unit * 0.28f)
                quadraticBezierTo(c.x, c.y - unit * 0.44f, c.x + unit * 0.18f, c.y - unit * 0.28f)
                lineTo(c.x + unit * 0.12f, c.y - unit * 0.20f)
                lineTo(c.x - unit * 0.12f, c.y - unit * 0.20f)
                close()
            }
            drawPath(cap, robe)
            drawPath(cap, gold.copy(alpha = 0.5f), style = Stroke(unit * 0.012f))
            drawLine(gold, Offset(c.x + unit * 0.12f, c.y - unit * 0.30f), Offset(c.x + unit * 0.27f, c.y - unit * 0.42f), unit * 0.018f)
            drawOval(Color(0xFF2A9D8F), topLeft = Offset(c.x + unit * 0.22f, c.y - unit * 0.46f), size = Size(unit * 0.10f, unit * 0.05f))
            // Collar.
            drawRoundRect(
                creamColor(),
                topLeft = Offset(c.x - unit * 0.12f, c.y - unit * 0.02f),
                size = Size(unit * 0.24f, unit * 0.07f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(unit * 0.03f)
            )
        }
    }

    // Chest emblem.
    drawSuit(suit, rank, at = Offset(c.x, c.y + unit * 0.27f), unit = unit * 0.16f, crown = false)
    // Halo line behind the figure.
    drawCircle(gold.copy(alpha = 0.22f), unit * 0.40f, c, style = Stroke(unit * 0.012f))
}

@Composable
fun CoinView(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center
        drawCircle(Color(0xFF8A5A12), r, c)
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFF3B0), Color(0xFFFFC93C), Color(0xFFD18B0B)),
                center = Offset(c.x - r * 0.25f, c.y - r * 0.3f),
                radius = r * 1.3f
            ),
            r * 0.92f,
            c
        )
        drawCircle(Color(0xFFFFF0A8).copy(alpha = 0.65f), r * 0.78f, c, style = Stroke(r * 0.06f))
        val star = Path().apply {
            for (i in 0 until 10) {
                val angle = (-90 + i * 36) * Math.PI / 180
                val radius = if (i % 2 == 0) r * 0.48f else r * 0.22f
                val x = c.x + (kotlin.math.cos(angle) * radius).toFloat()
                val y = c.y + (kotlin.math.sin(angle) * radius).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(star, Color(0xFF7A4E12).copy(alpha = 0.8f))
        drawCircle(Color.White.copy(alpha = 0.35f), r * 0.16f, Offset(c.x - r * 0.3f, c.y - r * 0.38f))
    }
}

private fun creamColor(): Color = Color(0xFFFFF6E2)

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

private fun DrawScope.drawSuit(
    suit: SpanishSuit,
    rank: Int,
    at: Offset = center,
    unit: Float = size.minDimension,
    crown: Boolean = true
) {
    val w = unit
    val h = unit
    val c = at
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
    if (crown && rank >= 10) {
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
    Box(
        modifier.width(width).aspectRatio(0.66f)
            .shadow(if (highlighted) 10.dp else 5.dp, shape, clip = false)
            .clip(shape).then(clickMod)
    ) {
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
                drawOval(
                    Color.White.copy(alpha = 0.18f),
                    topLeft = Offset(size.width * 0.16f, size.height * 0.18f),
                    size = Size(size.width * 0.28f, size.height * 0.18f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.22f),
                    radius = size.width * 0.035f,
                    center = Offset(size.width * 0.78f, size.height * 0.22f)
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
            .shadow(6.dp, RoundedCornerShape((size.value * 0.22f).dp), clip = false)
            .clip(RoundedCornerShape((size.value * 0.22f).dp))
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF7ECD8), Color(0xFFE2CFAC))
                )
            )
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

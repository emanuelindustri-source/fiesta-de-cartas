package com.fiestadecartas.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.CardBackStyle
import com.fiestadecartas.app.data.Cosmetics
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.data.TableFelt
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun ShopScreen() {
    val context = LocalContext.current
    val palette = LocalPalette.current
    GameScaffold(
        title = stringResource(R.string.shop_title),
        subtitle = stringResource(R.string.shop_subtitle),
        onBack = { Nav.go(Screen.Profile) }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinView(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text("${Profile.coins}", style = MaterialTheme.typography.headlineMedium, color = palette.accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.coins), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { Nav.go(Screen.Profile) }) {
                    Text(stringResource(R.string.profile))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.card_backs), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Cosmetics.cardBacks.forEach { style ->
                CardBackRow(context, style)
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.table_felts), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Cosmetics.felts.forEach { felt ->
                FeltRow(context, felt)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CardBackRow(context: android.content.Context, style: CardBackStyle) {
    val owned = Cosmetics.isCardBackOwned(style.id)
    val selected = Cosmetics.cardBack.id == style.id
    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(46.dp).height(64.dp)) {
                SpanishCardBack(width = 46.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(style.nameRes), fontWeight = FontWeight.Bold)
                Text(
                    if (owned) stringResource(if (selected) R.string.selected else R.string.owned)
                    else "${style.price} ${stringResource(R.string.coins)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            TextButton(onClick = {
                if (selected) return@TextButton
                if (owned) {
                    Cosmetics.selectCardBack(style)
                    Feedback.click()
                } else if (Cosmetics.buyCardBack(style)) {
                    Feedback.coin()
                } else {
                    Toast.makeText(context, R.string.not_enough_coins, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(
                    when {
                        selected -> stringResource(R.string.selected)
                        owned -> stringResource(R.string.select)
                        else -> stringResource(R.string.buy)
                    }
                )
            }
        }
    }
}

@Composable
private fun FeltRow(context: android.content.Context, felt: TableFelt) {
    val owned = Cosmetics.isFeltOwned(felt.id)
    val selected = Cosmetics.felt.id == felt.id
    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.width(46.dp).height(46.dp)
                    .background(Color(felt.color), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(felt.accent).copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(felt.nameRes), fontWeight = FontWeight.Bold)
                Text(
                    if (owned) stringResource(if (selected) R.string.selected else R.string.owned)
                    else "${felt.price} ${stringResource(R.string.coins)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            TextButton(onClick = {
                if (selected) return@TextButton
                if (owned) {
                    Cosmetics.selectFelt(felt)
                    Feedback.click()
                } else if (Cosmetics.buyFelt(felt)) {
                    Feedback.coin()
                } else {
                    Toast.makeText(context, R.string.not_enough_coins, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(
                    when {
                        selected -> stringResource(R.string.selected)
                        owned -> stringResource(R.string.select)
                        else -> stringResource(R.string.buy)
                    }
                )
            }
        }
    }
}

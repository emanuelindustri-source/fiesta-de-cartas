package com.fiestadecartas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.BuildConfig
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.ads.BannerAd
import com.fiestadecartas.app.ui.theme.Festival
import com.fiestadecartas.app.ui.theme.LocalFestival
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun LegendsScreen() {
    val festival = LocalFestival.current
    val palette = LocalPalette.current
    val storyRes = when (festival) {
        Festival.FIESTA -> R.string.fest_story_fiesta
        Festival.HALLOWEEN -> R.string.fest_story_halloween
        Festival.CHRISTMAS -> R.string.fest_story_christmas
        Festival.REYES -> R.string.fest_story_reyes
        Festival.VALENTINE -> R.string.fest_story_valentine
        Festival.EASTER -> R.string.fest_story_easter
        Festival.SEMANA_SANTA -> R.string.fest_story_semana
    }
    GameScaffold(
        title = stringResource(R.string.legends_title),
        subtitle = "${festival.emoji} ${festival.title}",
        onBack = { Nav.go(Screen.Home) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(storyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "${festival.emoji} ${stringResource(R.string.season_clock)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.legends_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            LegendCard("👣", stringResource(R.string.legend_silbon), stringResource(R.string.legend_silbon_text))
            LegendCard("🌺", stringResource(R.string.legend_sayona), stringResource(R.string.legend_sayona_text))
            LegendCard("🐐", stringResource(R.string.legend_chupacabra), stringResource(R.string.legend_chupacabra_text))
            LegendCard("💧", stringResource(R.string.legend_llorona), stringResource(R.string.legend_llorona_text))
            Spacer(Modifier.height(16.dp))
            BannerAd()
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun LegendCard(emoji: String, title: String, text: String) {
    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AboutScreen() {
    val palette = LocalPalette.current
    GameScaffold(
        title = stringResource(R.string.about),
        subtitle = "v${BuildConfig.VERSION_NAME}",
        onBack = { Nav.go(Screen.Settings) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("🃏🎉", fontSize = 54.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.app_name),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.tagline),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(14.dp))
            InfoCard("🔐", stringResource(R.string.privacy), "Todo se guarda en tu teléfono: nombre, idioma, tema y estadísticas. No hay cuentas ni servidores propios. La cámara solo se usa para leer códigos QR; el WiFi local solo transporta la partida o la APK entre los dispositivos conectados. Los anuncios los sirve Google AdMob y puedes rechazar la personalización desde Ajustes.")
            InfoCard("🎨", "Arte y marcas", "Todo el arte de las cartas y tableros es original, inspirado en la baraja española y en juegos tradicionales de dominio público. Fiesta de Colores, Kemix y Nervioso son adaptaciones independientes; no están afiliadas a Mattel, UNO, Kemps ni a sus dueños. Fiesta de Cartas es un proyecto independiente.")
            InfoCard("📡", "Juego por WiFi", "El anfitrión crea una sala y comparte un QR. Los invitados escanean desde la app y juegan en la misma red. No se necesita internet ni datos móviles. Compartir la app también usa un servidor local temporal que se apaga al salir de la pantalla.")
            InfoCard("📧", "Contacto", "Ideas, reportes o alianzas: emanuel.industri@gmail.com")
            Spacer(Modifier.height(10.dp))
            Text(
                "${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME} · ${stringResource(R.string.made_in)}",
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.accent,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            BannerAd()
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun InfoCard(emoji: String, title: String, text: String) {
    GlassPanel(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
    }
}

package com.fiestadecartas.app

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.fiestadecartas.app.ads.Interstitial
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.game.caida.CaidaScreen
import com.fiestadecartas.app.game.fiesta.FiestaScreen
import com.fiestadecartas.app.game.kemix.KemixScreen
import com.fiestadecartas.app.game.ludo.LudoScreen
import com.fiestadecartas.app.game.nervioso.NerviosoScreen
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.ui.AboutScreen
import com.fiestadecartas.app.ui.HomeScreen
import com.fiestadecartas.app.ui.LegendsScreen
import com.fiestadecartas.app.ui.LobbyScreen
import com.fiestadecartas.app.ui.ScannerScreen
import com.fiestadecartas.app.ui.SettingsScreen
import com.fiestadecartas.app.ui.ShareScreen
import com.fiestadecartas.app.ui.theme.FiestaTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FiestaTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    var showConsent by remember { mutableStateOf(Prefs.adsConsent == null) }
    val lostConnection = androidx.compose.ui.res.stringResource(R.string.connection_lost)

    LaunchedEffect(Unit) {
        LanSession.onGameStart = { game, _, _ ->
            Nav.go(Screen.Game(game, lan = true))
        }
        Interstitial.preload(context)
    }
    LaunchedEffect(LanSession.role) {
        if (LanSession.role != "none") {
            LanSession.onPeerLeft = {
                if (LanSession.role == "guest") {
                    LanSession.stop()
                    Nav.go(Screen.Home)
                    android.widget.Toast.makeText(context, lostConnection, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AnimatedContent(
        targetState = Nav.screen,
        transitionSpec = {
            (fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 16 }) togetherWith fadeOut(tween(140))
        },
        label = "screen"
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen()
            is Screen.Settings -> SettingsScreen()
            is Screen.Profile -> com.fiestadecartas.app.ui.ProfileScreen()
            is Screen.Review -> com.fiestadecartas.app.ui.ReviewScreen()
            is Screen.Game -> when (screen.id) {
                "caida" -> CaidaScreen(lan = screen.lan)
                "ludo" -> LudoScreen(lan = screen.lan)
                "fiesta" -> FiestaScreen(lan = screen.lan)
                "kemix" -> KemixScreen()
                else -> NerviosoScreen()
            }
            is Screen.Lobby -> LobbyScreen(screen.game)
            is Screen.Scanner -> ScannerScreen()
            is Screen.Share -> ShareScreen()
            is Screen.Legends -> LegendsScreen()
            is Screen.About -> AboutScreen()
        }
    }

    BackHandler(enabled = Nav.screen !is Screen.Home) {
        if (Nav.screen is Screen.Game) {
            LanSession.stop()
        }
        if (Nav.screen is Screen.Lobby) LanSession.stop()
        Nav.go(Screen.Home)
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(androidx.compose.ui.res.stringResource(R.string.ads_consent_title)) },
            text = {
                Text(androidx.compose.ui.res.stringResource(R.string.ads_consent_body))
            },
            confirmButton = {
                TextButton(onClick = {
                    Prefs.setAdsConsent(true)
                    showConsent = false
                    Interstitial.preload(context)
                }) { Text(androidx.compose.ui.res.stringResource(R.string.accept)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    Prefs.setAdsConsent(false)
                    showConsent = false
                }) { Text(androidx.compose.ui.res.stringResource(R.string.reject)) }
            }
        )
    }
}

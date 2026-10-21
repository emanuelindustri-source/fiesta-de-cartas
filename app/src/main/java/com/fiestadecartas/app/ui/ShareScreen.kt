package com.fiestadecartas.app.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.ads.BannerAd
import com.fiestadecartas.app.net.ShareServer
import com.fiestadecartas.app.net.qrBitmap
import com.fiestadecartas.app.ui.theme.LocalPalette
import java.io.File

@Composable
fun ShareScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val palette = LocalPalette.current
    val server = remember { ShareServer(context) }
    var url by remember { mutableStateOf<String?>(null) }
    var started by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { server.stop() }
    }
    LaunchedEffect(Unit) {
        if (!started) {
            started = true
            url = server.start()
        }
    }
    val apkSize = remember {
        runCatching { File(context.applicationInfo.sourceDir).length() / (1024 * 1024) }.getOrDefault(0L)
    }
    GameScaffold(
        title = stringResource(R.string.share_wifi),
        subtitle = stringResource(R.string.apk_ready),
        onBack = { Nav.go(Screen.Home) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📡", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.share_app), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.share_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                val link = url
                if (link.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.loading))
                    }
                } else {
                    val bmp = remember(link) { qrBitmap(link, 720) }
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                            .background(Color.White, RoundedCornerShape(24.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bmp.asImageBitmap(), null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                        Text(
                            link,
                            Modifier.fillMaxWidth().padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton("⬇ ${stringResource(R.string.download_apk)}", Modifier.weight(1f)) {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link)))
                            }
                        }
                        SecondaryButton(stringResource(R.string.copy_link), Modifier.weight(1f)) {
                            clipboard.setText(AnnotatedString(link))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(stringResource(R.string.share_link), Modifier.fillMaxWidth(), icon = {
                        Icon(Icons.Rounded.Share, null, modifier = Modifier.size(18.dp))
                    }) {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Fiesta de Cartas 🃏🎉 · descárgala en tu WiFi: $link"
                            )
                        }
                        context.startActivity(Intent.createChooser(send, "Compartir"))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.install_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "APK · ${apkSize} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            GlassPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📶", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.wifi_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                SecondaryButton(stringResource(R.string.open_scanner), Modifier.fillMaxWidth(), icon = {
                    Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(18.dp))
                }) { Nav.go(Screen.Scanner) }
            }
            Spacer(Modifier.height(16.dp))
            BannerAd()
            Spacer(Modifier.height(30.dp))
        }
    }
}

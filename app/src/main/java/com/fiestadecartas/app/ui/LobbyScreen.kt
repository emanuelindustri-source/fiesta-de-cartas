package com.fiestadecartas.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
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
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.net.qrBitmap
import com.fiestadecartas.app.net.roomQrPayload
import com.fiestadecartas.app.ui.theme.LocalPalette

fun lanPlayersFor(gameId: String): Int = com.fiestadecartas.app.data.GameConfig.playersFor(gameId)

@Composable
fun LobbyScreen(gameId: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val palette = LocalPalette.current
    val game = GAMES.firstOrNull { it.id == gameId }
    var address by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("") }
    var codeText by remember { mutableStateOf("") }
    val guestLabel = stringResource(R.string.guest)
    var name by remember { mutableStateOf(Prefs.nickname.ifBlank { guestLabel }) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val role = LanSession.role

    LaunchedEffect(Unit) {
        // If we arrived as a guest from the scanner, stay in the waiting room.
    }

    GameScaffold(
        title = stringResource(R.string.lobby),
        subtitle = game?.let { stringResource(it.nameRes) } ?: stringResource(R.string.choose_game),
        onBack = {
            LanSession.stop()
            Nav.go(Screen.Home)
        }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            if (role == "none") {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📡", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.share_wifi), style = MaterialTheme.typography.titleLarge)
                            Text(
                                stringResource(R.string.wifi_lobby_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    PrimaryButton(stringResource(R.string.create_room), Modifier.fillMaxWidth()) {
                        busy = true
                        val ok = LanSession.hostRoom(gameId, lanPlayersFor(gameId), name)
                        busy = false
                        if (ok) error = null else error = LanSession.status
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(18); Prefs.setNickname(name) },
                        label = { Text(stringResource(R.string.nickname)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.join_room), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.wifi_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("IP") },
                            placeholder = { Text("192.168.1.10") },
                            singleLine = true,
                            modifier = Modifier.weight(1.6f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it.filter(Char::isDigit).take(5) },
                            label = { Text(stringResource(R.string.port)) },
                            placeholder = { Text("8988") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it.filter(Char::isDigit).take(4) },
                        label = { Text(stringResource(R.string.room_code)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(stringResource(R.string.join_room), Modifier.weight(1f), enabled = !busy) {
                            val ip = address.trim().removePrefix("http://").substringBefore("/").substringBefore(":")
                            val port = portText.toIntOrNull() ?: 8988
                            if (ip.isBlank()) { error = context.getString(R.string.enter_host_ip); return@PrimaryButton }
                            busy = true
                            val ok = LanSession.join(ip, port, ip, gameId, 0L, codeText, name)
                            busy = false
                            if (ok) {
                                Nav.go(Screen.Lobby(gameId))
                            } else error = LanSession.status
                        }
                        SecondaryButton(stringResource(R.string.scan_qr), Modifier.weight(1f)) {
                            Nav.go(Screen.Scanner)
                        }
                    }
                }
            }

            if (role == "host") {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            StatChip(stringResource(R.string.host), color = palette.accent)
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.server_started), style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${LanSession.ip}:${LanSession.port}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Surface(
                            onClick = {
                                clipboard.setText(AnnotatedString(context.getString(R.string.invite_message, LanSession.code)))
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = palette.accent.copy(alpha = 0.16f)
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp), tint = palette.accent)
                                Spacer(Modifier.width(6.dp))
                                Text(LanSession.code, style = MaterialTheme.typography.titleMedium, color = palette.accent)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val payload = roomQrPayload(LanSession.ip, LanSession.port, LanSession.code)
                    val bmp = remember(payload) { qrBitmap(payload, 640) }
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            .background(Color.White, RoundedCornerShape(22.dp)).padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.qr_hint),
                        Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (LanSession.guestConnected) palette.accent2.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (LanSession.guestConnected) "🟢" else "⏳")
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (LanSession.guestConnected) "${LanSession.guestName} · ${stringResource(R.string.connected)}"
                                else stringResource(R.string.waiting),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        stringResource(R.string.start_game),
                        Modifier.fillMaxWidth(),
                        enabled = LanSession.guestConnected
                    ) {
                        LanSession.startHostedGame()
                    }
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton(stringResource(R.string.share_app), Modifier.fillMaxWidth()) {
                        Nav.go(Screen.Share)
                    }
                }
            }

            if (role == "guest") {
                GlassPanel(Modifier.fillMaxWidth()) {
                    StatChip(stringResource(R.string.guest), color = palette.accent2)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (LanSession.game.isNotBlank()) {
                            stringResource(
                                R.string.connected_to,
                                LanSession.hostName.ifBlank { LanSession.ip }
                            ) + "\n" + stringResource(R.string.waiting)
                        } else {
                            LanSession.status.ifBlank { stringResource(R.string.connecting) }
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "${LanSession.ip}:${LanSession.port} · ${LanSession.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Spacer(Modifier.height(16.dp))
                    SecondaryButton(stringResource(R.string.leave), Modifier.fillMaxWidth()) {
                        LanSession.stop()
                        Nav.go(Screen.Home)
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.wifi_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

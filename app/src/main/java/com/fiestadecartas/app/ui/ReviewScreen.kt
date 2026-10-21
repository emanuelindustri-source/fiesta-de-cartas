package com.fiestadecartas.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestadecartas.app.BuildConfig
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.DeviceInfo
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.theme.LocalPalette

@Composable
fun ReviewScreen() {
    val context = LocalContext.current
    val palette = LocalPalette.current
    var stars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val categories = remember { mutableStateListOf<String>() }
    val device = remember { DeviceInfo.detect(context) }
    val levelText = stringResource(R.string.level_value, Profile.level)
    val rankText = stringResource(Profile.rankRes)
    val deviceText = stringResource(device.labelRes)
    val deviceLabel = stringResource(R.string.device)
    val playedLabel = stringResource(R.string.played)
    val winsLabel = stringResource(R.string.wins)
    val categoryText = categories.joinToString(", ")
    val subjectText = stringResource(R.string.rate_subject, stars)

    GameScaffold(
        title = stringResource(R.string.rate_app),
        subtitle = stringResource(R.string.profile_subtitle),
        onBack = { Nav.go(Screen.Settings) }
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.rate_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.rate_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { value ->
                        androidx.compose.material3.IconButton(onClick = {
                            stars = value
                            Feedback.click()
                        }) {
                            Icon(
                                if (value <= stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "$value",
                                tint = palette.accent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "fun" to R.string.rate_category_fun,
                        "design" to R.string.rate_category_design,
                        "bugs" to R.string.rate_category_bugs,
                        "ideas" to R.string.rate_category_ideas
                    ).forEach { (key, res) ->
                        FilterChip(
                            selected = key in categories,
                            onClick = {
                                if (key in categories) categories.remove(key) else categories.add(key)
                                Feedback.click()
                            },
                            label = { Text(stringResource(res), fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it.take(1200) },
                    label = { Text(stringResource(R.string.rate_comment)) },
                    minLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(stringResource(R.string.rate_send), Modifier.fillMaxWidth()) {
                    val body = buildString {
                        appendLine("Fiesta de Cartas v${BuildConfig.VERSION_NAME}")
                        appendLine("$levelText · $rankText")
                        appendLine("$deviceLabel: $deviceText")
                        appendLine("${Prefs.played} $playedLabel · ${Prefs.wins} $winsLabel")
                        appendLine("★".repeat(stars))
                        if (categories.isNotEmpty()) appendLine(categoryText)
                        appendLine()
                        append(comment.ifBlank { "(sin comentario)" })
                    }
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:emanuel.industri@gmail.com")
                    ).apply {
                        putExtra(Intent.EXTRA_SUBJECT, subjectText)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.rate_no_mail, Toast.LENGTH_LONG).show()
                    } catch (_: Exception) {
                        Toast.makeText(context, R.string.rate_no_mail, Toast.LENGTH_LONG).show()
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "emanuel.industri@gmail.com",
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

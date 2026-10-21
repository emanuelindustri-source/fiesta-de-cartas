package com.fiestadecartas.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fiestadecartas.app.R

/**
 * Collects the game's public messages while a match is running.
 * It is intentionally lightweight: no backend and no storage between matches.
 */
@Composable
fun rememberMatchHistory(message: String): SnapshotStateList<String> {
    val history = remember { mutableStateListOf<String>() }
    var last by remember { mutableStateOf("") }
    LaunchedEffect(message) {
        val clean = message.trim()
        if (clean.isNotBlank() && clean != last) {
            last = clean
            history.add(clean)
            if (history.size > 250) history.removeAt(0)
        }
    }
    return history
}

@Composable
fun MatchHistoryStrip(
    entries: List<String>,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.last_plays),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onOpen, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text(stringResource(R.string.full_history), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (entries.isEmpty()) {
                Text(
                    stringResource(R.string.no_history),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            } else {
                entries.takeLast(5).forEach { entry ->
                    Text(
                        "· $entry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MatchHistoryDialog(
    entries: List<String>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.full_history))
            }
        },
        text = {
            if (entries.isEmpty()) {
                Text(stringResource(R.string.no_history))
            } else {
                Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                    entries.forEachIndexed { index, entry ->
                        Text(
                            "${index + 1}. $entry",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (entries.isNotEmpty()) {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(entries.joinToString("\n")))
                    Toast.makeText(context, R.string.history_copied, Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.copy_history)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

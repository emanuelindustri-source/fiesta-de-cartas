package com.fiestadecartas.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.fiestadecartas.app.Nav
import com.fiestadecartas.app.R
import com.fiestadecartas.app.Screen
import com.fiestadecartas.app.data.Prefs
import com.fiestadecartas.app.net.LanSession
import com.fiestadecartas.app.net.parseRoomQr
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }
    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

    var handled by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var torch by remember { mutableStateOf(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    val controller = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { controller.unbind() }
            runCatching { scanner.close() }
            executor.shutdown()
        }
    }
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        runCatching { controller.bindToLifecycle(lifecycleOwner) }
        controller.setImageAnalysisAnalyzer(executor, ImageAnalysis.Analyzer { proxy: ImageProxy ->
            val media = proxy.image
            if (media == null) { proxy.close(); return@Analyzer }
            val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val value = barcodes.firstOrNull()?.rawValue
                    if (!handled && !value.isNullOrBlank()) {
                        handled = true
                        val room = parseRoomQr(value)
                        if (room != null) {
                            val (ip, port, code) = room
                            val ok = LanSession.join(
                                address = ip, portNumber = port, host = ip,
                                gameId = "caida", seedValue = 0L, codeValue = code,
                                name = Prefs.nickname.ifBlank { context.getString(R.string.guest) }
                            )
                            if (ok) Nav.go(Screen.Lobby("caida")) else {
                                errorText = LanSession.status
                                handled = false
                            }
                        } else if (value.startsWith("http://") || value.startsWith("https://") ||
                            value.startsWith("FIESTA|APK|")
                        ) {
                            val url = if (value.startsWith("FIESTA|APK|")) value.removePrefix("FIESTA|APK|") else value
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            Nav.go(Screen.Share)
                        } else {
                            errorText = value
                            handled = false
                        }
                    }
                }
                .addOnCompleteListener { proxy.close() }
        })
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.controller = controller
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📷", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.camera_permission), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.camera_need),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                PrimaryButton(stringResource(R.string.retry)) { launcher.launch(Manifest.permission.CAMERA) }
            }
        }

        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = { Nav.go(Screen.Home) },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (hasPermission) {
                    Surface(
                        onClick = {
                            torch = !torch
                            runCatching { controller.enableTorch(torch) }
                        },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (torch) Icons.Rounded.FlashOff else Icons.Rounded.FlashOn, null, tint = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(250.dp).background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 4.dp.toPx()
                    val corner = 38.dp.toPx()
                    val len = 54.dp.toPx()
                    val c = Color(0xFFFFD166)
                    fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
                        drawLine(c, androidx.compose.ui.geometry.Offset(x1, y1), androidx.compose.ui.geometry.Offset(x2, y2), stroke)
                    line(0f, corner, 0f, 0f); line(0f, 0f, corner, 0f)
                    line(size.width - corner, 0f, size.width, 0f); line(size.width, 0f, size.width, corner)
                    line(0f, size.height - corner, 0f, size.height); line(0f, size.height, corner, size.height)
                    line(size.width - corner, size.height, size.width, size.height); line(size.width, size.height, size.width, size.height - corner)
                }
                Text("🔍", fontSize = 52.sp)
            }
            Spacer(Modifier.height(20.dp))
            Surface(color = Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(18.dp)) {
                Text(
                    stringResource(R.string.scan_desc),
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            if (errorText != null) {
                Spacer(Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(14.dp)) {
                    Text(
                        stringResource(R.string.invalid_qr),
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

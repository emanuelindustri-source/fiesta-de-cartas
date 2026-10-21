package com.fiestadecartas.app.net

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

fun qrBitmap(text: String, size: Int = 720, dark: Int = Color.rgb(12, 26, 38), light: Int = Color.WHITE): Bitmap {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 1,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (matrix.get(x, y)) dark else light
        }
    }
    bmp.setPixels(pixels, 0, size, 0, 0, size, size)
    return bmp
}

fun roomQrPayload(ip: String, port: Int, code: String): String = "FIESTA|JOIN|$ip|$port|$code"

fun parseRoomQr(payload: String): Triple<String, Int, String>? {
    val parts = payload.trim().split("|")
    if (parts.size < 5 || parts[0] != "FIESTA" || parts[1] != "JOIN") return null
    val ip = parts[2]
    val port = parts[3].toIntOrNull() ?: return null
    val code = parts[4]
    return Triple(ip, port, code)
}

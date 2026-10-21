package com.fiestadecartas.app.data

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.view.WindowManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fiestadecartas.app.R

enum class DeviceKind(val labelRes: Int) {
    PHONE(R.string.device_phone),
    TABLET(R.string.device_tablet),
    TV(R.string.device_tv),
    DUAL(R.string.device_dual)
}

/**
 * Lightweight device detection used for layout choices.
 * Games remain fully playable with touch, D-pad or a TV remote.
 */
object DeviceInfo {
    fun detect(context: Context): DeviceKind {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        val pm = context.packageManager
        val television = uiMode == Configuration.UI_MODE_TYPE_TELEVISION ||
            pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
            pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
        if (television) return DeviceKind.TV

        val smallest = context.resources.configuration.smallestScreenWidthDp
        val displays = runCatching {
            context.getSystemService(DisplayManager::class.java)?.displays?.size ?: 1
        }.getOrDefault(1)
        if (displays >= 2 && smallest >= 480) return DeviceKind.DUAL
        return if (smallest >= 600) DeviceKind.TABLET else DeviceKind.PHONE
    }

    fun isTelevision(context: Context): Boolean = detect(context) == DeviceKind.TV

    fun isLarge(context: Context): Boolean = detect(context) != DeviceKind.PHONE

    /** Comfortable reading width; on phones the content fills the screen. */
    fun contentWidth(context: Context): Dp = when (detect(context)) {
        DeviceKind.PHONE -> 640.dp
        DeviceKind.TABLET -> 860.dp
        DeviceKind.DUAL -> 900.dp
        DeviceKind.TV -> 1100.dp
    }

    /** True when the device reports a hardware keyboard/remote style focus. */
    fun needsFocus(context: Context): Boolean {
        val wm = context.getSystemService(WindowManager::class.java)
        return isTelevision(context) || wm != null && context.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS
    }
}

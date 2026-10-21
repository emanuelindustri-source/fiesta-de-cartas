package com.fiestadecartas.app.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fiestadecartas.app.BuildConfig
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Thin, reflective bridge to the optional Honeygain SDK.
 *
 * The SDK is proprietary (`hgsdk.aar`) and is not committed to this repository.
 * When the AAR and an API key are provided, the bridge initialises it, shows the
 * official consent flow and starts it only while the app is in use. Without the
 * AAR the rest of the app keeps working and the support card is shown as
 * unavailable instead of crashing.
 */
object HoneygainSupport {
    private const val SDK_CLASS = "com.honeygain.hgsdk.HgSdk"
    private const val SDK_FILE_CLASS = "com.honeygain.hgsdk.HgSdkKt"

    private var sdkClass: Class<*>? = null
    private var sdkInstance: Any? = null
    private var bridgeClass: Class<*>? = null

    var available by mutableStateOf(false)
        private set
    var configured by mutableStateOf(false)
        private set
    var optedIn by mutableStateOf(false)
        private set
    var running by mutableStateOf(false)
        private set
    var lastError by mutableStateOf<String?>(null)
        private set

    val apiKeyPresent: Boolean get() = BuildConfig.HONEYGAIN_API_KEY.isNotBlank()

    fun init(context: Context) {
        if (sdkClass == null) {
            runCatching {
                val cls = Class.forName(SDK_CLASS)
                sdkClass = cls
                sdkInstance = runCatching { cls.getField("INSTANCE").get(null) }.getOrNull()
                if (sdkInstance == null) {
                    sdkInstance = runCatching { cls.getDeclaredConstructor().newInstance() }.getOrNull()
                }
                bridgeClass = runCatching { Class.forName(SDK_FILE_CLASS) }.getOrNull()
                available = true
            }.onFailure {
                lastError = it.message
                available = false
            }
        }
        configured = available && apiKeyPresent
        if (!configured) return
        invoke("initialize", context.applicationContext, BuildConfig.HONEYGAIN_API_KEY)
        setBoolean("isBackground", false)
        refresh()
    }

    /** Reads the SDK state. Safe to call from Compose and onResume. */
    fun refresh() {
        if (!available) return
        readBoolean("isOptedIn")?.let { optedIn = it }
        readBoolean("isRunning")?.let { running = it }
        lastError = callGetter("lastError")?.toString() ?: lastError
    }

    /** Opens Honeygain's official legal consent screen. */
    fun requestConsent(activity: Activity) {
        if (!configured) {
            lastError = "Honeygain SDK is not configured"
            return
        }
        val opened = invoke("requestConsent") || invoke("requestConsent", activity)
        if (!opened) lastError = callGetter("lastError")?.toString() ?: "Consent screen unavailable"
    }

    fun startIfOptedIn() {
        if (!configured) return
        refresh()
        if (optedIn && !running) invoke("start")
        refresh()
    }

    fun stop() {
        if (!available) return
        invoke("stop")
        refresh()
    }

    fun optOut() {
        if (!available) return
        invoke("optOut")
        refresh()
    }

    private fun invoke(name: String, vararg args: Any?): Boolean {
        val owner = sdkClass ?: return false
        val instance = sdkInstance
        val direct = matching(owner.methods, name, args.size, args, receiver = false)
        if (direct != null) {
            return runCatching {
                direct.isAccessible = true
                direct.invoke(if (Modifier.isStatic(direct.modifiers)) null else instance, *args)
                true
            }.onFailure { lastError = it.message }.getOrDefault(false)
        }
        val bridge = bridgeClass ?: return false
        val withReceiver = matching(bridge.methods, name, args.size + 1, args, receiver = true)
        if (withReceiver != null) {
            return runCatching {
                withReceiver.isAccessible = true
                withReceiver.invoke(null, instance, *args)
                true
            }.onFailure { lastError = it.message }.getOrDefault(false)
        }
        return false
    }

    private fun callGetter(name: String): Any? {
        val owner = sdkClass ?: return null
        val instance = sdkInstance
        val direct = owner.methods.firstOrNull {
            it.parameterCount == 0 && (it.name == name || it.name == "get" + name.replaceFirstChar(Char::uppercase))
        }
        if (direct != null) {
            return runCatching { direct.invoke(instance) }.getOrNull()
        }
        val bridge = bridgeClass ?: return null
        val extension = bridge.methods.firstOrNull { it.parameterCount == 1 && it.name == name }
        if (extension != null) {
            return runCatching { extension.invoke(null, instance) }.getOrNull()
        }
        return runCatching { owner.getField(name).get(instance) }.getOrNull()
    }

    private fun readBoolean(name: String): Boolean? = when (val value = callGetter(name)) {
        is Boolean -> value
        null -> null
        else -> runCatching { value.toString().toBooleanStrict() }.getOrNull()
    }

    private fun setBoolean(name: String, value: Boolean) {
        val owner = sdkClass ?: return
        val setter = "set" + name.replaceFirstChar(Char::uppercase)
        val candidates = owner.methods.filter { it.parameterCount == 1 && (it.name == setter || it.name == "set" + name) }
        val direct = candidates.firstOrNull { it.parameterTypes[0] == Boolean::class.javaPrimitiveType }
        if (direct != null) {
            runCatching { direct.invoke(sdkInstance, value) }
            return
        }
        val bridge = bridgeClass ?: return
        val extension = bridge.methods.firstOrNull {
            it.parameterCount == 2 && it.name == setter && it.parameterTypes[1] == Boolean::class.javaPrimitiveType
        }
        if (extension != null) runCatching { extension.invoke(null, sdkInstance, value) }
    }

    private fun matching(
        methods: Array<Method>,
        name: String,
        count: Int,
        args: Array<out Any?>,
        receiver: Boolean
    ): Method? {
        return methods.firstOrNull { method ->
            if (method.name != name || method.parameterCount != count) return@firstOrNull false
            val offset = if (receiver) 1 else 0
            if (count < offset) return@firstOrNull false
            args.indices.all { i ->
                val type = method.parameterTypes[i + offset]
                val arg = args[i]
                arg == null || type.isAssignableFrom(arg.javaClass) || isBoxed(type, arg.javaClass)
            }
        }
    }

    private fun isBoxed(type: Class<*>, arg: Class<*>): Boolean = when (type) {
        java.lang.Boolean.TYPE -> arg == java.lang.Boolean::class.java
        java.lang.Integer.TYPE -> arg == java.lang.Integer::class.java
        java.lang.Long.TYPE -> arg == java.lang.Long::class.java
        else -> false
    }
}

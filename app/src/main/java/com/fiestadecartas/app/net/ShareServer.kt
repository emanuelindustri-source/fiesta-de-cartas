package com.fiestadecartas.app.net

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ShareServer(private val context: Context) {
    private var server: ServerSocket? = null
    private var thread: Thread? = null
    @Volatile var running = false
        private set
    var url: String = ""
        private set
    var port: Int = 0
        private set

    fun start(): String? {
        if (running) return url
        return try {
            val s = ServerSocket(0)
            server = s
            port = s.localPort
            val ip = localIp()
            url = "http://$ip:$port/"
            running = true
            thread = thread(isDaemon = true, name = "fiesta-share") {
                while (running) {
                    val client = try { s.accept() } catch (_: Exception) { break }
                    runCatching { handle(client) }
                }
            }
            url
        } catch (e: Exception) {
            running = false
            null
        }
    }

    private fun handle(socket: Socket) {
        socket.use { s ->
            s.soTimeout = 8000
            val input = s.getInputStream().bufferedReader()
            val request = input.readLine() ?: return
            while (true) {
                val line = input.readLine() ?: break
                if (line.isEmpty()) break
            }
            val path = request.split(" ").getOrNull(1)?.substringBefore('?') ?: "/"
            val out = BufferedOutputStream(s.getOutputStream())
            when (path) {
                "/app.apk", "/fiesta.apk" -> serveApk(out)
                "/favicon.ico" -> {
                    out.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n".toByteArray())
                    out.flush()
                }
                else -> servePage(out)
            }
        }
    }

    private fun servePage(out: OutputStream) {
        val html = """
            <!doctype html>
            <html lang="es"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>Fiesta de Cartas · Instalar</title>
            <style>
            body{margin:0;font-family:system-ui,Segoe UI,sans-serif;background:radial-gradient(circle at 20% 0%,#1d3a4d,#0b1220 60%);color:#fff;min-height:100vh;display:grid;place-items:center;padding:24px}
            .card{max-width:440px;background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.12);border-radius:28px;padding:28px;backdrop-filter:blur(12px);box-shadow:0 30px 80px rgba(0,0,0,.4)}
            h1{font-family:Georgia,serif;font-size:32px;margin:0 0 4px;color:#ffd166}
            p{color:#cfd8e3;line-height:1.55}
            .badge{display:inline-block;background:#ffb703;color:#231a00;font-weight:800;padding:6px 12px;border-radius:999px;font-size:12px;letter-spacing:.5px}
            a{display:block;text-align:center;background:linear-gradient(135deg,#ffb703,#ff8a00);color:#231a00;text-decoration:none;font-weight:800;padding:16px;border-radius:16px;margin:20px 0 12px;font-size:17px}
            small{color:#92a4b8;display:block;line-height:1.5}
            .ic{font-size:44px}
            </style></head><body><div class="card">
            <div class="ic">🃏🎉</div>
            <h1>Fiesta de Cartas</h1>
            <div class="badge">APK · Android 7.0+</div>
            <p>Caída, Ludo, Fiesta de Colores, Kemix y Nervioso. Juega contra bots o con amigos por WiFi.</p>
            <a href="/app.apk">⬇️ Descargar la APK</a>
            <small>1. Descarga el archivo.<br>2. Si Android lo pide, permite instalar apps desde esta fuente.<br>3. Abre la app y comparte la fiesta. Estás conectado a la red del anfitrión: la descarga no usa datos móviles.</small>
            </div></body></html>
        """.trimIndent()
        val bytes = html.toByteArray(Charsets.UTF_8)
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun serveApk(out: OutputStream) {
        val apk = File(context.applicationInfo.sourceDir)
        if (!apk.exists()) {
            out.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n".toByteArray())
            out.flush()
            return
        }
        val length = apk.length()
        out.write(
            ("HTTP/1.1 200 OK\r\nContent-Type: application/vnd.android.package-archive\r\n" +
                "Content-Disposition: attachment; filename=\"FiestaDeCartas.apk\"\r\n" +
                "Content-Length: $length\r\nConnection: close\r\n\r\n").toByteArray()
        )
        apk.inputStream().use { input -> input.copyTo(out, 64 * 1024) }
        out.flush()
    }

    fun stop() {
        running = false
        runCatching { server?.close() }
        server = null
        thread = null
        url = ""
    }
}

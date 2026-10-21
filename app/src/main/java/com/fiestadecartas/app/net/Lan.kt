package com.fiestadecartas.app.net

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

object LanSession {
    var role by mutableStateOf("none") // none | host | guest
        private set
    var game by mutableStateOf("")
        private set
    var code by mutableStateOf("")
        private set
    var ip by mutableStateOf("")
        private set
    var port by mutableStateOf(0)
        private set
    var seed by mutableStateOf(0L)
        private set
    var players by mutableStateOf(2)
        private set
    var mySeat by mutableStateOf(0)
        private set
    var hostName by mutableStateOf("")
        private set
    var guestName by mutableStateOf("")
        private set
    var guestConnected by mutableStateOf(false)
        private set
    var status by mutableStateOf("")
        private set

    var onRemoteAction: ((String) -> Unit)? = null
    var onGameStart: ((game: String, seed: Long, players: Int) -> Unit)? = null
    var onPeerLeft: (() -> Unit)? = null

    private var server: LanServer? = null
    private var client: LanClient? = null

    val active: Boolean get() = role != "none" && mySeat >= 0

    fun hostRoom(gameId: String, playersCount: Int, name: String): Boolean {
        stop()
        return try {
            role = "host"
            game = gameId
            players = playersCount
            hostName = name.ifBlank { "Anfitrión" }
            guestName = "Invitado"
            mySeat = 0
            code = (1000 + Random.nextInt(9000)).toString()
            seed = Random.nextLong(1, Long.MAX_VALUE)
            val srv = LanServer(
                code = code,
                onGuestJoin = { gName ->
                    guestName = gName.ifBlank { "Invitado" }
                    guestConnected = true
                    status = "Invitado conectado"
                },
                onAction = { action -> onRemoteAction?.invoke(action) },
                onDisconnected = {
                    guestConnected = false
                    status = "Invitado desconectado"
                    onPeerLeft?.invoke()
                }
            )
            server = srv
            ip = localIp()
            port = srv.port
            status = "Sala lista"
            Log.d("FiestaLAN", "hostRoom game=$gameId players=$playersCount ip=$ip port=$port code=$code")
            true
        } catch (e: Exception) {
            role = "none"
            status = e.message ?: "No se pudo crear la sala"
            false
        }
    }

    fun startHostedGame() {
        if (role != "host") return
        val payload = "START|$seed|$game|$players|$hostName|$guestName"
        server?.broadcast(payload)
        onGameStart?.invoke(game, seed, players)
    }

    fun join(address: String, portNumber: Int, host: String, gameId: String, seedValue: Long, codeValue: String, name: String): Boolean {
        stop()
        role = "guest"
        game = gameId
        ip = address
        port = portNumber
        code = codeValue
        seed = seedValue
        hostName = host
        guestName = name.ifBlank { "Invitado" }
        players = 2
        mySeat = 1
        val cli = LanClient(
            address = address,
            port = portNumber,
            onMessage = { msg ->
                val parts = msg.split("|")
                when (parts.getOrNull(0)) {
                    "START" -> {
                        seed = parts.getOrNull(1)?.toLongOrNull() ?: seed
                        game = parts.getOrNull(2) ?: game
                        players = parts.getOrNull(3)?.toIntOrNull() ?: players
                        hostName = parts.getOrNull(4) ?: hostName
                        guestName = parts.getOrNull(5) ?: guestName
                        onGameStart?.invoke(game, seed, players)
                    }
                    "ACTION" -> {
                        val action = parts.getOrNull(1)
                        if (action != null) onRemoteAction?.invoke(action)
                    }
                    "BYE" -> onPeerLeft?.invoke()
                }
            },
            onDisconnected = { onPeerLeft?.invoke() }
        )
        // Never open a socket on the main thread: NetworkOnMainThreadException
        // would make every join fail with a generic error.
        val latch = CountDownLatch(1)
        var failure: Exception? = null
        thread(isDaemon = true, name = "fiesta-lan-connect") {
            try {
                cli.connect(name)
            } catch (e: Exception) {
                failure = e
            } finally {
                latch.countDown()
            }
        }
        val finished = try {
            latch.await(6, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            false
        }
        val error = failure
        if (!finished || error != null) {
            cli.stop()
            role = "none"
            status = error?.message ?: "No se pudo conectar"
            if (error != null) Log.e("FiestaLAN", "join failed for $address:$portNumber", error)
            return false
        }
        client = cli
        Log.d("FiestaLAN", "join success client=${cli.hashCode()}")
        status = "Conectado a $host"
        return true
    }

    fun sendAction(action: String) {
        Log.d("FiestaLAN", "sendAction role=$role action=$action client=${client?.hashCode()}")
        when (role) {
            "host" -> server?.broadcast("ACTION|$action")
            "guest" -> client?.send("ACTION|$action")
        }
    }

    fun peerActionReceived(action: String) {
        onRemoteAction?.invoke(action)
    }

    fun stop() {
        Log.d("FiestaLAN", "LanSession.stop role=$role client=${client?.hashCode()} server=${server?.hashCode()}")
        server?.stop(); server = null
        client?.stop(); client = null
        role = "none"
        game = ""
        guestConnected = false
        code = ""
        status = ""
        onRemoteAction = null
        onPeerLeft = null
    }
}

class LanServer(
    private val code: String,
    private val onGuestJoin: (String) -> Unit,
    private val onAction: (String) -> Unit,
    private val onDisconnected: () -> Unit
) {
    private val socket = ServerSocket(0)
    val port: Int = socket.localPort
    init { Log.d("FiestaLAN", "server listening port=$port") }
    @Volatile private var writer: PrintWriter? = null
    @Volatile private var running = true
    private var clientSocket: Socket? = null
    private val main = Handler(Looper.getMainLooper())
    // All socket writes happen here: println() from the UI thread throws
    // NetworkOnMainThreadException on Android.
    private val writeExec = Executors.newSingleThreadExecutor { r ->
        Thread(r, "fiesta-lan-server-write").apply { isDaemon = true }
    }

    init {
        thread(isDaemon = true, name = "fiesta-lan-server") {
            try {
                while (running) {
                    val s = socket.accept()
                    clientSocket = s
                    val w = PrintWriter(s.getOutputStream(), true)
                    writer = w
                    val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                    val hello = reader.readLine() ?: ""
                    val parts = hello.split("|")
                    val guest = if (parts.getOrNull(0) == "HELLO") parts.getOrNull(1) ?: "Invitado" else "Invitado"
                    val guestCode = if (parts.getOrNull(0) == "HELLO") parts.getOrNull(2) ?: "" else ""
                    if (parts.getOrNull(0) == "HELLO" && guestCode.isNotBlank() && guestCode != code) {
                        w.println("BYE|Código incorrecto")
                        s.close()
                        continue
                    }
                    if (parts.getOrNull(0) == "HELLO") {
                        w.println("WELCOME|${LanSession.seed}|${LanSession.game}|${LanSession.players}|${LanSession.code}")
                        Log.d("FiestaLAN", "guest joined: $guest code=$guestCode")
                        main.post { onGuestJoin(guest) }
                    }
                    while (running) {
                        val line = reader.readLine() ?: break
                        val lp = line.split("|")
                        if (lp.getOrNull(0) == "ACTION") {
                            val action = lp.getOrNull(1) ?: continue
                            main.post { onAction(action) }
                        }
                        if (lp.getOrNull(0) == "BYE") break
                    }
                    writer = null
                    main.post { onDisconnected() }
                }
            } catch (e: Exception) {
                if (running) Log.d("FiestaLAN", "server stopped", e)
            }
        }
    }

    fun broadcast(message: String) {
        val w = writer
        if (w == null) {
            Log.d("FiestaLAN", "broadcast $message skipped: no guest")
            return
        }
        runCatching {
            writeExec.execute {
                val ok = runCatching { w.println(message) }.isSuccess
                Log.d("FiestaLAN", "broadcast $message ok=$ok")
            }
        }.onFailure { Log.d("FiestaLAN", "broadcast $message rejected", it) }
    }

    fun stop() {
        running = false
        val w = writer
        val client = clientSocket
        writer = null
        clientSocket = null
        if (w != null || client != null) {
            runCatching {
                writeExec.execute {
                    runCatching { w?.println("BYE|") }
                    runCatching { client?.close() }
                }
            }
        }
        writeExec.shutdown()
        // Closing the listen socket breaks accept() immediately.
        runCatching { socket.close() }
        // Best-effort cleanup if the queued write never completes.
        thread(isDaemon = true, name = "fiesta-lan-server-close") {
            Thread.sleep(500)
            runCatching { client?.close() }
        }
    }
}

class LanClient(
    private val address: String,
    private val port: Int,
    private val onMessage: (String) -> Unit,
    private val onDisconnected: () -> Unit
) {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    @Volatile private var running = false
    private val main = Handler(Looper.getMainLooper())
    // Writes must never run on the UI thread (NetworkOnMainThreadException).
    private val writeExec = Executors.newSingleThreadExecutor { r ->
        Thread(r, "fiesta-lan-client-write").apply { isDaemon = true }
    }

    fun connect(name: String) {
        val s = Socket()
        s.connect(InetSocketAddress(address, port), 4000)
        socket = s
        writer = PrintWriter(s.getOutputStream(), true)
        Log.d("FiestaLAN", "client ${hashCode()} connected writer=${writer != null}")
        writeExec.execute { runCatching { writer?.println("HELLO|$name|${LanSession.code}") } }
        running = true
        thread(isDaemon = true, name = "fiesta-lan-client") {
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                while (running) {
                    val line = reader.readLine() ?: break
                    Log.d("FiestaLAN", "guest received: $line")
                    main.post { onMessage(line) }
                }
            } catch (e: Exception) {
                if (running) Log.d("FiestaLAN", "client stopped", e)
            } finally {
                running = false
                main.post { onDisconnected() }
            }
        }
    }

    fun send(message: String) {
        val w = writer
        if (w == null) {
            Log.d("FiestaLAN", "client ${hashCode()} send $message skipped: no writer")
            return
        }
        runCatching {
            writeExec.execute {
                val ok = runCatching { w.println(message) }.isSuccess
                Log.d("FiestaLAN", "client ${hashCode()} send $message ok=$ok")
            }
        }.onFailure { Log.d("FiestaLAN", "client ${hashCode()} send $message rejected", it) }
    }

    fun stop() {
        Log.d("FiestaLAN", "client ${hashCode()} stop")
        running = false
        val w = writer
        val s = socket
        writer = null
        socket = null
        if (w != null || s != null) {
            runCatching {
                writeExec.execute {
                    runCatching { w?.println("BYE|") }
                    runCatching { s?.close() }
                }
            }
        }
        writeExec.shutdown()
        // Best-effort cleanup if the queued write never completes.
        thread(isDaemon = true, name = "fiesta-lan-client-close") {
            Thread.sleep(500)
            runCatching { s?.close() }
        }
    }
}

fun localIp(): String {
    val candidates = mutableListOf<Pair<Int, String>>()
    runCatching {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val nif = interfaces.nextElement()
            if (!nif.isUp || nif.isLoopback) continue
            val name = nif.name.lowercase()
            var score = 0
            if (name.startsWith("wlan") || name.contains("wifi")) score += 10
            if (name.startsWith("ap") || name.contains("softap")) score += 8
            if (name.startsWith("eth")) score += 3
            val addresses = nif.inetAddresses
            while (addresses.hasMoreElements()) {
                val a = addresses.nextElement()
                if (a is Inet4Address && !a.isLoopbackAddress && a.isSiteLocalAddress) {
                    candidates += score to a.hostAddress.orEmpty()
                }
            }
        }
    }
    return candidates.maxByOrNull { it.first }?.second ?: "127.0.0.1"
}

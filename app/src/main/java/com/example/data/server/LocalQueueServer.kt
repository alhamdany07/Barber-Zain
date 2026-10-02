package com.example.data.server

import android.util.Log
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.QueueStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalQueueServer(
    private val port: Int = 8080,
    private val getQueueData: () -> Pair<String, List<QueueEntity>> // returns (shopName, todayQueues)
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    var isRunning = false
        private set

    fun start() {
        if (isRunning) return
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.d("LocalQueueServer", "Queue server started on port $port")

                while (isActive && !serverSocket!!.isClosed) {
                    val client = serverSocket?.accept() ?: break
                    launch(Dispatchers.IO) {
                        handleClient(client)
                    }
                }
            } catch (e: Exception) {
                Log.e("LocalQueueServer", "Server exception: ${e.message}")
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        serverSocket = null
        serverJob = null
    }

    private fun handleClient(client: Socket) {
        try {
            client.use { socket ->
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val line = reader.readLine() ?: return
                val parts = line.split(" ")
                val method = if (parts.isNotEmpty()) parts[0] else "GET"
                val path = if (parts.size > 1) parts[1] else "/"

                val output = socket.getOutputStream()

                when {
                    path == "/api/queue" -> {
                        sendJsonResponse(output)
                    }
                    path == "/" || path.startsWith("/?") -> {
                        sendHtmlResponse(output)
                    }
                    else -> {
                        send404(output)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("LocalQueueServer", "Error handling client: ${e.message}")
        }
    }

    private fun sendJsonResponse(output: OutputStream) {
        val (shopName, queues) = getQueueData()
        val currentCalled = queues.firstOrNull { it.status == QueueStatus.CALLED || it.status == QueueStatus.IN_SERVICE }
        val waiting = queues.filter { it.status == QueueStatus.WAITING }.take(6)

        val json = buildString {
            append("{")
            append("\"shopName\":\"${escapeJson(shopName)}\",")
            append("\"current\":")
            if (currentCalled != null) {
                append("{")
                append("\"number\":\"${currentCalled.queueNumber}\",")
                append("\"customer\":\"${escapeJson(currentCalled.customerName)}\",")
                append("\"service\":\"${escapeJson(currentCalled.serviceName)}\",")
                append("\"kapster\":\"${escapeJson(currentCalled.kapsterName ?: "-")}\",")
                append("\"chair\":\"${escapeJson(currentCalled.chairNumber)}\",")
                append("\"status\":\"${currentCalled.status.name}\"")
                append("},")
            } else {
                append("null,")
            }
            append("\"waiting\":[")
            waiting.forEachIndexed { i, q ->
                append("{")
                append("\"number\":\"${q.queueNumber}\",")
                append("\"customer\":\"${escapeJson(q.customerName)}\",")
                append("\"service\":\"${escapeJson(q.serviceName)}\",")
                append("\"duration\":${q.serviceDuration}")
                append("}")
                if (i < waiting.size - 1) append(",")
            }
            append("]")
            append("}")
        }

        val bytes = json.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"

        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun sendHtmlResponse(output: OutputStream) {
        val (shopName, _) = getQueueData()
        val html = """
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$shopName - Layar Antrian</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background: #0f172a;
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            padding: 24px 32px;
        }
        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #1e293b;
            padding-bottom: 16px;
            margin-bottom: 24px;
        }
        .brand { display: flex; align-items: center; gap: 16px; }
        .logo-badge {
            background: linear-gradient(135deg, #1e3a8a, #2563eb);
            color: #f59e0b;
            font-size: 28px;
            width: 56px;
            height: 56px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 900;
        }
        .brand h1 { font-size: 28px; font-weight: 700; color: #ffffff; letter-spacing: -0.5px; }
        .brand p { font-size: 14px; color: #94a3b8; }
        .clock {
            font-size: 32px;
            font-weight: 700;
            color: #38bdf8;
            font-variant-numeric: tabular-nums;
            background: #1e293b;
            padding: 8px 20px;
            border-radius: 14px;
        }
        main {
            display: grid;
            grid-template-columns: 1.2fr 1fr;
            gap: 28px;
            flex: 1;
        }
        @media (max-width: 800px) {
            main { grid-template-columns: 1fr; }
        }
        .card {
            background: #1e293b;
            border-radius: 24px;
            padding: 28px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
            border: 1px solid #334155;
            display: flex;
            flex-direction: column;
        }
        .hero-card {
            background: radial-gradient(circle at top left, #1e3a8a 0%, #0f172a 100%);
            border: 2px solid #2563eb;
            text-align: center;
            justify-content: center;
            align-items: center;
            position: relative;
            overflow: hidden;
        }
        .hero-card::after {
            content: '';
            position: absolute;
            top: -50px;
            right: -50px;
            width: 150px;
            height: 150px;
            background: #38bdf8;
            opacity: 0.1;
            border-radius: 50%;
        }
        .badge-called {
            background: #f59e0b;
            color: #0f172a;
            padding: 6px 18px;
            border-radius: 9999px;
            font-size: 14px;
            font-weight: 800;
            letter-spacing: 1px;
            text-transform: uppercase;
            margin-bottom: 12px;
            animation: pulse 2s infinite;
        }
        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.05); }
            100% { transform: scale(1); }
        }
        .queue-big-number {
            font-size: 110px;
            font-weight: 900;
            color: #ffffff;
            line-height: 1;
            margin: 10px 0;
            text-shadow: 0 4px 20px rgba(56, 189, 248, 0.4);
            letter-spacing: 2px;
        }
        .queue-service {
            font-size: 24px;
            color: #e2e8f0;
            font-weight: 600;
            margin-bottom: 8px;
        }
        .queue-dest {
            font-size: 20px;
            color: #38bdf8;
            font-weight: 700;
            background: rgba(15, 23, 42, 0.6);
            padding: 8px 24px;
            border-radius: 12px;
            margin-top: 12px;
            display: inline-block;
        }
        .list-header {
            font-size: 18px;
            font-weight: 700;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 16px;
            display: flex;
            justify-content: space-between;
        }
        .queue-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
            overflow-y: auto;
        }
        .queue-item {
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 16px;
            padding: 14px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: all 0.2s;
        }
        .queue-item-left { display: flex; align-items: center; gap: 16px; }
        .queue-item-num {
            font-size: 22px;
            font-weight: 800;
            color: #f59e0b;
            background: #1e293b;
            padding: 6px 14px;
            border-radius: 10px;
            border: 1px solid #475569;
        }
        .queue-item-info h4 { font-size: 16px; color: #ffffff; }
        .queue-item-info p { font-size: 13px; color: #94a3b8; }
        .queue-item-time {
            font-size: 14px;
            font-weight: 600;
            color: #38bdf8;
            background: rgba(56, 189, 248, 0.1);
            padding: 4px 10px;
            border-radius: 8px;
        }
        footer {
            margin-top: 20px;
            text-align: center;
            font-size: 13px;
            color: #64748b;
        }
        .empty-state {
            text-align: center;
            color: #64748b;
            padding: 40px;
            font-size: 18px;
        }
    </style>
</head>
<body>
    <header>
        <div class="brand">
            <div class="logo-badge">✂</div>
            <div>
                <h1 id="shop-title">$shopName</h1>
                <p>Sistem Antrian Digital Barbershop</p>
            </div>
        </div>
        <div class="clock" id="clock">00:00:00</div>
    </header>

    <main>
        <div class="card hero-card" id="hero-card">
            <div class="badge-called" id="hero-badge">SEDANG DIPANGGIL</div>
            <div class="queue-big-number" id="hero-number">---</div>
            <div class="queue-service" id="hero-service">Menunggu Pelanggan</div>
            <div class="queue-dest" id="hero-dest">Kursi -</div>
        </div>

        <div class="card">
            <div class="list-header">
                <span>Antrian Berikutnya</span>
                <span id="waiting-count">0 Orang</span>
            </div>
            <div class="queue-list" id="queue-list">
                <div class="empty-state">Tidak ada antrian menunggu</div>
            </div>
        </div>
    </main>

    <footer>
        Pangkas Rambut Zain &bull; Tampilan TV Layar Antrian Real-Time
    </footer>

    <script>
        function updateClock() {
            const now = new Date();
            const timeStr = now.toLocaleTimeString('id-ID', { hour12: false });
            document.getElementById('clock').textContent = timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();

        let lastCalled = "";
        async function fetchQueue() {
            try {
                const res = await fetch('/api/queue');
                if (!res.ok) return;
                const data = await res.json();
                
                document.getElementById('shop-title').textContent = data.shopName;

                const heroCard = document.getElementById('hero-card');
                const heroNumber = document.getElementById('hero-number');
                const heroService = document.getElementById('hero-service');
                const heroDest = document.getElementById('hero-dest');
                const heroBadge = document.getElementById('hero-badge');

                if (data.current) {
                    heroNumber.textContent = data.current.number;
                    heroService.textContent = data.current.service + ' (' + data.current.customer + ')';
                    const kapsterStr = data.current.kapster !== '-' ? 'Kapster: ' + data.current.kapster : '';
                    heroDest.textContent = 'Kursi ' + data.current.chair + (kapsterStr ? ' • ' + kapsterStr : '');
                    heroBadge.textContent = data.current.status === 'CALLED' ? 'DIPANGGIL' : 'SEDANG DILAYANI';
                    heroBadge.style.display = 'inline-block';

                    if (data.current.number !== lastCalled && data.current.status === 'CALLED') {
                        lastCalled = data.current.number;
                        // Optional sound cue
                        try {
                            const ctx = new (window.AudioContext || window.webkitAudioContext)();
                            const osc = ctx.createOscillator();
                            osc.type = 'sine';
                            osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
                            osc.connect(ctx.destination);
                            osc.start();
                            osc.stop(ctx.currentTime + 0.3);
                        } catch(e) {}
                    }
                } else {
                    heroNumber.textContent = "---";
                    heroService.textContent = "Menunggu Pelanggan";
                    heroDest.textContent = "Siap Melayani";
                    heroBadge.style.display = 'none';
                }

                const listContainer = document.getElementById('queue-list');
                const countBadge = document.getElementById('waiting-count');
                countBadge.textContent = data.waiting.length + ' Orang';

                if (data.waiting.length === 0) {
                    listContainer.innerHTML = '<div class="empty-state">Tidak ada antrian menunggu</div>';
                } else {
                    listContainer.innerHTML = data.waiting.map(item => `
                        <div class="queue-item">
                            <div class="queue-item-left">
                                <div class="queue-item-num">${'$'}{item.number}</div>
                                <div class="queue-item-info">
                                    <h4>${'$'}{item.customer}</h4>
                                    <p>${'$'}{item.service}</p>
                                </div>
                            </div>
                            <div class="queue-item-time">~${'$'}{item.duration} Menit</div>
                        </div>
                    `).join('');
                }
            } catch (err) {
                console.error("Gagal update antrian", err);
            }
        }

        setInterval(fetchQueue, 2500);
        fetchQueue();
    </script>
</body>
</html>
        """.trimIndent()

        val bytes = html.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"

        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun send404(output: OutputStream) {
        val body = "404 Not Found"
        val header = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: ${body.length}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(header.toByteArray())
        output.write(body.toByteArray())
        output.flush()
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    companion object {
        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    if (networkInterface.isLoopback || !networkInterface.isUp) continue
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (address is Inet4Address && !address.isLoopbackAddress) {
                            val hostAddress = address.hostAddress
                            if (hostAddress != null && (hostAddress.startsWith("192.168.") || hostAddress.startsWith("10.") || hostAddress.startsWith("172."))) {
                                return hostAddress
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LocalQueueServer", "Error finding IP: ${e.message}")
            }
            return "192.168.1.100" // fallback
        }
    }
}

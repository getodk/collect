package org.odk.collect.maplibre

import org.odk.collect.maps.layers.TileSource
import timber.log.Timber
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.Locale

/** A minimal HTTP server that serves tiles from a set of TileSources. */
internal class TileHttpServer {
    private val sources = mutableMapOf<String, TileSource>()
    private val socket = createBoundSocket(PORT_MIN, PORT_MAX)
    private val server = ServerThread(socket)

    fun start() {
        server.start()
    }

    /**
     * Constructs a URL template for fetching tiles from this server for a given
     * tileset, with placeholders {z} for zoom level and {x} and {y} for coordinates.
     */
    fun getUrlTemplate(key: String): String {
        return String.format(
            Locale.US,
            "http://localhost:%d/%s/{z}/{x}/{y}",
            socket.localPort,
            key
        )
    }

    /**
     * Adds a TileSource with a given key.  Tiles from this source will be served
     * under the URL path /{key}/{zoom}/{x}/{y}.  If this TileSource implements
     * Closeable, it will be closed when this server is finalized with destroy().
     */
    fun addSource(key: String, source: TileSource) {
        sources[key] = source
    }

    /** Permanently closes all sockets and closeable TileSources. */
    fun destroy() {
        try {
            socket.close()
        } catch (_: IOException) { /* ignore */ }
        server.interrupt()
        sources.values.filterIsInstance<Closeable>().forEach { source ->
            try {
                source.close()
            } catch (_: IOException) { /* ignore */ }
        }
    }

    private inner class ServerThread(private val socket: ServerSocket) : Thread() {
        override fun run() {
            try {
                socket.reuseAddress = true
                Timber.i("Ready for requests on port %d", socket.localPort)
                while (!isInterrupted) {
                    val connection = socket.accept()
                    Timber.i("Accepted a client connection")
                    ResponseThread(connection).start()
                }
                Timber.i("Server thread interrupted")
            } catch (e: IOException) {
                Timber.i("Server thread stopped: %s", e.message)
            }
        }
    }

    private inner class ResponseThread(private val connection: Socket) : Thread() {
        override fun run() {
            try {
                connection.use { serve(it) }
            } catch (e: IOException) {
                Timber.e(e, "Unable to read request from socket")
            }
        }

        private fun serve(connection: Socket) {
            val request = BufferedReader(InputStreamReader(connection.getInputStream())).readLine()
            Timber.i("Received request: %s", request)
            if (request == null) {
                return
            }
            val start = System.currentTimeMillis()
            val response = getResponse(request)
            if (response == null) {
                Timber.i("%s: No tile at these coordinates", request)
                return
            }
            sendResponse(connection, response)
            val finish = System.currentTimeMillis()
            Timber.i("%s: Served %d bytes in %d ms", request, response.data.size, finish - start)
        }

        private fun getResponse(request: String): Response? {
            if (request.startsWith("GET /")) {
                val path = request.substring(5).split(" ", limit = 2)[0]
                val parts = path.split("/")
                if (parts.size == 4) {
                    try {
                        val key = URLDecoder.decode(parts[0], "utf-8")
                        val zoom = parts[1].toInt()
                        val x = parts[2].toInt()
                        val y = parts[3].toInt()
                        val source = sources[key]
                        if (source != null) {
                            val data = source.getTileBlob(zoom, x, y)
                            if (data != null) {
                                return Response(data, source.contentType, source.contentEncoding)
                            }
                        }
                    } catch (e: NumberFormatException) {
                        Timber.w(e, "Bad request %s", request)
                    }
                }
            }
            Timber.w("Ignoring request: %s", request)
            return null
        }

        private fun sendResponse(connection: Socket, response: Response) {
            val headers = String.format(
                Locale.US,
                "HTTP/1.0 200\r\n" +
                    "Content-Type: %s\r\n" +
                    "Content-Encoding: %s\r\n" +
                    "Content-Length: %d\r\n" +
                    "\r\n",
                response.contentType,
                response.contentEncoding,
                response.data.size
            )

            try {
                connection.getOutputStream().use { output ->
                    output.write(headers.toByteArray())
                    output.write(response.data)
                    output.flush()
                }
            } catch (e: IOException) {
                Timber.e(e, "Unable to write response to socket")
            }
        }
    }

    private class Response(
        val data: ByteArray,
        val contentType: String,
        val contentEncoding: String
    )

    companion object {
        private const val PORT_MIN = 8000
        private const val PORT_MAX = 8999

        /** Finds an available port and binds a ServerSocket to it. */
        private fun createBoundSocket(portMin: Int, portMax: Int): ServerSocket {
            for (port in portMin..portMax) {
                try {
                    return ServerSocket(port)
                } catch (_: BindException) {
                    continue // this port is in use; try another one
                }
            }
            Timber.e(Error("No ports available from $portMin to $portMax"))
            throw IOException("Could not find an available port")
        }
    }
}

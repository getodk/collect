package org.odk.collect.android.map;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/** A minimal HTTP server that serves tiles from a set of TileSources. */
public class TileHttpServer {
    public static final int PORT_MIN = 8000;
    public static final int PORT_MAX = 8999;

    final Map<String, TileSource> sources = new HashMap<>();
    final ServerThread server;
    final ServerSocket socket;

    public TileHttpServer() throws IOException {
        socket = createBoundSocket(PORT_MIN, PORT_MAX);
        if (socket == null) {
            throw new IOException("Could not find an available port");
        }
        server = new ServerThread(socket);
    }

    public void start() {
        server.start();
    }

    /** Finds an available port and binds a ServerSocket to it. */
    protected static ServerSocket createBoundSocket(int portMin, int portMax) throws IOException {
        for (int port = portMin; port <= portMax; port++) {
            try {
                return new ServerSocket(port);
            } catch (BindException e) {
                continue;  // this port is in use; try another one
            }
        }
        Timber.e("No ports available from %d to %d", portMin, portMax);
        return null;
    }

    /**
     * Constructs a URL template for fetching tiles from this server for a given
     * tileset, with placeholders {z} for zoom level and {x} and {y} for coordinates.
     */
    public String getUrlTemplate(String key) {
        return String.format(
            Locale.US, "http://localhost:%d/%s/{z}/{x}/{y}", socket.getLocalPort(), key);
    }

    /**
     * Adds a TileSource with a given key.  Tiles from this source will be served
     * under the URL path /{key}/{zoom}/{x}/{y}.  If this TileSource implements
     * Closeable, it will be closed when this server is finalized with destroy().
     */
    public void addSource(String key, TileSource source) {
        sources.put(key, source);
    }

    /** Permanently closes all sockets and closeable TileSources. */
    public void destroy() {
        try {
            socket.close();
        } catch (IOException e) { /* ignore */ }
        server.interrupt();
        for (TileSource source : sources.values()) {
            if (source instanceof Closeable) {
                try {
                    ((Closeable) source).close();
                } catch (IOException e) { /* ignore */ }
            }
        }
    }

    class ServerThread extends Thread {
        final ServerSocket socket;

        ServerThread(ServerSocket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                socket.setReuseAddress(true);
                Timber.i("Ready for requests on port %d", socket.getLocalPort());
                while (!isInterrupted()) {
                    Socket connection = socket.accept();
                    Timber.i("Accepted a client connection");
                    new ResponseThread(connection).start();
                }
                Timber.i("Server thread interrupted");
            } catch (IOException e) {
                Timber.i("Server thread stopped: %s", e.getMessage());
            }
        }
    }

    class ResponseThread extends Thread {
        final Socket connection;

        ResponseThread(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            try (Socket connection = this.connection) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                String request = new BufferedReader(reader).readLine();
                Timber.i("Received request: %s", request);
                if (request == null) {
                    return;
                }
                long start = System.currentTimeMillis();
                Response response = getResponse(request);
                if (response == null) {
                    Timber.i("%s: No tile at these coordinates", request);
                    return;
                }
                sendResponse(connection, response);
                long finish = System.currentTimeMillis();
                Timber.i("%s: Served %d bytes in %d ms", request, response.data.length, finish - start);
            } catch (IOException e) {
                Timber.e(e, "Unable to read request from socket");
            }
        }

        protected Response getResponse(String request) {
            if (request.startsWith("GET /")) {
                String path = request.substring(5).split(" ", 2)[0];
                String[] parts = path.split("/");
                if (parts.length == 4) {
                    try {
                        String key = URLDecoder.decode(parts[0], "utf-8");
                        int zoom = Integer.parseInt(parts[1]);
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        TileSource source = sources.get(key);
                        if (source != null) {
                            byte[] data = source.getTileBlob(zoom, x, y);
                            if (data != null) {
                                return new Response(data, source.getContentType(), source.getContentEncoding());
                            }
                        }
                    } catch (NumberFormatException e) {
                        Timber.w(e, "Bad request %s", request);
                    } catch (UnsupportedEncodingException e) { /* cannot happen because UTF-8 is built in */ }
                }
            }
            Timber.w("Ignoring request: %s", request);
            return null;
        }

        protected void sendResponse(Socket connection, Response response) {
            String headers = String.format(
                Locale.US,
                "HTTP/1.0 200\r\n" +
                    "Content-Type: %s\r\n" +
                    "Content-Encoding: %s\r\n" +
                    "Content-Length: %d\r\n" +
                    "\r\n",
                response.contentType,
                response.contentEncoding,
                response.data.length
            );

            try (OutputStream output = connection.getOutputStream()) {
                output.write(headers.getBytes());
                output.write(response.data);
                output.flush();
            } catch (IOException e) {
                Timber.e(e, "Unable to write response to socket");
            }
        }
    }

    public static class Response {
        byte[] data;
        String contentType;
        String contentEncoding;

        public Response(byte[] data, String contentType, String contentEncoding) {
            this.data = data;
            this.contentType = contentType;
            this.contentEncoding = contentEncoding;
        }
    }
}

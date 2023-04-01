package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private final List<String> allowedMethods = List.of(GET,POST);
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> allHandlers = new ConcurrentHashMap<>();
    public void addHandler(String method, String path, Handler handler) {
        var methodMap = allHandlers.get(method);

        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            allHandlers.put(method, methodMap);
        }
        methodMap.put(path, handler);
        allHandlers.put(method, methodMap);
    }
    public void listen(int port) throws IOException {
        if (port <= 0)
            throw  new IllegalArgumentException("Server port must be greater than 0");
        try (final var serverSocket = new ServerSocket(port)) {
            final var threadPool = Executors.newFixedThreadPool(64);
            while (true) {
                final var socket = serverSocket.accept();
                System.out.println(LocalDateTime.now() + ":  New accept, port " + socket.getLocalPort());
                threadPool.submit(() -> acceptConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptConnection(Socket socket) {
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }
            System.out.println(LocalDateTime.now() + ":  Request line: V");

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }


            // method и path
            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }
            System.out.println(LocalDateTime.now() + ":  Method, path: V");

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }
            System.out.println(LocalDateTime.now() + ":  Headers: V");

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

            final var request = new Request(method, path, headers);
            System.out.println(request);

            // для GET тела нет
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    final var body = new String(bodyBytes);
                    request.setBody(body);
                    System.out.println(body);
                }
            }
            request.toString();

            var methodMap = allHandlers.get(request.getMethod());

            if (methodMap == null) {
                notFound(out);
                return;
            }

            var handler = methodMap.get(request.getQueryParams());

            if (handler == null) {
                notFound(out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}



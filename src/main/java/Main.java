public class Main {
    public static void main(String[] args){
        final var server = new Server();

        // добавление handler'ов (обработчиков)
        server.addHandler("GET", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 201 Created\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        });

        server.addHandler("POST", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 202 Accepted\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        });

        server.listen(9999);
    }
}
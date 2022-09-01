import java.util.List;

public class Request {

    private String method;
    private String path;
    private List<String> headers;
    private String body;

    public String toString () {
        return "Метод запроса: " + method + "\n" +
                "Путь: " + path + "\n" +
                "Заголовки: " + headers.toString() + "\n" +
                "Тело запрса: " + body;
    }

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
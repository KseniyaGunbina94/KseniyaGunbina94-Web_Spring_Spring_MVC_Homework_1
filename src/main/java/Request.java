import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private String method;
    private String path;
    private List<String> headers;
    private String body;

    public String toString () {
        try {
            return "Метод запроса: " + method + "\n" +
                    "Путь: " + getQueryParams() + "\n" +
                    "Параметры: " + getQueryParam(path) + "\n" +
                    "Заголовки: " + headers.toString() + "\n" +
                    "Тело запрса: " + body;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }
    public List<NameValuePair> getQueryParam(String name) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(name), StandardCharsets.UTF_8);
        return params;
    }

    public String getQueryParams() throws URISyntaxException {
        String pathWithoutParams = path.split("\\?")[0];
        return pathWithoutParams;
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
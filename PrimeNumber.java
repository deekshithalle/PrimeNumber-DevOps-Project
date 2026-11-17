import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class PrimeNumber {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
                return;
            }
            Path page = Path.of("/app/index.html");
            if (!Files.exists(page)) page = Path.of("index.html");
            byte[] body = Files.readAllBytes(page);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/api/prime", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json; charset=UTF-8", "{\"error\":\"Use GET\"}");
                return;
            }
            try {
                String query = exchange.getRequestURI().getRawQuery();
                String value = null;
                if (query != null) {
                    for (String part : query.split("&")) {
                        String[] pair = part.split("=", 2);
                        if (pair.length == 2 && "number".equals(pair[0])) {
                            value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        }
                    }
                }
                if (value == null || value.isBlank()) {
                    send(exchange, 400, "application/json; charset=UTF-8",
                            "{\"error\":\"Please enter a whole number.\"}");
                    return;
                }
                int number = Integer.parseInt(value);
                boolean prime = isPrime(number);
                String json = "{\"number\":" + number + ",\"isPrime\":" + prime
                        + ",\"message\":\"" + number + (prime ? " is a Prime Number" : " is not a Prime Number") + "\"}";
                send(exchange, 200, "application/json; charset=UTF-8", json);
            } catch (NumberFormatException ex) {
                send(exchange, 400, "application/json; charset=UTF-8",
                        "{\"error\":\"Enter a valid integer.\"}");
            } catch (Exception ex) {
                send(exchange, 500, "application/json; charset=UTF-8",
                        "{\"error\":\"Unable to process the request.\"}");
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Prime Number web application running on port " + port);
    }

    private static boolean isPrime(int number) {
        if (number <= 1) return false;
        for (int i = 2; i <= number / i; i++) {
            if (number % i == 0) return false;
        }
        return true;
    }

    private static void send(HttpExchange exchange, int status, String contentType, String response)
            throws IOException {
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}

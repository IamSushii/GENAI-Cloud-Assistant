import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GenAIAgent {
    // 1. Insert your Google Gemini API Key here
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + API_KEY;
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        // 2. Launch the Web Server on Port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new ChatHandler());
        server.setExecutor(null); 
        server.start();
        System.out.println("--- Java Enterprise Web Server Running on Port 8080 ---");
    }

    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            String query = exchange.getRequestURI().getQuery();
            String prompt = "";
            String botReply = "Awaiting your command...";
            
            // 3. Extract the user's message from the web form
            if (query != null && query.startsWith("prompt=")) {
                prompt = query.substring(7).replace("+", " ").replace("%20", " ");
                botReply = getAIResponse(prompt);
            }

            // 4. The HTML Web Interface
            String html = "<html><body style='font-family: Arial; max-width: 600px; margin: 40px auto; background-color: #f9f9f9;'>" +
                          "<h2 style='color: #333;'>Java Enterprise AI Server</h2>" +
                          "<form method='GET' action='/'>" +
                          "<input type='text' name='prompt' placeholder='Ask the AI...' style='width: 75%; padding: 10px; border: 1px solid #ccc;' required>" +
                          "<button type='submit' style='width: 20%; padding: 10px; background: #007bff; color: white; border: none;'>Send</button>" +
                          "</form>" +
                          "<div style='margin-top: 20px; padding: 20px; background: white; border: 1px solid #ddd; border-radius: 5px;'>" +
                          "<strong>AI Response:</strong><br><br>" + botReply +
                          "</div></body></html>";

            byte[] response = html.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    private static String getAIResponse(String prompt) {
        String jsonPayload = "{\"contents\": [{\"parts\":[{\"text\": \"" + prompt + "\"}]}]}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            int textStartIndex = responseBody.indexOf("\"text\": \"") + 9;
            int textEndIndex = responseBody.indexOf("\"", textStartIndex);

            if (textStartIndex > 8 && textEndIndex > textStartIndex) {
                String reply = responseBody.substring(textStartIndex, textEndIndex);
                return reply.replace("\\n", "<br>").replace("\\\"", "\"");
            }
            return "Error parsing neural network data.";
        } catch (Exception e) {
            return "System Error executing API request.";
        }
    }
}
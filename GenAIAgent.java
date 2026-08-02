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
            String html = "<!DOCTYPE html>" +
    "<html lang='en'>" +
    "<head>" +
    "    <meta charset='UTF-8'>" +
    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
    "    <title>Enterprise AI Assistant</title>" +
    "    <script src='https://cdn.tailwindcss.com'></script>" +
    "</head>" +
    "<body class='bg-gray-900 text-gray-100 h-screen flex flex-col justify-between'>" +
    "    <!-- Header -->" +
    "    <header class='bg-gray-800 border-b border-gray-700 p-4 text-center font-bold text-lg text-blue-400'>" +
    "        Enterprise AI Cloud Assistant" +
    "    </header>" +
    "    <!-- Chat Container -->" +
    "    <main id='chat-container' class='flex-1 overflow-y-auto p-4 space-y-4 max-w-3xl w-full mx-auto'>" +
    "        <div class='flex items-start space-x-3'>" +
    "            <div class='bg-blue-600 text-white rounded-full h-8 w-8 flex items-center justify-center font-bold'>AI</div>" +
    "            <div class='bg-gray-800 p-3 rounded-lg max-w-lg text-sm'>" + (botReply != null ? botReply : "Hello! I am your enterprise Java AI assistant. How can I help you today?") + "</div>" +
    "        </div>" +
    "    </main>" +
    "    <!-- Input Footer -->" +
    "    <footer class='bg-gray-800 border-t border-gray-700 p-4'>" +
    "        <form method='GET' action='/' class='max-w-3xl mx-auto flex gap-2'>" +
    "            <input type='text' name='prompt' placeholder='Type your message here...' required " +
    "                class='flex-1 bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:outline-none focus:border-blue-500 text-sm'>" +
    "            <button type='submit' class='bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg text-sm font-semibold transition'>Send</button>" +
    "        </form>" +
    "    </footer>" +
    "</body>" +
    "</html>";

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
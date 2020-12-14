package urlshortener.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController extends BinaryWebSocketHandler {

    //Map<WebSocketSession, FileUploadInFlight> sessionToFileMap = new WeakHashMap<>();

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    @MessageMapping("/uploadCSV")
    @SendTo("/topic/getCSV")
    public String getCSV(String message) throws Exception {
        System.out.println("Evento recibido: ");
        System.out.println(message);
        // Chrome -> se puede mirar el estado de WS
        // Para respuestas en bloque ->
        return "Hello, message received";
    }

}


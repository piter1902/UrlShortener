package urlshortener.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import urlshortener.service.CSVHelper;

@Controller
public class WebSocketController extends BinaryWebSocketHandler {

    @Autowired
    private CSVHelper csvHelper;

    //Map<WebSocketSession, FileUploadInFlight> sessionToFileMap = new WeakHashMap<>();

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    @MessageMapping("/uploadCSV")
    @SendToUser("/topic/getCSV")
    public void getCSV(String message, SimpMessageHeaderAccessor ha, @Header("simpSessionId") String sessionId) throws Exception {
        String ip = (String) ha.getSessionAttributes().get("ip");
        System.out.println("IP: " + ip);
        System.out.println("Evento recibido de user con ID: " + sessionId);
        //System.out.println(message);
        csvHelper.saveCsvWS(message, ip, sessionId);
        // Chrome -> se puede mirar el estado de WS
//        return "Hello, message received";
    }

}


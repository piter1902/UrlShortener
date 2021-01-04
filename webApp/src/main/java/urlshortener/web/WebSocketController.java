package urlshortener.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import urlshortener.service.CSVHelper;

import java.util.List;

@Controller
public class WebSocketController extends BinaryWebSocketHandler {

    @Autowired
    private CSVHelper csvHelper;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private static final String WS_MESSAGE_TRANSFER_DESTINATION = "/topic/getCSV";
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    //Map<WebSocketSession, FileUploadInFlight> sessionToFileMap = new WeakHashMap<>();

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    /**
     * Obtains the message from the user (a block of URLs) and shorts them with the method csvHelper.shortUrlSlice
     * Finally, sends back those shorted URLs back to the user one by one
     * Receive all messages from the channel 'uploadCSV' and send replies to the channel /topic/getCSV using SendToUser
     * (only the user that request the operation will receive a reply)
     * @param message   URLs block received from the user
     * @param ha        SimpMessageHeaderAccessor for obtain the IP from the request
     * @param sessionId Client's session ID. Is unique for each user.
     */
    @MessageMapping("/uploadCSV")
    @SendToUser("/topic/getCSV")
    public void getCSV(String message, SimpMessageHeaderAccessor ha, @Header("simpSessionId") String sessionId) {
        String ip = (String) ha.getSessionAttributes().get("ip");
        log.debug("IP address: " + ip);
        log.info("Event received with ID: " + sessionId);
        List<String> shortedUrls = csvHelper.shortUrlSlice(message, ip);
        for (String url : shortedUrls){
            sendMessage(url, sessionId);
        }
    }

    /**
     * Sends the shorted url to the user using simpMessagingTemplate
     * Source: https://www.mokkapps.de/blog/sending-message-to-specific-anonymous-user-on-spring-websocket/
     *
     * @param message   message to the user
     * @param sessionId client's session ID
     */
    private void sendMessage(String message, String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setHeader(SimpMessageHeaderAccessor.SESSION_ID_HEADER, sessionId);
        simpMessagingTemplate.convertAndSendToUser(sessionId, WS_MESSAGE_TRANSFER_DESTINATION, message,
                accessor.getMessageHeaders());
    }

}


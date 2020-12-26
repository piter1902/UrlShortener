package urlshortener.workers;

import urlshortener.domain.ShortURL;
import urlshortener.service.URLCheckerService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;

public class Receiver {

    public static final String RECEIVE_METHOD_NAME = "receiveMessage";
    @Autowired
    private Gson gson;
    @Autowired
    private URLCheckerService urlCheckerService;

    public void receiveMessage(String toVerify) {
        System.err.println("[Receiver] ha recibido el mensaje \"" + toVerify + '"');
        try {
            ShortURL su = urlCheckerService.validateURL(gson.fromJson(toVerify, ShortURL.class));
            // Update validated field status
            su = urlCheckerService.markAsValidated(su);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

package UrlShortenerWorkers.workers;

import UrlShortenerWorkers.domain.ShortURL;
import UrlShortenerWorkers.service.URLCheckerService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {

    @Autowired
    private Gson gson;

    @Autowired
    private URLCheckerService urlCheckerService;

    public static final String RECEIVE_METHOD_NAME = "receiveMessage";

    public void receiveMessage(String toVerify) {
        System.err.println("[Receiver] ha recibido el mensaje \"" + toVerify + '"');
        ShortURL su = urlCheckerService.validateURL(gson.fromJson(toVerify, ShortURL.class));
    }
}

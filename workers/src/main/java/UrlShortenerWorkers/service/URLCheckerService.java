package UrlShortenerWorkers.service;

import UrlShortenerWorkers.domain.ShortURL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class URLCheckerService {

    @Autowired
    private ShortURLService shortUrlService;

    /**
     * Method that checks if url is safe and notifies to service associated to shorted urls.
     *
     * @param su  shortUrl object to modify (if needed) safe.
     * @return [su] object with safe field updated.
     * [su.safe] will be true if HTTP GET over url returns 200.
     * Else will be false.
     */
    public ShortURL validateURL(ShortURL su) {
        // TODO: Update this to add timeouts
        // Source: https://www.baeldung.com/java-http-request
        String url = su.getTarget();
        try {
            URL url_check = new URL(url);
            HttpURLConnection con = (HttpURLConnection) url_check.openConnection();
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                // Request returns 200. Url is valid.
                su = shortUrlService.markAs(su, true);
                System.out.format("URL %s valida\n", su.getTarget());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
//            log.debug("LA URL NO ES VALIDA");
            su = shortUrlService.markAs(su, false);
        }
        return su;
    }
}

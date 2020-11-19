package urlshortener.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import urlshortener.domain.ShortURL;
import urlshortener.domain.ShortUrlStatus;
import urlshortener.service.ShortURLService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UrlShortenerStatus {

    @Autowired
    private ShortURLService shortURLService;


    /**
     * Method that obtains status given ShortUrl's hash
     *
     * @param hash hash to check status
     * @return 200 and ShortUrlStatus object containing ShortUrl[hash]'s status iff ShortUrl[hash] exists.
     * 404 and null iff ShortUrl[hash] doesn't exist.
     */
    @GetMapping(value = "/status/{hash}")
    public ResponseEntity<ShortUrlStatus> status(@PathVariable(name = "hash") String hash, HttpServletRequest request) {
        ShortURL su = shortURLService.findByKey(hash);
        if (su != null) {
            // ShortUrl exists
            // Creation of URI
            ShortURL aux = shortURLService.create(su.getTarget(), null, request.getRemoteAddr());
            ShortUrlStatus shortUrlStatus = new ShortUrlStatus(hash, su.getSafe(), aux.getUri());
            return new ResponseEntity<>(shortUrlStatus, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}

package urlshortener.web;

import common.domain.ShortURL;
import common.domain.ShortUrlStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
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
    @Operation(method = "GET", description = "Returns status given a Shortened URL. Status is composed by " +
            "its hash (identifier), safe flag (if it's reachable, it'll true) and its URI")
    @Parameter(name = "hash", description = "Hash to obtain Shortened URL status", required = true, example = "eab67425")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "There is a Shortened URL that has id = hash. Return Status."),
            @ApiResponse(responseCode = "404", description = "There isn't a Shortened URL that has id = hash")
    })
    @GetMapping(value = "/status/{hash}")
    public ResponseEntity<ShortUrlStatus> status(@PathVariable(name = "hash") String hash, HttpServletRequest request) {
        ShortURL su = shortURLService.findByKey(hash);
        if (su != null) {
            // ShortUrl exists
            // Creation of URI
            ShortURL aux = shortURLService.create(su.getTarget(), null, request.getRemoteAddr());
            ShortUrlStatus shortUrlStatus = new ShortUrlStatus(hash, su.getSafe(), su.getValidated(), aux.getUri());
            return new ResponseEntity<>(shortUrlStatus, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}

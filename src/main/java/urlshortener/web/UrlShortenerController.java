package urlshortener.web;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.ShortURLService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
public class UrlShortenerController {
    public static final String CSV_SEPARATOR = ";";
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id,
                                        HttpServletRequest request) {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null && l.getSafe()) {
            clickService.saveClick(id, extractIP(request));
            return createSuccessfulRedirectToResponse(l);
        } else if (l != null && !l.getSafe()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/link", method = RequestMethod.POST)
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
                                              @RequestParam(value = "sponsor", required = false)
                                                      String sponsor,
                                              HttpServletRequest request) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                "https"});
        if (urlValidator.isValid(url)) {
            ShortURL su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
            // Sends the shortURL to a message queue to validate
            // Source: https://www.baeldung.com/java-http-request
            try {
                URL url_check = new URL(url);
                HttpURLConnection con = (HttpURLConnection) url_check.openConnection();
                con.setRequestMethod("GET");
                if (con.getResponseCode() == 200) {
                    // Request returns 200. Url is valid.
                    shortUrlService.markAs(su, true);
                    System.out.format("URL %s valida\n", su.getTarget());
                } /*else {
          //su = shortUrlService.markAs(su, false);
        }*/
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("LA URL NO ES VALIDA");
                su = shortUrlService.markAs(su, false);
            }

            // Returns shortURL
            HttpHeaders h = new HttpHeaders();
            h.setLocation(su.getUri());
            return new ResponseEntity<>(su, h, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(l.getTarget()));
        return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
    }

    @PostMapping(value = "/uploadCSV")
    // TODO: Change return type to download the file
    public ResponseEntity<byte[]> uploadCsv(@RequestParam(name = "file") MultipartFile file) {
        try {
            // Contenido del fichero
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.replaceAll("\r\n", "\n").split("\n");
            for (String s : lines) {
                String[] fields = s.split(CSV_SEPARATOR);
                // TODO: Short the URL line and append it to the return file
                System.out.println(s);
            }
            File f = new File("files/" + UUID.randomUUID() + ".csv");
            PrintWriter pw = new PrintWriter(f);
            pw.print(content);
            pw.close();
            return new ResponseEntity<>(content.getBytes(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

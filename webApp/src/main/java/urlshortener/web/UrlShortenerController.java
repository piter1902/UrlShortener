package urlshortener.web;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.HashCalculator;
import urlshortener.service.QRCodeService;
import urlshortener.service.ShortURLService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
// OpenApi documentation source: https://www.dariawan.com/tutorials/spring/documenting-spring-boot-rest-api-springdoc-openapi-3/
@OpenAPIDefinition(
        info = @Info(
                title = "Short URL API",
                description = "REST API to short URLs",
                version = "1.0"
        )
)
public class UrlShortenerController {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerController.class);

    private static final String CSV_SEPARATOR = ";";
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    private final QRCodeService qrCodeService;

    /*@Autowired
    Sender sender;*/

    /**
     * Public constructor
     *
     * @param shortUrlService short url service
     * @param clickService    click service
     * @param qrCodeService   qrcode service
     */
    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService,
                                  QRCodeService qrCodeService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Endpoint to redirect client to shortUrl location
     *
     * @param id      of shortUrl (hash code)
     * @param request object
     * @return 307 and redirects to shortUrl(id).target iff shortUrl(id) exists and it's safe.
     * 400 iff shortUrl(id) not exists and shortUrl(id) isn't safe.
     * 404 iff shortUrl(id) not exists.
     */
    @RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
    @Operation(method = "GET", description = "Redirects from shortened URL to target URL")
    @Parameter(name = "id", description = "ID of to obtain target URL", required = true, example = "eab67425")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "307", description = "Redirecting to target URL. OK."
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Destination URL unreachable. ERROR."
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "ID not found. ERROR."
                    )
            }
    )
    public ResponseEntity<?> redirectTo(@PathVariable String id,
                                        HttpServletRequest request) {

        //sender.send("Mensaje desde "+id);

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

    /**
     * Endpoint that shorts [url]
     *
     * @param url     uri to short
     * @param sponsor sponsor
     * @param request object
     * @return 201 iff shortUrl has been created successfully.
     * 200 iff shortUrl hasn't been created (it exists)
     * 400 iff url isn't valid (UrlValidator class)
     */
    @RequestMapping(value = "/link", method = RequestMethod.POST)
    @Operation(method = "POST", description = "Creates a shortened URL from URL. It can also generate QR code for " +
            "shortened URL.")
    @Parameters(
            value = {
                    @Parameter(name = "url", description = "URL to short", required = true, example = "http://example.org"),
                    @Parameter(name = "sponsor", description = "Sponsor of shortened URL"),
                    @Parameter(name = "qrcode", description = "Boolean that marks if qr code will be generated")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "URL was successfully created. OK."),
                    @ApiResponse(responseCode = "200", description = "URL already exists. OK."),
                    @ApiResponse(responseCode = "400", description = "Provided URL isn't following scheme http:// or " +
                            "https://. ERROR.")
            }
    )
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
                                              @RequestParam(value = "sponsor", required = false)
                                                      String sponsor,
                                              @RequestParam(value = "qrcode", required = false) boolean qrcode,
                                              HttpServletRequest request) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                "https"});
        if (urlValidator.isValid(url)) {
            ShortURL su = shortUrlService.create(url, sponsor, request.getRemoteAddr());
            su = shortUrlService.findByKey(su.getHash());
            if (su == null) {
                log.debug("No existe. Creando.");
                // ShortUrl NOT exists. Saving it.
                su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
                if (qrcode) {
                    // Generate QR code
                    String qrCode = qrCodeService.getQRCode(su);
                    su.setQrCode(qrCode);
                    shortUrlService.saveQrPath(su);
                }
                // validate url
                su = validateURL(url, su);
                System.err.println("En la creación" + new Gson().toJson(su));
                // Returns shortURL
                HttpHeaders h = new HttpHeaders();
                h.setLocation(su.getUri());
                // Update QRCode URI if needed
                if (qrcode) {
                    su = qrCodeService.updateQrURI(su);
                }
                return new ResponseEntity<>(su, h, HttpStatus.CREATED);
            } else {
                // ShortUrl exists. Return it.
                log.debug("ShortUrl exists. Returning.");
                ShortURL aux = shortUrlService.create(url, sponsor, request.getRemoteAddr());
                su = shortUrlService.findByKey(HashCalculator.calculateHash(url));
                su.setUri(aux.getUri());
                // Update QRCode URI if needed
                if (qrcode) {
                    // QR code required
                    if (su.getQrCode().isEmpty()) {
                        // Shorturl object exists but doesn't have qr code associated
                        // We have to generate QRCode and store it
                        String qrCodePath = qrCodeService.getQRCode(su);
                        su.setQrCode(qrCodePath);
                        shortUrlService.saveQrPath(su);
                    }
                    // Updating QRcode path uri
                    su = qrCodeService.updateQrURI(su);
                } else {
                    // Set QR code to empty string ("")
                    su = qrCodeService.noQrCode(su);
                }
                return new ResponseEntity<>(su, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Method that checks if url is safe
     *
     * @param url to check safety
     * @param su  shortUrl object to modify (if needed) safe.
     * @return [su] object with safe field updated.
     * [su.safe] will be true if HTTP GET over url returns 200.
     * Else will be false.
     */
    private ShortURL validateURL(String url, ShortURL su) {
        // TODO: Sends the shortURL to a message queue to validate
        // Source: https://www.baeldung.com/java-http-request
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
            log.debug("LA URL NO ES VALIDA");
            su = shortUrlService.markAs(su, false);
        }
        return su;
    }

    /**
     * Endpoint that returns QR code of shortUrl(hash)
     *
     * @param hash to obtain shortUrl QR code.
     * @return 200 and QR code in bytes iff shortUrl exists.
     * 404 iff shortUrl doesn't exists.
     * @throws IOException iff converting qr code image to byte array fails
     */
    @GetMapping(value = "/qr/{hash}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    @Operation(method = "GET", description = "Returns QR code image given hash of shortened URL")
    @Parameter(name = "hash", description = "Hash of the shortened URL", required = true, example = "eab67425")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "QR code is generated for hash provided. OK.",
                            content = @Content(mediaType = "image/png")
                    ),
                    @ApiResponse(responseCode = "404", description = "QR code not exists for hash. ERROR.")
            }
    )
    public ResponseEntity<byte[]> getQRCode(@PathVariable(name = "hash") String hash) throws IOException {
        ShortURL su = shortUrlService.findByKey(hash);
        log.debug(new Gson().toJson(su));
        if (su != null && !su.getQrCode().isEmpty()) {
            // QR exits
            return new ResponseEntity<>(qrCodeService.getQrByteArray(hash), HttpStatus.OK);
        } else {
            // QR not exists
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Method that returns ip address from a request
     *
     * @param request object to extract ip address
     * @return ip address from [request] object
     */
    private String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    /**
     * Method that returns ResponseEntity with Location header and status code updated.
     *
     * @param l shortUrl object to create response
     * @return ResponseEntity ResponseEntity with Location header and status code updated.
     */
    private ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(l.getTarget()));
        return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
    }

    /**
     * Method that read a CSV file "file" which contains URLs and create a
     * new CSV file that contains those original URLs shorted.
     *
     * @param file    CSV File which contains URLs separated with ;
     * @param sponsor sponsor
     * @param request request
     * @return created CSV file name
     */
    @PostMapping(value = "/uploadCSV")
    // TODO: Document with OpenAPI 3
    public ResponseEntity<byte[]> uploadCsv(@RequestParam(name = "file") MultipartFile file,
                                            @RequestParam(value = "sponsor", required = false)
                                                    String sponsor,
                                            HttpServletRequest request) {
        try {
            // Contenido del fichero
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.replaceAll("\r\n", "\n").split("\n");
            String filename = UUID.randomUUID() + ".csv";
            //CSV to return
            File f = new File("files/" + filename);
            PrintWriter pw = new PrintWriter(f);
            for (String s : lines) {
                String[] fields = s.split(CSV_SEPARATOR);
                for (String url : fields) {
                    UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                            "https"});
                    // Check if the URL is valid
                    if (urlValidator.isValid(url)) {
                        ShortURL su = shortUrlService.create(url, sponsor, request.getRemoteAddr());
                        su = shortUrlService.findByKey(su.getHash());
                        if (su == null) {
                            System.err.println("No existe. Creando.");
                            // ShortUrl DOES NOT exists. Saving it.
                            su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
                            // Check if url is safe
                            su = validateURL(url, su);
                        } else {
                            // ShortUrl exists. Return it.
                            System.err.println("Existe. Devolviendo.");
                            su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
                        }
                        // Write on the CSV file
                        pw.print(su.getUri() + ";");
                        System.err.println("Original URL: " + url);
                        System.err.println("Shorted URL: " + su.getUri());
                    } else {
                        // Url NOT valid
                        // Write "ERROR" on the CSV file
                        pw.print("ERROR;");
                        System.err.println("Invalid URL: " + url);
                    }

                }
                pw.print('\n');
                System.err.println("Full line: " + s);
            }
            // Close output stream
            pw.close();
            // Return CSV file name and Http Status
            return new ResponseEntity<>(filename.getBytes(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Invalid file format!!".getBytes(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint that returns a CSV file that contains shorted URLs
     *
     * @param filename CSV file name
     * @return Resource that represents the CSV file with shorted URLs
     * @throws IOException Throws an exception if FileInputStream doesn't find the CSV file
     */
    @RequestMapping(path = "/files/{filename}", method = RequestMethod.GET)
    // TODO: Document with OpenAPI 3
    public ResponseEntity<Resource> downloadCSV(@PathVariable(name = "filename") String filename) throws IOException {
        System.err.println("Comenzamos la descarga de " + filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream("files/" + filename));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("content-disposition", "attachment; filename=" + filename);
        responseHeaders.add("Content-Type", "text/csv");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}

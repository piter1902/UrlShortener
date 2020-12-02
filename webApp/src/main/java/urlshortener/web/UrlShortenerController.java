package urlshortener.web;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import urlshortener.domain.ShortURL;
import urlshortener.rabbitAdapters.Sender;
import urlshortener.service.*;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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

    //    private static final String CSV_SEPARATOR = ";";
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    private final QRCodeService qrCodeService;

    private Sender sender;

    @Autowired
    private CSVHelper csvHelper;

    /**
     * Public constructor
     *
     * @param shortUrlService short url service
     * @param clickService    click service
     * @param qrCodeService   qrcode service
     * @param sender          sender adapter to rabbitmq
     */
    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService,
                                  QRCodeService qrCodeService, Sender sender) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
        this.qrCodeService = qrCodeService;
        this.sender = sender;
    }

    /**
     * Endpoint to redirect client to shortUrl location
     *
     * @param id      of shortUrl (hash code)
     * @param request object
     * @return 307 and redirects to shortUrl(id).target iff shortUrl(id) exists and it's safe.
     * 400 and json object with error field iff shortUrl(id) not exists and shortUrl(id) isn't safe or shortUrl(id).validated is false.
     * 404 and json object with error field iff shortUrl(id) not exists.
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
                            responseCode = "400", description = "Destination URL unreachable or it hasn't been validated yet. ERROR.",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ error: \"Error messsage\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "ID not found. ERROR.",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ error: \"Error messsage\"}"))
                    )
            }
    )
    public ResponseEntity<?> redirectTo(@PathVariable String id,
                                        HttpServletRequest request) {

        //sender.send("Mensaje desde "+id);

        ShortURL l = shortUrlService.findByKey(id);
        if (l != null && l.getValidated() && l.getSafe()) {
            // 200
            clickService.saveClick(id, extractIP(request));
            return createSuccessfulRedirectToResponse(l);
        } else if (l != null && (!l.getSafe() || !l.getValidated())) {
            // 400
            // Creation of error message
            Map<String, String> jsonErrorMap = new HashMap<>();
            String errorValue = "URL de destino no validada todavía";
            if (!l.getSafe()) {
                // error is url unreachable
                errorValue = "URL de destino no alcanzable";
            }
            jsonErrorMap.put("error", errorValue);
            return new ResponseEntity<>(jsonErrorMap, HttpStatus.BAD_REQUEST);
        } else {
            // 404
            Map<String, String> jsonErrorMap = new HashMap<>();
            jsonErrorMap.put("error", String.format("El hash %s no existe", id));
            return new ResponseEntity<>(jsonErrorMap, HttpStatus.NOT_FOUND);
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
                validateURL(su);
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
     * Method that checks if url is safe (asynchronous)
     *
     * @param su shortUrl object to check safety.
     */
    private void validateURL(ShortURL su) {
        sender.send(su);
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
        } else if (su != null) {
            // QR not exists but there is a shortUrl su that su.hash = hash
            // Create QR code
            su.setQrCode(qrCodeService.getQRCode(su));
            shortUrlService.saveQrPath(su);
            return new ResponseEntity<>(qrCodeService.getQrByteArray(hash), HttpStatus.OK);
        } else {
            // QR not exists
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Method that creates a CSV file that contains shorted URLs
     *
     * @param file    CSV File which contains URLs separated with ;
     * @param request request
     * @return created CSV file name
     */
    @PostMapping(value = "/uploadCSV")
    @ResponseBody
    @Operation(method = "POST", description = "Reads a CSV file which contains URLs and create a new CSV file " +
            "that contains those original URLs shorted")
    @Parameter(name = "file", description = "CSV File which contains URLs separated with", required = true, example = "urls.csv")

    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Original file is correct and new CSV file is correctly created",
                            content = @Content(mediaType = "file/csv")
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file format")
            }
    )
    public ResponseEntity<?> uploadCsv(@RequestParam(name = "file") MultipartFile file,
                                       HttpServletRequest request) {
        if (csvHelper.hasCSVFormat(file)) {
            try {
                // Name of the shorted CSV file
                String filename = UUID.randomUUID() + ".csv";
                //CSV to return
                URI location = csvHelper.save(filename, file, request.getRemoteAddr());
                // Return CSV file name and Http Status
                return ResponseEntity.created(location).contentType(MediaType.parseMediaType("text/csv")).body(filename);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(new Gson().toJson("error: Invaild file format"), HttpStatus.BAD_REQUEST);
            }
        } else {
            System.err.println("ERROR: format " + file.getContentType());
            return new ResponseEntity<>(new Gson().toJson("error: Invaild file format"), HttpStatus.BAD_REQUEST);
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
     * Endpoint that returns a CSV file that contains shorted URLs
     *
     * @param filename CSV file name
     * @return Resource that represents the CSV file with shorted URLs
     * @throws IOException Throws an exception if FileInputStream doesn't find the CSV file
     */
    @RequestMapping(path = "/files/{filename}", method = RequestMethod.GET)
    @ResponseBody
    @Operation(method = "GET", description = "Returns a CSV file that contains shorted URLs")
    @Parameter(name = "filename", description = "Name of the CSV file that contains shorted URLs (created with /uploadCSV) ", required = true, example = "<UID>.csv")

    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "CSV file exists. Returns its content.",
                            content = @Content(mediaType = "file/csv")
                    ),
                    @ApiResponse(responseCode = "404", description = "File doesn't exist on the server")
            }
    )
    public ResponseEntity<Resource> downloadCSV(@PathVariable(name = "filename") String filename) throws IOException {
        System.err.println("Comenzamos la descarga de " + filename);
        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream("files/" + filename));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-disposition", "attachment; filename=" + filename);
            responseHeaders.add("Content-Type", "application/csv");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (IOException e) {
            return new ResponseEntity<Resource>((Resource) null, HttpStatus.NOT_FOUND);
        }
    }

}

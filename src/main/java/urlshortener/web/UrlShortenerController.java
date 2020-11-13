package urlshortener.web;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
import urlshortener.service.ShortURLService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
public class UrlShortenerController {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerController.class);

    public static final String CSV_SEPARATOR = ";";
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    /*@Autowired
    Sender sender;*/

    /**
     * Public constructor
     *
     * @param shortUrlService short url service
     * @param clickService    click service
     */
    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
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
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
                                              @RequestParam(value = "sponsor", required = false)
                                                      String sponsor,
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
                // Generate QR code
                String qrCode = null;
                try {
                    qrCode = generateQRCode(su, su.getUri());
                } catch (WriterException | IOException | NullPointerException e) {
                    e.printStackTrace();
                }
                su.setQrCode(qrCode);
                /*su =*/
                shortUrlService.saveQR(su);
                // validate url
                su = validateURL(url, su);
                System.err.println("En la creación" + new Gson().toJson(su));
                // Returns shortURL
                HttpHeaders h = new HttpHeaders();
                h.setLocation(su.getUri());
                updateQrURI(su);
                return new ResponseEntity<>(su, h, HttpStatus.CREATED);
            } else {
                // ShortUrl exists. Return it.
                log.debug("ShortUrl exists. Returning.");
                ShortURL aux = shortUrlService.create(url, sponsor, request.getRemoteAddr());
                su = shortUrlService.findByKey(HashCalculator.calculateHash(url));
                su.setUri(aux.getUri());
                updateQrURI(su);
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
     * Method that updates [su] object to create su.qrCode using su.uri(scheme, host, port)
     *
     * @param su object to update.
     */
    private void updateQrURI(ShortURL su) {
        // Add URI prefix to qrCode path
        URI uri = su.getUri();
        try {
            su.setQrCode(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(),
                    "/" + su.getQrCode(), null, null).toASCIIString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that returns QR code path for [su]. qr directory must exist in project's root.
     *
     * @param su shortUrl object to encode
     * @return base64 encoded string that contains [su] target QR code.
     * @throws WriterException iff QR encoder fails
     * @throws IOException     iff ByteArrayOutputStream fails
     */
    private String generateQRCode(ShortURL su, URI uri) throws WriterException, IOException {
        // Sources:
        //  https://www.baeldung.com/java-generating-barcodes-qr-codes
        //  https://stackoverflow.com/questions/7178937/java-bufferedimage-to-png-format-base64-string/25109418
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(su.getUri().toASCIIString(), BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        // Convert BufferedImage to PNG
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
//        String encoded = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        // Save QR to /qr directory
        String qrFilePath = "qr/" + su.getHash() + ".png";
        ImageIO.write(bufferedImage, "png", new File(qrFilePath));
//        URI baseUri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(),
//                "/" + qrFilePath, null, null);
//        String qrFileURI = baseUri.toASCIIString();
//        System.err.println(encoded);
//        return encoded;
        return qrFilePath;
    }

    /**
     * Endpoint that returns QR code of shortUrl(hash)
     *
     * @param hash to obtain shortUrl QR code.
     * @return 200 and QR code in bytes iff shortUrl exists.
     * 404 iff shortUrl doesn't exists.
     * @throws IOException iff ImageIO.write from QR code image to byteOutputStream fails.
     */
    @GetMapping(value = "/qr/{hash}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> getQRCode(@PathVariable(name = "hash") String hash) throws IOException {
        ShortURL su = shortUrlService.findByKey(hash);
        log.debug(new Gson().toJson(su));
        if (su != null) {
            // QR exits
            BufferedImage bufferedImage = ImageIO.read(new File("qr/" + hash + ".png"));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), HttpStatus.OK);
        } else {
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

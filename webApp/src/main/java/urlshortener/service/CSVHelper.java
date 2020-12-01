package urlshortener.service;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import urlshortener.rabbitAdapters.Sender;
import urlshortener.service.ShortURLService;
import urlshortener.domain.ShortURL;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVPrinter;
import urlshortener.web.UrlShortenerController;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Declares methods to manage csv files
 * Documentation source: https://bezkoder.com/spring-boot-upload-csv-file/
 *
 * @author Juan José Tambo
 */
@Service
public class CSVHelper {

    @Autowired
    private ShortURLService shortUrlService;
    @Autowired
    private Sender sender;

    // Set of valid file types
    public final Set<String> TYPES = new HashSet<String>() {{
        add("text/csv");
        add("application/vnd.ms-excel");
    }};

    private static final Logger log = LoggerFactory.getLogger(CSVHelper.class);

    /**
     * @param file uploaded file
     * @return Check if the file has the correct format (CSV)
     */
    public boolean hasCSVFormat(MultipartFile file) {
        return TYPES.contains(file.getContentType());
    }

    /**
     * Method that short a url and checks if it exists on the server (if not, saves the shorted url created)
     *
     * @param url        original url that must be shorted
     * @param remoteAddr addr of the HttpRequest
     * @return ShortURL object created
     */
    private ShortURL shortUrl(String url, String remoteAddr) {

        ShortURL su = shortUrlService.create(url, null, remoteAddr);
        su = shortUrlService.findByKey(su.getHash());
        if (su == null) {
            log.debug("No existe url. Creando ...");
            // ShortUrl DOES NOT exists. Saving it.
            su = shortUrlService.save(url, null, remoteAddr);
            // Check if url is safe
            validateURL(su);
        } else {
            // ShortUrl exists. Return it.
            log.debug("Existe url. Devolviendo.");
            su = shortUrlService.save(url, null, remoteAddr);
        }
        return su;
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
     * Creates and saves a CSV file which contains shorted URLs from the URLs contained on the file "filename"
     *
     * @param file       uploaded file
     * @param filename   name of the new file
     * @param remoteAddr addr of the HttpRequest
     * @return name of the created CSV file
     */
    public URI save(String filename, MultipartFile file, String remoteAddr) {
        try {
            boolean firstTime = true;
            URI location = null; // Element to return on the location
            // Stream to read the content from the original CSV file
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            // Parse original CSV file
            CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withDelimiter(';'));
            // Contains all records from the original CSV file
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            // CSVWriter to the shorted CSV file
            CSVPrinter printer = new CSVPrinter(new FileWriter("files/" + filename), CSVFormat.DEFAULT.withDelimiter(';'));
            // For each line
            for (CSVRecord csvRecord : csvRecords) {
                log.debug("CSV line content:" + csvRecords);
                Iterator<String> values = csvRecord.iterator();
                for (Iterator<String> it = values; it.hasNext(); ) {
                    String url = it.next();
                    UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
                    //Check if the url is valid
                    if (urlValidator.isValid(url)) {
                        // Shorts the url
                        ShortURL su = shortUrl(url, remoteAddr);
                        // Write on the CSV file
                        printer.printRecord(su.getUri());
                        // It's just important the first URL shorted
                        if (firstTime) {
                            location = su.getUri();
                            firstTime = false;
                        }
                        log.debug("Original URL: " + url);
                        log.debug("Shorted URL: " + su.getUri());
                    } else {
                        // Url NOT valid
                        // Write "ERROR" on the CSV file
                        printer.printRecord("ERROR: Invalid URL");
                        log.debug("Invalid URL: " + url);
                    }
                }
                printer.println();
            }
            // Close output stream
            printer.close();
            return location;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

}

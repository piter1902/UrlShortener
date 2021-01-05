package urlshortener.service;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import urlshortener.domain.ShortURL;
import urlshortener.fixtures.ShortURLFixture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CSVServiceTest {

    @Autowired
    CSVHelper csvHelper = new CSVHelper();

    static final String CLIENT_IP = "0.0.0.0.0.1";


    @Test
    public void testValidUrl() {
        String validUrl = "http://example.org/";
        ShortURL shortURL = ShortURLFixture.exampleOrgUrl();
        String response = csvHelper.shortUrlCsvFormat(validUrl, CLIENT_IP);
        String[] splittedResponse = response.split(",");
        Assertions.assertEquals(validUrl, splittedResponse[0], "The response was wrong");
        Assertions.assertEquals(shortURL.getUri().toString(), splittedResponse[1], "Shorted URLs are different");
    }

    @Test
    public void testInvalidUrl() {
        String invalidUrl = "htp://example.org/";
        String response = csvHelper.shortUrlCsvFormat(invalidUrl, CLIENT_IP);
        String[] splittedResponse = response.split(",");
        Assertions.assertEquals(invalidUrl, splittedResponse[0], "The response was wrong");
        Assertions.assertEquals("debe ser una URI http o https", splittedResponse[2], "The response was wrong");
    }

}

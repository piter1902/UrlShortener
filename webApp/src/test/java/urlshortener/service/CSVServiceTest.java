package urlshortener.service;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import urlshortener.domain.ShortURL;
import urlshortener.fixtures.ShortURLFixture;

import java.util.ArrayList;
import java.util.List;

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
        String response = csvHelper.shortUrlSlice(validUrl, CLIENT_IP).get(0);
        String[] splittedResponse = response.split(",");
        Assertions.assertEquals(validUrl, splittedResponse[0], "The response was wrong");
        Assertions.assertEquals(shortURL.getUri().toString(), splittedResponse[1], "Shorted URLs are different");
    }

    @Test
    public void testInvalidUrl() {
        String invalidUrl = "htp://example.org/";
        String response = csvHelper.shortUrlSlice(invalidUrl, CLIENT_IP).get(0);
        String[] splittedResponse = response.split(",");
        Assertions.assertEquals(invalidUrl, splittedResponse[0], "The response was wrong");
        Assertions.assertEquals("debe ser una URI http o https", splittedResponse[2], "The response was wrong");
    }

    @Test
    public void testMultipleUrl() {
        List<String> urlSlice = new ArrayList<>();
        urlSlice.add(ShortURLFixture.url1().getTarget());
        urlSlice.add(ShortURLFixture.url1modified().getTarget());
        urlSlice.add(ShortURLFixture.url3().getTarget());

        // Add manually the URIs
        List<String> response = csvHelper.shortUrlSlice( String.join(",",urlSlice), CLIENT_IP);
        //Check if all of the URLs have been sorted correctly and the response is correct
        Assertions.assertEquals("http://localhost/6bb9db44", response.get(0).split(",")[1], "The response was wrong");
        Assertions.assertEquals("http://localhost/e344fe11", response.get(1).split(",")[1], "The response was wrong");
        Assertions.assertEquals("http://localhost/dc112544", response.get(2).split(",")[1], "The response was wrong");


    }
}

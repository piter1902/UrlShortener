package UrlShortenerWorkers.service;

import UrlShortenerWorkers.domain.ShortURL;
import UrlShortenerWorkers.fixtures.ShortURLFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.net.MalformedURLException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class URLCheckerServiceTest {

    // How Mockito autowired works: https://dzone.com/articles/use-mockito-mock-autowired
    @InjectMocks    // Create an instance of test object and try to fill its (private) fields with Mocks and Spies
    private URLCheckerService urlCheckerService;
    @Mock
    private ShortURLService shortURLService;

    @BeforeEach
    private void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateURL_IfUrlIsValid() throws MalformedURLException {
        // Prepare to mock save
        when(shortURLService.markAs(any(ShortURL.class), eq(true))).then(
                (Answer<ShortURL>) su -> {
                    ShortURL resp = ShortURLFixture.exampleOrgUrl();
                    resp.setSafe(true);
                    return resp;
                }
        );

        ShortURL valid = ShortURLFixture.exampleOrgUrl();
        // Check function
        ShortURL response = urlCheckerService.validateURL(valid);
        // Assertions
        Assertions.assertEquals(true, response.getSafe(), "Response shortened URL is not safe");
        Assertions.assertEquals(valid.getTarget(), response.getTarget(), "Response target has been modified");
    }

    @Test
    void validateURL_IfUrlIsNotValid() {
        // Prepare to mock save
        when(shortURLService.markAs(any(ShortURL.class), eq(false))).then(
                (Answer<ShortURL>) su -> {
                    ShortURL resp = ShortURLFixture.badUrl();
                    resp.setSafe(false);
                    return resp;
                }
        );
        ShortURL notValid = ShortURLFixture.badUrl();
        // Check function (exception has to be thrown)
        Assertions.assertThrows(MalformedURLException.class, () -> {
            ShortURL response = urlCheckerService.validateURL(notValid);
            // Assertions
            Assertions.assertEquals(false, response.getSafe(), "Response shortened URL is safe");
            Assertions.assertEquals(notValid.getTarget(), response.getTarget(), "Response target has been modified");
        }, "No exception has been thrown by URL validation");
    }

    @Test
    void validateURL_IfURlTimesOut() throws MalformedURLException {
        // URL to target
        String target = "http://" + UUID.randomUUID().toString() + ".com";
        // Prepare to mock save
        when(shortURLService.markAs(any(ShortURL.class), eq(false))).then(
                (Answer<ShortURL>) su -> {
                    ShortURL resp = ShortURLFixture.badUrl();
                    resp.setSafe(false);
                    resp.setTarget(target);
                    return resp;
                }
        );
        ShortURL notValid = ShortURLFixture.badUrl();
        notValid.setTarget(target);
        ShortURL response = urlCheckerService.validateURL(notValid);
        // Assertions
        Assertions.assertNotEquals(true, response.getSafe(), "Response shortened URL is safe");
        Assertions.assertEquals(notValid.getTarget(), response.getTarget(), "Response target has been modified");
    }
}
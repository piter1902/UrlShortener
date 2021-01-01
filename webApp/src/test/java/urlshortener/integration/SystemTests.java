package urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import urlshortener.repository.ShortURLRepo;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class SystemTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ShortURLRepo shortURLRepo;

    @Before
    public void before() {
        shortURLRepo.deleteAll();
    }

    @Test
    public void testHome() {
        ResponseEntity<String> entity = restTemplate.getForEntity("/", String.class);
        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(entity.getHeaders().getContentType());
        assertTrue(
                entity.getHeaders().getContentType().isCompatibleWith(new MediaType("text", "html")));
        assertThat(entity.getBody(), containsString("<title>URL"));
    }

    @Test
    public void testCss() {
        ResponseEntity<String> entity =
                restTemplate.getForEntity("https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css", String.class);
        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        assertThat(entity.getHeaders().getContentType(), is(MediaType.valueOf("text/css;charset=utf-8")));
        assertThat(entity.getBody(), containsString("body"));
    }

    @Test
    public void testCreateLink() throws Exception {
        ResponseEntity<String> entity = postLink("http://example.org/");

        assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(entity.getHeaders().getLocation(),
                is(new URI("http://localhost:" + this.port + "/16a3e3e5")));
        assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json")));
        ReadContext rc = JsonPath.parse(entity.getBody());
        assertThat(rc.read("$.hash"), is("16a3e3e5"));
        assertThat(rc.read("$.uri"), is("http://localhost:" + this.port + "/16a3e3e5"));
        assertThat(rc.read("$.target"), is("http://example.org/"));
        assertThat(rc.read("$.sponsor"), is(nullValue()));
    }

    @Test
    public void testRedirectionWithoutRabbit() throws Exception {
        postLink("http://example.org/");
        // Not waiting validation because there are no workers -> Error 400
        ResponseEntity<String> entity = restTemplate.getForEntity("/16a3e3e5", String.class);
        assertThat(entity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        // No more checks. An error has ocurred (HTTP status code 400)
//        assertThat(entity.getHeaders().getLocation(), is(new URI("http://example.org/")));
    }

    @Test(timeout = 2000)
    @Ignore // TODO: Ignored until Workers can be launched for testing
    public void testRedirection() throws Exception {
        postLink("http://example.org/");
        // Wait for workers validation
        Thread.sleep(1000);
        ResponseEntity<String> entity = restTemplate.getForEntity("/16a3e3e5", String.class);
        assertThat(entity.getStatusCode(), is(HttpStatus.TEMPORARY_REDIRECT));
        assertThat(entity.getHeaders().getLocation(), is(new URI("http://example.org/")));
    }

    private ResponseEntity<String> postLink(String url) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("url", url);
        return restTemplate.postForEntity("/link", parts, String.class);
    }


}
package urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
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
            restTemplate.getForEntity("/webjars/bootstrap/3.3.5/css/bootstrap.min.css", String.class);
    assertThat(entity.getStatusCode(), is(HttpStatus.OK));
    assertThat(entity.getHeaders().getContentType(), is(MediaType.valueOf("text/css")));
    assertThat(entity.getBody(), containsString("body"));
  }

  // TODO: Check why is this tests failing in Travis-CI
  @Ignore
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
  public void testRedirection() throws Exception {
    postLink("http://example.org/");

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
package urlshortener.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.QRCodeService;
import urlshortener.service.ShortURLService;

import java.net.URI;
import java.sql.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static urlshortener.fixtures.ShortURLFixture.exampleOrgUrl;
import static urlshortener.fixtures.ShortURLFixture.someUrl;

public class UrlShortenerTests {

    private MockMvc mockMvc;

    @Mock
    private ClickService clickService;

    @Mock
    private ShortURLService shortUrlService;

    @InjectMocks
    private UrlShortenerController urlShortener;

    @Mock
    private QRCodeService qrCodeService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(urlShortener).build();
    }

    @Test
    public void thatRedirectToReturnsTemporaryRedirectIfKeyExists()
            throws Exception {
        when(shortUrlService.findByKey("someKey")).thenReturn(someUrl());

        mockMvc.perform(get("/{id}", "someKey")).andDo(print())
                .andExpect(status().isTemporaryRedirect())
                .andExpect(redirectedUrl("http://example.com/"));
    }

    @Test
    public void thatRedirecToReturnsNotFoundIdIfKeyDoesNotExist()
            throws Exception {
        when(shortUrlService.findByKey("someKey")).thenReturn(null);

        mockMvc.perform(get("/{id}", "someKey")).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void thatShortenerCreatesARedirectIfTheURLisOK() throws Exception {
        configureSave(null);
        // Configure shortUrlService to return a valid ShortURL
        when(shortUrlService.create(any(), any(), any())).then(
                (Answer<ShortURL>) invocation -> new ShortURL("16a3e3e5",
                        "http://example.org", new URI("http://localhost/16a3e3e5"), null,
                        new Date(System.currentTimeMillis()).toString(), null, 307, true,
                        null, null, null)
        );
        // Configure shortUrlService to return null (URL not exists)
        when(shortUrlService.findByKey("16a3e3e5")).then(
                (Answer<ShortURL>) invocation -> null
        );

        when(shortUrlService.markAs(any(), eq(true))).then(
                (Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSafe(true);
                    return su;
                }
        );

        when(qrCodeService.updateQrURI(any(ShortURL.class))).then(
                (Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSafe(true);
                    return su;
                }
        );

        mockMvc.perform(post("/link").param("url", "http://example.org/"))
                .andDo(print())
                .andExpect(redirectedUrl("http://localhost/16a3e3e5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hash", is("16a3e3e5")))
                .andExpect(jsonPath("$.uri", is("http://localhost/16a3e3e5")))
                .andExpect(jsonPath("$.target", is("http://example.org/")))
                .andExpect(jsonPath("$.sponsor", is(nullValue())));
    }

    @Test
    public void thatShortenerCreatesARedirectWithSponsor() throws Exception {
        configureSave("http://sponsor.com/");
        configureCreate("http://sponsor.com");

        when(shortUrlService.saveQrPath(any())).then(
                (Answer<ShortURL>) invocation -> new ShortURL()
        );

        when(shortUrlService.markAs(any(), eq(true))).then(
                (Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSponsor("http://sponsor.com/");
                    su.setSafe(true);
                    return su;
                }
        );

        when(qrCodeService.getQRCode(any(ShortURL.class))).thenReturn("");
        when(qrCodeService.updateQrURI(any(ShortURL.class))).then(
                (Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSponsor("http://sponsor.com/");
                    su.setSafe(true);
                    return su;
                }
        );

        mockMvc.perform(
                post("/link").param("url", "http://example.org/").param(
                        "sponsor", "http://sponsor.com/")).andDo(print())
                .andExpect(redirectedUrl("http://localhost/16a3e3e5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hash", is("16a3e3e5")))
                .andExpect(jsonPath("$.uri", is("http://localhost/16a3e3e5")))
                .andExpect(jsonPath("$.target", is("http://example.org/")))
                .andExpect(jsonPath("$.sponsor", is("http://sponsor.com/")));
    }

    @Test
    public void thatShortenerFailsIfTheURLisWrong() throws Exception {
        configureSave(null);
        configureCreate(null);

        mockMvc.perform(post("/link").param("url", "someKey")).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void thatShortenerFailsIfTheRepositoryReturnsNull() throws Exception {
        when(shortUrlService.save(any(String.class), any(String.class), any(String.class)))
                .thenReturn(null);

        mockMvc.perform(post("/link").param("url", "someKey")).andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void configureCreate(String sponsor) {
        when(shortUrlService.create(any(), any(), any()))
                .then((Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSponsor(sponsor);
                    return su;
                });
    }

    private void configureSave(String sponsor) {
        when(shortUrlService.save(any(), any(), any()))
                .then((Answer<ShortURL>) invocation -> {
                    ShortURL su = exampleOrgUrl();
                    su.setSponsor(sponsor);
                    return su;
                });
    }
}

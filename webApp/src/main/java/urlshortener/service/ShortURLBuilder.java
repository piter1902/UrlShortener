package urlshortener.service;

import org.springframework.http.HttpStatus;
import urlshortener.domain.ShortURL;

import java.net.URI;
import java.sql.Date;
import java.util.UUID;
import java.util.function.Function;

public class ShortURLBuilder {

    private String hash;
    private String target;
    private URI uri;
    private String sponsor;
    private String created;
    private String owner;
    private Integer mode;
    private Boolean safe;
    private Boolean validated;
    private String ip;
    private String country;
    private String qrCode;

    static ShortURLBuilder newInstance() {
        return new ShortURLBuilder();
    }

    ShortURL build() {
        return new ShortURL(
                hash,
                target,
                uri,
                sponsor,
                created,
                owner,
                mode,
                safe,
                validated,
                ip,
                country,
                qrCode
        );
    }

    ShortURLBuilder target(String url) {
        target = url;
        hash = HashCalculator.calculateHash(url);
        return this;
    }

    ShortURLBuilder sponsor(String sponsor) {
        this.sponsor = sponsor;
        return this;
    }

    ShortURLBuilder createdNow() {
        this.created = new Date(System.currentTimeMillis()).toString();
        return this;
    }

    ShortURLBuilder randomOwner() {
        this.owner = UUID.randomUUID().toString();
        return this;
    }

    ShortURLBuilder temporaryRedirect() {
        this.mode = HttpStatus.TEMPORARY_REDIRECT.value();
        return this;
    }

    ShortURLBuilder treatAsSafe() {
        this.safe = true;
        return this;
    }

    ShortURLBuilder notValidated() {
        this.validated = false;
        return this;
    }

    ShortURLBuilder ip(String ip) {
        this.ip = ip;
        return this;
    }

    ShortURLBuilder unknownCountry() {
        this.country = null;
        return this;
    }

    ShortURLBuilder uri(Function<String, URI> extractor) {
        this.uri = extractor.apply(hash);
        return this;
    }

    ShortURLBuilder emptyQrPath() {
        this.qrCode = "";
        return this;
    }
}

package UrlShortenerWorkers.domain;


import java.net.URI;

public class ShortUrlStatus {

    // Hash of ShortURL
    private String hash;

    // Safeness of target URL
    private boolean safe;

    // Status of validation process
    private boolean validated;

    // Shortened URL
    private URI uri;

    public ShortUrlStatus(String hash, boolean safe, boolean validated, URI uri) {
        this.hash = hash;
        this.safe = safe;
        this.validated = validated;
        this.uri = uri;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}

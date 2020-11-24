package UrlShortenerWorkers.domain;

import java.net.URI;

public class ShortUrlStatus {

    // Hash of ShortURL
    private String hash;

    // Safeness of target URL
    private boolean safe;

    // Shortened URL
    private URI uri;

    public ShortUrlStatus(String hash, boolean safe, URI uri) {
        this.hash = hash;
        this.safe = safe;
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

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}

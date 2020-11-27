package urlshortener.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;

public class ShortUrlStatus {

    // Hash of ShortURL
    @Schema(title = "Hash", name = "hash", description = "Hash of shortened URL", required = true, example = "eab67425")
    private String hash;

    // Safeness of target URL
    @Schema(title = "Safeness", name = "safe", description = "Target URL is reachable",
            required = true, example = "true", allowableValues = {"true", "false"})
    private boolean safe;

    // Shortened URL
    @Schema(title = "Shortened URL", name = "uri", description = "Generated shortened URL", required = true)
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

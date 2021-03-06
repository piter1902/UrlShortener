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

    // Status of validation process
    @Schema(title = "Validation Status", name = "validated", description = "Status of validation process",
            required = true, example = "true", allowableValues = {"true", "false"})
    private boolean validated;

    // Shortened URL
    @Schema(title = "Shortened URL", name = "uri", description = "Generated shortened URL", required = true)
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

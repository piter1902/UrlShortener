package urlshortener.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.net.URI;

@RedisHash("ShortUrl")
public class ShortURL /*implements Serializable*/ {

    @Id
    @Schema(name = "Hash", description = "Hash of shortened URL", required = true, example = "eab67425")
    private String hash;

    @Schema(name = "Target URL", description = "Target URL", required = true, example = "http://example.org/")
    private String target;

    @Schema(name = "Shortened URL", description = "Generated shortened URL", required = true)
    private URI uri;

    @Schema(name = "Sponsor", description = "Sponsor of shortened URL", required = true, example = "http://sponsor.com/")
    private String sponsor;

    @Schema(name = "Creation date", description = "Creation date in format yyyy-MM-dd", required = true, example = "2020-11-17")
    private String created;

    @Schema(name = "Owner", description = "Creator's UUID of shortened URL", required = true, example = "a676a4e5-c574-4699-9697-946d08217ec1")
    private String owner;

    @Schema(name = "Redirection mode", description = "Redirection mode. Always 307", required = true, example = "307", defaultValue = "307")
    private Integer mode;

    @Schema(name = "Safeness", description = "Target URL is reachable", required = true, example = "true", allowableValues = {"true", "false"})
    private Boolean safe;

    @Schema(name = "IP", description = "Request IP", required = true, example = "192.168.0.158")
    private String ip;

    @Schema(name = "Country", description = "Creator's country. Not in use.", required = true, defaultValue = "null")
    private String country;

    // QR code
    @Schema(name = "QR Code", description = "QR Code URL", required = true)
    private String qrCode;

    public ShortURL(String hash, String target, URI uri, String sponsor,
                    String created, String owner, Integer mode, Boolean safe, String ip,
                    String country, String qrCode) {
        this.hash = hash;
        this.target = target;
        this.uri = uri;
        this.sponsor = sponsor;
        this.created = created;
        this.owner = owner;
        this.mode = mode;
        this.safe = safe;
        this.ip = ip;
        this.country = country;
        this.qrCode = qrCode;
    }

    public ShortURL() {
    }

    public String getHash() {
        return hash;
    }

    public String getTarget() {
        return target;
    }

    public URI getUri() {
        return uri;
    }

    public String getCreated() {
        return created;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getMode() {
        return mode;
    }

    public String getSponsor() {
        return sponsor;
    }

    public Boolean getSafe() {
        return safe;
    }

    public String getIP() {
        return ip;
    }

    public String getCountry() {
        return country;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setUri(java.net.URI uri) {
        this.uri = uri;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public void setSafe(Boolean safe) {
        this.safe = safe;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}

package common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.net.URI;

@RedisHash("ShortUrl")
public class ShortURL /*implements Serializable*/ {

    @Id
    @Schema(title = "Hash", name = "hash", description = "Hash of shortened URL", required = true, example = "eab67425")
    private String hash;

    @Schema(title = "Target URL", name = "target",
            description = "Target URL", required = true, example = "http://example.org/")
    private String target;

    @Schema(title = "Shortened URL", name = "uri", description = "Generated shortened URL", required = true)
    private URI uri;

    @Schema(title = "Sponsor", name = "sponsor",
            description = "Sponsor of shortened URL", required = true, example = "http://sponsor.com/")
    private String sponsor;

    @Schema(title = "Creation date", name = "created",
            description = "Creation date in format yyyy-MM-dd", required = true, example = "2020-11-17")
    private String created;

    @Schema(title = "Owner", name = "owner",
            description = "Creator's UUID of shortened URL", required = true, example = "a676a4e5-c574-4699-9697-946d08217ec1")
    private String owner;

    @Schema(title = "Redirection mode", name = "mode",
            description = "Redirection mode. Always 307", required = true, example = "307", defaultValue = "307")
    private Integer mode;

    @Schema(title = "Safeness", name = "safe",
            description = "Target URL is reachable", required = true, example = "true", allowableValues = {"true", "false"})
    private Boolean safe;

    // Status of validation process
    @Schema(title = "Validation Status", name = "validated", description = "Status of validation process",
            required = true, example = "true", allowableValues = {"true", "false"})
    private Boolean validated;

    @Schema(title = "IP", name = "ip", description = "Request IP", required = true, example = "192.168.0.158")
    private String ip;

    @Schema(title = "Country", name = "country",
            description = "Creator's country. Not in use.", required = true, defaultValue = "null")
    private String country;

    // QR code
    @Schema(title = "QR Code", name = "qrCode", description = "QR Code URL", required = true)
    private String qrCode;

    public ShortURL(String hash, String target, URI uri, String sponsor,
                    String created, String owner, Integer mode, Boolean safe, Boolean validated, String ip,
                    String country, String qrCode) {
        this.hash = hash;
        this.target = target;
        this.uri = uri;
        this.sponsor = sponsor;
        this.created = created;
        this.owner = owner;
        this.mode = mode;
        this.safe = safe;
        this.validated = validated;
        this.ip = ip;
        this.country = country;
        this.qrCode = qrCode;
    }

    public ShortURL() {
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(java.net.URI uri) {
        this.uri = uri;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public Boolean getSafe() {
        return safe;
    }

    public void setSafe(Boolean safe) {
        this.safe = safe;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public String getIP() {
        return ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}

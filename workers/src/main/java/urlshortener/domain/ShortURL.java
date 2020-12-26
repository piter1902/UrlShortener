package urlshortener.domain;

//import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.net.URI;

@RedisHash("ShortUrl")
public class ShortURL /*implements Serializable*/ {

    @Id
    private String hash;

    private String target;

    private URI uri;

    private String sponsor;

    private String created;

    private String owner;

    private Integer mode;

    private Boolean safe;

    // Status of validation process
    private Boolean validated;

    private String ip;

    private String country;

    // QR code
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

    public void setUri(URI uri) {
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

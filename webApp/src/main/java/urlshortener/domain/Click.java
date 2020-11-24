package urlshortener.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("Click")
public class Click {

    @Id
    private Long id;
    private String hash;
    private String created;
    private String referrer;
    private String browser;
    private String platform;
    private String ip;
    private String country;

    public Click(Long id, String hash, String created, String referrer,
                 String browser, String platform, String ip, String country) {
        this.id = id;
        this.hash = hash;
        this.created = created;
        this.referrer = referrer;
        this.browser = browser;
        this.platform = platform;
        this.ip = ip;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

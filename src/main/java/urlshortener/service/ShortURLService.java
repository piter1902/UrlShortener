package urlshortener.service;

import org.springframework.stereotype.Service;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepository;
import urlshortener.web.UrlShortenerController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ShortURLService {

  private final ShortURLRepository shortURLRepository;

  /**
   * Public constructor
   *
   * @param shortURLRepository shortUrlRepository
   */
  public ShortURLService(ShortURLRepository shortURLRepository) {
    this.shortURLRepository = shortURLRepository;
  }

  /**
   * Method that finds the shortUrl object with hash = [id]
   *
   * @param id hash to find
   * @return ShortUrl object with hash = [id]
   */
  public ShortURL findByKey(String id) {
    return shortURLRepository.findByKey(id);
  }

  /**
   * Method that returns ShortUrl object with [url], [sponsor], [ip]
   *
   * @param url     uri to short
   * @param sponsor sponsor
   * @param ip      ip
   * @return ShortUrl object with specified data.
   */
  public ShortURL create(String url, String sponsor, String ip) {
    return ShortURLBuilder.newInstance()
            .target(url)
            .uri((String hash) -> linkTo(methodOn(UrlShortenerController.class).redirectTo(hash, null))
                    .toUri())
            .sponsor(sponsor)
            .createdNow()
            .randomOwner()
            .temporaryRedirect()
            .treatAsSafe()
            .ip(ip)
            .unknownCountry()
            .build();
  }

  /**
   * Method that stores shortUrl composed with [url], [sponsor], [ip]
   *
   * @param url     uri to short
   * @param sponsor sponsor
   * @param ip      ip
   * @return ShortUrl object stored
   */
  public ShortURL save(String url, String sponsor, String ip) {
    ShortURL su = create(url, sponsor, ip);
    return shortURLRepository.save(su);
  }

  /**
   * Method that saves QR code path
   *
   * @param su to store changes
   * @return [su] object
   */
  public ShortURL saveQR(ShortURL su) {
    shortURLRepository.update(su);
    return su;
  }

  /**
   * Method that marks and stores shortUrl object with safeness = [mark]
   *
   * @param su   object to update
   * @param mark safeness
   * @return [su] object updated with [su.safebness] = [mark]
   */
  public ShortURL markAs(ShortURL su, boolean mark) {
    return shortURLRepository.mark(su, mark);
  }
}

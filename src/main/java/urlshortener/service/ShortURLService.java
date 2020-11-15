package urlshortener.service;

import org.springframework.stereotype.Service;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepo;
import urlshortener.web.UrlShortenerController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ShortURLService {

  //private final ShortURLRepository shortURLRepository;
  private final ShortURLRepo shortURLRepo;

  /**
   * Public constructor
   *
   * @param shortURLRepo shortUrlRepository
   */
  /*public ShortURLService(ShortURLRepository shortURLRepository) {
    this.shortURLRepository = shortURLRepository;
  }*/
  public ShortURLService(ShortURLRepo shortURLRepo) {
    this.shortURLRepo = shortURLRepo;
  }


  /**
   * Method that finds the shortUrl object with hash = [id]
   *
   * @param id hash to find
   * @return ShortUrl object with hash = [id] or {@literal null} if not exists
   */
  public ShortURL findByKey(String id) {
    Optional<ShortURL> ret = shortURLRepo.findById(id);
    return ret.orElse(null);
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
            .emptyQrPath()
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
    return shortURLRepo.save(su);
  }

  /**
   * Method that saves QR code path. [su] object has to have QrCode not empty
   *
   * @param su to store changes
   * @return [su] object
   */
  public ShortURL saveQrPath(ShortURL su) {
    /*shortURLRepo.update(su);
    return su;*/
    return shortURLRepo.save(separateQrPath(su));
  }

  /**
   * Method that marks and stores shortUrl object with safeness = [mark]
   *
   * @param su   object to update
   * @param mark safeness
   * @return [su] object updated with [su.safebness] = [mark]
   */
  public ShortURL markAs(ShortURL su, boolean mark) {
//    return shortURLRepository.mark(su, mark);
    su.setSafe(mark);
    if (!su.getQrCode().isEmpty()) {
      separateQrPath(su);
    }
    return shortURLRepo.save(su);
  }

  private ShortURL separateQrPath(ShortURL su) {
    try {
      URI qrUri = new URI(su.getQrCode());
      System.err.println("QR URI = " + qrUri.getPath());
      su.setQrCode(qrUri.getPath());
    } catch (URISyntaxException e) {
      // It's not a URI
    }
    return su;
  }
}

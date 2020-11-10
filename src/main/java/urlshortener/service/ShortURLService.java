package urlshortener.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.stereotype.Service;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepository;
import urlshortener.web.UrlShortenerController;

@Service
public class ShortURLService {

  private final ShortURLRepository shortURLRepository;

  public ShortURLService(ShortURLRepository shortURLRepository) {
    this.shortURLRepository = shortURLRepository;
  }

  public ShortURL findByKey(String id) {
    return shortURLRepository.findByKey(id);
  }

  public ShortURL save(String url, String sponsor, String ip) {
    ShortURL su = create(url, sponsor, ip);
    return shortURLRepository.save(su);
  }

  public ShortURL saveQR(ShortURL su) {
    shortURLRepository.update(su);
    return su;
  }

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

  public ShortURL markAs(ShortURL su, boolean mark) {
//    ShortURL ret = shortURLRepository.mark(su, mark);
//    BeanUtils.copyProperties(su, ret);
//    new DirectFieldAccessor(ret).setPropertyValue("safe", mark);
//    new DirectFieldAccessor(ret).setPropertyValue("uri", su.getUri());
//    new DirectFieldAccessor(ret).setPropertyValue("hash", su.getHash());
//    new DirectFieldAccessor(ret).setPropertyValue("target", su.getTarget());
//    new DirectFieldAccessor(ret).setPropertyValue("sponsor", su.getSponsor());
//    return ret;
    return shortURLRepository.mark(su, mark);
  }
}

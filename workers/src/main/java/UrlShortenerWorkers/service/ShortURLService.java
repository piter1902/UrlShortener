package UrlShortenerWorkers.service;

import common.domain.ShortURL;
import common.repository.ShortURLRepo;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

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

    /**
     * Method that marks shortUrl object with validated = true
     *
     * @param su object to update
     * @return [su] object with validated field updated
     */
    public ShortURL markAsValidated(ShortURL su) {
        su.setValidated(true);
        return shortURLRepo.save(su);
    }
}
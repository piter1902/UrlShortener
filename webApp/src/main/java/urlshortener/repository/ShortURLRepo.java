package urlshortener.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import urlshortener.domain.ShortURL;

@Repository
public interface ShortURLRepo extends CrudRepository<ShortURL, String> {
}

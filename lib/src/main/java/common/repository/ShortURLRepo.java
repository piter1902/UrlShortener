package common.repository;

import common.domain.ShortURL;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortURLRepo extends CrudRepository<ShortURL, String> {
}


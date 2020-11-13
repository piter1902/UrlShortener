package urlshortener.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import urlshortener.domain.Click;

@Repository
public interface ClickRepo extends CrudRepository<Click, Long> {
}

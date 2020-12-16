package urlshortener.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import urlshortener.domain.Click;

@Component
public interface ClickRepo extends CrudRepository<Click, Long> {
}

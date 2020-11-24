package urlshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import urlshortener.domain.Click;
import urlshortener.repository.ClickRepo;

@Service
public class ClickService {

  private static final Logger log = LoggerFactory
          .getLogger(ClickService.class);

  private final ClickRepo clickRepo;

  public ClickService(ClickRepo clickRepo) {
    this.clickRepo = clickRepo;
  }

  public void saveClick(String hash, String ip) {
    Click cl = ClickBuilder.newInstance().hash(hash).createdNow().ip(ip).build();
    cl = clickRepo.save(cl);
    log.info("[" + hash + "] saved with id [" + cl.getId() + "]");
  }

}

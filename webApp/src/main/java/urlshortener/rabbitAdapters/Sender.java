package urlshortener.rabbitAdapters;

import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import urlshortener.config.RabbitConfig;
import urlshortener.domain.ShortURL;

public class Sender {

    // To isolate exchange name
    private static final String EXCHANGE_NAME = "url-safeness-verificartor";

    @Autowired
    private Gson gson;

    @Autowired
    private RabbitTemplate template;

    public void send(ShortURL toVerify) {
        String jsonToVerify = gson.toJson(toVerify);
        this.template.convertAndSend(EXCHANGE_NAME, "", jsonToVerify);
        System.out.println(" [x] Sent '" + jsonToVerify + "'");
    }
}

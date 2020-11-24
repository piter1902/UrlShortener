package urlshortener.messagingrabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class Sender {

    @Autowired
    private RabbitTemplate template;

    public void send(String msg) {
        this.template.convertAndSend(RabbitConfig.EXCHANGE_NAME, "", msg);
        System.out.println(" [x] Sent '" + msg + "'");
    }
}

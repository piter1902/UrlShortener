package urlshortener.messagingrabbitmq;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "exchange_name";

//    @Bean
//    DirectExchange exchange() {
//        return new DirectExchange(EXCHANGE_NAME);
//    }

    @Bean
    Sender sender() {
        return new Sender();
    }

}

package urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import urlshortener.rabbitAdapters.Sender;

@Configuration
public class ConfigRabbit {

    @Bean
    Sender sender() {
        return new Sender();
    }

}

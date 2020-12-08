package urlshortener.config;

import common.rabbit.Sender;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class ConfigRabbit {

    @Bean
    Sender sender() {
        return new Sender();
    }

}
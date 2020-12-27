package UrlShortenerWorkers.Config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public Gson gsonSerializer() {
        return new Gson();
    }
}
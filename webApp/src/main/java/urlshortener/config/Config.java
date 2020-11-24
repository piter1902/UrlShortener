package urlshortener.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;

public class Config {

    @Bean
    public Gson gson() {
        return new Gson();
    }
}

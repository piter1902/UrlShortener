package urlshortener.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

public class Config {

    @Bean
    public Gson gson() {
        return new Gson();
    }

    // Thread Pool for executing async tasks (create QR code)
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("QRcode-");
        executor.initialize();
        return executor;
    }
}

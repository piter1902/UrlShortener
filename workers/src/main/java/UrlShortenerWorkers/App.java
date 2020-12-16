package UrlShortenerWorkers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "common"})
@EntityScan({"common"})
public class App extends SpringBootServletInitializer /*implements CommandLineRunner*/ {

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @Autowired
//    Sender sender;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }

//    @Override
//    public void run(String... args) throws InterruptedException {
//        sender.send("mensaje desde application");
//    }
}
package workers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import workers.rabbitmq.Sender;

@SpringBootApplication
public class App extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Sender sender;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        sender.send("mensaje desde application");
    }
}
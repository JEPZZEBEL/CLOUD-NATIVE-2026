package cl.duoc.tickets.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Value("${app.queues.ok}")
    private String okQueue;

    @Value("${app.queues.error}")
    private String errorQueue;

    @Bean
    public Queue okQueueBean() {
        return new Queue(okQueue, true);
    }

    @Bean
    public Queue errorQueueBean() {
        return new Queue(errorQueue, true);
    }
}

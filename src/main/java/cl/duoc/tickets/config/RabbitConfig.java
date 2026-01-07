package cl.duoc.tickets.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.exchange:tickets.ex}")
    private String exchangeName;

    @Value("${app.queues.ok}")
    private String okQueueName;

    @Value("${app.queues.error}")
    private String errorQueueName;

    @Value("${app.routing.ok:ok}")
    private String okRoutingKey;

    @Value("${app.routing.error:error}")
    private String errorRoutingKey;

    // Exchange
    @Bean
    public DirectExchange ticketsExchange() {
        return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    // Cola ERROR (DLQ)
    @Bean(name = "errorQueueBean")
    public Queue errorQueueBean() {
        return QueueBuilder.durable(errorQueueName).build();
    }

    // Cola OK con dead-letter hacia ERROR
    @Bean(name = "okQueueBean")
    public Queue okQueueBean() {
        return QueueBuilder.durable(okQueueName)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", errorRoutingKey)
                .build();
    }

    // Bindings
    @Bean
    public Binding okBinding(@Qualifier("okQueueBean") Queue okQueue, DirectExchange ticketsExchange) {
        return BindingBuilder.bind(okQueue).to(ticketsExchange).with(okRoutingKey);
    }

    @Bean
    public Binding errorBinding(@Qualifier("errorQueueBean") Queue errorQueue, DirectExchange ticketsExchange) {
        return BindingBuilder.bind(errorQueue).to(ticketsExchange).with(errorRoutingKey);
    }

    // Declara todo en el broker
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    // Fuerza conexión para que aparezca en la UI
    @Bean
    public ApplicationRunner forceRabbitConnection(RabbitTemplate rabbitTemplate) {
        return args -> rabbitTemplate.execute(channel -> null);
    }
}

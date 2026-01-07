package cl.duoc.tickets.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueConsumer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.queues.ok}")
    private String okQueue;

    public QueueConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Object consumeOneFromOk() {
        return rabbitTemplate.receiveAndConvert(okQueue);
    }
}

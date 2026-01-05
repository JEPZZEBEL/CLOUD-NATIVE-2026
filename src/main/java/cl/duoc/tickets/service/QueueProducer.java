package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String okQueue;
    private final String errorQueue;

    public QueueProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.queues.ok}") String okQueue,
            @Value("${app.queues.error}") String errorQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.okQueue = okQueue;
        this.errorQueue = errorQueue;
    }

    public void sendOkOrFallback(TicketMessage message) {
        try {
            rabbitTemplate.convertAndSend(okQueue, message);
        } catch (Exception ex) {
            rabbitTemplate.convertAndSend(errorQueue, message);
        }
    }
}

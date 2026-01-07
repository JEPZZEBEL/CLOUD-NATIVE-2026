package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.exchange:tickets.ex}")
    private String exchange;

    @Value("${app.routing.ok:ok}")
    private String okRoutingKey;

    @Value("${app.routing.error:error}")
    private String errorRoutingKey;

    public QueueProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOk(TicketMessage msg) {
        rabbitTemplate.convertAndSend(exchange, okRoutingKey, msg);
    }

    public void sendError(TicketMessage msg) {
        rabbitTemplate.convertAndSend(exchange, errorRoutingKey, msg);
    }
}

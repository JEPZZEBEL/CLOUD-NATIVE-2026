package cl.duoc.tickets.service;

import cl.duoc.tickets.config.AppQueuesProps;
import cl.duoc.tickets.dto.queue.TicketQueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketQueueProducer {

    private final RabbitTemplate rabbitTemplate;
    private final AppQueuesProps queues;

    public void sendOk(TicketQueueMessage msg) {
        rabbitTemplate.convertAndSend(queues.getOk(), msg);
    }

    public void sendError(TicketQueueMessage msg) {
        rabbitTemplate.convertAndSend(queues.getError(), msg);
    }
}

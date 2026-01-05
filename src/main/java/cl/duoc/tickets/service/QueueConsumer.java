package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class QueueConsumer {

    private final OracleTicketLogService oracleTicketLogService;

    public QueueConsumer(OracleTicketLogService oracleTicketLogService) {
        this.oracleTicketLogService = oracleTicketLogService;
    }

    @RabbitListener(queues = "${app.queues.ok}")
    public void consumeOkQueue(TicketMessage message) {
        oracleTicketLogService.saveFromQueue(message);
    }
}

package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
import cl.duoc.tickets.entity.TicketQueueLog;
import cl.duoc.tickets.repository.TicketQueueLogRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketQueueConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final TicketQueueLogRepository logRepository;

    @Value("${app.queues.ok}")
    private String okQueue;

    @Value("${app.queues.error}")
    private String errorQueue;

    public TicketQueueConsumer(RabbitTemplate rabbitTemplate,
                               TicketQueueLogRepository logRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.logRepository = logRepository;
    }

    /**
     * Consume 1 mensaje desde la cola OK y lo guarda en ticket_queue_log.
     * Si falla el procesamiento/guardado -> reenvía el mensaje a la cola ERROR.
     *
     * Retorna:
     * - TicketQueueLog guardado si OK
     * - null si cola vacía o si se envió a ERROR
     */
    public TicketQueueLog consumeOneFromOkAndSave() {

        Object obj = rabbitTemplate.receiveAndConvert(okQueue);

        // cola vacía
        if (obj == null) {
            return null;
        }

        TicketMessage msg;

        // Si no se puede castear, manda lo que llegó a error y corta
        try {
            msg = (TicketMessage) obj;
        } catch (Exception castEx) {
            rabbitTemplate.convertAndSend(errorQueue, obj);
            return null;
        }

        try {
            // Crear log
            TicketQueueLog log = new TicketQueueLog();
            log.setTicketId(msg.getTicketId());
            log.setEventoId(msg.getEventoId());
            log.setUsuario(msg.getUsuario());
            log.setCantidad(msg.getCantidad());
            log.setCreatedAt(msg.getCreatedAt());

            // Guardar log
            return logRepository.save(log);

        } catch (Exception ex) {
            // Si falla guardar/procesar -> manda el mismo mensaje a la cola ERROR
            try {
                rabbitTemplate.convertAndSend(errorQueue, msg);
            } catch (Exception ignored) {
                // no rompas la app si Rabbit se cae en este punto
            }
            return null;
        }
    }
}

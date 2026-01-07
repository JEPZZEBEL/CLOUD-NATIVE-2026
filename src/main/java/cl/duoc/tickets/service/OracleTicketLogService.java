package cl.duoc.tickets.service;

import cl.duoc.tickets.entity.TicketQueueLog;
import cl.duoc.tickets.repository.TicketQueueLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OracleTicketLogService {

    private final TicketQueueLogRepository repository;

    public OracleTicketLogService(TicketQueueLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Guarda un registro proveniente de RabbitMQ.
     * Esta implementación NO usa builder ni constructor con parámetros
     * para evitar errores de compilación.
     */
    public TicketQueueLog guardarLog(
            String ticketId,
            Long eventoId,
            String usuario,
            Integer cantidad,
            Instant createdAt
    ) {

        TicketQueueLog log = new TicketQueueLog();
        log.setTicketId(ticketId);
        log.setEventoId(eventoId);
        log.setUsuario(usuario);
        log.setCantidad(cantidad);
        log.setCreatedAt(createdAt);

        return repository.save(log);
    }
}

package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
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

    public TicketQueueLog saveFromQueue(TicketMessage msg) {
        Instant createdAt = (msg.getCreatedAt() != null) ? msg.getCreatedAt() : Instant.now();

        TicketQueueLog log = new TicketQueueLog(
                msg.getTicketId(),
                msg.getEventoId(),
                msg.getUsuario(),
                msg.getCantidad(),
                createdAt
        );

        return repository.save(log);
    }
}

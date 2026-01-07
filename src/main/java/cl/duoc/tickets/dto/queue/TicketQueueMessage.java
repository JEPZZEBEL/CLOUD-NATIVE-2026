package cl.duoc.tickets.dto.queue;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TicketQueueMessage {
    private String ticketId;
    private Long eventoId;
    private String usuario;
    private Integer cantidad;
    private Instant createdAt;

    // Para cola error
    private String error;
}

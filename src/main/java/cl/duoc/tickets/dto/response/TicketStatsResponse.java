package cl.duoc.tickets.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketStatsResponse {
    private Long eventoId;
    private long totalTickets;
    private long totalReservas;
    private long totalVentas;
}

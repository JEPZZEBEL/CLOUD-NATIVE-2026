package cl.duoc.tickets.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketResponse {
    private String ticketId;
    private String efsPath;
    private String estado;
}

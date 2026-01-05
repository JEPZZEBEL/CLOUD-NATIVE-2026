package cl.duoc.tickets.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketResponse {

    private String ticketId;

    // Requeridos por la actividad
    private Long eventoId;
    private String usuario;

    // Estado del ticket (GENERADO, SUBIDO, RESERVADO, VENDIDO)
    private String estado;

    // Persistencia de archivos
    private String efsPath;
    private String s3Key;
}

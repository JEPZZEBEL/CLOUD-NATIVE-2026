package cl.duoc.tickets.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EstadisticasResponse {
    private Long eventoId;
    private long ticketsGenerados;
    private long ticketsSubidos;
    private long reservasTotales;
    private long ventasTotales;
    private long ingresos;
}

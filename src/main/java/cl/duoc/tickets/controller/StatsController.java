package cl.duoc.tickets.controller;

import cl.duoc.tickets.dto.response.EstadisticasResponse;
import cl.duoc.tickets.service.StatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/estadisticas")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // 7) Estadísticas de ventas y reservas por evento
    @GetMapping("/evento/{eventoId}")
    public EstadisticasResponse estadisticasPorEvento(@PathVariable Long eventoId) {
        return statsService.estadisticasPorEvento(eventoId);
    }
}

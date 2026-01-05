package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.response.EstadisticasResponse;
import cl.duoc.tickets.entity.Reserva;
import cl.duoc.tickets.repository.ReservaRepository;
import cl.duoc.tickets.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final TicketRepository ticketRepo;
    private final ReservaRepository reservaRepo;

    public StatsService(TicketRepository ticketRepo, ReservaRepository reservaRepo) {
        this.ticketRepo = ticketRepo;
        this.reservaRepo = reservaRepo;
    }

    public EstadisticasResponse estadisticasPorEvento(Long eventoId) {

        // 1) Tickets (sin traer listas completas)
        long ticketsGenerados = ticketRepo.countByEventoId(eventoId);
        long ticketsSubidos = ticketRepo.countByEventoIdAndEstado(eventoId, "SUBIDO");

        // 2) Reservas
        List<Reserva> reservas = reservaRepo.findByEventoId(eventoId);

        long reservasTotales = reservas.size();

        // Regla del profe / tuya: "SUBIDO" = pagado (venta)
        long ventasTotales = ticketsSubidos;

        // 3) Ingresos (suma precio_total de reservas, tolerante a null)
        long ingresos = reservas.stream()
                .mapToLong(r -> r.getPrecioTotal() == null ? 0L : r.getPrecioTotal())
                .sum();

        return EstadisticasResponse.builder()
                .eventoId(eventoId)
                .ticketsGenerados(ticketsGenerados)
                .ticketsSubidos(ticketsSubidos)
                .reservasTotales(reservasTotales)
                .ventasTotales(ventasTotales)
                .ingresos(ingresos)
                .build();
    }
}

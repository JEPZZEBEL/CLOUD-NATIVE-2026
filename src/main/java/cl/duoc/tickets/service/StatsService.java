package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.response.EstadisticasResponse;
import cl.duoc.tickets.entity.Reserva;
import cl.duoc.tickets.entity.Ticket;
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
        List<Ticket> tickets = ticketRepo.findByEventoId(eventoId);
        List<Reserva> reservas = reservaRepo.findByEventoId(eventoId);

        long ticketsGenerados = tickets.size();
        long ticketsSubidos = tickets.stream().filter(t -> "SUBIDO".equalsIgnoreCase(t.getEstado())).count();

        long reservasTotales = reservas.size();
        long ventasTotales = ticketsSubidos; // asumiendo SUBIDO = pagado

        long ingresos = reservas.stream().mapToLong(r -> r.getPrecioTotal() == null ? 0L : r.getPrecioTotal()).sum();

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

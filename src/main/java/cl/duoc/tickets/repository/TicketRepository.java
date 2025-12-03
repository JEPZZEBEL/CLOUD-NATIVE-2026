package cl.duoc.tickets.repository;

import cl.duoc.tickets.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findByEventoId(Long eventoId);
    List<Ticket> findByUsuario(String usuario);
}

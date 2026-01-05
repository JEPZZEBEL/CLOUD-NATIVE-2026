package cl.duoc.tickets.repository;

import cl.duoc.tickets.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Para estadísticas por evento
    List<Reserva> findByEventoId(Long eventoId);

}

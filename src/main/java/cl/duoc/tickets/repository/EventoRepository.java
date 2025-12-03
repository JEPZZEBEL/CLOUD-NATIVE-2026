package cl.duoc.tickets.repository;

import cl.duoc.tickets.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {}

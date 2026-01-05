package cl.duoc.tickets.repository;

import cl.duoc.tickets.entity.TicketQueueLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketQueueLogRepository extends JpaRepository<TicketQueueLog, Long> {
}

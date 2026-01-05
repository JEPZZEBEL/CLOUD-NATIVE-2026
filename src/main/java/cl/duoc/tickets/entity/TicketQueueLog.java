package cl.duoc.tickets.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "TICKET_QUEUE_LOG")
public class TicketQueueLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "evento_id")
    private Long eventoId;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "created_at")
    private Instant createdAt;

    public TicketQueueLog() { }

    public TicketQueueLog(String ticketId, Long eventoId, String usuario, Integer cantidad, Instant createdAt) {
        this.ticketId = ticketId;
        this.eventoId = eventoId;
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

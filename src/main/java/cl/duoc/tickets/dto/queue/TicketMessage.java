package cl.duoc.tickets.dto.queue;

import java.time.Instant;

public class TicketMessage {

    private String ticketId;
    private Long eventoId;
    private String usuario;
    private Integer cantidad;
    private Instant createdAt;

    public TicketMessage() { }

    public TicketMessage(String ticketId, Long eventoId, String usuario, Integer cantidad, Instant createdAt) {
        this.ticketId = ticketId;
        this.eventoId = eventoId;
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.createdAt = createdAt;
    }

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

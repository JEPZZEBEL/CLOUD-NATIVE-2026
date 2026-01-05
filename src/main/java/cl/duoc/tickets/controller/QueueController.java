package cl.duoc.tickets.controller;

import cl.duoc.tickets.dto.queue.TicketMessage;
import cl.duoc.tickets.service.QueueProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueProducer producer;

    public QueueController(QueueProducer producer) {
        this.producer = producer;
    }

    // Endpoint simple para probar cola (no depende de tu lógica actual de tickets)
    @PostMapping("/produce")
    public ResponseEntity<String> produce(@RequestBody ProduceRequest req) {
        TicketMessage msg = new TicketMessage(
                req.getTicketId(),
                req.getEventoId(),
                req.getUsuario(),
                req.getCantidad(),
                Instant.now()
        );

        producer.sendOkOrFallback(msg);
        return ResponseEntity.accepted().body("Mensaje enviado a cola OK (fallback a ERROR si falla).");
    }

    // DTO interno para request
    public static class ProduceRequest {
        private String ticketId;
        private Long eventoId;
        private String usuario;
        private Integer cantidad;

        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }

        public Long getEventoId() { return eventoId; }
        public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }
}

package cl.duoc.tickets.controller;

import cl.duoc.tickets.entity.TicketQueueLog;
import cl.duoc.tickets.service.TicketQueueConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final TicketQueueConsumer consumer;

    public QueueController(TicketQueueConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Consume 1 mensaje desde tickets.ok:
     * - Si lo guarda OK -> devuelve el registro
     * - Si no hay mensaje -> 204
     * - Si falla y se manda a tickets.error -> 202
     */
    @PostMapping("/consume-ok")
    public ResponseEntity<?> consumeOne() {
        TicketQueueLog saved = consumer.consumeOneFromOkAndSave();

        if (saved == null) {
            // Puede ser cola vacía o que se reenvió a error.
            // Para diferenciar, lo dejamos como Accepted (demo simple)
            return ResponseEntity.accepted().body("Procesado: cola vacía o mensaje enviado a ERROR");
        }

        return ResponseEntity.ok(saved);
    }
}

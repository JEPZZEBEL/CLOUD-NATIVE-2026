package cl.duoc.tickets.controller;

import cl.duoc.tickets.dto.request.GenerarTicketRequest;
import cl.duoc.tickets.dto.request.UpdateTicketRequest;
import cl.duoc.tickets.dto.response.S3UploadResponse;
import cl.duoc.tickets.dto.response.TicketResponse;
import cl.duoc.tickets.entity.Ticket;
import cl.duoc.tickets.service.S3Service;
import cl.duoc.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final S3Service s3Service;

    public TicketController(TicketService ticketService, S3Service s3Service) {
        this.ticketService = ticketService;
        this.s3Service = s3Service;
    }

    // 1) Generar ticket y guardar en EFS
    @PostMapping("/generar")
    public TicketResponse generar(@Valid @RequestBody GenerarTicketRequest req) throws Exception {
        Ticket t = ticketService.generarTicket(req);
        return toResponse(t);
    }

    // 2) Subir ticket a S3
    @PostMapping("/{ticketId}/subir-s3")
    public S3UploadResponse subirS3(@PathVariable("ticketId") String ticketId) throws Exception {
        Ticket t = ticketService.subirTicketAS3(ticketId);
        return S3UploadResponse.builder()
                .ticketId(t.getId())
                .bucket(s3Service.getBucket())
                .s3Key(t.getS3Key())
                .build();
    }

    // 3) Listar tickets por evento
    @GetMapping("/evento/{eventoId}")
    public List<TicketResponse> listarPorEvento(@PathVariable("eventoId") Long eventoId) {
        return ticketService.listarPorEvento(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 4) Listar tickets por usuario
    @GetMapping("/usuario/{usuario}")
    public List<TicketResponse> listarPorUsuario(@PathVariable("usuario") String usuario) {
        return ticketService.listarPorUsuario(usuario)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 4.1) Descargar PDF (S3 si existe, si no EFS) - para demo
    @GetMapping("/{ticketId}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable("ticketId") String ticketId) throws Exception {
        byte[] pdf = ticketService.descargarPdf(ticketId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ticket-" + ticketId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 5) Modificar detalles del ticket
    @PutMapping("/{ticketId}")
    public TicketResponse actualizar(@PathVariable("ticketId") String ticketId,
                                     @RequestBody UpdateTicketRequest req) {
        Ticket t = ticketService.actualizarTicket(ticketId, req);
        return toResponse(t);
    }

    // 6) Eliminar ticket específico (EFS + S3 + BD)
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> eliminar(@PathVariable("ticketId") String ticketId) throws Exception {
        ticketService.eliminarTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    // ✅ IMPORTANTE:
    // Las estadísticas quedan SOLO en StatsController:
    // GET /api/tickets/estadisticas/evento/{eventoId}

    private TicketResponse toResponse(Ticket t) {
        return TicketResponse.builder()
                .ticketId(t.getId())
                .eventoId(t.getEventoId())
                .usuario(t.getUsuario())
                .estado(t.getEstado())
                .efsPath(t.getEfsPath())
                .s3Key(t.getS3Key())
                .build();
    }
}

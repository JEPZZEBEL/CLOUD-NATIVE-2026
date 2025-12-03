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
        return TicketResponse.builder()
                .ticketId(t.getId())
                .efsPath(t.getEfsPath())
                .estado(t.getEstado())
                .build();
    }

    // 2) Subir ticket a S3
    @PostMapping("/{ticketId}/subir-s3")
    public S3UploadResponse subirS3(@PathVariable String ticketId) throws Exception {
        Ticket t = ticketService.subirTicketAS3(ticketId);
        return S3UploadResponse.builder()
                .ticketId(t.getId())
                .bucket(s3Service.getBucket())
                .s3Key(t.getS3Key())
                .build();
    }

    // 3) Descargar por evento (lista de keys S3 o paths EFS)
    @GetMapping("/evento/{eventoId}")
    public List<Ticket> listarPorEvento(@PathVariable Long eventoId) {
        return ticketService.listarPorEvento(eventoId);
    }

    // 4) Descargar por usuario
    @GetMapping("/usuario/{usuario}")
    public List<Ticket> listarPorUsuario(@PathVariable String usuario) {
        return ticketService.listarPorUsuario(usuario);
    }

    // Extra: descargar PDF directo desde S3 por ticketId (te sirve para demo)
    @GetMapping("/{ticketId}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String ticketId) throws Exception {
        Ticket t = ticketService.listarPorUsuario("dummy")
                .stream().filter(x -> x.getId().equals(ticketId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (t.getS3Key() == null) {
            throw new RuntimeException("Ticket aún no está en S3");
        }

        byte[] pdf = s3Service.download(t.getS3Key());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + ticketId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 5) Modificar detalles del ticket
    @PutMapping("/{ticketId}")
    public Ticket actualizar(@PathVariable String ticketId,
                             @RequestBody UpdateTicketRequest req) {
        return ticketService.actualizarTicket(ticketId, req);
    }

    // 6) Eliminar ticket específico (EFS + S3 + BD)
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> eliminar(@PathVariable String ticketId) throws Exception {
        ticketService.eliminarTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}

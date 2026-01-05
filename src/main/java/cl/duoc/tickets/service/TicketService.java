package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.request.GenerarTicketRequest;
import cl.duoc.tickets.dto.request.UpdateTicketRequest;
import cl.duoc.tickets.dto.response.TicketStatsResponse;
import cl.duoc.tickets.entity.Reserva;
import cl.duoc.tickets.entity.Ticket;
import cl.duoc.tickets.exception.NotFoundException;
import cl.duoc.tickets.repository.ReservaRepository;
import cl.duoc.tickets.repository.TicketRepository;
import cl.duoc.tickets.util.PdfGenerator;
import cl.duoc.tickets.util.S3KeyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepo;
    private final ReservaRepository reservaRepo;
    private final EfsService efsService;
    private final S3Service s3Service;

    public TicketService(TicketRepository ticketRepo,
                         ReservaRepository reservaRepo,
                         EfsService efsService,
                         S3Service s3Service) {
        this.ticketRepo = ticketRepo;
        this.reservaRepo = reservaRepo;
        this.efsService = efsService;
        this.s3Service = s3Service;
    }

    // =============================
    // 1) Generar ticket y guardar EFS + BD (+ reserva para estadísticas)
    // =============================
    @Transactional
    public Ticket generarTicket(GenerarTicketRequest req) throws Exception {
        String ticketId = UUID.randomUUID().toString();

        // 1) Crear reserva (para estadísticas)
        Reserva reserva = Reserva.builder()
                .eventoId(req.getEventoId())
                .usuario(req.getUsuario())
                .cantidad(req.getCantidad())
                .precioTotal(req.getPrecioTotal() == null ? 0L : req.getPrecioTotal())
                .createdAt(LocalDateTime.now())
                .build();
        reservaRepo.save(reserva);

        // 2) Generar PDF y guardarlo en EFS
        Path efsPath = efsService.buildEfsPath(req.getEventoId(), req.getUsuario(), ticketId);
        PdfGenerator.generateTicketPdf(efsPath, ticketId, req.getEventoId(), req.getUsuario(), req.getCantidad());

        // 3) Guardar ticket en BD
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .eventoId(req.getEventoId())
                .usuario(req.getUsuario())
                .efsPath(efsPath.toString())
                .estado("GENERADO") // sugerido: RESERVADO / VENDIDO según tu flujo
                .createdAt(LocalDateTime.now())
                .build();

        return ticketRepo.save(ticket);
    }

    // =============================
    // 2) Subir ticket a S3
    // =============================
    @Transactional
    public Ticket subirTicketAS3(String ticketId) throws Exception {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        if (t.getEfsPath() == null || t.getEfsPath().isBlank()) {
            throw new IllegalStateException("Ticket sin EFS path");
        }

        byte[] pdf = efsService.readFile(Path.of(t.getEfsPath()));
        String key = S3KeyBuilder.build(t.getEventoId(), t.getUsuario(), t.getId());

        s3Service.upload(key, pdf);

        t.setS3Key(key);
        t.setEstado("SUBIDO");
        return ticketRepo.save(t);
    }

    // =============================
    // 3) Listar
    // =============================
    public List<Ticket> listarPorEvento(Long eventoId) {
        return ticketRepo.findByEventoId(eventoId);
    }

    public List<Ticket> listarPorUsuario(String usuario) {
        return ticketRepo.findByUsuario(usuario);
    }

    // =============================
    // 4) Descargar PDF (S3 si existe, si no EFS)
    // =============================
    public byte[] descargarPdf(String ticketId) throws Exception {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        // 1) Preferir S3
        if (t.getS3Key() != null && !t.getS3Key().isBlank()) {
            return s3Service.download(t.getS3Key());
        }

        // 2) Si no está en S3, usar EFS
        if (t.getEfsPath() != null && !t.getEfsPath().isBlank()) {
            return efsService.readFile(Path.of(t.getEfsPath()));
        }

        throw new IllegalStateException("Ticket sin PDF (no tiene S3Key ni EFSPath).");
    }

    // =============================
    // 5) Modificar ticket
    // =============================
    @Transactional
    public Ticket actualizarTicket(String ticketId, UpdateTicketRequest req) {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        if (req.getEstado() != null && !req.getEstado().isBlank()) {
            t.setEstado(req.getEstado());
        }

        // Si luego quieres permitir actualizar otros campos, hazlo aquí.
        return ticketRepo.save(t);
    }

    // =============================
    // 6) Eliminar ticket (EFS + S3 + BD)
    // =============================
    @Transactional
    public void eliminarTicket(String ticketId) throws Exception {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        // borrar en EFS
        if (t.getEfsPath() != null && !t.getEfsPath().isBlank()) {
            efsService.deleteFileIfExists(Path.of(t.getEfsPath()));
        }

        // borrar en S3 si existe
        if (t.getS3Key() != null && !t.getS3Key().isBlank()) {
            s3Service.delete(t.getS3Key());
        }

        ticketRepo.deleteById(ticketId);
    }

    // =============================
    // 7) Estadísticas (REQUERIDO por pauta)
    // =============================
    public TicketStatsResponse obtenerEstadisticas(Long eventoId) {

        // Basado en Ticket (si tu BD ya guarda estados):
        long totalTickets = ticketRepo.countByEventoId(eventoId);

        // Estos 2 funcionan si tú manejas estos estados:
        long totalVentas = ticketRepo.countByEventoIdAndEstado(eventoId, "VENDIDO");
        long totalReservas = ticketRepo.countByEventoIdAndEstado(eventoId, "RESERVADO");

        return TicketStatsResponse.builder()
                .eventoId(eventoId)
                .totalTickets(totalTickets)
                .totalVentas(totalVentas)
                .totalReservas(totalReservas)
                .build();
    }
}

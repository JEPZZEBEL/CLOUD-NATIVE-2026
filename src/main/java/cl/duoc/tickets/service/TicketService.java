package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.queue.TicketMessage;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepo;
    private final ReservaRepository reservaRepo;
    private final EfsService efsService;
    private final S3Service s3Service;
    private final QueueProducer queueProducer; // ✅ NUEVO

    public TicketService(TicketRepository ticketRepo,
                         ReservaRepository reservaRepo,
                         EfsService efsService,
                         S3Service s3Service,
                         QueueProducer queueProducer) { // ✅ NUEVO
        this.ticketRepo = ticketRepo;
        this.reservaRepo = reservaRepo;
        this.efsService = efsService;
        this.s3Service = s3Service;
        this.queueProducer = queueProducer;
    }

    // =============================
    // 1) Generar ticket y guardar EFS + BD (+ reserva para estadísticas)
    // =============================
    @Transactional
    public Ticket generarTicket(GenerarTicketRequest req) throws Exception {
        String ticketId = UUID.randomUUID().toString();

        try {
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
                    .estado("GENERADO")
                    .createdAt(LocalDateTime.now())
                    .build();

            Ticket saved = ticketRepo.save(ticket);

            // ✅ 4) Enviar mensaje a cola OK
            TicketMessage msg = new TicketMessage(
                    saved.getId(),
                    saved.getEventoId(),
                    saved.getUsuario(),
                    req.getCantidad(),
                    Instant.now()
            );
            queueProducer.sendOk(msg);

            return saved;

        } catch (Exception ex) {
            // ✅ Enviar mensaje a cola ERROR (con info para evidencia)
            TicketMessage errMsg = new TicketMessage(
                    ticketId,                 // si alcanzó a generarse, queda el id
                    req.getEventoId(),
                    req.getUsuario(),
                    req.getCantidad(),
                    Instant.now()
            );

            // Si tu TicketMessage no tiene campo "error", igual sirve para demostrar cola error.
            // Si SÍ tienes campo error, me dices y lo ajusto.
            try {
                queueProducer.sendError(errMsg);
            } catch (Exception ignored) {
                // Si también falla Rabbit, no bloquees el flujo: igual lanzamos la excepción original
            }

            throw ex;
        }
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

        if (t.getS3Key() != null && !t.getS3Key().isBlank()) {
            return s3Service.download(t.getS3Key());
        }

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

        return ticketRepo.save(t);
    }

    // =============================
    // 6) Eliminar ticket (EFS + S3 + BD)
    // =============================
    @Transactional
    public void eliminarTicket(String ticketId) throws Exception {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        if (t.getEfsPath() != null && !t.getEfsPath().isBlank()) {
            efsService.deleteFileIfExists(Path.of(t.getEfsPath()));
        }

        if (t.getS3Key() != null && !t.getS3Key().isBlank()) {
            s3Service.delete(t.getS3Key());
        }

        ticketRepo.deleteById(ticketId);
    }

    // =============================
    // 7) Estadísticas
    // =============================
    public TicketStatsResponse obtenerEstadisticas(Long eventoId) {
        long totalTickets = ticketRepo.countByEventoId(eventoId);
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

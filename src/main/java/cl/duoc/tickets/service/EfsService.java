package cl.duoc.tickets.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class EfsService {

    @Value("${efs.base-path}")
    private String basePath;

    public Path buildEfsPath(Long eventoId, String usuario, String ticketId) {
        return Path.of(basePath, "evento" + eventoId, usuario, "ticket-" + ticketId + ".pdf");
    }

    public byte[] readFile(Path path) throws Exception {
        return Files.readAllBytes(path);
    }

    public void deleteFileIfExists(Path path) throws Exception {
        Files.deleteIfExists(path);
    }
}

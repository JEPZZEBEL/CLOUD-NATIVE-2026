package cl.duoc.tickets.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
public class Ticket {

    @Id
    private String id; // UUID

    private Long eventoId;

    private String usuario;

    private String efsPath;

    private String s3Key;

    private String estado; // GENERADO, SUBIDO, ANULADO, etc.

    private LocalDateTime createdAt;
}

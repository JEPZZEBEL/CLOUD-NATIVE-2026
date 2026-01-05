package cl.duoc.tickets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(nullable = false)
    private String usuario;

    @Column(name = "efs_path")
    private String efsPath;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(nullable = false)
    private String estado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

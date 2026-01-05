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
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_total")
    private Long precioTotal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

package cl.duoc.tickets.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GenerarTicketRequest {

    @NotNull
    private Long eventoId;

    @NotBlank
    private String usuario;

    @NotNull @Min(1)
    private Integer cantidad;

    // opcional: precio total si quieres
    private Long precioTotal;
}

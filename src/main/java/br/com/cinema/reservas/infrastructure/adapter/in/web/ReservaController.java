package br.com.cinema.reservas.infrastructure.adapter.in.web;

import br.com.cinema.reservas.application.port.in.ReservarAssentoUseCase;
import br.com.cinema.reservas.domain.Reserva;
import br.com.cinema.reservas.domain.ReservaDomainException;
import br.com.cinema.reservas.domain.StatusReserva;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de entrada web. Conversa apenas com a porta de entrada -
 * nunca com o service nem com o repositorio, conforme o ArchitectureGuardTest.
 */
@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Reserva de assentos orientada a eventos")
public class ReservaController {

    private final ReservarAssentoUseCase useCase;

    ReservaController(ReservarAssentoUseCase useCase) {
        this.useCase = useCase;
    }

    public record ReservarRequest(
            @NotNull UUID sessaoId,
            @NotNull UUID assentoId,
            @NotNull UUID clienteId,
            @NotNull BigDecimal valor) {}

    public record ReservaResponse(
            UUID id, UUID sessaoId, UUID assentoId, UUID clienteId,
            BigDecimal valor, StatusReserva status, String motivo, Instant expiraEm) {

        static ReservaResponse de(Reserva r) {
            return new ReservaResponse(r.getId(), r.getSessaoId(), r.getAssentoId(),
                    r.getClienteId(), r.getValor(), r.getStatus(), r.getMotivo(), r.getExpiraEm());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserva um assento e publica o AssentoReservadoEvent")
    public ReservaResponse reservar(@Valid @RequestBody ReservarRequest req) {
        return ReservaResponse.de(
                useCase.reservar(req.sessaoId(), req.assentoId(), req.clienteId(), req.valor()));
    }

    @GetMapping("/{reservaId}")
    @Operation(summary = "Consulta o estado atual de uma reserva")
    public ReservaResponse consultar(@PathVariable UUID reservaId) {
        return ReservaResponse.de(useCase.consultar(reservaId));
    }

    @ExceptionHandler(ReservaDomainException.class)
    ProblemDetail tratarRegraDeNegocio(ReservaDomainException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Regra de negócio violada");
        return problema;
    }
}

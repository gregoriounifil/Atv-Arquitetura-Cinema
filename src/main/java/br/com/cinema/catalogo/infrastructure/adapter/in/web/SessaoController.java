package br.com.cinema.catalogo.infrastructure.adapter.in.web;

import br.com.cinema.catalogo.application.port.in.ConsultarSessoesUseCase;
import br.com.cinema.catalogo.application.port.in.ProgramarSessaoUseCase;
import br.com.cinema.catalogo.domain.model.Sessao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Adaptador de entrada web. Conversa apenas com as portas de entrada. */
@RestController
@RequestMapping("/api/v1/sessoes")
@Tag(name = "Catalogo - Sessoes", description = "Programacao e consulta de sessoes")
public class SessaoController {

    private final ProgramarSessaoUseCase programarUseCase;
    private final ConsultarSessoesUseCase consultarUseCase;

    SessaoController(ProgramarSessaoUseCase programarUseCase, ConsultarSessoesUseCase consultarUseCase) {
        this.programarUseCase = programarUseCase;
        this.consultarUseCase = consultarUseCase;
    }

    public record ProgramarSessaoRequest(
            @NotNull UUID filmeId,
            @NotNull UUID salaId,
            @NotNull Instant inicio,
            @NotNull @Positive BigDecimal precoBase) {}

    public record SessaoResponse(UUID id, UUID filmeId, UUID salaId,
                                 Instant inicio, Instant fim, BigDecimal precoBase) {

        static SessaoResponse de(Sessao s) {
            return new SessaoResponse(s.getId(), s.getFilmeId(), s.getSalaId(),
                    s.getInicio(), s.getFim(), s.getPrecoBase());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Programa uma sessao e publica SessaoCriadaEvent")
    public SessaoResponse programar(@Valid @RequestBody ProgramarSessaoRequest req) {
        return SessaoResponse.de(
                programarUseCase.programar(req.filmeId(), req.salaId(), req.inicio(), req.precoBase()));
    }

    @GetMapping("/{sessaoId}")
    @Operation(summary = "Consulta uma sessao")
    public SessaoResponse buscar(@PathVariable UUID sessaoId) {
        return SessaoResponse.de(consultarUseCase.buscar(sessaoId));
    }

    @GetMapping
    @Operation(summary = "Lista as sessoes de um filme")
    public List<SessaoResponse> listarPorFilme(@RequestParam UUID filmeId) {
        return consultarUseCase.listarPorFilme(filmeId).stream().map(SessaoResponse::de).toList();
    }
}

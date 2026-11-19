package br.com.cinema.catalogo.infrastructure.adapter.in.web;

import br.com.cinema.catalogo.application.port.in.GerenciarSalasUseCase;
import br.com.cinema.catalogo.domain.model.Sala;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Adaptador de entrada web. Conversa apenas com a porta de entrada. */
@RestController
@RequestMapping("/api/v1/salas")
@Tag(name = "Catalogo - Salas", description = "Cadastro e consulta de salas")
public class SalaController {

    private final GerenciarSalasUseCase useCase;

    SalaController(GerenciarSalasUseCase useCase) {
        this.useCase = useCase;
    }

    public record CadastrarSalaRequest(@NotBlank String nome, @NotNull @Positive Integer capacidade) {}

    public record SalaResponse(UUID id, String nome, int capacidade) {

        static SalaResponse de(Sala s) {
            return new SalaResponse(s.getId(), s.getNome(), s.getCapacidade());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma sala")
    public SalaResponse cadastrar(@Valid @RequestBody CadastrarSalaRequest req) {
        return SalaResponse.de(useCase.cadastrar(req.nome(), req.capacidade()));
    }

    @GetMapping
    @Operation(summary = "Lista as salas cadastradas")
    public List<SalaResponse> listar() {
        return useCase.listar().stream().map(SalaResponse::de).toList();
    }

    @GetMapping("/{salaId}")
    @Operation(summary = "Consulta uma sala")
    public SalaResponse buscar(@PathVariable UUID salaId) {
        return SalaResponse.de(useCase.buscar(salaId));
    }
}

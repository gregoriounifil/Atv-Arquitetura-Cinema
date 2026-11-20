package br.com.cinema.catalogo.infrastructure.adapter.in.web;

import br.com.cinema.catalogo.application.port.in.GerenciarFilmesUseCase;
import br.com.cinema.catalogo.domain.model.ClassificacaoIndicativa;
import br.com.cinema.catalogo.domain.model.Filme;
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
@RequestMapping("/api/v1/filmes")
@Tag(name = "Catalogo - Filmes", description = "Cadastro e consulta de filmes")
public class FilmeController {

    private final GerenciarFilmesUseCase useCase;

    FilmeController(GerenciarFilmesUseCase useCase) {
        this.useCase = useCase;
    }

    public record CadastrarFilmeRequest(
            @NotBlank String titulo,
            @NotNull @Positive Integer duracaoMinutos,
            @NotNull ClassificacaoIndicativa classificacao) {}

    public record FilmeResponse(UUID id, String titulo, int duracaoMinutos,
                                ClassificacaoIndicativa classificacao) {

        static FilmeResponse de(Filme f) {
            return new FilmeResponse(f.getId(), f.getTitulo(), f.getDuracaoMinutos(), f.getClassificacao());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um filme")
    public FilmeResponse cadastrar(@Valid @RequestBody CadastrarFilmeRequest req) {
        return FilmeResponse.de(useCase.cadastrar(req.titulo(), req.duracaoMinutos(), req.classificacao()));
    }

    @GetMapping
    @Operation(summary = "Lista os filmes cadastrados")
    public List<FilmeResponse> listar() {
        return useCase.listar().stream().map(FilmeResponse::de).toList();
    }

    @GetMapping("/{filmeId}")
    @Operation(summary = "Consulta um filme")
    public FilmeResponse buscar(@PathVariable UUID filmeId) {
        return FilmeResponse.de(useCase.buscar(filmeId));
    }
}

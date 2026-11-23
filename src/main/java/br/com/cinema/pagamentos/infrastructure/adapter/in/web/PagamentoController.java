package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.application.port.in.ProcessarPagamentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagamentos")
@Tag(name = "Pagamentos", description = "Operações de consulta e checkout/processamento de pagamentos de ingressos")
public class PagamentoController {

    private final ProcessarPagamentoUseCase processarPagamentoUseCase;
    private final ConsultarPagamentoUseCase consultarPagamentoUseCase;

    public PagamentoController(
            ProcessarPagamentoUseCase processarPagamentoUseCase,
            ConsultarPagamentoUseCase consultarPagamentoUseCase
    ) {
        this.processarPagamentoUseCase = processarPagamentoUseCase;
        this.consultarPagamentoUseCase = consultarPagamentoUseCase;
    }

    @PostMapping(value = "/{id}/processar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Processar pagamento da reserva",
            description = "Efetua a transação via Cartão de Crédito, Débito ou PIX. Simulação determinística: final 4242 aprova, final 0000 recusa, final 9999 simula latência (1,5s) e PIX confirma imediatamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento processado com sucesso (aprovado ou recusado pela operadora)",
                    content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do cartão inválidos ou incompletos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Transição inválida (cobrança já finalizada)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Prazo limite de 10 minutos para pagamento expirou",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagamentoResponse> processar(
            @Parameter(description = "Identificador único do pagamento", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @RequestBody @Valid ProcessarPagamentoRequest request
    ) {
        PagamentoResult resultado = processarPagamentoUseCase.executar(request.toCommand(id));
        return ResponseEntity.ok(PagamentoResponse.from(resultado));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar pagamento por ID", description = "Retorna os detalhes e o status atual da cobrança.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagamentoResponse> buscarPorId(
            @Parameter(description = "Identificador único do pagamento", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        PagamentoResult resultado = consultarPagamentoUseCase.buscarPorId(id);
        return ResponseEntity.ok(PagamentoResponse.from(resultado));
    }

    @GetMapping(value = "/reserva/{reservaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultar pagamento por ID da Reserva", description = "Retorna a cobrança associada a uma reserva de assento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado para a reserva informada",
                    content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum pagamento encontrado para esta reserva",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagamentoResponse> buscarPorReservaId(
            @Parameter(description = "Identificador da reserva", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID reservaId
    ) {
        PagamentoResult resultado = consultarPagamentoUseCase.buscarPorReservaId(reservaId);
        return ResponseEntity.ok(PagamentoResponse.from(resultado));
    }
}

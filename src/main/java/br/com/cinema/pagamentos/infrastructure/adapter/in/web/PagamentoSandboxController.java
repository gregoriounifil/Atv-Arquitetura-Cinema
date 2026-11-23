package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaCommand;
import br.com.cinema.pagamentos.application.port.in.CriarCobrancaReservaUseCase;
import br.com.cinema.pagamentos.application.port.in.ExpirarPagamentosPendentesUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@Profile("!prod")
public class PagamentoSandboxController {

    private final CriarCobrancaReservaUseCase criarCobrancaReservaUseCase;
    private final ConsultarPagamentoUseCase consultarPagamentoUseCase;
    private final ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase;

    public PagamentoSandboxController(
            CriarCobrancaReservaUseCase criarCobrancaReservaUseCase,
            ConsultarPagamentoUseCase consultarPagamentoUseCase,
            ExpirarPagamentosPendentesUseCase expirarPagamentosPendentesUseCase
    ) {
        this.criarCobrancaReservaUseCase = criarCobrancaReservaUseCase;
        this.consultarPagamentoUseCase = consultarPagamentoUseCase;
        this.expirarPagamentosPendentesUseCase = expirarPagamentosPendentesUseCase;
    }

    @GetMapping("/")
    public String redirecionarRaiz() {
        return "redirect:/pagamentos/sandbox";
    }

    @GetMapping("/pagamentos/sandbox")
    public String sandbox(
            @RequestParam(required = false) String erro,
            Model model
    ) {
        model.addAttribute("erro", erro);
        return "pagamentos/sandbox";
    }

    @PostMapping("/pagamentos/sandbox/criar")
    public String criarCobrancaSimulada(
            @RequestParam(defaultValue = "45.00") BigDecimal valor,
            @RequestParam(defaultValue = "Duna: Parte 2 (Legendado)") String filme,
            @RequestParam(defaultValue = "Sala 04 • IMAX") String sala,
            @RequestParam(defaultValue = "Hoje • 21:00") String horario,
            @RequestParam(defaultValue = "G12") String assento,
            RedirectAttributes redirectAttributes
    ) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addAttribute("erro", "Informe um valor maior que zero para gerar a cobrança.");
            return "redirect:/pagamentos/sandbox";
        }

        PagamentoResult criada = criarCobrancaReservaUseCase.executar(new CriarCobrancaCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                valor
        ));

        redirectAttributes.addAttribute("filme", filme);
        redirectAttributes.addAttribute("sala", sala);
        redirectAttributes.addAttribute("horario", horario);
        redirectAttributes.addAttribute("assento", assento);
        return "redirect:/pagamentos/" + criada.id() + "/checkout";
    }

    @PostMapping("/pagamentos/sandbox/{id}/simular-expiracao")
    @ResponseBody
    public ResponseEntity<PagamentoResponse> simularExpiracao(@PathVariable UUID id) {
        PagamentoResult atual = consultarPagamentoUseCase.buscarPorId(id);
        expirarPagamentosPendentesUseCase.expirarPagamentosVencidos(atual.expiraEm().plusSeconds(1));
        PagamentoResult atualizado = consultarPagamentoUseCase.buscarPorId(id);
        return ResponseEntity.ok(PagamentoResponse.from(atualizado));
    }
}

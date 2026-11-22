package br.com.cinema.pagamentos.infrastructure.adapter.in.web;

import br.com.cinema.pagamentos.application.port.in.ConsultarPagamentoUseCase;
import br.com.cinema.pagamentos.application.port.in.PagamentoResult;
import br.com.cinema.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/pagamentos")
public class PagamentoViewController {

    private final ConsultarPagamentoUseCase consultarPagamentoUseCase;
    private final Environment environment;

    public PagamentoViewController(
            ConsultarPagamentoUseCase consultarPagamentoUseCase,
            Environment environment
    ) {
        this.consultarPagamentoUseCase = consultarPagamentoUseCase;
        this.environment = environment;
    }

    @GetMapping("/{id}/checkout")
    public String checkout(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Duna: Parte 2 (Legendado)") String filme,
            @RequestParam(defaultValue = "Sala 04 • IMAX") String sala,
            @RequestParam(defaultValue = "Hoje • 21:00") String horario,
            @RequestParam(defaultValue = "G12") String assento,
            HttpServletResponse response,
            Model model
    ) {
        try {
            PagamentoResult resultado = consultarPagamentoUseCase.buscarPorId(id);
            PagamentoResponse pagamento = PagamentoResponse.from(resultado);

            model.addAttribute("pagamento", pagamento);
            model.addAttribute("filme", filme);
            model.addAttribute("sala", sala);
            model.addAttribute("horario", horario);
            model.addAttribute("assento", assento);
            model.addAttribute("sandboxAtivo", !environment.acceptsProfiles(Profiles.of("prod")));
            model.addAttribute("codigoPixSimulado", gerarPayloadPixSimulado(pagamento));
            return "pagamentos/checkout";
        } catch (PagamentoNaoEncontradoException ex) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            model.addAttribute("pagamentoId", id);
            model.addAttribute("mensagem", ex.getMessage());
            model.addAttribute("sandboxAtivo", !environment.acceptsProfiles(Profiles.of("prod")));
            return "pagamentos/nao-encontrado";
        }
    }

    private String gerarPayloadPixSimulado(PagamentoResponse pagamento) {
        String sufixo = pagamento.id().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "00020126580014BR.GOV.BCB.PIX0136cinema-" + sufixo
                + "5204000053039865405" + pagamento.valor().toPlainString()
                + "5802BR5913CINEMA_TICKET6009SAO_PAULO6304" + sufixo.substring(0, 4);
    }
}

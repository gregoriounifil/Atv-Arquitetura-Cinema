package br.com.cinema.pagamentos.domain.model;

import java.util.Objects;

public record DadosCartao(
        String numeroCartao,
        String nomeTitular,
        String validade,
        String cvv
) {
    public DadosCartao {
        Objects.requireNonNull(numeroCartao, "numeroCartao e obrigatorio");
        Objects.requireNonNull(nomeTitular, "nomeTitular e obrigatorio");
        Objects.requireNonNull(validade, "validade e obrigatoria");
        Objects.requireNonNull(cvv, "cvv e obrigatorio");
    }

    public String obterUltimosQuatroDigitos() {
        String limpo = numeroCartao.replaceAll("\\s+", "");
        return limpo.length() >= 4 ? limpo.substring(limpo.length() - 4) : "****";
    }
}

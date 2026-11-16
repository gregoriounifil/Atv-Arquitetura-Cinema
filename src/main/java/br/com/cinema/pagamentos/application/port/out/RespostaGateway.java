package br.com.cinema.pagamentos.application.port.out;

public record RespostaGateway(
        boolean aprovado,
        String codigoTransacao,
        String motivoRecusa
) {}

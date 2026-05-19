package br.com.sistemamoedas.service;

public record EventoSistema(
        String tipo,
        Long transacaoId,
        String codigoCupom,
        String alunoEmail,
        String professorEmail,
        String empresaEmail,
        String vantagem,
        int valor,
        String criadoEm) {
}

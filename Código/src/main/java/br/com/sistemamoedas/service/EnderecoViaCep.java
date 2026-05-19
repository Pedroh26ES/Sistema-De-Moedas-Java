package br.com.sistemamoedas.service;

public record EnderecoViaCep(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String enderecoFormatado) {
}

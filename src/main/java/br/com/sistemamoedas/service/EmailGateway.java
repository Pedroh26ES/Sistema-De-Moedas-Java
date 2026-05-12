package br.com.sistemamoedas.service;

public interface EmailGateway {

    void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia);
}

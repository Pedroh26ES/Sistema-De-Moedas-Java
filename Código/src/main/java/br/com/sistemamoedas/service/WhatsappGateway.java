package br.com.sistemamoedas.service;

public interface WhatsappGateway {

    void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia);

    void enviarParaChat(String chatId, String mensagem);

    default void enviarLogoParaChat(String chatId, String legenda) {
    }
}

package br.com.sistemamoedas.service;

public interface WhatsappGateway {

    void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia);

    void enviarParaChat(String chatId, String mensagem);

    default boolean enviarLogoParaChat(String chatId, String legenda) {
        return false;
    }

    default boolean enviarImagemParaChat(String chatId, byte[] png, String nomeArquivo, String legenda) {
        return false;
    }
}

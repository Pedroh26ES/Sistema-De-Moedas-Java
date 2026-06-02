package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.service.WhatsappBotService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api/whatsapp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WhatsappController {

    @Inject
    WhatsappBotService bot;

    @POST
    @Path("webhook")
    public BotResponse webhook(Map<String, Object> payload) {
        String chatId = primeiroValor(payload, "chatId", "from", "author");
        String texto = primeiroValor(payload, "body", "text", "message");
        Object nested = payload.get("payload");
        if (nested instanceof Map<?, ?> mapa) {
            if (chatId.isBlank()) {
                chatId = primeiroValor(mapa, "chatId", "from", "author");
            }
            if (texto.isBlank()) {
                texto = primeiroValor(mapa, "body", "text", "message");
            }
        }
        if (chatId.isBlank() || texto.isBlank()) {
            return new BotResponse("ignorado", "Evento sem chatId ou texto.");
        }
        String resposta = bot.processarWebhook(chatId, texto);
        return new BotResponse("respondido", resposta);
    }


    private String primeiroValor(Map<?, ?> payload, String... chaves) {
        for (String chave : chaves) {
            Object valor = payload.get(chave);
            if (valor instanceof String texto && !texto.isBlank()) {
                return texto;
            }
        }
        return "";
    }


    public record BotResponse(String status, String resposta) {
    }
}

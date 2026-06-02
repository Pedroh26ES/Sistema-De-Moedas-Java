package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Usuario;
import br.com.sistemamoedas.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.imageio.ImageIO;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WahaWhatsappGateway implements WhatsappGateway {

    private static final Logger LOG = Logger.getLogger(WahaWhatsappGateway.class);
    private static final String APP_NAME = "Valoriza Ae";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Inject
    UsuarioRepository usuarios;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "valoriza.whatsapp.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "valoriza.whatsapp.endpoint", defaultValue = "http://localhost:3000")
    String endpoint;

    @ConfigProperty(name = "valoriza.whatsapp.session", defaultValue = "default")
    String session;

    @ConfigProperty(name = "valoriza.whatsapp.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "valoriza.whatsapp.recipient-overrides")
    Optional<String> recipientOverrides;

    @ConfigProperty(name = "valoriza.whatsapp.default-chat-id")
    Optional<String> defaultChatId;

    @ConfigProperty(name = "valoriza.whatsapp.fail-on-error", defaultValue = "false")
    boolean failOnError;

    @ConfigProperty(name = "valoriza.whatsapp.humanized-replies", defaultValue = "true")
    boolean humanizedReplies;

    @ConfigProperty(name = "valoriza.whatsapp.typing-enabled", defaultValue = "true")
    boolean typingEnabled;

    @ConfigProperty(name = "valoriza.whatsapp.typing-min-ms", defaultValue = "900")
    int typingMinMs;

    @ConfigProperty(name = "valoriza.whatsapp.typing-max-ms", defaultValue = "3200")
    int typingMaxMs;

    @ConfigProperty(name = "valoriza.whatsapp.typing-ms-per-char", defaultValue = "18")
    int typingMsPerChar;

    @ConfigProperty(name = "valoriza.app.public-url", defaultValue = "http://localhost:8080")
    String publicUrl;

    @Override
    public void enviarParaChat(String chatId, String mensagem) {
        if (!enabled) {
            return;
        }
        String normalizado = normalizarChatId(chatId);
        if (normalizado.isBlank()) {
            LOG.info("WhatsApp nao enviado: chatId invalido.");
            return;
        }
        try {
            enviarRespostaHumanizada(normalizado, mensagem);
            LOG.infof("WhatsApp enviado pelo WAHA para %s", normalizado);
        } catch (RegraNegocioException erro) {
            tratarFalha(erro, normalizado);
        } catch (Exception erro) {
            tratarFalha(new RegraNegocioException("Nao foi possivel enviar WhatsApp pelo WAHA."), normalizado, erro);
        }
    }

    @Override
    public void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia) {
        if (!enabled) {
            return;
        }

        Optional<String> chatId = resolverChatId(destinatario);
        if (chatId.isEmpty()) {
            LOG.infof("WhatsApp nao enviado para %s: telefone nao cadastrado.", destinatario);
            return;
        }

        try {
            enviarPeloWaha(chatId.get(), montarMensagem(assunto, conteudo, codigoReferencia));
            LOG.infof("WhatsApp enviado pelo WAHA para %s: %s", chatId.get(), assunto);
        } catch (RegraNegocioException erro) {
            tratarFalha(erro, destinatario);
        } catch (Exception erro) {
            tratarFalha(new RegraNegocioException("Nao foi possivel enviar WhatsApp pelo WAHA."), destinatario, erro);
        }
    }

    private Optional<String> resolverChatId(String destinatario) {
        String email = destinatario == null ? "" : destinatario.trim().toLowerCase(Locale.ROOT);
        Optional<String> override = buscarOverride(email);
        if (override.isPresent()) {
            return normalizarChatIdOptional(override.get());
        }

        Optional<Usuario> usuario = usuarios.porEmail(email);
        if (usuario.isPresent()) {
            Optional<String> telefone = telefoneDoUsuario(usuario.get());
            if (telefone.isPresent()) {
                return normalizarChatIdOptional(telefone.get());
            }
        }

        return defaultChatId.flatMap(this::normalizarChatIdOptional);
    }

    private Optional<String> buscarOverride(String email) {
        if (email.isBlank() || recipientOverrides.isEmpty() || recipientOverrides.get().isBlank()) {
            return Optional.empty();
        }
        for (String item : recipientOverrides.get().split("[,;\\n]")) {
            int separador = item.indexOf('=');
            if (separador <= 0) {
                continue;
            }
            String chave = item.substring(0, separador).trim().toLowerCase(Locale.ROOT);
            String valor = item.substring(separador + 1).trim();
            if (email.equals(chave) && !valor.isBlank()) {
                return Optional.of(valor);
            }
        }
        return Optional.empty();
    }

    private Optional<String> telefoneDoUsuario(Usuario usuario) {
        if (usuario.telefoneWhatsapp != null && !usuario.telefoneWhatsapp.isBlank()) {
            return Optional.of(usuario.telefoneWhatsapp);
        }
        if (usuario instanceof EmpresaParceira empresa && empresa.contato != null && !empresa.contato.isBlank()) {
            return Optional.of(empresa.contato);
        }
        return Optional.empty();
    }

    private Optional<String> normalizarChatIdOptional(String valor) {
        String chatId = normalizarChatId(valor);
        return chatId.isBlank() ? Optional.empty() : Optional.of(chatId);
    }

    private String normalizarChatId(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String normalizado = valor.trim();
        if (normalizado.contains("@")) {
            return normalizado;
        }
        String digitos = normalizado.replaceAll("\\D+", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }
        if (digitos.length() < 12) {
            return "";
        }
        return digitos + "@c.us";
    }
    private void enviarRespostaHumanizada(String chatId, String mensagem) throws Exception {
        if (!humanizedReplies) {
            enviarPeloWaha(chatId, mensagem);
            return;
        }

        sinalizarPresenca(chatId, "typing");
        esperarTempoDigitacao(mensagem);
        try {
            enviarPeloWaha(chatId, mensagem);
        } finally {
            sinalizarPresenca(chatId, "paused");
        }
    }

    private void esperarTempoDigitacao(String mensagem) {
        long tempo = tempoDigitacaoMs(mensagem);
        if (tempo <= 0) {
            return;
        }
        try {
            Thread.sleep(tempo);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
        }
    }

    private long tempoDigitacaoMs(String mensagem) {
        int minimo = Math.max(0, typingMinMs);
        int maximo = Math.max(minimo, typingMaxMs);
        int porCaractere = Math.max(0, typingMsPerChar);
        int tamanho = mensagem == null ? 0 : mensagem.length();
        long calculado = minimo + ((long) tamanho * porCaractere);
        return Math.min(maximo, calculado);
    }

    private void sinalizarPresenca(String chatId, String presenca) {
        if (!typingEnabled || chatId == null || chatId.isBlank()) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("chatId", chatId);
            payload.put("presence", presenca);

            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpointBase() + "/api/" + sessaoAtual() + "/presence"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");
            apiKey.filter(valor -> !valor.isBlank())
                    .ifPresent(valor -> builder.header("X-Api-Key", valor.trim()));

            HttpResponse<String> response = httpClient.send(
                    builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.warnf("WAHA nao aceitou presenca %s para %s: %s", presenca, chatId, resumoResposta(response.body()));
            }
        } catch (Exception erro) {
            LOG.warnf(erro, "Nao foi possivel sinalizar presenca %s no WhatsApp para %s.", presenca, chatId);
        }
    }


    private void enviarImagemPeloWaha(String chatId, String legenda) throws Exception {
        Map<String, Object> arquivo = new LinkedHashMap<>();
        arquivo.put("mimetype", "image/png");
        arquivo.put("filename", "valoriza-ae.png");
        arquivo.put("data", logoPngBase64());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("session", sessaoAtual());
        payload.put("chatId", chatId);
        payload.put("file", arquivo);
        payload.put("caption", legenda);

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpointBase() + "/api/sendImage"))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        apiKey.filter(valor -> !valor.isBlank())
                .ifPresent(valor -> builder.header("X-Api-Key", valor.trim()));

        HttpResponse<String> response = httpClient.send(
                builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RegraNegocioException("WAHA recusou o envio da logo: " + resumoResposta(response.body()));
        }
    }

    private String logoPngBase64() throws Exception {
        int size = 512;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255));
            g.fillRect(0, 0, size, size);

            GradientPaint capGradient = new GradientPaint(110, 105, new Color(6, 58, 95), 405, 300, new Color(2, 31, 54));
            GradientPaint coinGradient = new GradientPaint(185, 205, new Color(255, 210, 63), 330, 395, new Color(244, 169, 0));

            Path2D cap = new Path2D.Double();
            cap.moveTo(68, 186);
            cap.curveTo(60, 181, 60, 169, 69, 165);
            cap.lineTo(238, 92);
            cap.curveTo(255, 85, 273, 85, 290, 92);
            cap.lineTo(459, 165);
            cap.curveTo(468, 169, 468, 181, 459, 186);
            cap.lineTo(350, 233);
            cap.curveTo(324, 211, 294, 200, 264, 200);
            cap.curveTo(234, 200, 204, 211, 178, 233);
            cap.closePath();
            g.setPaint(capGradient);
            g.fill(cap);

            Path2D band = new Path2D.Double();
            band.moveTo(177, 232);
            band.curveTo(220, 198, 308, 198, 351, 232);
            band.lineTo(351, 302);
            band.curveTo(313, 265, 229, 265, 177, 302);
            band.closePath();
            g.setPaint(capGradient);
            g.fill(band);

            g.setPaint(capGradient);
            g.setStroke(new BasicStroke(24, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(407, 188, 407, 335);
            g.fill(new Ellipse2D.Double(383, 316, 48, 48));
            Path2D tassel = new Path2D.Double();
            tassel.moveTo(407, 347);
            tassel.lineTo(435, 444);
            tassel.lineTo(369, 444);
            tassel.closePath();
            g.fill(tassel);

            g.setPaint(coinGradient);
            g.fill(new Ellipse2D.Double(134, 205, 264, 264));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(22));
            g.draw(new Ellipse2D.Double(176, 247, 180, 180));
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    private void enviarPeloWaha(String chatId, String mensagem) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("session", sessaoAtual());
        payload.put("chatId", chatId);
        payload.put("text", mensagem);

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpointBase() + "/api/sendText"))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        apiKey.filter(valor -> !valor.isBlank())
                .ifPresent(valor -> builder.header("X-Api-Key", valor.trim()));

        HttpResponse<String> response = httpClient.send(
                builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RegraNegocioException("WAHA recusou o envio do WhatsApp: " + resumoResposta(response.body()));
        }
    }

    private String montarMensagem(String assunto, String conteudo, String codigoReferencia) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append(APP_NAME).append("\n");
        mensagem.append(assunto == null || assunto.isBlank() ? "Atualizacao do sistema" : assunto.trim());
        mensagem.append("\n\n").append(resumoLimpo(conteudo));
        if (codigoReferencia != null && !codigoReferencia.isBlank()) {
            mensagem.append("\n\nCodigo: ").append(codigoReferencia.trim().toUpperCase(Locale.ROOT));
        }
        mensagem.append("\nPainel: ").append(baseUrl());
        return mensagem.toString();
    }

    private String resumoLimpo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return "Acompanhe os detalhes no seu painel.";
        }
        String texto = conteudo.replaceAll("\\s+", " ")
                .replace("..", ".")
                .replace(" O QR Code e o status ficam disponiveis no seu painel.", "")
                .trim();
        return texto.length() > 700 ? texto.substring(0, 697).trim() + "..." : texto;
    }

    private String sessaoAtual() {
        return session == null || session.isBlank() ? "default" : session.trim();
    }

    private String endpointBase() {
        return endpoint == null || endpoint.isBlank()
                ? "http://localhost:3000"
                : endpoint.replaceAll("/+$", "");
    }

    private String baseUrl() {
        return publicUrl == null || publicUrl.isBlank()
                ? "http://localhost:8080"
                : publicUrl.replaceAll("/+$", "");
    }

    private String resumoResposta(String body) {
        if (body == null || body.isBlank()) {
            return "sem detalhes retornados pelo WAHA.";
        }
        String texto = body.replaceAll("\\s+", " ").trim();
        return texto.length() > 220 ? texto.substring(0, 220) + "..." : texto;
    }

    private void tratarFalha(RegraNegocioException erro, String destinatario) {
        tratarFalha(erro, destinatario, erro);
    }

    private void tratarFalha(RegraNegocioException erro, String destinatario, Throwable causa) {
        if (failOnError) {
            throw erro;
        }
        LOG.warnf(causa, "WhatsApp nao enviado para %s. %s", destinatario, erro.getMessage());
    }
}

package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.EmailNotificacao;
import br.com.sistemamoedas.repository.EmailNotificacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailOutboxGateway implements EmailGateway {

    private static final Logger LOG = Logger.getLogger(EmailOutboxGateway.class);
    private static final String APP_NAME = "Valoriza Ae";
    private static final DateTimeFormatter EMAIL_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern MOEDAS_PATTERN = Pattern.compile("(\\d[\\d.]*)\\s+moedas", Pattern.CASE_INSENSITIVE);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Inject
    EmailNotificacaoRepository emails;

    
    @Inject
    WhatsappGateway whatsapps;
@Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "valoriza.emailjs.enabled", defaultValue = "false")
    boolean emailJsEnabled;

    @ConfigProperty(name = "valoriza.emailjs.endpoint", defaultValue = "https://api.emailjs.com/api/v1.0/email/send")
    String emailJsEndpoint;

    @ConfigProperty(name = "valoriza.emailjs.service-id")
    Optional<String> emailJsServiceId;

    @ConfigProperty(name = "valoriza.emailjs.template-id")
    Optional<String> emailJsTemplateId;

    @ConfigProperty(name = "valoriza.emailjs.public-key")
    Optional<String> emailJsPublicKey;

    @ConfigProperty(name = "valoriza.app.public-url", defaultValue = "http://localhost:8080")
    String publicUrl;

    @ConfigProperty(name = "valoriza.emailjs.private-key")
    Optional<String> emailJsPrivateKey;

    @ConfigProperty(name = "valoriza.emailjs.ignored-domains", defaultValue = "moedas.com")
    String ignoredDomains;

    @Override
    @Transactional
    public void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia) {
        emails.persist(new EmailNotificacao(destinatario, assunto, conteudo, codigoReferencia));
        whatsapps.enviar(destinatario, assunto, conteudo, codigoReferencia);
        if (!deveEnviarEmailReal(destinatario)) {
            LOG.infof("Email registrado no painel para %s: %s", destinatario, assunto);
            return;
        }

        enviarPeloEmailJs(destinatario, assunto, conteudo, codigoReferencia);
        LOG.infof("Email enviado pelo EmailJS para %s: %s", destinatario, assunto);
    }

    private boolean deveEnviarEmailReal(String destinatario) {
        if (!emailJsEnabled) {
            return false;
        }
        if (destinatario == null || destinatario.isBlank()) {
            return false;
        }
        String email = destinatario.trim().toLowerCase(Locale.ROOT);
        for (String dominio : ignoredDomains.split(",")) {
            String normalizado = dominio.trim().toLowerCase(Locale.ROOT);
            if (!normalizado.isBlank() && email.endsWith("@" + normalizado)) {
                return false;
            }
        }
        return true;
    }

    private void enviarPeloEmailJs(String destinatario, String assunto, String conteudo, String codigoReferencia) {
        validarConfiguracaoEmailJs();
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("service_id", configObrigatoria(emailJsServiceId));
            payload.put("template_id", configObrigatoria(emailJsTemplateId));
            payload.put("user_id", configObrigatoria(emailJsPublicKey));
            emailJsPrivateKey
                    .filter(valor -> !valor.isBlank())
                    .ifPresent(valor -> payload.put("accessToken", valor));

            EmailTemplateModel modelo = montarModelo(assunto, conteudo, codigoReferencia);
            Map<String, Object> parametros = new LinkedHashMap<>();
            parametros.put("app_name", APP_NAME);
            parametros.put("from_name", APP_NAME);
            parametros.put("logo_url", baseUrl() + "/assets/logo-valoriza-ae.svg");
            parametros.put("to_email", destinatario.trim());
            parametros.put("to_name", destinatario.trim());
            parametros.put("reply_to", destinatario.trim());
            parametros.put("email", destinatario.trim());
            parametros.put("user_email", destinatario.trim());
            parametros.put("recipient_email", destinatario.trim());
            parametros.put("name", modelo.audience);
            parametros.put("title", modelo.headline);
            parametros.put("time", LocalDateTime.now().format(EMAIL_TIME_FORMAT));
            parametros.put("subject", assunto);
            parametros.put("assunto", assunto);
            parametros.put("message", conteudo);
            parametros.put("mensagem", conteudo);
            parametros.put("preheader", modelo.preheader);
            parametros.put("audience", modelo.audience);
            parametros.put("email_tag", modelo.tag);
            parametros.put("headline", modelo.headline);
            parametros.put("intro_text", modelo.introText);
            parametros.put("summary_text", modelo.summaryText);
            parametros.put("status_label", modelo.statusLabel);
            parametros.put("status_value", modelo.statusValue);
            parametros.put("highlight_label", modelo.highlightLabel);
            parametros.put("highlight_value", modelo.highlightValue);
            parametros.put("highlight_hint", modelo.highlightHint);
            parametros.put("moedas_label", modelo.highlightLabel);
            parametros.put("moedas_value", modelo.highlightValue);
            parametros.put("reference_label", modelo.referenceLabel);
            parametros.put("reference_value", modelo.referenceValue);
            parametros.put("codigo_referencia", modelo.referenceValue);
            parametros.put("codigo", modelo.referenceValue);
            parametros.put("status", modelo.statusValue);
            parametros.put("action_url", modelo.actionUrl);
            parametros.put("action_href", modelo.actionUrl);
            parametros.put("button_url", modelo.actionUrl);
            parametros.put("redirect_url", modelo.actionUrl);
            parametros.put("link", modelo.actionUrl);
            parametros.put("action_label", modelo.actionLabel);
            parametros.put("footer_note", modelo.footerNote);
            parametros.put("detail_one_label", modelo.detailOneLabel);
            parametros.put("detail_one_value", modelo.detailOneValue);
            parametros.put("detail_two_label", modelo.detailTwoLabel);
            parametros.put("detail_two_value", modelo.detailTwoValue);
            parametros.put("detail_three_label", modelo.detailThreeLabel);
            parametros.put("detail_three_value", modelo.detailThreeValue);
            parametros.put("flow_one_title", modelo.flowOneTitle);
            parametros.put("flow_one_text", modelo.flowOneText);
            parametros.put("flow_two_title", modelo.flowTwoTitle);
            parametros.put("flow_two_text", modelo.flowTwoText);
            parametros.put("flow_three_title", modelo.flowThreeTitle);
            parametros.put("flow_three_text", modelo.flowThreeText);
            parametros.put("secondary_action_label", modelo.secondaryActionLabel);
            parametros.put("secondary_action_url", modelo.secondaryActionUrl);
            parametros.put("support_note", modelo.supportNote);
            payload.put("template_params", parametros);

            HttpRequest request = HttpRequest.newBuilder(URI.create(emailJsEndpoint))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String detalhes = respostaEmailJs(response.body());
                LOG.errorf("EmailJS recusou o envio para %s. Status %d. Resposta: %s",
                        destinatario, response.statusCode(), detalhes);
                throw new RegraNegocioException("EmailJS recusou o envio: " + detalhes);
            }
        } catch (RegraNegocioException erro) {
            throw erro;
        } catch (Exception erro) {
            LOG.errorf(erro, "Falha ao enviar email pelo EmailJS para %s", destinatario);
            throw new RegraNegocioException("Nao foi possivel enviar o email real pelo EmailJS.");
        }
    }

    private void validarConfiguracaoEmailJs() {
        if (configObrigatoria(emailJsServiceId).isBlank()
                || configObrigatoria(emailJsTemplateId).isBlank()
                || configObrigatoria(emailJsPublicKey).isBlank()) {
            throw new RegraNegocioException("Configuracao do EmailJS incompleta.");
        }
    }

    private String configObrigatoria(Optional<String> valor) {
        return valor.filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .orElse("");
    }

    private EmailTemplateModel montarModelo(String assunto, String conteudo, String codigoReferencia) {
        String codigo = codigoReferencia == null || codigoReferencia.isBlank()
                ? "Atualizacao registrada"
                : codigoReferencia.trim().toUpperCase(Locale.ROOT);
        String painelUrl = baseUrl();
        String validacaoUrl = codigoReferencia == null || codigoReferencia.isBlank()
                ? painelUrl
                : painelUrl + "/empresa?cupom=" + codigo;
        String redefinicaoSenhaUrl = codigoReferencia == null || codigoReferencia.isBlank()
                ? painelUrl + "/esqueci-senha"
                : painelUrl + "/redefinir-senha?token=" + codigoReferencia.trim();

        EmailTemplateModel modelo = new EmailTemplateModel();
        modelo.preheader = resumo(assunto, conteudo);
        modelo.headline = assunto;
        modelo.referenceValue = codigo;
        modelo.secondaryActionUrl = painelUrl;

        switch (assunto) {
            case "Voce recebeu moedas estudantis" -> {
                modelo.audience = "Aluno";
                modelo.tag = "Moedas recebidas";
                modelo.headline = "Moedas recebidas";
                modelo.introText = "Seu professor reconheceu sua participacao e seu saldo foi atualizado.";
                modelo.summaryText = resumoMoedasRecebidas(conteudo);
                modelo.statusLabel = "Enviado por";
                modelo.statusValue = extrairProfessorMoedasRecebidas(conteudo);
                modelo.highlightLabel = "Moedas recebidas";
                modelo.highlightValue = extrairMoedas(conteudo);
                modelo.highlightHint = extrairSaldoAtual(conteudo);
                modelo.referenceLabel = "Tipo";
                modelo.referenceValue = "Credito de moedas";
                modelo.actionLabel = "Abrir meu painel";
                modelo.actionUrl = painelUrl + "/aluno";
                modelo.detailOneLabel = "Impacto";
                modelo.detailOneValue = "Saldo e extrato atualizados";
                modelo.detailTwoLabel = "Origem";
                modelo.detailTwoValue = "Reconhecimento do professor";
                modelo.detailThreeLabel = "Uso";
                modelo.detailThreeValue = "Disponivel para vantagens";
                modelo.flowOneTitle = "Recebido";
                modelo.flowOneText = "O professor registrou o reconhecimento com justificativa.";
                modelo.flowTwoTitle = "Disponivel";
                modelo.flowTwoText = "As moedas ja aparecem no saldo do aluno.";
                modelo.flowThreeTitle = "Proximo passo";
                modelo.flowThreeText = "O aluno pode acompanhar metas e resgatar vantagens no catalogo.";
                modelo.footerNote = "Confira o saldo, o extrato e as vantagens disponiveis no seu painel.";
            }
            case "Envio de moedas confirmado" -> {
                modelo.audience = "Professor";
                modelo.tag = "Envio confirmado";
                modelo.headline = "Moedas enviadas ao aluno";
                modelo.introText = "O aluno recebeu as moedas e o motivo ficou salvo no extrato.";
                modelo.summaryText = resumoMoedasEnviadas(conteudo);
                modelo.statusLabel = "Aluno";
                modelo.statusValue = extrairAlunoMoedasEnviadas(conteudo);
                modelo.highlightLabel = "Moedas enviadas";
                modelo.highlightValue = extrairMoedas(conteudo);
                modelo.highlightHint = "Cota do professor atualizada";
                modelo.referenceLabel = "Tipo";
                modelo.referenceValue = "Envio de moedas";
                modelo.actionLabel = "Ver extrato";
                modelo.actionUrl = painelUrl + "/professor";
                modelo.detailOneLabel = "Auditoria";
                modelo.detailOneValue = "Historico do professor";
                modelo.detailTwoLabel = "Aluno";
                modelo.detailTwoValue = "Notificado no painel";
                modelo.detailThreeLabel = "Cota";
                modelo.detailThreeValue = "Saldo recalculado";
                modelo.flowOneTitle = "Enviado";
                modelo.flowOneText = "As moedas foram debitadas da cota do professor.";
                modelo.flowTwoTitle = "Registrado";
                modelo.flowTwoText = "A justificativa ficou vinculada ao extrato.";
                modelo.flowThreeTitle = "Visivel";
                modelo.flowThreeText = "O aluno consegue ver o recebimento no proprio painel.";
                modelo.footerNote = "Use o extrato para acompanhar sua cota semestral e todos os reconhecimentos enviados.";
            }
            case "Cupom de troca gerado" -> {
                modelo.audience = "Aluno";
                modelo.tag = "Cupom gerado";
                modelo.headline = "Cupom pronto para apresentar";
                modelo.introText = "A vantagem foi reservada. Mostre o codigo ou o QR Code no parceiro e aguarde a validacao.";
                modelo.summaryText = resumoCupomAluno(conteudo, codigo);
                modelo.statusValue = "Pendente de validacao";
                modelo.highlightLabel = "Codigo do cupom";
                modelo.highlightValue = codigo;
                modelo.highlightHint = "Use somente depois da validacao";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Abrir meu cupom";
                modelo.actionUrl = painelUrl + "/aluno";
                modelo.secondaryActionLabel = "Ver no painel";
                modelo.secondaryActionUrl = painelUrl + "/aluno";
                modelo.detailOneLabel = "Apresentacao";
                modelo.detailOneValue = "Codigo ou QR Code";
                modelo.detailTwoLabel = "Uso";
                modelo.detailTwoValue = "Aguardar validacao";
                modelo.detailThreeLabel = "Status";
                modelo.detailThreeValue = "Aparece no painel";
                modelo.flowOneTitle = "Resgatado";
                modelo.flowOneText = "As moedas foram usadas para reservar a vantagem.";
                modelo.flowTwoTitle = "Apresente";
                modelo.flowTwoText = "Mostre o codigo ou o QR Code no parceiro.";
                modelo.flowThreeTitle = "Valide";
                modelo.flowThreeText = "O beneficio e liberado quando o parceiro confirma o atendimento.";
                modelo.footerNote = "O beneficio so deve ser utilizado depois que o parceiro confirmar o cupom.";
            }
            case "Nova troca para validar" -> {
                modelo.audience = "Empresa parceira";
                modelo.tag = "Validacao pendente";
                modelo.headline = "Cupom aguardando validacao";
                modelo.introText = "Confira o codigo apresentado pelo aluno e valide somente quando entregar o beneficio.";
                modelo.summaryText = resumoCupomEmpresa(conteudo, codigo);
                modelo.statusValue = "Aguardando atendimento";
                modelo.highlightLabel = "Codigo do cupom";
                modelo.highlightValue = codigo;
                modelo.highlightHint = "Validar apenas no atendimento";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Validar cupom";
                modelo.actionUrl = validacaoUrl;
                modelo.detailOneLabel = "Responsavel";
                modelo.detailOneValue = "Parceiro da vantagem";
                modelo.detailTwoLabel = "Conferencia";
                modelo.detailTwoValue = "Codigo apresentado";
                modelo.detailThreeLabel = "Resultado";
                modelo.detailThreeValue = "Entrega confirmada";
                modelo.flowOneTitle = "Receba";
                modelo.flowOneText = "O aluno apresenta o codigo ou QR Code.";
                modelo.flowTwoTitle = "Confira";
                modelo.flowTwoText = "Verifique se o cupom pertence a sua empresa.";
                modelo.flowThreeTitle = "Confirme";
                modelo.flowThreeText = "Valide apenas depois de entregar o beneficio.";
                modelo.footerNote = "A validacao confirma que a vantagem foi entregue e evita uso duplicado do mesmo cupom.";
            }
            case "Cupom ja validado no atendimento" -> {
                modelo.audience = "Empresa parceira";
                modelo.tag = "Cupom ja validado";
                modelo.headline = "Este atendimento ja consta como concluido";
                modelo.introText = "O cupom informado ja aparece como validado no historico da empresa.";
                modelo.summaryText = "Este cupom ja foi validado. Consulte o historico antes de liberar qualquer novo atendimento.";
                modelo.statusValue = "Validado";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Abrir painel";
                modelo.actionUrl = validacaoUrl;
                modelo.detailOneLabel = "Atendimento";
                modelo.detailOneValue = "Concluido";
                modelo.detailTwoLabel = "Uso duplicado";
                modelo.detailTwoValue = "Bloqueado";
                modelo.detailThreeLabel = "Historico";
                modelo.detailThreeValue = "Disponivel";
                modelo.flowOneTitle = "Consultado";
                modelo.flowOneText = "O cupom foi localizado no painel.";
                modelo.flowTwoTitle = "Conferido";
                modelo.flowTwoText = "O status atual ja esta validado.";
                modelo.flowThreeTitle = "Arquivado";
                modelo.flowThreeText = "O registro permanece no historico da empresa.";
                modelo.footerNote = "Use o painel para acompanhar cupons pendentes, validados e historico de vantagens.";
            }
            case "Cupom validado" -> {
                modelo.audience = "Aluno";
                modelo.tag = "Beneficio confirmado";
                modelo.headline = "Seu cupom foi validado pelo parceiro";
                modelo.introText = "A troca foi concluida e o registro ja aparece no seu extrato.";
                modelo.summaryText = resumoLimpo(conteudo);
                modelo.statusValue = "Beneficio utilizado";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Ver extrato";
                modelo.actionUrl = painelUrl + "/aluno";
                modelo.detailOneLabel = "Validacao";
                modelo.detailOneValue = "Confirmada";
                modelo.detailTwoLabel = "Beneficio";
                modelo.detailTwoValue = "Entregue pelo parceiro";
                modelo.detailThreeLabel = "Historico";
                modelo.detailThreeValue = "Extrato atualizado";
                modelo.flowOneTitle = "Apresentado";
                modelo.flowOneText = "O cupom foi apresentado no parceiro.";
                modelo.flowTwoTitle = "Validado";
                modelo.flowTwoText = "A empresa confirmou o atendimento.";
                modelo.flowThreeTitle = "Concluido";
                modelo.flowThreeText = "A troca ficou salva no extrato do aluno.";
                modelo.footerNote = "Cupons validados continuam disponiveis no historico para consulta.";
            }
            case "Cupom temporariamente desativado" -> {
                modelo.audience = "Aluno";
                modelo.tag = "Cupom pausado";
                modelo.headline = "A vantagem foi pausada pelo parceiro";
                modelo.introText = "Aguarde a republicacao antes de tentar utilizar este cupom no atendimento.";
                modelo.summaryText = resumoLimpo(conteudo);
                modelo.statusValue = "Uso pausado";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Acompanhar cupom";
                modelo.actionUrl = painelUrl + "/aluno";
                modelo.detailOneLabel = "Cupom";
                modelo.detailOneValue = "Continua registrado";
                modelo.detailTwoLabel = "Atendimento";
                modelo.detailTwoValue = "Temporariamente pausado";
                modelo.detailThreeLabel = "Proximo passo";
                modelo.detailThreeValue = "Aguardar reativacao";
                modelo.flowOneTitle = "Pausado";
                modelo.flowOneText = "O parceiro interrompeu temporariamente a vantagem.";
                modelo.flowTwoTitle = "Aguarde";
                modelo.flowTwoText = "Nao tente utilizar enquanto estiver pausado.";
                modelo.flowThreeTitle = "Retorno";
                modelo.flowThreeText = "Voce sera avisado quando o cupom voltar a ficar disponivel.";
                modelo.footerNote = "O cupom continua registrado, mas deve aguardar a vantagem voltar ao catalogo.";
            }
            case "Cupom disponivel novamente" -> {
                modelo.audience = "Aluno";
                modelo.tag = "Cupom reativado";
                modelo.headline = "A vantagem voltou a ficar disponivel";
                modelo.introText = "O cupom pode ser apresentado novamente ao parceiro para validacao.";
                modelo.summaryText = resumoLimpo(conteudo);
                modelo.statusValue = "Disponivel";
                modelo.referenceLabel = "Codigo do cupom";
                modelo.actionLabel = "Abrir meu cupom";
                modelo.actionUrl = painelUrl + "/aluno";
                modelo.secondaryActionLabel = "Ver no painel";
                modelo.secondaryActionUrl = painelUrl + "/aluno";
                modelo.detailOneLabel = "Cupom";
                modelo.detailOneValue = "Ativo novamente";
                modelo.detailTwoLabel = "Apresentacao";
                modelo.detailTwoValue = "Codigo ou QR Code";
                modelo.detailThreeLabel = "Validacao";
                modelo.detailThreeValue = "Necessaria no parceiro";
                modelo.flowOneTitle = "Reativado";
                modelo.flowOneText = "A vantagem voltou ao catalogo do parceiro.";
                modelo.flowTwoTitle = "Apresente";
                modelo.flowTwoText = "Mostre o cupom durante o atendimento.";
                modelo.flowThreeTitle = "Confirme";
                modelo.flowThreeText = "Aguarde a validacao antes de usar o beneficio.";
                modelo.footerNote = "Apresente o codigo ou QR Code e aguarde a confirmacao do parceiro.";
            }
            case "Recuperacao de senha" -> {
                modelo.audience = "Conta";
                modelo.tag = "Recuperacao de acesso";
                modelo.headline = "Crie uma nova senha para acessar sua conta";
                modelo.introText = "Recebemos um pedido para trocar a senha da sua conta no Valoriza Ae.";
                modelo.summaryText = "Use o botao para criar uma nova senha. O link expira em 30 minutos e funciona apenas uma vez.";
                modelo.statusValue = "Link valido por 30 minutos";
                modelo.highlightLabel = "Validade do link";
                modelo.highlightValue = "30 minutos";
                modelo.highlightHint = "Uso unico";
                modelo.referenceLabel = "Seguranca";
                modelo.referenceValue = "Uso unico";
                modelo.actionLabel = "Criar nova senha";
                modelo.actionUrl = redefinicaoSenhaUrl;
                modelo.secondaryActionLabel = "Voltar ao login";
                modelo.secondaryActionUrl = painelUrl + "/login";
                modelo.detailOneLabel = "Validade";
                modelo.detailOneValue = "30 minutos";
                modelo.detailTwoLabel = "Uso";
                modelo.detailTwoValue = "Apenas uma vez";
                modelo.detailThreeLabel = "Acesso";
                modelo.detailThreeValue = "Senha nova no login";
                modelo.flowOneTitle = "Abra o link";
                modelo.flowOneText = "Use o botao deste email para acessar a troca de senha.";
                modelo.flowTwoTitle = "Defina a senha";
                modelo.flowTwoText = "Escolha uma nova senha com pelo menos 6 caracteres.";
                modelo.flowThreeTitle = "Entre novamente";
                modelo.flowThreeText = "Depois da troca, use a nova senha para acessar seu painel.";
                modelo.footerNote = "Se voce nao pediu essa alteracao, ignore este email. Sua senha atual continua a mesma.";
                modelo.supportNote = "Por seguranca, o link expira em 30 minutos e deixa de funcionar depois da primeira troca.";
            }
            default -> {
                modelo.audience = "Usuario";
                modelo.tag = "Atualizacao";
                modelo.introText = "Uma nova atualizacao foi registrada no Valoriza Ae.";
                modelo.summaryText = resumoLimpo(conteudo);
                modelo.referenceLabel = "Referencia";
                modelo.actionLabel = "Abrir painel";
                modelo.actionUrl = painelUrl;
                modelo.footerNote = "Acesse o Valoriza Ae para ver os detalhes completos.";
            }
        }
        return modelo;
    }

    private String extrairMoedas(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return "Moedas registradas";
        }
        Matcher matcher = MOEDAS_PATTERN.matcher(conteudo);
        if (matcher.find()) {
            return matcher.group(1) + " moedas";
        }
        return "Moedas registradas";
    }

    private String resumoMoedasRecebidas(String conteudo) {
        Matcher novo = Pattern.compile("Professor (.+?) enviou (.+?)\\. Saldo atual: (.+?)\\. Motivo: (.+?)(?:\\.|$)",
                Pattern.CASE_INSENSITIVE).matcher(conteudo == null ? "" : conteudo);
        if (novo.find()) {
            return novo.group(2).trim() + " enviados por " + novo.group(1).trim()
                    + ". Motivo: " + normalizarPontuacao(novo.group(4));
        }
        Matcher matcher = Pattern.compile("Voce recebeu (.+?) de (.+?)\\. Motivo informado: (.+?)(?:\\. O valor|\\.$)",
                Pattern.CASE_INSENSITIVE).matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return matcher.group(1).trim() + " de " + matcher.group(2).trim() + ". Motivo: "
                    + normalizarPontuacao(matcher.group(3));
        }
        return resumoLimpo(conteudo);
    }

    private String extrairSaldoAtual(String conteudo) {
        Matcher matcher = Pattern.compile("Saldo atual:\\s*(.+?)\\.", Pattern.CASE_INSENSITIVE)
                .matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return "Saldo atual: " + matcher.group(1).trim();
        }
        return "Disponivel para resgatar beneficios";
    }

    private String resumoMoedasEnviadas(String conteudo) {
        Matcher matcher = Pattern.compile("Envio confirmado para (.+?): (.+?) registradas\\. Motivo informado: (.+?)(?:\\. O registro|\\.$)",
                Pattern.CASE_INSENSITIVE).matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return matcher.group(2).trim() + " para " + matcher.group(1).trim() + ". Motivo: "
                    + normalizarPontuacao(matcher.group(3));
        }
        return resumoLimpo(conteudo);
    }

    private String extrairProfessorMoedasRecebidas(String conteudo) {
        String texto = conteudo == null ? "" : conteudo;
        Matcher novo = Pattern.compile("Professor (.+?) enviou", Pattern.CASE_INSENSITIVE).matcher(texto);
        if (novo.find()) {
            return novo.group(1).trim();
        }
        Matcher antigo = Pattern.compile("Voce recebeu .+? de (.+?)\\. Motivo", Pattern.CASE_INSENSITIVE)
                .matcher(texto);
        if (antigo.find()) {
            return antigo.group(1).trim();
        }
        return "Professor";
    }

    private String extrairAlunoMoedasEnviadas(String conteudo) {
        Matcher matcher = Pattern.compile("Envio confirmado para (.+?):", Pattern.CASE_INSENSITIVE)
                .matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Aluno";
    }

    private String resumoCupomAluno(String conteudo, String codigo) {
        Matcher matcher = Pattern.compile("Cupom (.+?) gerado para (.+?)\\. Mostre", Pattern.CASE_INSENSITIVE)
                .matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return "Cupom " + matcher.group(1).trim() + " para " + matcher.group(2).trim()
                    + ". Apresente no parceiro e aguarde a validacao.";
        }
        return "Cupom " + codigo + " gerado. Apresente no parceiro e aguarde a validacao.";
    }

    private String resumoCupomEmpresa(String conteudo, String codigo) {
        Matcher matcher = Pattern.compile("Novo cupom para validar: (.+?)\\. Aluno: (.+?)\\. Vantagem: (.+?)\\. Use",
                Pattern.CASE_INSENSITIVE).matcher(conteudo == null ? "" : conteudo);
        if (matcher.find()) {
            return "Cupom " + matcher.group(1).trim() + ". Aluno: " + matcher.group(2).trim()
                    + ". Vantagem: " + matcher.group(3).trim() + ".";
        }
        return "Cupom " + codigo + " aguardando validacao no atendimento.";
    }

    private String resumoLimpo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return "Confira os detalhes no seu painel.";
        }
        String texto = normalizarPontuacao(conteudo)
                .replace(" O registro ja aparece no seu extrato e no painel do aluno.", "")
                .replace(" O valor ja aparece no seu saldo e no extrato.", "")
                .replace(" O QR Code e o status ficam disponiveis no seu painel.", "");
        return texto.length() > 180 ? texto.substring(0, 177).trim() + "..." : texto;
    }

    private String normalizarPontuacao(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        return texto.replaceAll("\\s+", " ").replace("..", ".").trim();
    }

    private String resumo(String assunto, String conteudo) {
        return assunto + " - acompanhe os detalhes pelo seu painel.";
    }

    private String respostaEmailJs(String body) {
        if (body == null || body.isBlank()) {
            return "sem detalhes retornados pelo provedor.";
        }
        String texto = body.replaceAll("\\s+", " ").trim();
        if (texto.length() > 240) {
            return texto.substring(0, 240) + "...";
        }
        return texto;
    }

    private String baseUrl() {
        return publicUrl == null || publicUrl.isBlank()
                ? "http://localhost:8080"
                : publicUrl.replaceAll("/+$", "");
    }

    private static class EmailTemplateModel {
        String audience = "Usuario";
        String tag = "Atualizacao";
        String headline = "Atualizacao no Valoriza Ae";
        String introText = "Uma nova movimentacao foi registrada no Valoriza Ae.";
        String summaryText = "Confira os detalhes no seu painel.";
        String statusLabel = "Status";
        String statusValue = "Registrado";
        String highlightLabel = "Destaque";
        String highlightValue = "Atualizacao registrada";
        String highlightHint = "Confira no painel";
        String referenceLabel = "Referencia";
        String referenceValue = "Atualizacao registrada";
        String actionLabel = "Abrir painel";
        String actionUrl = "#";
        String secondaryActionLabel = "Ver detalhes";
        String secondaryActionUrl = "#";
        String footerNote = "Acesse o Valoriza Ae para acompanhar os detalhes.";
        String supportNote = "Se algo parecer incorreto, confira seu painel antes de tomar qualquer acao.";
        String detailOneLabel = "Registro";
        String detailOneValue = "Registro salvo";
        String detailTwoLabel = "Painel";
        String detailTwoValue = "Atualizado";
        String detailThreeLabel = "Historico";
        String detailThreeValue = "Disponivel";
        String flowOneTitle = "Registro";
        String flowOneText = "A movimentacao foi salva no Valoriza Ae.";
        String flowTwoTitle = "Notificacao";
        String flowTwoText = "Os envolvidos recebem a atualizacao.";
        String flowThreeTitle = "Acompanhamento";
        String flowThreeText = "O historico fica disponivel no painel.";
        String preheader = "Atualizacao registrada no Valoriza Ae.";
    }
}

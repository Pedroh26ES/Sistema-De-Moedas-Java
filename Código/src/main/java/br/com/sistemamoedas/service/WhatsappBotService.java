package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.TipoTransacao;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.domain.Usuario;
import br.com.sistemamoedas.domain.Vantagem;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.EmpresaParceiraRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import br.com.sistemamoedas.repository.UsuarioRepository;
import br.com.sistemamoedas.security.SenhaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class WhatsappBotService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private final ConcurrentMap<String, Perfil> perfisSelecionados = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> usuariosAutenticados = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> emailsEmLogin = new ConcurrentHashMap<>();

    @Inject
    UsuarioRepository usuarios;

    @Inject
    SenhaService senhas;

    @Inject
    AlunoRepository alunos;

    @Inject
    EmpresaParceiraRepository empresas;

    @Inject
    TransacaoRepository transacoes;

    @Inject
    AlunoService alunoService;

    @Inject
    MoedaService moedaService;

    @Inject
    VantagemService vantagemService;

    @Inject
    WhatsappGateway whatsapp;

    @ConfigProperty(name = "valoriza.whatsapp.recipient-overrides")
    Optional<String> recipientOverrides;

    public String processarMensagem(String chatId, String texto) {
        String telefone = normalizarTelefone(chatId);
        String mensagem = texto == null ? "" : texto.trim();
        if (mensagem.isBlank()) {
            return conviteLogin(List.of());
        }

        Optional<CredenciaisWhatsapp> login = loginSolicitado(mensagem);
        if (login.isPresent()) {
            return autenticarWhatsapp(telefone, login.get());
        }

        String comando = normalizarComando(mensagem);
        if (comando.equals("sair") || comando.equals("logout")) {
            usuariosAutenticados.remove(telefone);
            perfisSelecionados.remove(telefone);
            emailsEmLogin.remove(telefone);
            return "✅ Atendimento encerrado neste WhatsApp.\n\nQuando quiser voltar, envie *oi* e eu inicio seu acesso novamente.";
        }

        if (!usuariosAutenticados.containsKey(telefone)) {
            Optional<String> emailPendente = Optional.ofNullable(emailsEmLogin.get(telefone));
            if (emailPendente.isPresent()) {
                return autenticarWhatsapp(telefone, new CredenciaisWhatsapp(emailPendente.get(), mensagem));
            }

            Optional<String> emailInformado = emailInformado(mensagem);
            if (emailInformado.isPresent()) {
                emailsEmLogin.put(telefone, emailInformado.get());
                return "Perfeito! Encontrei seu e-mail no atendimento.\n\nAgora informe sua senha para eu liberar seu painel no Valoriza Aê. 🔐";
            }
        }

        List<Usuario> candidatos = usuariosPorTelefone(telefone);
        Optional<Perfil> trocaPerfil = perfilSolicitado(mensagem);
        if (!usuariosAutenticados.containsKey(telefone)) {
            if (trocaPerfil.isPresent() && !candidatos.isEmpty()) {
                return orientarLoginPorPerfil(trocaPerfil.get(), candidatos);
            }
            return candidatos.isEmpty() ? usuarioNaoEncontrado(telefone) : conviteLogin(candidatos);
        }

        if (candidatos.isEmpty()) {
            usuariosAutenticados.remove(telefone);
            perfisSelecionados.remove(telefone);
            return usuarioNaoEncontrado(telefone);
        }

        if (trocaPerfil.isPresent()) {
            return orientarLoginPorPerfil(trocaPerfil.get(), candidatos);
        }

        Usuario usuario = escolherUsuario(telefone, candidatos);
        if (usuario == null) {
            return conviteLogin(candidatos);
        }

        if (comando.equals("menu") || comando.equals("ajuda") || comando.equals("inicio") || comando.equals("0")) {
            return menu(usuario);
        }
        if (comando.equals("trocar") || comando.equals("perfil")) {
            usuariosAutenticados.remove(telefone);
            perfisSelecionados.remove(telefone);
            return menuEscolhaPerfil(candidatos);
        }

        try {
            return switch (usuario.perfil) {
                case ALUNO -> responderAluno((Aluno) usuario, comando, mensagem);
                case PROFESSOR -> responderProfessor((Professor) usuario, comando, mensagem);
                case EMPRESA -> responderEmpresa((EmpresaParceira) usuario, comando, mensagem);
            };
        } catch (RegraNegocioException erro) {
            return "⚠️ Não consegui concluir essa ação: " + erro.getMessage() + "\n\nEnvie *menu* para voltar às opções.";
        } catch (Exception erro) {
            return "🤔 Não consegui entender essa ação agora.\n\nEnvie *menu* para ver o que posso fazer por você.";
        }
    }

    private String autenticarWhatsapp(String telefone, CredenciaisWhatsapp credenciais) {
        if (telefone == null || telefone.isBlank()) {
            return "⚠️ Não consegui identificar este WhatsApp. Tente novamente pelo atendimento conectado ao Valoriza Aê.";
        }

        Optional<Usuario> usuarioEncontrado = usuarios.porEmail(credenciais.email());
        if (usuarioEncontrado.isEmpty() || !usuarioEncontrado.get().ativo
                || !senhas.confere(credenciais.senha(), usuarioEncontrado.get().senhaHash)) {
            emailsEmLogin.remove(telefone);
            return "🔐 Não consegui confirmar esse acesso.\n\nConfira seu e-mail e senha. Para tentar novamente, informe primeiro o seu e-mail cadastrado.";
        }

        Usuario usuario = usuarioEncontrado.get();
        if (!telefonePertenceAoUsuario(usuario, telefone)) {
            emailsEmLogin.remove(telefone);
            return "🔒 Esse login existe, mas este WhatsApp não está vinculado a essa conta.\n\nAtualize o telefone no perfil ou use o número configurado para teste.";
        }

        emailsEmLogin.remove(telefone);
        usuariosAutenticados.put(telefone, usuario.id);
        perfisSelecionados.put(telefone, usuario.perfil);
        return "✅ Login confirmado como " + nomePerfil(usuario.perfil) + ".\n\n" + menu(usuario);
    }

    private Optional<CredenciaisWhatsapp> loginSolicitado(String mensagem) {
        Matcher login = Pattern.compile("^(login|entrar)\\s+(\\S+@\\S+)\\s+(.+)$", Pattern.CASE_INSENSITIVE)
                .matcher(mensagem == null ? "" : mensagem.trim());
        if (!login.find()) {
            return Optional.empty();
        }
        return Optional.of(new CredenciaisWhatsapp(login.group(2).trim().toLowerCase(Locale.ROOT), login.group(3).trim()));
    }

    private Optional<String> emailInformado(String mensagem) {
        String texto = mensagem == null ? "" : mensagem.trim().toLowerCase(Locale.ROOT);
        if (!texto.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return Optional.empty();
        }
        return Optional.of(texto);
    }

    private boolean telefonePertenceAoUsuario(Usuario usuario, String telefone) {
        String normalizado = normalizarTelefone(telefone);
        if (normalizado.isBlank()) {
            return false;
        }
        if (telefonesCompativeis(normalizado, usuario.telefoneWhatsapp)) {
            return true;
        }
        if (usuario instanceof EmpresaParceira empresa && telefonesCompativeis(normalizado, empresa.contato)) {
            return true;
        }
        return emailsPorTelefoneNoOverride(normalizado).stream()
                .anyMatch(email -> email.equalsIgnoreCase(usuario.email));
    }

    public String processarWebhook(String chatId, String texto) {
        String resposta = processarMensagem(chatId, texto);
        if (deveEnviarLogoBoasVindas(resposta)) {
            whatsapp.enviarLogoParaChat(chatId, "Valoriza Aê | Atendimento acadêmico");
        }
        whatsapp.enviarParaChat(chatId, resposta);
        return resposta;
    }

    private boolean deveEnviarLogoBoasVindas(String resposta) {
        return resposta != null && resposta.startsWith("👋 Olá! Eu sou José");
    }

    private String responderAluno(Aluno aluno, String comando, String original) {
        if (comando.equals("1") || comando.equals("saldo")) {
            return "🪙 Valoriza Aê - Saldo\n\n" + aluno.nome + ", você tem *" + aluno.saldoMoedas
                    + " moedas disponíveis*.\n\nEnvie *vantagens* para escolher um benefício ou *cupons* para acompanhar seus resgates.";
        }
        if (comando.equals("2") || comando.equals("extrato")) {
            return extratoAluno(aluno);
        }
        if (comando.equals("3") || comando.equals("vantagens") || comando.equals("catalogo")) {
            return catalogoAluno(aluno);
        }
        if (comando.equals("4") || comando.equals("cupons")) {
            return cuponsAluno(aluno);
        }
        Matcher resgate = Pattern.compile("^(resgatar|trocar)\\s+(\\d+)$", Pattern.CASE_INSENSITIVE).matcher(original);
        if (resgate.find()) {
            Long vantagemId = Long.valueOf(resgate.group(2));
            String codigo = vantagemService.resgatar(aluno.id, vantagemId);
            return "🎟️ Cupom gerado com sucesso!\n\nCódigo: *" + codigo
                    + "*\nMostre o código ou o QR Code no parceiro e aguarde a validação antes de usar o benefício.";
        }
        return "🤔 Não entendi essa opção de aluno.\n\n" + menu(aluno);
    }

    private String responderProfessor(Professor professor, String comando, String original) {
        if (comando.equals("1") || comando.equals("cota") || comando.equals("saldo")) {
            return "👩‍🏫 Valoriza Aê - Professor\n\nCota disponível: *" + professor.saldoMoedas
                    + " moedas*.\nDepartamento: " + professor.departamento
                    + "\nInstituição: " + professor.instituicao.nome;
        }
        if (comando.equals("2") || comando.equals("alunos")) {
            return listarAlunos();
        }
        if (comando.equals("3") || comando.equals("extrato")) {
            return extratoProfessor(professor);
        }
        Matcher envio = Pattern.compile("^enviar\\s+(\\d+)\\s+(\\d+)\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(original);
        if (envio.find()) {
            Long alunoId = Long.valueOf(envio.group(1));
            int valor = Integer.parseInt(envio.group(2));
            String motivo = envio.group(3).trim();
            moedaService.enviarMoedas(professor.id, alunoId, valor, motivo);
            return "✅ Moedas enviadas com sucesso!\n\nAluno ID: " + alunoId + "\nValor: *" + valor
                    + " moedas*\nMotivo: " + motivo + "\n\nO envio já entrou no extrato do professor e do aluno.";
        }
        return "🤔 Não entendi essa opção de professor.\n\n" + menu(professor);
    }

    private String responderEmpresa(EmpresaParceira empresa, String comando, String original) {
        if (comando.equals("1") || comando.equals("pendentes") || comando.equals("cupons")) {
            return cuponsPendentesEmpresa(empresa);
        }
        if (comando.equals("2") || comando.equals("resgates") || comando.equals("historico")) {
            return resgatesEmpresa(empresa);
        }
        if (comando.equals("3") || comando.equals("vantagens") || comando.equals("catalogo")) {
            return vantagensEmpresa(empresa);
        }
        Matcher validar = Pattern.compile("^validar\\s+([A-Za-z0-9-]+)$", Pattern.CASE_INSENSITIVE).matcher(original);
        if (validar.find()) {
            Transacao transacao = vantagemService.validarCupom(empresa.id, validar.group(1));
            return "✅ Cupom validado com sucesso!\n\nCódigo: *" + transacao.codigoCupom
                    + "*\nAluno: " + transacao.aluno.nome
                    + "\nBenefício: " + transacao.vantagem.titulo;
        }
        return "🤔 Não entendi essa opção de parceiro.\n\n" + menu(empresa);
    }

    private String menu(Usuario usuario) {
        return switch (usuario.perfil) {
            case ALUNO -> "🎓 Valoriza Aê - Menu do aluno\n\n"
                    + "🪙 1. Ver saldo\n"
                    + "📄 2. Ver extrato recente\n"
                    + "🎁 3. Ver vantagens disponíveis\n"
                    + "🎟️ 4. Ver meus cupons\n\n"
                    + "Atalho: *resgatar ID_DA_VANTAGEM*\n"
                    + "Exemplo: resgatar 3\n\n"
                    + "Envie *trocar* para acessar outro perfil.";
            case PROFESSOR -> "👩‍🏫 Valoriza Aê - Menu do professor\n\n"
                    + "🪙 1. Ver cota disponível\n"
                    + "🎓 2. Listar alunos\n"
                    + "📄 3. Ver extrato recente\n\n"
                    + "Atalho: *enviar ID_ALUNO VALOR MOTIVO*\n"
                    + "Exemplo: enviar 1 50 Participação ativa em aula\n\n"
                    + "Envie *trocar* para acessar outro perfil.";
            case EMPRESA -> "🏪 Valoriza Aê - Menu do parceiro\n\n"
                    + "🎟️ 1. Cupons pendentes\n"
                    + "📄 2. Histórico de resgates\n"
                    + "🎁 3. Vantagens cadastradas\n\n"
                    + "Atalho: *validar CODIGO_DO_CUPOM*\n"
                    + "Exemplo: validar SME-ABC12345\n\n"
                    + "Envie *trocar* para acessar outro perfil.";
        };
    }

    private String orientarLoginPorPerfil(Perfil perfil, List<Usuario> candidatos) {
        List<Usuario> encontrados = candidatos.stream().filter(usuario -> usuario.perfil == perfil).toList();
        if (encontrados.isEmpty()) {
            return "Este WhatsApp nao esta vinculado ao perfil " + nomePerfil(perfil) + ".";
        }
        if (encontrados.size() == 1) {
            return "🔐 Para entrar como " + nomePerfil(perfil) + ", informe este e-mail no atendimento:\n"
                    + encontrados.get(0).email;
        }
        return menuEscolhaPerfil(encontrados);
    }

    private String menuEscolhaPerfil(List<Usuario> candidatos) {
        StringBuilder resposta = new StringBuilder("👋 Olá! Eu sou José e realizarei seu atendimento hoje.\n\n");
        resposta.append("Encontrei mais de uma conta neste WhatsApp. Para continuar com segurança, informe primeiro o e-mail do perfil que deseja acessar:\n");
        for (Usuario usuario : candidatos.stream().sorted(Comparator.comparing(u -> u.perfil.name())).toList()) {
            resposta.append("- ").append(nomePerfil(usuario.perfil)).append(": ")
                    .append(usuario.email).append("\n");
        }
        return resposta.toString().trim();
    }

    private String conviteLogin(List<Usuario> candidatos) {
        StringBuilder resposta = new StringBuilder("👋 Olá! Eu sou José e realizarei seu atendimento hoje.\n\n");
        resposta.append("Você está usando o *Valoriza Aê*. Para ir para a próxima etapa, informe seu e-mail cadastrado no sistema.\n\n");
        resposta.append("Depois eu peço sua senha e libero o atendimento conforme o seu perfil.");
        if (!candidatos.isEmpty()) {
            resposta.append("\n\nContas encontradas neste WhatsApp:");
            candidatos.stream()
                    .sorted(Comparator.comparing(u -> u.perfil.name()))
                    .forEach(usuario -> resposta.append("\n- ")
                            .append(nomePerfil(usuario.perfil))
                            .append(": ")
                            .append(usuario.email));
        }
        return resposta.toString();
    }

    private String usuarioNaoEncontrado(String telefone) {
        return "👋 Olá! Eu sou José e realizarei seu atendimento hoje.\n\n"
                + "Você está usando o *Valoriza Aê*. Para ir para a próxima etapa, informe seu e-mail cadastrado no sistema.\n\n"
                + "Se este WhatsApp ainda não estiver cadastrado, atualize o telefone no perfil pelo painel web.";

    }

    private String extratoAluno(Aluno aluno) {
        List<Transacao> itens = transacoes.extratoAluno(aluno).stream().limit(5).toList();
        if (itens.isEmpty()) {
            return "📄 Seu extrato ainda não tem movimentações.";
        }
        StringBuilder resposta = new StringBuilder("📄 Extrato recente\n\n");
        for (Transacao item : itens) {
            resposta.append(item.criadaEm.format(DATA)).append(" - ")
                    .append(tipo(item)).append(" - ")
                    .append(item.valor).append(" moedas");
            if (item.codigoCupom != null) {
                resposta.append(" - ").append(item.codigoCupom);
            }
            resposta.append("\n");
        }
        return resposta.toString().trim();
    }

    private String catalogoAluno(Aluno aluno) {
        List<Vantagem> vantagens = vantagemService.catalogo().stream().limit(8).toList();
        if (vantagens.isEmpty()) {
            return "🎁 Nenhuma vantagem disponível agora.";
        }
        StringBuilder resposta = new StringBuilder("🎁 Vantagens disponíveis\nSaldo: ").append(aluno.saldoMoedas).append(" moedas\n\n");
        for (Vantagem vantagem : vantagens) {
            resposta.append("ID ").append(vantagem.id).append(" - ").append(vantagem.titulo)
                    .append("\nCusto: ").append(vantagem.custoMoedas).append(" moedas")
                    .append("\nParceiro: ").append(vantagem.empresa.nome).append("\n\n");
        }
        resposta.append("Para resgatar, envie: *resgatar ID*");
        return resposta.toString().trim();
    }

    private String cuponsAluno(Aluno aluno) {
        List<Transacao> cupons = transacoes.extratoAluno(aluno).stream()
                .filter(t -> t.tipo == TipoTransacao.RESGATE_VANTAGEM && t.codigoCupom != null)
                .limit(6)
                .toList();
        if (cupons.isEmpty()) {
            return "🎟️ Você ainda não tem cupons.";
        }
        StringBuilder resposta = new StringBuilder("🎟️ Seus cupons\n\n");
        for (Transacao cupom : cupons) {
            resposta.append(cupom.codigoCupom).append(" - ").append(cupom.vantagem.titulo)
                    .append("\nStatus: ").append(cupom.cupomValidado ? "validado" : statusCupom(cupom))
                    .append("\n\n");
        }
        return resposta.toString().trim();
    }

    private String listarAlunos() {
        List<Aluno> lista = alunos.listAll().stream().limit(8).toList();
        if (lista.isEmpty()) {
            return "🎓 Nenhum aluno cadastrado.";
        }
        StringBuilder resposta = new StringBuilder("🎓 Alunos cadastrados\n\n");
        for (Aluno aluno : lista) {
            resposta.append("ID ").append(aluno.id).append(" - ").append(aluno.nome)
                    .append(" - ").append(aluno.curso)
                    .append(" - saldo ").append(aluno.saldoMoedas).append("\n");
        }
        resposta.append("\nPara enviar: *enviar ID_ALUNO VALOR MOTIVO*");
        return resposta.toString().trim();
    }

    private String extratoProfessor(Professor professor) {
        List<Transacao> itens = transacoes.extratoProfessor(professor).stream().limit(5).toList();
        if (itens.isEmpty()) {
            return "📄 Ainda não há movimentações no extrato do professor.";
        }
        StringBuilder resposta = new StringBuilder("📄 Extrato do professor\n\n");
        for (Transacao item : itens) {
            resposta.append(item.criadaEm.format(DATA)).append(" - ").append(tipo(item))
                    .append(" - ").append(item.valor).append(" moedas");
            if (item.aluno != null) {
                resposta.append(" - ").append(item.aluno.nome);
            }
            resposta.append("\n");
        }
        return resposta.toString().trim();
    }

    private String cuponsPendentesEmpresa(EmpresaParceira empresa) {
        List<Transacao> pendentes = transacoes.extratoEmpresa(empresa).stream()
                .filter(t -> t.codigoCupom != null && !t.cupomValidado)
                .limit(8)
                .toList();
        if (pendentes.isEmpty()) {
            return "✅ Não há cupons pendentes para validar.";
        }
        StringBuilder resposta = new StringBuilder("🎟️ Cupons pendentes\n\n");
        for (Transacao cupom : pendentes) {
            resposta.append(cupom.codigoCupom).append(" - ").append(cupom.aluno.nome)
                    .append("\nVantagem: ").append(cupom.vantagem.titulo)
                    .append("\nComando: validar ").append(cupom.codigoCupom).append("\n\n");
        }
        return resposta.toString().trim();
    }

    private String resgatesEmpresa(EmpresaParceira empresa) {
        List<Transacao> itens = transacoes.extratoEmpresa(empresa).stream().limit(6).toList();
        if (itens.isEmpty()) {
            return "📄 Nenhum resgate recebido ainda.";
        }
        StringBuilder resposta = new StringBuilder("📄 Resgates recentes\n\n");
        for (Transacao item : itens) {
            resposta.append(item.criadaEm.format(DATA)).append(" - ").append(item.codigoCupom)
                    .append(" - ").append(item.vantagem.titulo)
                    .append(" - ").append(item.cupomValidado ? "validado" : "pendente")
                    .append("\n");
        }
        return resposta.toString().trim();
    }

    private String vantagensEmpresa(EmpresaParceira empresa) {
        List<Vantagem> lista = vantagemService.vantagensDaEmpresa(empresa.id).stream().limit(8).toList();
        if (lista.isEmpty()) {
            return "🎁 Nenhuma vantagem cadastrada para esta empresa.";
        }
        StringBuilder resposta = new StringBuilder("🎁 Vantagens do parceiro\n\n");
        for (Vantagem vantagem : lista) {
            resposta.append("ID ").append(vantagem.id).append(" - ").append(vantagem.titulo)
                    .append(" - ").append(vantagem.custoMoedas).append(" moedas")
                    .append(" - ").append(vantagem.ativa ? "ativa" : "pausada")
                    .append("\n");
        }
        return resposta.toString().trim();
    }

    private List<Usuario> usuariosPorTelefone(String telefone) {
        Set<Usuario> encontrados = new LinkedHashSet<>(usuarios.porTelefoneWhatsapp(telefone));
        for (String email : emailsPorTelefoneNoOverride(telefone)) {
            usuarios.porEmail(email).ifPresent(encontrados::add);
        }
        return new ArrayList<>(encontrados);
    }

    private List<String> emailsPorTelefoneNoOverride(String telefone) {
        if (recipientOverrides.isEmpty() || recipientOverrides.get().isBlank()) {
            return List.of();
        }
        String normalizado = normalizarTelefone(telefone);
        List<String> emails = new ArrayList<>();
        for (String item : recipientOverrides.get().split("[,;\\n]")) {
            int separador = item.indexOf('=');
            if (separador <= 0) {
                continue;
            }
            String email = item.substring(0, separador).trim().toLowerCase(Locale.ROOT);
            String destino = normalizarTelefone(item.substring(separador + 1));
            if (!email.isBlank() && telefonesCompativeis(normalizado, destino)) {
                emails.add(email);
            }
        }
        return emails;
    }

    private Usuario escolherUsuario(String telefone, List<Usuario> candidatos) {
        Long usuarioId = usuariosAutenticados.get(telefone);
        if (usuarioId != null) {
            Optional<Usuario> autenticado = candidatos.stream()
                    .filter(usuario -> usuario.id != null && usuario.id.equals(usuarioId))
                    .findFirst();
            if (autenticado.isPresent()) {
                return autenticado.get();
            }
            usuariosAutenticados.remove(telefone);
            perfisSelecionados.remove(telefone);
            return null;
        }
        if (candidatos.size() == 1) {
            return candidatos.get(0);
        }
        Perfil perfil = perfisSelecionados.get(telefone);
        if (perfil == null) {
            return null;
        }
        return usuarioPorPerfil(candidatos, perfil).orElse(null);
    }

    private Optional<Usuario> usuarioPorPerfil(List<Usuario> candidatos, Perfil perfil) {
        return candidatos.stream().filter(usuario -> usuario.perfil == perfil).findFirst();
    }

    private Optional<Perfil> perfilSolicitado(String mensagem) {
        String comando = normalizarComando(mensagem);
        if (comando.equals("aluno") || comando.equals("perfil aluno") || comando.equals("1 aluno")) {
            return Optional.of(Perfil.ALUNO);
        }
        if (comando.equals("professor") || comando.equals("perfil professor") || comando.equals("2 professor")) {
            return Optional.of(Perfil.PROFESSOR);
        }
        if (comando.equals("empresa") || comando.equals("parceiro") || comando.equals("perfil empresa")
                || comando.equals("3 empresa")) {
            return Optional.of(Perfil.EMPRESA);
        }
        return Optional.empty();
    }

    private String statusCupom(Transacao cupom) {
        return cupom.vantagem != null && !cupom.vantagem.ativa ? "pausado" : "pendente de validacao";
    }

    private String tipo(Transacao transacao) {
        return switch (transacao.tipo) {
            case CREDITO_SEMESTRAL -> "Credito semestral";
            case ENVIO_MOEDAS -> "Envio de moedas";
            case RESGATE_VANTAGEM -> "Resgate de vantagem";
        };
    }

    private String nomePerfil(Perfil perfil) {
        return switch (perfil) {
            case ALUNO -> "Aluno";
            case PROFESSOR -> "Professor";
            case EMPRESA -> "Empresa";
        };
    }

    private String normalizarComando(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private record CredenciaisWhatsapp(String email, String senha) {
    }

    private boolean telefonesCompativeis(String telefoneA, String telefoneB) {
        String normalizadoA = normalizarTelefone(telefoneA);
        String normalizadoB = normalizarTelefone(telefoneB);
        return !normalizadoA.isBlank() && variantesTelefone(normalizadoA).contains(normalizadoB);
    }

    private Set<String> variantesTelefone(String telefone) {
        String normalizado = normalizarTelefone(telefone);
        Set<String> variantes = new LinkedHashSet<>();
        if (normalizado.isBlank()) {
            return variantes;
        }
        variantes.add(normalizado);
        if (normalizado.startsWith("55") && normalizado.length() == 13 && normalizado.charAt(4) == '9') {
            variantes.add(normalizado.substring(0, 4) + normalizado.substring(5));
        }
        if (normalizado.startsWith("55") && normalizado.length() == 12) {
            variantes.add(normalizado.substring(0, 4) + "9" + normalizado.substring(4));
        }
        return variantes;
    }

    private String normalizarTelefone(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String digitos = valor.replaceAll("\\D+", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }
        return digitos;
    }
}

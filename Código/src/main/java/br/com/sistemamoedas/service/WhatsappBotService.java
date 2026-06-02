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
    private static final int OPCAO_CANCELAR_VALIDACAO = 9;
    private final ConcurrentMap<String, Perfil> perfisSelecionados = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> usuariosAutenticados = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> emailsEmLogin = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<CupomValidacaoOpcao>> cuponsEmValidacao = new ConcurrentHashMap<>();
    private final Set<String> promptsValidacaoCupom = ConcurrentHashMap.newKeySet();

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

    @Inject
    QrCodeService qrCodes;

    @ConfigProperty(name = "valoriza.whatsapp.recipient-overrides")
    Optional<String> recipientOverrides;

    @ConfigProperty(name = "valoriza.app.public-url", defaultValue = "http://localhost:8080")
    String publicUrl;

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
            limparSelecaoValidacao(telefone);
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
                return "✅ Perfeito! Encontrei seu 📧 e-mail no atendimento.\n\nAgora informe sua 🔐 senha para eu liberar seu painel no Valoriza Aê.";
            }
        }

        List<Usuario> candidatos = usuariosPorTelefone(telefone);
        usuarioAutenticado(telefone).ifPresent(usuario -> {
            if (candidatos.stream().noneMatch(candidato -> candidato.id != null && candidato.id.equals(usuario.id))) {
                candidatos.add(usuario);
            }
        });
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

        if (temSelecaoValidacao(telefone) && cancelarValidacaoSolicitado(comando)) {
            limparSelecaoValidacao(telefone);
            return comMenu(usuario, "✅ Validação cancelada. Nenhum cupom foi alterado.");
        }

        if (comando.equals("menu") || comando.equals("ajuda") || comando.equals("inicio")
                || (comando.equals("0") && !temSelecaoValidacao(telefone))) {
            limparSelecaoValidacao(telefone);
            return menu(usuario);
        }
        if (comando.equals("trocar") || comando.equals("perfil")) {
            usuariosAutenticados.remove(telefone);
            perfisSelecionados.remove(telefone);
            limparSelecaoValidacao(telefone);
            return menuEscolhaPerfil(candidatos);
        }

        try {
            return switch (usuario.perfil) {
                case ALUNO -> responderAluno((Aluno) usuario, comando, mensagem);
                case PROFESSOR -> responderProfessor((Professor) usuario, comando, mensagem);
                case EMPRESA -> responderEmpresa((EmpresaParceira) usuario, telefone, comando, mensagem);
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

        emailsEmLogin.remove(telefone);
        usuariosAutenticados.put(telefone, usuario.id);
        perfisSelecionados.put(telefone, usuario.perfil);
        return "✅ Login confirmado como *" + nomePerfil(usuario.perfil) + "*.\n\n" + menu(usuario);
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
        String telefone = normalizarTelefone(chatId);
        String resposta = processarMensagem(chatId, texto);
        if (deveEnviarLogoBoasVindas(resposta)) {
            whatsapp.enviarLogoParaChat(chatId, "Valoriza Aê | Atendimento acadêmico");
        }
        whatsapp.enviarParaChat(chatId, resposta);
        if (deveEnviarPromptValidacaoCupom(telefone)) {
            whatsapp.enviarParaChat(chatId, promptValidacaoCupom(telefone));
        }
        if (deveEnviarQrCodesCupons(texto)) {
            enviarQrCodesCupons(chatId);
        }
        return resposta;
    }

    private boolean deveEnviarLogoBoasVindas(String resposta) {
        return resposta != null && resposta.startsWith("👋 Olá! Eu sou José");
    }

    private String responderAluno(Aluno aluno, String comando, String original) {
        if (comando.equals("1") || comando.equals("saldo")) {
            return comMenu(aluno, "🪙 Valoriza Aê - Saldo\n\n" + aluno.nome + ", você tem *" + aluno.saldoMoedas
                    + " moedas disponíveis*.");
        }
        if (comando.equals("2") || comando.equals("extrato")) {
            return comMenu(aluno, extratoAluno(aluno));
        }
        if (comando.equals("3") || comando.equals("vantagens") || comando.equals("catalogo")) {
            return comMenu(aluno, catalogoAluno(aluno));
        }
        if (comando.equals("4") || comando.equals("cupons")) {
            return comMenu(aluno, cuponsAluno(aluno));
        }
        Matcher resgate = Pattern.compile("^(resgatar|trocar)\\s+(\\d+)$", Pattern.CASE_INSENSITIVE).matcher(original);
        if (resgate.find()) {
            Long vantagemId = Long.valueOf(resgate.group(2));
            String codigo = vantagemService.resgatar(aluno.id, vantagemId);
            return comMenu(aluno, "🎟️ Cupom gerado com sucesso!\n\nCódigo: *" + codigo
                    + "*\nMostre o código ou o QR Code no parceiro e aguarde a validação antes de usar o benefício.");
        }
        return opcaoNaoEntendida(aluno);
    }

    private String responderProfessor(Professor professor, String comando, String original) {
        if (comando.equals("1") || comando.equals("cota") || comando.equals("saldo")) {
            return comMenu(professor, "👩‍🏫 Valoriza Aê - Professor\n\nCota disponível: *" + professor.saldoMoedas
                    + " moedas*.\nDepartamento: " + professor.departamento
                    + "\nInstituição: " + professor.instituicao.nome);
        }
        if (comando.equals("2") || comando.equals("alunos")) {
            return comMenu(professor, listarAlunos());
        }
        if (comando.equals("3") || comando.equals("extrato")) {
            return comMenu(professor, extratoProfessor(professor));
        }
        Matcher envio = Pattern.compile("^enviar\\s+(\\d+)\\s+(\\d+)\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(original);
        if (envio.find()) {
            Long alunoId = Long.valueOf(envio.group(1));
            int valor = Integer.parseInt(envio.group(2));
            String motivo = envio.group(3).trim();
            moedaService.enviarMoedas(professor.id, alunoId, valor, motivo);
            String alunoNome = alunos.findByIdOptional(alunoId).map(aluno -> aluno.nome).orElse("Aluno selecionado");
            return comMenu(professor, "✅ Moedas enviadas com sucesso!\n\nAluno: " + alunoNome + "\nValor: *" + valor
                    + " moedas*\nMotivo: " + motivo + "\n\nO envio já entrou no extrato do professor e do aluno.");
        }
        return opcaoNaoEntendida(professor);
    }

    private String responderEmpresa(EmpresaParceira empresa, String telefone, String comando, String original) {
        Optional<List<Integer>> selecaoCupons = selecaoCuponsInformada(telefone, original);
        if (selecaoCupons.isPresent()) {
            return validarCuponsSelecionados(empresa, telefone, selecaoCupons.get());
        }
        if (comando.equals("1") || comando.equals("pendentes") || comando.equals("cupons")) {
            List<Transacao> pendentes = cuponsPendentesEmpresaLista(empresa);
            atualizarSelecaoValidacao(telefone, pendentes);
            return cuponsPendentesEmpresa(pendentes);
        }
        if (comando.equals("2") || comando.equals("resgates") || comando.equals("historico")) {
            limparSelecaoValidacao(telefone);
            return comMenu(empresa, resgatesEmpresa(empresa));
        }
        if (comando.equals("3") || comando.equals("vantagens") || comando.equals("catalogo")) {
            limparSelecaoValidacao(telefone);
            return comMenu(empresa, vantagensEmpresa(empresa));
        }
        Optional<String> codigoCupom = codigoCupomInformado(original);
        if (codigoCupom.isPresent()) {
            limparSelecaoValidacao(telefone);
            return validarCupomEmpresa(empresa, codigoCupom.get());
        }
        return opcaoNaoEntendida(empresa);
    }

    private String validarCuponsSelecionados(EmpresaParceira empresa, String telefone, List<Integer> selecoes) {
        List<CupomValidacaoOpcao> opcoes = cuponsEmValidacao.getOrDefault(telefone, List.of());
        if (opcoes.isEmpty()) {
            limparSelecaoValidacao(telefone);
            return comMenu(empresa, "🎟️ Não há uma lista de cupons aberta agora.\n\nEscolha a opção *1* para consultar os cupons pendentes.");
        }
        if (selecaoCancelarValidacao(selecoes)) {
            limparSelecaoValidacao(telefone);
            return comMenu(empresa, "✅ Validação cancelada. Nenhum cupom foi alterado.");
        }

        List<Integer> indices = normalizarSelecoes(selecoes, opcoes.size());
        if (indices.isEmpty()) {
            return "🤔 Não encontrei essa opção na lista de cupons.\n\n" + promptValidacaoCupom(telefone);
        }

        StringBuilder resposta = new StringBuilder("✅ Validação concluída\n\n");
        int validados = 0;
        int falhas = 0;
        for (Integer indice : indices) {
            CupomValidacaoOpcao opcao = opcoes.get(indice - 1);
            try {
                Transacao transacao = vantagemService.validarCupom(empresa.id, opcao.codigo());
                resposta.append("• *").append(transacao.codigoCupom).append("*")
                        .append("\nAluno: ").append(transacao.aluno.nome)
                        .append("\nBenefício: ").append(transacao.vantagem.titulo)
                        .append("\n\n");
                validados++;
            } catch (RegraNegocioException erro) {
                resposta.append("• *").append(opcao.codigo()).append("* não foi validado: ")
                        .append(erro.getMessage()).append("\n\n");
                falhas++;
            }
        }

        limparSelecaoValidacao(telefone);
        if (validados > 0 && falhas == 0) {
            resposta.append("Atendimento registrado no histórico do parceiro e no painel do aluno.");
        } else if (validados > 0) {
            resposta.append("Os cupons confirmados já foram registrados. Confira os demais antes de tentar novamente.");
        } else {
            resposta.append("Nenhum cupom foi confirmado nesta tentativa.");
        }
        return comMenu(empresa, resposta.toString().trim());
    }

    private String validarCupomEmpresa(EmpresaParceira empresa, String codigoCupom) {
        Transacao transacao = vantagemService.validarCupom(empresa.id, codigoCupom);
        return comMenu(empresa, "✅ Cupom validado com sucesso!\n\nCódigo: *" + transacao.codigoCupom
                + "*\nAluno: " + transacao.aluno.nome
                + "\nBenefício: " + transacao.vantagem.titulo
                + "\n\nAtendimento confirmado no histórico do parceiro e no painel do aluno.");
    }

    private Optional<String> codigoCupomInformado(String textoOriginal) {
        String texto = textoOriginal == null ? "" : textoOriginal.trim().toUpperCase(Locale.ROOT);
        Matcher validar = Pattern.compile("^VALIDAR\\s+([A-Z0-9-]+)$").matcher(texto);
        if (validar.find()) {
            return Optional.of(validar.group(1));
        }
        if (texto.matches("^[A-Z]{2,}-[A-Z0-9-]{4,}$")) {
            return Optional.of(texto);
        }
        return Optional.empty();
    }

    private boolean cancelarValidacaoSolicitado(String comando) {
        return comando.equals("cancelar") || comando.equals("voltar") || comando.equals("nao") || comando.equals("não");
    }

    private boolean selecaoCancelarValidacao(List<Integer> selecoes) {
        return selecoes.stream().anyMatch(numero -> numero == OPCAO_CANCELAR_VALIDACAO);
    }

    private Optional<List<Integer>> selecaoCuponsInformada(String telefone, String textoOriginal) {
        if (!temSelecaoValidacao(telefone)) {
            return Optional.empty();
        }
        String texto = textoOriginal == null ? "" : textoOriginal.trim();
        if (!texto.matches("\\d+(\\s*[,; ]\\s*\\d+)*")) {
            return Optional.empty();
        }
        List<Integer> selecoes = new ArrayList<>();
        Matcher numeros = Pattern.compile("\\d+").matcher(texto);
        while (numeros.find()) {
            selecoes.add(Integer.parseInt(numeros.group()));
        }
        return selecoes.isEmpty() ? Optional.empty() : Optional.of(selecoes);
    }

    private List<Integer> normalizarSelecoes(List<Integer> selecoes, int totalOpcoes) {
        if (selecoes.stream().anyMatch(numero -> numero == 0)) {
            List<Integer> todos = new ArrayList<>();
            for (int indice = 1; indice <= totalOpcoes; indice++) {
                todos.add(indice);
            }
            return todos;
        }
        Set<Integer> unicos = new LinkedHashSet<>();
        for (Integer selecao : selecoes) {
            if (selecao != null && selecao >= 1 && selecao <= totalOpcoes) {
                unicos.add(selecao);
            }
        }
        return new ArrayList<>(unicos);
    }

    private void atualizarSelecaoValidacao(String telefone, List<Transacao> pendentes) {
        if (pendentes.isEmpty()) {
            limparSelecaoValidacao(telefone);
            return;
        }
        List<CupomValidacaoOpcao> opcoes = pendentes.stream()
                .map(cupom -> new CupomValidacaoOpcao(
                        cupom.codigoCupom,
                        cupom.aluno != null ? cupom.aluno.nome : "Aluno",
                        cupom.vantagem != null ? cupom.vantagem.titulo : "Benefício"))
                .toList();
        cuponsEmValidacao.put(telefone, opcoes);
        promptsValidacaoCupom.add(telefone);
    }

    private boolean temSelecaoValidacao(String telefone) {
        return telefone != null && cuponsEmValidacao.containsKey(telefone);
    }

    private void limparSelecaoValidacao(String telefone) {
        if (telefone == null) {
            return;
        }
        cuponsEmValidacao.remove(telefone);
        promptsValidacaoCupom.remove(telefone);
    }

    private boolean deveEnviarPromptValidacaoCupom(String telefone) {
        return telefone != null && promptsValidacaoCupom.remove(telefone);
    }

    private String promptValidacaoCupom(String telefone) {
        List<CupomValidacaoOpcao> opcoes = cuponsEmValidacao.getOrDefault(telefone, List.of());
        if (opcoes.isEmpty()) {
            return "🎟️ Não há cupons pendentes para validar no momento.";
        }
        StringBuilder resposta = new StringBuilder("🎟️ Você deseja validar algum cupom?\n\n");
        resposta.append("0. Validar todos os cupons atuais\n");
        for (int indice = 0; indice < opcoes.size(); indice++) {
            CupomValidacaoOpcao opcao = opcoes.get(indice);
            resposta.append(indice + 1).append(". ")
                    .append(opcao.codigo())
                    .append(" - ").append(opcao.aluno())
                    .append(" - ").append(opcao.vantagem())
                    .append("\n");
        }
        resposta.append(OPCAO_CANCELAR_VALIDACAO).append(". Cancelar validação\n");
        resposta.append("\nDigite uma opção, mais de uma opção separada por espaço, ou envie *menu* para voltar.");
        return resposta.toString().trim();
    }

    private String opcaoNaoEntendida(Usuario usuario) {
        return "🤔 Não entendi o que você quis dizer.\n\n"
                + "Escolha uma das opções abaixo:\n\n"
                + menu(usuario);
    }

    private String comMenu(Usuario usuario, String resposta) {
        if (resposta == null || resposta.isBlank()) {
            return menu(usuario);
        }
        return resposta.trim() + "\n\n" + menu(usuario);
    }
    private String menu(Usuario usuario) {
        return switch (usuario.perfil) {
            case ALUNO -> "🎓 Valoriza Aê - Menu do aluno\n\n"
                    + "🪙 1. Ver saldo\n"
                    + "📄 2. Ver extrato recente\n"
                    + "🎁 3. Ver vantagens disponíveis\n"
                    + "🎟️ 4. Ver meus cupons\n\n"
                    + "Envie *trocar* para acessar outra conta/perfil.\n\n"
                    + "Digite o número da opção desejada:";
            case PROFESSOR -> "👩‍🏫 Valoriza Aê - Menu do professor\n\n"
                    + "🪙 1. Ver cota disponível\n"
                    + "🎓 2. Listar alunos\n"
                    + "📄 3. Ver extrato recente\n\n"
                    + "Envie *trocar* para acessar outra conta/perfil.\n\n"
                    + "Digite o número da opção desejada:";
            case EMPRESA -> "🏪 Valoriza Aê - Menu do parceiro\n\n"
                    + "🎟️ 1. Validar cupom do aluno\n"
                    + "📄 2. Histórico de resgates\n"
                    + "🎁 3. Vantagens cadastradas\n\n"
                    + "Envie *trocar* para acessar outra conta/perfil.\n\n"
                    + "Digite o número da opção desejada:";
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
        resposta.append("Você está usando o *Valoriza Aê*.\nPara ir para a próxima etapa, informe seu 📧 e-mail cadastrado no sistema.\n\n");
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
                + "Você está usando o *Valoriza Aê*.\nPara ir para a próxima etapa, informe seu 📧 e-mail cadastrado no sistema.";

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
            resposta.append("ID ").append(vantagem.id).append(" - *").append(vantagem.titulo).append("*")
                    .append("\nCusto: ").append(vantagem.custoMoedas).append(" moedas")
                    .append("\nParceiro: ").append(vantagem.empresa.nome).append("\n\n");
        }
        resposta.append("Para resgatar com segurança, abra o painel do aluno e escolha a vantagem no catálogo.");
        return resposta.toString().trim();
    }

    private String cuponsAluno(Aluno aluno) {
        List<Transacao> cupons = cuponsDoAluno(aluno);
        if (cupons.isEmpty()) {
            return "🎟️ Você ainda não tem cupons.";
        }
        StringBuilder resposta = new StringBuilder("🎟️ Seus cupons\n\n");
        int indice = 1;
        for (Transacao cupom : cupons) {
            String titulo = cupom.vantagem != null ? cupom.vantagem.titulo : "Benefício resgatado";
            resposta.append(indice++).append(". *").append(titulo).append("*")
                    .append("\nCódigo: *").append(cupom.codigoCupom).append("*")
                    .append("\nStatus: ").append(cupom.cupomValidado ? "validado" : statusCupom(cupom));
            String descricao = descricaoCurta(cupom.vantagem);
            if (!descricao.isBlank()) {
                resposta.append("\nDescrição: ").append(descricao);
            }
            resposta.append("\n\n");
        }
        resposta.append("Os QR Codes aparecem logo abaixo. Se algum anexo não chegar, o código do cupom acima continua válido no parceiro.");
        return resposta.toString().trim();
    }

    private List<Transacao> cuponsDoAluno(Aluno aluno) {
        return transacoes.extratoAluno(aluno).stream()
                .filter(t -> t.tipo == TipoTransacao.RESGATE_VANTAGEM && t.codigoCupom != null)
                .toList();
    }

    private void enviarQrCodesCupons(String chatId) {
        String telefone = normalizarTelefone(chatId);
        Optional<Usuario> usuario = usuarioAutenticado(telefone);
        if (usuario.isEmpty() || !(usuario.get() instanceof Aluno aluno)) {
            return;
        }
        List<Transacao> cupons = cuponsDoAluno(aluno);
        int enviados = 0;
        int falhas = 0;
        for (Transacao cupom : cupons) {
            byte[] png = qrCodes.gerarPng(conteudoQrCodeCupom(cupom));
            boolean enviado = whatsapp.enviarImagemParaChat(chatId, png, nomeArquivoCupom(cupom), legendaQrCodeCupom(cupom));
            if (enviado) {
                enviados++;
            } else {
                falhas++;
            }
        }
        if (!cupons.isEmpty() && falhas > 0) {
            whatsapp.enviarParaChat(chatId, avisoQrCodeNaoAnexado(cupons.size(), enviados));
        }
    }

    private String avisoQrCodeNaoAnexado(int totalCupons, int enviados) {
        if (enviados > 0) {
            return "📎 Enviei *" + enviados + "* QR Code(s), mas alguns anexos não chegaram nesta conexão.\n\n"
                    + "Os cupons continuam válidos: apresente os códigos listados acima no parceiro ou abra *Meus cupons* no painel do aluno.";
        }
        return "📎 Não consegui anexar os QR Codes nesta conexão do WhatsApp.\n\n"
                + "Seus *" + totalCupons + "* cupom(ns) continuam válidos: apresente os códigos listados acima no parceiro ou abra *Meus cupons* no painel do aluno para ver os QR Codes.";
    }

    private boolean deveEnviarQrCodesCupons(String texto) {
        String comando = normalizarComando(texto);
        return comando.equals("4") || comando.equals("cupons");
    }

    private String legendaQrCodeCupom(Transacao cupom) {
        String titulo = cupom.vantagem != null ? cupom.vantagem.titulo : "Benefício resgatado";
        StringBuilder legenda = new StringBuilder("🎟️ *").append(titulo).append("*")
                .append("\nCupom: *").append(cupom.codigoCupom).append("*")
                .append("\nStatus: ").append(cupom.cupomValidado ? "validado" : statusCupom(cupom));
        String descricao = descricaoCurta(cupom.vantagem);
        if (!descricao.isBlank()) {
            legenda.append("\n").append(descricao);
        }
        legenda.append("\n\nMostre este QR Code no parceiro para validação.");
        return legenda.toString();
    }

    private String conteudoQrCodeCupom(Transacao cupom) {
        return baseUrl() + "/empresa?cupom=" + cupom.codigoCupom;
    }

    private String nomeArquivoCupom(Transacao cupom) {
        String codigo = cupom.codigoCupom == null ? "cupom" : cupom.codigoCupom.toLowerCase(Locale.ROOT);
        return "qr-" + codigo.replaceAll("[^a-z0-9-]", "-") + ".png";
    }

    private String descricaoCurta(Vantagem vantagem) {
        if (vantagem == null || vantagem.descricao == null || vantagem.descricao.isBlank()) {
            return "";
        }
        String texto = vantagem.descricao.replaceAll("\\s+", " ").trim();
        return texto.length() > 120 ? texto.substring(0, 117).trim() + "..." : texto;
    }

    private String baseUrl() {
        return publicUrl == null || publicUrl.isBlank()
                ? "http://localhost:8080"
                : publicUrl.replaceAll("/+$", "");
    }

    private String listarAlunos() {
        List<Aluno> lista = alunos.listAll().stream().limit(8).toList();
        if (lista.isEmpty()) {
            return "🎓 Nenhum aluno cadastrado.";
        }
        StringBuilder resposta = new StringBuilder("🎓 Alunos cadastrados\n\n");
        for (Aluno aluno : lista) {
            resposta.append("ID ").append(aluno.id).append(" - *").append(aluno.nome).append("*")
                    .append("\nCurso: ").append(aluno.curso)
                    .append("\nSaldo atual: ").append(aluno.saldoMoedas).append(" moedas\n\n");
        }
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

    private List<Transacao> cuponsPendentesEmpresaLista(EmpresaParceira empresa) {
        return transacoes.extratoEmpresa(empresa).stream()
                .filter(t -> t.codigoCupom != null && !t.cupomValidado)
                .limit(8)
                .toList();
    }

    private String cuponsPendentesEmpresa(List<Transacao> pendentes) {
        if (pendentes.isEmpty()) {
            return "✅ Não há cupons pendentes para validar.";
        }
        StringBuilder resposta = new StringBuilder("🎟️ Cupons pendentes\n\n");
        int indice = 1;
        for (Transacao cupom : pendentes) {
            resposta.append(indice++).append(". Cupom: *").append(cupom.codigoCupom).append("*")
                    .append("\nAluno: ").append(cupom.aluno.nome)
                    .append("\nVantagem: ").append(cupom.vantagem.titulo)
                    .append("\nStatus: aguardando confirmação no atendimento\n\n");
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
            resposta.append("ID ").append(vantagem.id).append(" - *").append(vantagem.titulo).append("*")
                    .append("\nCusto: ").append(vantagem.custoMoedas).append(" moedas")
                    .append("\nStatus: ").append(vantagem.ativa ? "publicada" : "pausada")
                    .append("\n\n");
        }
        return resposta.toString().trim();
    }

    private Optional<Usuario> usuarioAutenticado(String telefone) {
        Long usuarioId = usuariosAutenticados.get(telefone);
        if (usuarioId == null) {
            return Optional.empty();
        }
        return usuarios.findByIdOptional(usuarioId);
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

    private record CupomValidacaoOpcao(String codigo, String aluno, String vantagem) {
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

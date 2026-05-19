package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmailNotificacao;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Instituicao;
import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.domain.Vantagem;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.CursoRepository;
import br.com.sistemamoedas.repository.EmailNotificacaoRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import br.com.sistemamoedas.security.SessaoService;
import br.com.sistemamoedas.security.SessaoUsuario;
import br.com.sistemamoedas.service.AlunoService;
import br.com.sistemamoedas.service.CadastroService;
import br.com.sistemamoedas.service.EnderecoViaCep;
import br.com.sistemamoedas.service.MoedaService;
import br.com.sistemamoedas.service.QrCodeService;
import br.com.sistemamoedas.service.RegraNegocioException;
import br.com.sistemamoedas.service.VantagemService;
import br.com.sistemamoedas.service.ViaCepService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiController {

    @Inject
    SessaoService sessoes;

    @Inject
    CadastroService cadastros;

    @Inject
    AlunoService alunoService;

    @Inject
    MoedaService moedaService;

    @Inject
    VantagemService vantagemService;

    @Inject
    AlunoRepository alunos;

    @Inject
    CursoRepository cursos;

    @Inject
    TransacaoRepository transacoes;

    @Inject
    EmailNotificacaoRepository notificacoes;

    @Inject
    QrCodeService qrCodes;

    @Inject
    ViaCepService viaCep;

    @GET
    @Path("me")
    public Response me(@CookieParam(SessaoService.COOKIE) String token) {
        return sessoes.porToken(token)
                .<Response>map(sessao -> Response.ok(toSession(sessao)).build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorDto("Usuario nao autenticado."))
                        .build());
    }

    @POST
    @Path("login")
    public Response login(LoginRequest request) {
        SessaoUsuario sessao = sessoes.login(request.email(), request.senha());
        return Response.ok(toSession(sessao))
                .header(HttpHeaders.SET_COOKIE, Web.cookieSessao(SessaoService.COOKIE, sessao.token()))
                .build();
    }

    @POST
    @Path("logout")
    public Response logout(@CookieParam(SessaoService.COOKIE) String token) {
        sessoes.logout(token);
        return Response.ok(new MessageDto("Sessao encerrada."))
                .header(HttpHeaders.SET_COOKIE, Web.cookieExpirado(SessaoService.COOKIE))
                .build();
    }

    @GET
    @Path("instituicoes")
    public List<InstituicaoDto> instituicoes() {
        return cadastros.instituicoesDisponiveis().stream().map(this::toInstituicao).toList();
    }

    @GET
    @Path("cep/{cep}")
    public EnderecoViaCep consultarCep(@PathParam("cep") String cep) {
        return viaCep.consultar(cep);
    }

    @POST
    @Path("alunos")
    public Response cadastrarAluno(CadastroAlunoRequest request) {
        Aluno aluno = cadastros.cadastrarAluno(request.nome(), request.email(), request.senha(), request.cpf(),
                request.rg(), request.endereco(), request.instituicaoId(), request.curso());
        return Response.status(Response.Status.CREATED).entity(toAluno(aluno)).build();
    }

    @POST
    @Path("empresas")
    public Response cadastrarEmpresa(CadastroEmpresaRequest request) {
        EmpresaParceira empresa = cadastros.cadastrarEmpresa(request.nome(), request.email(), request.senha(),
                request.cnpj(), request.endereco(), request.contato());
        return Response.status(Response.Status.CREATED).entity(toEmpresa(empresa)).build();
    }

    @GET
    @Path("aluno/dashboard")
    public AlunoDashboardDto dashboardAluno(@CookieParam(SessaoService.COOKIE) String token) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.ALUNO);
        Aluno aluno = alunoService.buscar(sessao.usuarioId());
        List<Transacao> extrato = alunoService.extrato(sessao.usuarioId());
        Map<Long, Vantagem> vantagensDoAluno = new LinkedHashMap<>();
        vantagemService.catalogo().forEach(vantagem -> vantagensDoAluno.put(vantagem.id, vantagem));
        extrato.stream()
                .map(transacao -> transacao.vantagem)
                .filter(vantagem -> vantagem != null)
                .forEach(vantagem -> vantagensDoAluno.putIfAbsent(vantagem.id, vantagem));
        return new AlunoDashboardDto(
                toAluno(aluno),
                extrato.stream().map(t -> toTransacao(t, Perfil.ALUNO)).toList(),
                vantagensDoAluno.values().stream().map(vantagem -> toVantagemAluno(aluno, vantagem)).toList(),
                notificacoes.porDestinatario(sessao.email()).stream().map(this::toNotificacao).toList(),
                "Veja seu saldo, acompanhe cupons e evite resgatar novamente uma vantagem que ja esta no extrato.");
    }

    @POST
    @Path("aluno/resgates")
    public CupomDto resgatar(@CookieParam(SessaoService.COOKIE) String token, ResgateRequest request) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.ALUNO);
        String codigo = vantagemService.resgatar(sessao.usuarioId(), request.vantagemId());
        return new CupomDto(codigo, qrCodeUrl(codigo),
                "Resgate realizado. Cupom enviado para aluno e empresa parceira.");
    }

    @GET
    @Path("cupons/{codigo}/qrcode")
    @Produces("image/png")
    public Response qrCodeCupom(@CookieParam(SessaoService.COOKIE) String token, @PathParam("codigo") String codigo,
            @Context UriInfo uriInfo) {
        SessaoUsuario sessao = sessoes.porToken(token)
                .orElseThrow(() -> new RegraNegocioException("Faca login para acessar o QR Code do cupom."));
        Transacao transacao = transacoes.cupomPorCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RegraNegocioException("Cupom nao encontrado."));
        boolean permitido = switch (sessao.perfil()) {
            case ALUNO -> transacao.aluno != null && transacao.aluno.id.equals(sessao.usuarioId());
            case EMPRESA -> transacao.empresa != null && transacao.empresa.id.equals(sessao.usuarioId());
            case PROFESSOR -> false;
        };
        if (!permitido) {
            throw new RegraNegocioException("Seu perfil nao possui acesso a este QR Code.");
        }

        String validacaoUrl = uriInfo.getBaseUriBuilder()
                .replacePath("empresa")
                .queryParam("cupom", transacao.codigoCupom)
                .build()
                .toString();
        String conteudo = "Valoriza Ae\nCupom: " + transacao.codigoCupom
                + "\nAluno: " + transacao.aluno.nome
                + "\nVantagem: " + transacao.vantagem.titulo
                + "\nValidar em: " + validacaoUrl;
        return Response.ok(qrCodes.gerarPng(conteudo))
                .type("image/png")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @GET
    @Path("professor/dashboard")
    public ProfessorDashboardDto dashboardProfessor(@CookieParam(SessaoService.COOKIE) String token) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
        Professor professor = moedaService.buscarProfessor(sessao.usuarioId());
        return new ProfessorDashboardDto(
                toProfessor(professor),
                alunos.listAll().stream().map(this::toAlunoOption).toList(),
                moedaService.extratoProfessor(sessao.usuarioId()).stream()
                        .map(t -> toTransacao(t, Perfil.PROFESSOR))
                        .toList(),
                notificacoes.porDestinatario(sessao.email()).stream().map(this::toNotificacao).toList(),
                "Envie moedas com justificativa e acompanhe quanto da sua cota semestral ainda esta disponivel.");
    }

    @POST
    @Path("professor/envios")
    public MessageDto enviarMoedas(@CookieParam(SessaoService.COOKIE) String token, EnvioMoedasRequest request) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
        moedaService.enviarMoedas(sessao.usuarioId(), request.alunoId(), request.valor(), request.mensagem());
        return new MessageDto("Moedas enviadas com sucesso.");
    }

    @POST
    @Path("professor/credito-semestral")
    public MessageDto creditarSemestre(@CookieParam(SessaoService.COOKIE) String token) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
        moedaService.creditarCotaSemestral(sessao.usuarioId());
        return new MessageDto("Cota semestral creditada.");
    }

    @GET
    @Path("empresa/dashboard")
    public EmpresaDashboardDto dashboardEmpresa(@CookieParam(SessaoService.COOKIE) String token) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        EmpresaParceira empresa = vantagemService.buscarEmpresa(sessao.usuarioId());
        return new EmpresaDashboardDto(
                toEmpresa(empresa),
                vantagemService.vantagensDaEmpresa(sessao.usuarioId()).stream().map(this::toVantagem).toList(),
                transacoes.extratoEmpresa(empresa).stream().map(t -> toTransacao(t, Perfil.EMPRESA)).toList(),
                notificacoes.porDestinatario(sessao.email()).stream().map(this::toNotificacao).toList(),
                "Gerencie beneficios publicados e confirme no atendimento os cupons apresentados pelos alunos.");
    }

    @POST
    @Path("empresa/vantagens")
    public Response cadastrarVantagem(@CookieParam(SessaoService.COOKIE) String token, VantagemRequest request) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        Vantagem vantagem = vantagemService.cadastrar(sessao.usuarioId(), request.titulo(), request.descricao(),
                request.fotoUrl(), request.custoMoedas());
        return Response.status(Response.Status.CREATED).entity(toVantagem(vantagem)).build();
    }

    @PUT
    @Path("empresa/vantagens/{id}")
    public MessageDto atualizarVantagem(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id,
            VantagemRequest request) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        vantagemService.atualizar(sessao.usuarioId(), id, request.titulo(), request.descricao(), request.fotoUrl(),
                request.custoMoedas(), request.ativa());
        return new MessageDto("Vantagem atualizada.");
    }

    @DELETE
    @Path("empresa/vantagens/{id}")
    public MessageDto excluirVantagem(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        vantagemService.excluir(sessao.usuarioId(), id);
        return new MessageDto("Vantagem excluida.");
    }

    @DELETE
    @Path("empresa/vantagens/{id}/excluir")
    public MessageDto excluirVantagemDefinitiva(@CookieParam(SessaoService.COOKIE) String token,
            @PathParam("id") Long id) {
        return excluirVantagem(token, id);
    }

    @PUT
    @Path("empresa/vantagens/{id}/status")
    public VantagemDto alterarStatusVantagem(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id,
            StatusVantagemRequest request) {
        if (request == null || request.ativa() == null) {
            throw new RegraNegocioException("Status da vantagem e obrigatorio.");
        }
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        return toVantagem(vantagemService.alterarStatus(sessao.usuarioId(), id, request.ativa()));
    }

    @POST
    @Path("empresa/cupons/validar")
    public TransacaoDto validarCupom(@CookieParam(SessaoService.COOKIE) String token, ValidarCupomRequest request) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        return toTransacao(vantagemService.validarCupom(sessao.usuarioId(), request.codigo()), Perfil.EMPRESA);
    }

    private SessionDto toSession(SessaoUsuario sessao) {
        return new SessionDto(sessao.usuarioId(), sessao.nome(), sessao.email(), sessao.perfil().name());
    }

    private InstituicaoDto toInstituicao(Instituicao instituicao) {
        return new InstituicaoDto(instituicao.id, instituicao.nome, instituicao.cidade,
                cursos.porInstituicao(instituicao).stream().map(curso -> curso.nome).toList());
    }

    private AlunoDto toAluno(Aluno aluno) {
        return new AlunoDto(aluno.id, aluno.nome, aluno.email, aluno.cpf, aluno.rg, aluno.endereco,
                aluno.instituicao.nome, aluno.curso, aluno.saldoMoedas);
    }

    private AlunoOptionDto toAlunoOption(Aluno aluno) {
        return new AlunoOptionDto(aluno.id, aluno.nome, aluno.email, aluno.curso, aluno.saldoMoedas);
    }

    private ProfessorDto toProfessor(Professor professor) {
        return new ProfessorDto(professor.id, professor.nome, professor.email, professor.cpf, professor.departamento,
                professor.instituicao.nome, professor.saldoMoedas, professor.ultimoCreditoSemestral);
    }

    private EmpresaDto toEmpresa(EmpresaParceira empresa) {
        return new EmpresaDto(empresa.id, empresa.nome, empresa.email, empresa.cnpj, empresa.endereco, empresa.contato);
    }

    private VantagemDto toVantagem(Vantagem vantagem) {
        return new VantagemDto(vantagem.id, vantagem.titulo, vantagem.descricao, vantagem.fotoUrl,
                vantagem.custoMoedas, vantagem.ativa, vantagem.empresa.nome, false, null, false, null,
                null, !transacoes.existeParaVantagem(vantagem));
    }

    private VantagemDto toVantagemAluno(Aluno aluno, Vantagem vantagem) {
        return transacoes.resgateAlunoVantagem(aluno, vantagem)
                .map(resgate -> new VantagemDto(vantagem.id, vantagem.titulo, vantagem.descricao, vantagem.fotoUrl,
                        vantagem.custoMoedas, vantagem.ativa, vantagem.empresa.nome, true, resgate.codigoCupom,
                        resgate.cupomValidado, resgate.criadaEm.toString(), qrCodeUrl(resgate.codigoCupom), false))
                .orElseGet(() -> toVantagem(vantagem));
    }

    private TransacaoDto toTransacao(Transacao transacao, Perfil perfil) {
        String contraparte = switch (perfil) {
            case ALUNO -> {
                if (transacao.professor != null) {
                    yield transacao.professor.nome;
                }
                if (transacao.empresa != null) {
                    yield transacao.empresa.nome;
                }
                yield "Sistema";
            }
            case PROFESSOR -> transacao.aluno != null ? transacao.aluno.nome : "Sistema";
            case EMPRESA -> transacao.aluno != null ? transacao.aluno.nome : "Sistema";
        };
        return new TransacaoDto(transacao.id, transacao.tipo.name(), transacao.valor, transacao.criadaEm.toString(),
                transacao.mensagem, transacao.codigoCupom, contraparte,
                transacao.vantagem != null ? transacao.vantagem.titulo : null, transacao.cupomValidado,
                transacao.validadoEm != null ? transacao.validadoEm.toString() : null,
                transacao.vantagem == null || transacao.vantagem.ativa, qrCodeUrl(transacao.codigoCupom));
    }

    private NotificacaoDto toNotificacao(EmailNotificacao notificacao) {
        return new NotificacaoDto(notificacao.id, notificacao.destinatario, notificacao.assunto,
                notificacao.conteudo, notificacao.codigoReferencia, notificacao.criadoEm.toString());
    }

    private String qrCodeUrl(String codigoCupom) {
        return codigoCupom == null || codigoCupom.isBlank() ? null : "/api/cupons/" + codigoCupom + "/qrcode";
    }

    public record ErrorDto(String mensagem) {
    }

    public record MessageDto(String mensagem) {
    }

    public record LoginRequest(String email, String senha) {
    }

    public record CadastroAlunoRequest(String nome, String email, String senha, String cpf, String rg, String endereco,
            Long instituicaoId, String curso) {
    }

    public record CadastroEmpresaRequest(String nome, String email, String senha, String cnpj, String endereco,
            String contato) {
    }

    public record ResgateRequest(Long vantagemId) {
    }

    public record EnvioMoedasRequest(Long alunoId, int valor, String mensagem) {
    }

    public record VantagemRequest(String titulo, String descricao, String fotoUrl, int custoMoedas, boolean ativa) {
    }

    public record StatusVantagemRequest(Boolean ativa) {
    }

    public record ValidarCupomRequest(String codigo) {
    }

    public record SessionDto(Long id, String nome, String email, String perfil) {
    }

    public record InstituicaoDto(Long id, String nome, String cidade, List<String> cursos) {
    }

    public record AlunoDto(Long id, String nome, String email, String cpf, String rg, String endereco,
            String instituicao, String curso, int saldoMoedas) {
    }

    public record AlunoOptionDto(Long id, String nome, String email, String curso, int saldoMoedas) {
    }

    public record ProfessorDto(Long id, String nome, String email, String cpf, String departamento, String instituicao,
            int saldoMoedas, String ultimoCreditoSemestral) {
    }

    public record EmpresaDto(Long id, String nome, String email, String cnpj, String endereco, String contato) {
    }

    public record VantagemDto(Long id, String titulo, String descricao, String fotoUrl, int custoMoedas, boolean ativa,
            String empresaNome, boolean adquirida, String codigoCupom, boolean cupomValidado, String resgatadaEm,
            String qrCodeUrl, boolean excluivel) {
    }

    public record TransacaoDto(Long id, String tipo, int valor, String criadaEm, String mensagem, String codigoCupom,
            String contraparte, String vantagem, boolean cupomValidado, String validadoEm, boolean vantagemAtiva,
            String qrCodeUrl) {
    }

    public record AlunoDashboardDto(AlunoDto aluno, List<TransacaoDto> extrato, List<VantagemDto> vantagens,
            List<NotificacaoDto> notificacoes,
            String resumo) {
    }

    public record ProfessorDashboardDto(ProfessorDto professor, List<AlunoOptionDto> alunos, List<TransacaoDto> extrato,
            List<NotificacaoDto> notificacoes,
            String resumo) {
    }

    public record EmpresaDashboardDto(EmpresaDto empresa, List<VantagemDto> vantagens, List<TransacaoDto> resgates,
            List<NotificacaoDto> notificacoes, String resumo) {
    }

    public record CupomDto(String codigo, String qrCodeUrl, String mensagem) {
    }

    public record NotificacaoDto(Long id, String destinatario, String assunto, String conteudo, String codigoReferencia,
            String criadaEm) {
    }
}

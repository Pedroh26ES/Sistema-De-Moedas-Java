package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.TipoTransacao;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.domain.Vantagem;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.EmpresaParceiraRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import br.com.sistemamoedas.repository.VantagemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class VantagemService {

    private static final String CUPOM_ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    @Inject
    VantagemRepository vantagens;

    @Inject
    EmpresaParceiraRepository empresas;

    @Inject
    AlunoRepository alunos;

    @Inject
    TransacaoRepository transacoes;

    @Inject
    EmailGateway emails;

    @Inject
    RabbitMqFilaService filaEventos;

    public List<Vantagem> catalogo() {
        return vantagens.ativas();
    }

    public List<Vantagem> vantagensDaEmpresa(Long empresaId) {
        return vantagens.daEmpresa(buscarEmpresa(empresaId));
    }

    @Transactional
    public Vantagem cadastrar(Long empresaId, String titulo, String descricao, String fotoUrl, int custoMoedas) {
        EmpresaParceira empresa = buscarEmpresa(empresaId);
        validarDados(titulo, descricao, fotoUrl, custoMoedas);
        Vantagem vantagem = new Vantagem(titulo.trim(), descricao.trim(), fotoUrl.trim(), custoMoedas, empresa);
        vantagens.persist(vantagem);
        return vantagem;
    }

    @Transactional
    public void atualizar(Long empresaId, Long vantagemId, String titulo, String descricao, String fotoUrl,
            int custoMoedas, boolean ativa) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, vantagemId);
        validarDados(titulo, descricao, fotoUrl, custoMoedas);
        vantagem.titulo = titulo.trim();
        vantagem.descricao = descricao.trim();
        vantagem.fotoUrl = fotoUrl.trim();
        vantagem.custoMoedas = custoMoedas;
        vantagem.ativa = ativa;
    }

    @Transactional
    public void remover(Long empresaId, Long vantagemId) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, vantagemId);
        vantagem.ativa = false;
    }

    @Transactional
    public void excluir(Long empresaId, Long vantagemId) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, vantagemId);
        if (transacoes.existeParaVantagem(vantagem)) {
            throw new RegraNegocioException(
                    "Esta vantagem ja possui cupons ou resgates. Pause a vantagem para ocultar do catalogo sem perder o historico.");
        }
        vantagens.delete(vantagem);
    }

    @Transactional
    public Vantagem alterarStatus(Long empresaId, Long vantagemId, boolean ativa) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, vantagemId);
        boolean mudouStatus = vantagem.ativa != ativa;
        vantagem.ativa = ativa;
        if (mudouStatus) {
            notificarAlunosComCupomPendente(vantagem, ativa);
        }
        return vantagem;
    }

    @Transactional
    public String resgatar(Long alunoId, Long vantagemId) {
        Aluno aluno = alunos.findByIdOptional(alunoId)
                .orElseThrow(() -> new RegraNegocioException("Aluno nao encontrado."));
        Vantagem vantagem = vantagens.findByIdOptional(vantagemId)
                .filter(v -> v.ativa)
                .orElseThrow(() -> new RegraNegocioException("Vantagem indisponivel."));
        if (transacoes.resgateAlunoVantagem(aluno, vantagem).isPresent()) {
            throw new RegraNegocioException("Voce ja possui esta vantagem. Consulte o cupom no seu extrato.");
        }
        if (aluno.saldoMoedas < vantagem.custoMoedas) {
            throw new RegraNegocioException("Saldo insuficiente para resgatar esta vantagem.");
        }

        String codigo = gerarCodigoCupom();
        aluno.saldoMoedas -= vantagem.custoMoedas;

        Transacao transacao = new Transacao();
        transacao.tipo = TipoTransacao.RESGATE_VANTAGEM;
        transacao.valor = vantagem.custoMoedas;
        transacao.aluno = aluno;
        transacao.empresa = vantagem.empresa;
        transacao.vantagem = vantagem;
        transacao.codigoCupom = codigo;
        transacao.mensagem = "Resgate da vantagem " + vantagem.titulo;
        transacoes.persist(transacao);

        String conteudoAluno = "Cupom " + codigo + " gerado para a vantagem " + vantagem.titulo
                + ". Apresente este codigo na troca presencial.";
        String conteudoEmpresa = "O aluno " + aluno.nome + " resgatou " + vantagem.titulo + ". Codigo: " + codigo;
        emails.enviar(aluno.email, "Cupom de troca gerado", conteudoAluno, codigo);
        emails.enviar(vantagem.empresa.email, "Nova troca para validar", conteudoEmpresa, codigo);
        filaEventos.publicar(new EventoSistema("CUPOM_GERADO", transacao.id, codigo, aluno.email, null,
                vantagem.empresa.email, vantagem.titulo, vantagem.custoMoedas, transacao.criadaEm.toString()));
        return codigo;
    }

    @Transactional
    public Transacao validarCupom(Long empresaId, String codigo) {
        EmpresaParceira empresa = buscarEmpresa(empresaId);
        if (codigo == null || codigo.isBlank()) {
            throw new RegraNegocioException("Codigo do cupom e obrigatorio.");
        }
        Transacao transacao = transacoes.cupomDaEmpresa(empresa, codigo.trim().toUpperCase())
                .orElseThrow(() -> new RegraNegocioException("Cupom nao encontrado para esta empresa."));
        if (transacao.cupomValidado) {
            throw new RegraNegocioException("Este cupom ja foi validado.");
        }
        if (transacao.vantagem != null && !transacao.vantagem.ativa) {
            throw new RegraNegocioException("Cupom temporariamente desativado pelo parceiro.");
        }

        transacao.cupomValidado = true;
        transacao.validadoEm = LocalDateTime.now();
        emails.enviar(transacao.aluno.email, "Cupom validado",
                "Seu cupom " + transacao.codigoCupom + " foi validado por " + empresa.nome + ".", transacao.codigoCupom);
        filaEventos.publicar(new EventoSistema("CUPOM_VALIDADO", transacao.id, transacao.codigoCupom,
                transacao.aluno.email, null, empresa.email, transacao.vantagem.titulo, transacao.valor,
                transacao.validadoEm.toString()));
        return transacao;
    }

    public Vantagem buscarDaEmpresa(Long empresaId, Long vantagemId) {
        Vantagem vantagem = vantagens.findByIdOptional(vantagemId)
                .orElseThrow(() -> new RegraNegocioException("Vantagem nao encontrada."));
        if (!vantagem.empresa.id.equals(empresaId)) {
            throw new RegraNegocioException("Vantagem nao pertence a empresa logada.");
        }
        return vantagem;
    }

    public EmpresaParceira buscarEmpresa(Long empresaId) {
        return empresas.findByIdOptional(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Empresa nao encontrada."));
    }

    private void validarDados(String titulo, String descricao, String fotoUrl, int custoMoedas) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("Titulo da vantagem e obrigatorio.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descricao da vantagem e obrigatoria.");
        }
        if (fotoUrl == null || fotoUrl.isBlank()) {
            throw new RegraNegocioException("Foto da vantagem e obrigatoria.");
        }
        if (custoMoedas <= 0) {
            throw new RegraNegocioException("Custo em moedas deve ser maior que zero.");
        }
    }

    private String gerarCodigoCupom() {
        StringBuilder codigo = new StringBuilder("SME-");
        for (int i = 0; i < 8; i++) {
            codigo.append(CUPOM_ALFABETO.charAt(random.nextInt(CUPOM_ALFABETO.length())));
        }
        return codigo.toString();
    }

    private void notificarAlunosComCupomPendente(Vantagem vantagem, boolean ativa) {
        for (Transacao transacao : transacoes.cuponsPendentesDaVantagem(vantagem)) {
            if (transacao.aluno == null) {
                continue;
            }
            if (ativa) {
                emails.enviar(transacao.aluno.email, "Cupom disponivel novamente",
                        "O cupom " + transacao.codigoCupom + " da vantagem " + vantagem.titulo
                                + " voltou a ficar disponivel para validacao no parceiro.",
                        transacao.codigoCupom);
                filaEventos.publicar(new EventoSistema("CUPOM_REATIVADO", transacao.id, transacao.codigoCupom,
                        transacao.aluno.email, null, vantagem.empresa.email, vantagem.titulo, transacao.valor,
                        LocalDateTime.now().toString()));
            } else {
                emails.enviar(transacao.aluno.email, "Cupom temporariamente desativado",
                        "O cupom " + transacao.codigoCupom + " da vantagem " + vantagem.titulo
                                + " foi desativado enquanto o parceiro pausa a vantagem. Aguarde a republicacao antes de tentar utilizar.",
                        transacao.codigoCupom);
                filaEventos.publicar(new EventoSistema("CUPOM_DESATIVADO", transacao.id, transacao.codigoCupom,
                        transacao.aluno.email, null, vantagem.empresa.email, vantagem.titulo, transacao.valor,
                        LocalDateTime.now().toString()));
            }
        }
    }
}

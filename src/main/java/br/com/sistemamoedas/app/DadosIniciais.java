package br.com.sistemamoedas.app;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Curso;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Instituicao;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.TipoTransacao;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.domain.Vantagem;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.CursoRepository;
import br.com.sistemamoedas.repository.EmpresaParceiraRepository;
import br.com.sistemamoedas.repository.InstituicaoRepository;
import br.com.sistemamoedas.repository.ProfessorRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import br.com.sistemamoedas.repository.VantagemRepository;
import br.com.sistemamoedas.security.SenhaService;
import br.com.sistemamoedas.service.EmailGateway;
import br.com.sistemamoedas.service.MoedaService;
import br.com.sistemamoedas.service.SemestreUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class DadosIniciais {

    @Inject
    InstituicaoRepository instituicoes;

    @Inject
    CursoRepository cursos;

    @Inject
    ProfessorRepository professores;

    @Inject
    AlunoRepository alunos;

    @Inject
    EmpresaParceiraRepository empresas;

    @Inject
    VantagemRepository vantagens;

    @Inject
    TransacaoRepository transacoes;

    @Inject
    SenhaService senhas;

    @Inject
    EmailGateway emails;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (instituicoes.count() == 0) {
            instituicoes.persist(new Instituicao("PUC Minas", "Belo Horizonte"));
            instituicoes.persist(new Instituicao("UFMG", "Belo Horizonte"));
            instituicoes.persist(new Instituicao("CEFET-MG", "Belo Horizonte"));
        }

        Instituicao puc = instituicoes.find("nome", "PUC Minas").firstResult();
        Instituicao ufmg = instituicoes.find("nome", "UFMG").firstResult();
        Instituicao cefet = instituicoes.find("nome", "CEFET-MG").firstResult();

        salvarCurso(puc, "Administracao");
        salvarCurso(puc, "Ciencia da Computacao");
        salvarCurso(puc, "Engenharia de Computacao");
        salvarCurso(puc, "Engenharia de Software");
        salvarCurso(puc, "Sistemas de Informacao");

        salvarCurso(ufmg, "Ciencia da Computacao");
        salvarCurso(ufmg, "Engenharia de Controle e Automacao");
        salvarCurso(ufmg, "Engenharia Eletrica");
        salvarCurso(ufmg, "Matematica Computacional");
        salvarCurso(ufmg, "Sistemas de Informacao");

        salvarCurso(cefet, "Design");
        salvarCurso(cefet, "Engenharia de Computacao");
        salvarCurso(cefet, "Engenharia de Software");
        salvarCurso(cefet, "Engenharia Mecatronica");
        salvarCurso(cefet, "Sistemas de Informacao");

        if (professores.count() == 0) {
            Professor professor = new Professor("Professor Demo", "professor@moedas.com", senhas.gerarHash("123456"),
                    "11122233344", "Engenharia de Software", puc);
            professor.saldoMoedas = MoedaService.COTA_SEMESTRAL;
            professor.ultimoCreditoSemestral = SemestreUtil.atual();
            professores.persist(professor);

            Transacao transacao = new Transacao();
            transacao.tipo = TipoTransacao.CREDITO_SEMESTRAL;
            transacao.valor = MoedaService.COTA_SEMESTRAL;
            transacao.professor = professor;
            transacao.mensagem = "Credito inicial do semestre " + professor.ultimoCreditoSemestral;
            transacoes.persist(transacao);
        }

        if (alunos.count() == 0) {
            Aluno aluno = new Aluno("Aluno Demo", "aluno@moedas.com", senhas.gerarHash("123456"), "55566677788",
                    "MG123456", "Rua da Universidade, 100", puc, "Engenharia de Software");
            alunos.persist(aluno);

            Aluno marina = new Aluno("Marina Lima", "marina@moedas.com", senhas.gerarHash("123456"), "44455566677",
                    "MG654321", "Avenida do Campus, 45", puc, "Sistemas de Informacao");
            alunos.persist(marina);
        }

        if (alunos.find("email", "marina@moedas.com").firstResult() == null) {
            alunos.persist(new Aluno("Marina Lima", "marina@moedas.com", senhas.gerarHash("123456"), "44455566677",
                    "MG654321", "Avenida do Campus, 45", puc, "Sistemas de Informacao"));
        }

        if (empresas.count() == 0) {
            EmpresaParceira empresa = new EmpresaParceira("Cantina Parceira", "empresa@moedas.com",
                    senhas.gerarHash("123456"), "12345678000190", "Campus Principal", "31 99999-0000");
            empresas.persist(empresa);
        }

        if (empresas.find("email", "livraria@moedas.com").firstResult() == null) {
            empresas.persist(new EmpresaParceira("Livraria Saber", "livraria@moedas.com",
                    senhas.gerarHash("123456"), "34567890000112", "Bloco B - Campus Principal", "31 98888-1212"));
        }

        EmpresaParceira empresa = empresas.find("email", "empresa@moedas.com").firstResult();
        if (empresa != null) {
            salvarOuAtualizarVantagem(empresa, "Voucher de impressao",
                    "60 paginas P&B para listas, relatorios ou comprovantes academicos. | Como usar: apresente o codigo do cupom no setor parceiro e aguarde a validacao antes de imprimir.",
                    "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=900&q=80",
                    45);
            salvarOuAtualizarVantagem(empresa, "Combo cafe e estudo",
                    "Cafe, pao de queijo e uma bebida gelada para uma pausa entre aulas. | Como usar: mostre o codigo do cupom no caixa e aguarde a validacao para retirar o combo.",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80",
                    65);
            salvarOuAtualizarVantagem(empresa, "Desconto na cantina",
                    "R$ 15 de desconto em refeicao, lanche natural ou suco selecionado. | Como usar: informe o codigo do cupom antes do pagamento e aguarde a validacao para aplicar o desconto.",
                    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=900&q=80",
                    80);
            salvarOuAtualizarVantagem(empresa, "Kit semana de provas",
                    "Agua, barra de cereal e snack para dias de avaliacao longa. | Como usar: apresente o codigo do cupom no balcao e aguarde a validacao para retirar o kit.",
                    "https://images.unsplash.com/photo-1553531384-cc64ac80f931?auto=format&fit=crop&w=900&q=80",
                    95);
        }

        EmpresaParceira livraria = empresas.find("email", "livraria@moedas.com").firstResult();
        if (livraria != null) {
            salvarOuAtualizarVantagem(livraria, "Material academico",
                    "Caderno universitario, caneta marca-texto e bloco adesivo para estudo. | Como usar: apresente o codigo do cupom no balcao e aguarde a validacao para retirar o kit.",
                    "https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=900&q=80",
                    120);
            salvarOuAtualizarVantagem(livraria, "Credito na livraria",
                    "R$ 30 de credito para livro, apostila, xerox encadernada ou material de apoio. | Como usar: escolha o item, informe o codigo do cupom no caixa e aguarde a validacao para usar o credito.",
                    "https://images.unsplash.com/photo-1526243741027-444d633d7365?auto=format&fit=crop&w=900&q=80",
                    180);
            salvarOuAtualizarVantagem(livraria, "Encadernacao de trabalho",
                    "Encadernacao simples de TCC, relatorio ou portifolio com ate 120 folhas. | Como usar: entregue o material impresso, apresente o codigo do cupom e aguarde a validacao no atendimento.",
                    "https://images.unsplash.com/photo-1606326608606-aa0b62935f2b?auto=format&fit=crop&w=900&q=80",
                    150);
            salvarOuAtualizarVantagem(livraria, "Mentoria de carreira",
                    "Sessao individual de 40 minutos para revisar curriculo, LinkedIn e portfolio. | Como usar: informe o codigo do cupom ao parceiro e aguarde a validacao para confirmar o agendamento.",
                    "https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=900&q=80",
                    240);
        }

        Professor professor = professores.find("email", "professor@moedas.com").firstResult();
        Aluno aluno = alunos.find("email", "aluno@moedas.com").firstResult();
        if (professor != null && aluno != null
                && transacoes.find("tipo = ?1 and aluno = ?2", TipoTransacao.ENVIO_MOEDAS, aluno).firstResult() == null) {
            int valor = 180;
            professor.saldoMoedas -= valor;
            aluno.saldoMoedas += valor;

            Transacao transacao = new Transacao();
            transacao.tipo = TipoTransacao.ENVIO_MOEDAS;
            transacao.valor = valor;
            transacao.professor = professor;
            transacao.aluno = aluno;
            transacao.mensagem = "Participacao consistente nas atividades praticas do laboratorio.";
            transacoes.persist(transacao);

            emails.enviar(aluno.email, "Voce recebeu moedas estudantis",
                    "O professor " + professor.nome + " enviou " + valor
                            + " moedas. Motivo: " + transacao.mensagem,
                    null);
        }

        criarResgateDemo(aluno, "Voucher de impressao", "SME-DEMO2026", false);
        criarResgateDemo(aluno, "Desconto na cantina", "SME-USADO25", true);
    }

    private void criarResgateDemo(Aluno aluno, String tituloVantagem, String codigo, boolean validado) {
        if (aluno == null || transacoes.find("codigoCupom", codigo).firstResult() != null) {
            return;
        }
        Vantagem vantagem = vantagens.find("titulo", tituloVantagem).firstResult();
        if (vantagem == null || aluno.saldoMoedas < vantagem.custoMoedas) {
            return;
        }

        aluno.saldoMoedas -= vantagem.custoMoedas;
        Transacao transacao = new Transacao();
        transacao.tipo = TipoTransacao.RESGATE_VANTAGEM;
        transacao.valor = vantagem.custoMoedas;
        transacao.aluno = aluno;
        transacao.empresa = vantagem.empresa;
        transacao.vantagem = vantagem;
        transacao.codigoCupom = codigo;
        transacao.cupomValidado = validado;
        transacao.validadoEm = validado ? LocalDateTime.now() : null;
        transacao.mensagem = "Resgate da vantagem " + vantagem.titulo;
        transacoes.persist(transacao);

        emails.enviar(aluno.email, "Cupom de troca gerado",
                "Cupom " + codigo + " gerado para a vantagem " + vantagem.titulo + ".", codigo);
        emails.enviar(vantagem.empresa.email, validado ? "Cupom ja validado no atendimento" : "Nova troca para validar",
                "O aluno " + aluno.nome + " resgatou " + vantagem.titulo + ". Codigo: " + codigo, codigo);
    }

    private void salvarOuAtualizarVantagem(EmpresaParceira empresa, String titulo, String descricao, String fotoUrl,
            int custoMoedas) {
        Vantagem vantagem = vantagens.find("titulo", titulo).firstResult();
        if (vantagem == null) {
            vantagens.persist(new Vantagem(titulo, descricao, fotoUrl, custoMoedas, empresa));
            return;
        }

        vantagem.descricao = descricao;
        vantagem.fotoUrl = fotoUrl;
        vantagem.custoMoedas = custoMoedas;
        vantagem.empresa = empresa;
    }

    private void salvarCurso(Instituicao instituicao, String nome) {
        if (instituicao == null || cursos.porInstituicaoENome(instituicao, nome).isPresent()) {
            return;
        }
        cursos.persist(new Curso(nome, instituicao));
    }
}

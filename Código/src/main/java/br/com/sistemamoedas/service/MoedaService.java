package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.TipoTransacao;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.ProfessorRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class MoedaService {

    public static final int COTA_SEMESTRAL = 1000;

    @Inject
    ProfessorRepository professores;

    @Inject
    AlunoRepository alunos;

    @Inject
    TransacaoRepository transacoes;

    @Inject
    EmailGateway emails;

    @Inject
    EmailTemplateService emailTemplates;

    @Inject
    RabbitMqFilaService filaEventos;

    @Transactional
    public void creditarCotaSemestral(Long professorId) {
        Professor professor = buscarProfessor(professorId);
        String semestreAtual = SemestreUtil.atual();
        if (semestreAtual.equals(professor.ultimoCreditoSemestral)) {
            throw new RegraNegocioException("A cota deste semestre ja foi creditada.");
        }
        professor.saldoMoedas += COTA_SEMESTRAL;
        professor.ultimoCreditoSemestral = semestreAtual;

        Transacao transacao = new Transacao();
        transacao.tipo = TipoTransacao.CREDITO_SEMESTRAL;
        transacao.valor = COTA_SEMESTRAL;
        transacao.professor = professor;
        transacao.mensagem = "Credito semestral acumulavel: " + semestreAtual;
        transacoes.persist(transacao);
    }

    @Transactional
    public void enviarMoedas(Long professorId, Long alunoId, int valor, String mensagem) {
        Professor professor = buscarProfessor(professorId);
        Aluno aluno = alunos.findByIdOptional(alunoId)
                .orElseThrow(() -> new RegraNegocioException("Aluno nao encontrado."));
        if (valor <= 0) {
            throw new RegraNegocioException("O valor deve ser maior que zero.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new RegraNegocioException("A justificativa do reconhecimento e obrigatoria.");
        }
        if (professor.saldoMoedas < valor) {
            throw new RegraNegocioException("Saldo insuficiente para enviar moedas.");
        }

        professor.saldoMoedas -= valor;
        aluno.saldoMoedas += valor;

        Transacao transacao = new Transacao();
        transacao.tipo = TipoTransacao.ENVIO_MOEDAS;
        transacao.valor = valor;
        transacao.professor = professor;
        transacao.aluno = aluno;
        transacao.mensagem = mensagem.trim();
        transacoes.persist(transacao);

        emails.enviar(aluno.email, "Voce recebeu moedas estudantis",
                emailTemplates.moedasRecebidas(aluno, professor, valor, mensagem.trim()), null);
        emails.enviar(professor.email, "Envio de moedas confirmado",
                emailTemplates.moedasEnviadas(aluno, valor, mensagem.trim()), null);
        filaEventos.publicar(new EventoSistema("MOEDAS_ENVIADAS", transacao.id, null, aluno.email, professor.email,
                null, null, valor, transacao.criadaEm.toString()));
    }

    public List<Transacao> extratoProfessor(Long professorId) {
        return transacoes.extratoProfessor(buscarProfessor(professorId));
    }

    public Professor buscarProfessor(Long professorId) {
        return professores.findByIdOptional(professorId)
                .orElseThrow(() -> new RegraNegocioException("Professor nao encontrado."));
    }
}

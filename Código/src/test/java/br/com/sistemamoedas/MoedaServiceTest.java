package br.com.sistemamoedas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.ProfessorRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import br.com.sistemamoedas.service.MoedaService;
import br.com.sistemamoedas.service.RegraNegocioException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MoedaServiceTest {

    @Inject
    MoedaService moedas;

    @Inject
    ProfessorRepository professores;

    @Inject
    AlunoRepository alunos;

    @Inject
    TransacaoRepository transacoes;

    @Test
    void deveEnviarMoedasComJustificativaEAtualizarExtratos() {
        Professor professor = professores.find("email", "professor@moedas.com").firstResult();
        Aluno aluno = alunos.find("email", "aluno@moedas.com").firstResult();
        int saldoProfessorAntes = professor.saldoMoedas;
        int saldoAlunoAntes = aluno.saldoMoedas;

        moedas.enviarMoedas(professor.id, aluno.id, 25, "Participacao excelente em aula");

        professores.getEntityManager().clear();
        Professor professorAtualizado = professores.findById(professor.id);
        Aluno alunoAtualizado = alunos.findById(aluno.id);

        assertEquals(saldoProfessorAntes - 25, professorAtualizado.saldoMoedas);
        assertEquals(saldoAlunoAntes + 25, alunoAtualizado.saldoMoedas);
        assertTrue(transacoes.extratoAluno(alunoAtualizado).stream()
                .anyMatch(t -> "Participacao excelente em aula".equals(t.mensagem)));
    }

    @Test
    void deveBloquearEnvioSemJustificativa() {
        Professor professor = professores.find("email", "professor@moedas.com").firstResult();
        Aluno aluno = alunos.find("email", "aluno@moedas.com").firstResult();

        assertThrows(RegraNegocioException.class, () -> moedas.enviarMoedas(professor.id, aluno.id, 10, " "));
    }
}

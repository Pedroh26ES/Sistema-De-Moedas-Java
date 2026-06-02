package br.com.sistemamoedas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Instituicao;
import br.com.sistemamoedas.repository.InstituicaoRepository;
import br.com.sistemamoedas.service.CadastroService;
import br.com.sistemamoedas.service.RegraNegocioException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CadastroServiceTest {

    @Inject
    CadastroService cadastros;

    @Inject
    InstituicaoRepository instituicoes;

    @Test
    void deveCadastrarAlunoComCursoPreCadastradoDaInstituicao() {
        Instituicao puc = instituicoes.find("nome", "PUC Minas").firstResult();
        String sufixo = String.valueOf(System.nanoTime());

        Aluno aluno = cadastros.cadastrarAluno("Aluno Curso", "aluno-curso-" + sufixo + "@moedas.com", "ValorizaAe#2026!",
                "900" + sufixo.substring(0, 8), "MG" + sufixo.substring(0, 6), "Rua do Curso, 10", puc.id,
                "Engenharia de Software", null);

        assertEquals("Engenharia de Software", aluno.curso);
        assertEquals("PUC Minas", aluno.instituicao.nome);
    }

    @Test
    void deveBloquearCursoQueNaoPertenceAInstituicaoSelecionada() {
        Instituicao puc = instituicoes.find("nome", "PUC Minas").firstResult();
        String sufixo = String.valueOf(System.nanoTime());

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> cadastros.cadastrarAluno("Aluno Curso Invalido", "aluno-invalido-" + sufixo + "@moedas.com",
                        "ValorizaAe#2026!", "901" + sufixo.substring(0, 8), "MG" + sufixo.substring(0, 6),
                        "Rua do Curso, 20", puc.id, "Medicina", null));

        assertTrue(erro.getMessage().contains("Curso invalido"));
    }
}

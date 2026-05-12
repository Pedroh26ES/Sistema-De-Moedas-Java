package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.TransacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AlunoService {

    @Inject
    AlunoRepository alunos;

    @Inject
    TransacaoRepository transacoes;

    public Aluno buscar(Long id) {
        return alunos.findByIdOptional(id)
                .orElseThrow(() -> new RegraNegocioException("Aluno nao encontrado."));
    }

    public List<Transacao> extrato(Long alunoId) {
        return transacoes.extratoAluno(buscar(alunoId));
    }
}

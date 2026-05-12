package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.TipoTransacao;
import br.com.sistemamoedas.domain.Transacao;
import br.com.sistemamoedas.domain.Vantagem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    public List<Transacao> extratoAluno(Aluno aluno) {
        return list("aluno = ?1 order by criadaEm desc", aluno);
    }

    public List<Transacao> extratoProfessor(Professor professor) {
        return list("professor = ?1 order by criadaEm desc", professor);
    }

    public List<Transacao> extratoEmpresa(EmpresaParceira empresa) {
        return list("empresa = ?1 order by criadaEm desc", empresa);
    }

    public Optional<Transacao> cupomDaEmpresa(EmpresaParceira empresa, String codigo) {
        return find("empresa = ?1 and upper(codigoCupom) = ?2", empresa, codigo.toUpperCase()).firstResultOptional();
    }

    public Optional<Transacao> resgateAlunoVantagem(Aluno aluno, Vantagem vantagem) {
        return find("aluno = ?1 and vantagem = ?2 and tipo = ?3 order by criadaEm desc",
                aluno, vantagem, TipoTransacao.RESGATE_VANTAGEM).firstResultOptional();
    }

    public List<Transacao> cuponsPendentesDaVantagem(Vantagem vantagem) {
        return list("vantagem = ?1 and tipo = ?2 and codigoCupom is not null and cupomValidado = false",
                vantagem, TipoTransacao.RESGATE_VANTAGEM);
    }

    public boolean existeParaVantagem(Vantagem vantagem) {
        return count("vantagem", vantagem) > 0;
    }
}

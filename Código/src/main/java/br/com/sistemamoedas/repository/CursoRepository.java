package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Curso;
import br.com.sistemamoedas.domain.Instituicao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CursoRepository implements PanacheRepository<Curso> {

    public List<Curso> porInstituicao(Instituicao instituicao) {
        return list("instituicao = ?1 order by nome", instituicao);
    }

    public Optional<Curso> porInstituicaoENome(Instituicao instituicao, String nome) {
        return find("instituicao = ?1 and lower(nome) = ?2", instituicao, nome.trim().toLowerCase()).firstResultOptional();
    }
}

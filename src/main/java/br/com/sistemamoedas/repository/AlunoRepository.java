package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Aluno;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class AlunoRepository implements PanacheRepository<Aluno> {

    public Optional<Aluno> porEmail(String email) {
        return find("lower(email) = ?1", email == null ? "" : email.trim().toLowerCase()).firstResultOptional();
    }

    public boolean cpfEmUso(String cpf) {
        return find("cpf", cpf).firstResultOptional().isPresent();
    }
}

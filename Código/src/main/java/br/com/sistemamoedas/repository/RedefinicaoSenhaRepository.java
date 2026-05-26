package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.RedefinicaoSenha;
import br.com.sistemamoedas.domain.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RedefinicaoSenhaRepository implements PanacheRepository<RedefinicaoSenha> {

    public Optional<RedefinicaoSenha> porTokenHash(String tokenHash) {
        return find("tokenHash = ?1", tokenHash).firstResultOptional();
    }

    public void invalidarPendentes(Usuario usuario) {
        update("usado = true where usuario = ?1 and usado = false", usuario);
    }
}

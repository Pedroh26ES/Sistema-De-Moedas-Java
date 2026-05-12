package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Optional<Usuario> porEmail(String email) {
        return find("lower(email) = ?1", normalizar(email)).firstResultOptional();
    }

    public boolean emailEmUso(String email) {
        return porEmail(email).isPresent();
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

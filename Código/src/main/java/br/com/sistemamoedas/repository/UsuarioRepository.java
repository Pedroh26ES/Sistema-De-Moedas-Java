package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Optional<Usuario> porEmail(String email) {
        return find("lower(email) = ?1", normalizar(email)).firstResultOptional();
    }

    public boolean emailEmUso(String email) {
        return porEmail(email).isPresent();
    }

    public List<Usuario> porTelefoneWhatsapp(String telefone) {
        Set<String> variantes = variantesTelefone(telefone);
        if (variantes.isEmpty()) {
            return List.of();
        }
        return list("telefoneWhatsapp in ?1", variantes);
    }

    private Set<String> variantesTelefone(String telefone) {
        String normalizado = normalizarTelefone(telefone);
        Set<String> variantes = new LinkedHashSet<>();
        if (normalizado.isBlank()) {
            return variantes;
        }
        variantes.add(normalizado);
        if (normalizado.startsWith("55") && normalizado.length() == 13 && normalizado.charAt(4) == '9') {
            variantes.add(normalizado.substring(0, 4) + normalizado.substring(5));
        }
        if (normalizado.startsWith("55") && normalizado.length() == 12) {
            variantes.add(normalizado.substring(0, 4) + "9" + normalizado.substring(4));
        }
        return variantes;
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return "";
        }
        String digitos = telefone.replaceAll("\\D+", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }
        return digitos;
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

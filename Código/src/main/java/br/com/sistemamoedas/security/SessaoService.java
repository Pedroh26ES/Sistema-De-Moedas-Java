package br.com.sistemamoedas.security;

import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.domain.Usuario;
import br.com.sistemamoedas.repository.UsuarioRepository;
import br.com.sistemamoedas.service.RegraNegocioException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessaoService {

    public static final String COOKIE = "SME_SESSION";

    @Inject
    UsuarioRepository usuarios;

    @Inject
    SenhaService senhas;

    private final Map<String, SessaoUsuario> sessoes = new ConcurrentHashMap<>();

    public SessaoUsuario login(String email, String senha) {
        Usuario usuario = usuarios.porEmail(email)
                .filter(Usuario::isPersistent)
                .orElseThrow(() -> new RegraNegocioException("Email ou senha invalidos."));
        if (!usuario.ativo || !senhas.confere(senha, usuario.senhaHash)) {
            throw new RegraNegocioException("Email ou senha invalidos.");
        }
        String token = UUID.randomUUID().toString();
        SessaoUsuario sessao = new SessaoUsuario(token, usuario.id, usuario.nome, usuario.email, usuario.perfil);
        sessoes.put(token, sessao);
        return sessao;
    }

    public Optional<SessaoUsuario> porToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessoes.get(token));
    }

    public SessaoUsuario exigir(String token, Perfil perfil) {
        SessaoUsuario sessao = porToken(token)
                .orElseThrow(() -> new RegraNegocioException("Faca login para acessar esta area."));
        if (sessao.perfil() != perfil) {
            throw new RegraNegocioException("Seu perfil nao possui acesso a esta area.");
        }
        return sessao;
    }

    public void logout(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }

}

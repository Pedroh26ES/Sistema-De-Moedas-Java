package br.com.sistemamoedas.security;

import br.com.sistemamoedas.domain.RedefinicaoSenha;
import br.com.sistemamoedas.domain.Usuario;
import br.com.sistemamoedas.repository.RedefinicaoSenhaRepository;
import br.com.sistemamoedas.repository.UsuarioRepository;
import br.com.sistemamoedas.service.EmailGateway;
import br.com.sistemamoedas.service.RegraNegocioException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@ApplicationScoped
public class RecuperacaoSenhaService {

    private static final int TOKEN_BYTES = 16;
    private static final int MINUTOS_VALIDADE = 30;
    private final SecureRandom random = new SecureRandom();

    @Inject
    UsuarioRepository usuarios;

    @Inject
    RedefinicaoSenhaRepository redefinicoes;

    @Inject
    SenhaService senhas;

    @Inject
    EmailGateway emails;

    @Transactional
    public void solicitar(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("Informe o email cadastrado para recuperar a senha.");
        }
        Optional<Usuario> usuarioEncontrado = usuarios.porEmail(email).filter(usuario -> usuario.ativo);
        if (usuarioEncontrado.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioEncontrado.get();
        redefinicoes.invalidarPendentes(usuario);
        String token = gerarToken();
        redefinicoes.persist(new RedefinicaoSenha(usuario, hash(token), LocalDateTime.now().plusMinutes(MINUTOS_VALIDADE)));

        emails.enviar(usuario.email, "Recuperacao de senha",
                "Recebemos uma solicitacao para redefinir sua senha no Valoriza Ae. O link fica valido por "
                        + MINUTOS_VALIDADE + " minutos. Se voce nao pediu essa troca, ignore este aviso.",
                token);
    }

    @Transactional
    public void redefinir(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new RegraNegocioException("Link de recuperacao invalido.");
        }
        RedefinicaoSenha redefinicao = redefinicoes.porTokenHash(hash(token.trim()))
                .orElseThrow(() -> new RegraNegocioException("Link de recuperacao invalido ou expirado."));
        if (redefinicao.usado || redefinicao.expiraEm.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Link de recuperacao invalido ou expirado.");
        }

        redefinicao.usuario.senhaHash = senhas.gerarHash(novaSenha);
        redefinicao.usado = true;
        redefinicoes.invalidarPendentes(redefinicao.usuario);
    }

    private String gerarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception erro) {
            throw new IllegalStateException("Nao foi possivel proteger o token de recuperacao.", erro);
        }
    }
}

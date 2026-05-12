package br.com.sistemamoedas.security;

import br.com.sistemamoedas.service.RegraNegocioException;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@ApplicationScoped
public class SenhaService {

    private static final int ITERACOES = 65_536;
    private static final int TAMANHO_CHAVE = 256;
    private final SecureRandom random = new SecureRandom();

    public String gerarHash(String senha) {
        validarSenha(senha);
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(senha.toCharArray(), salt);
        return "PBKDF2$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean confere(String senha, String hashSalvo) {
        if (senha == null || hashSalvo == null || !hashSalvo.startsWith("PBKDF2$")) {
            return false;
        }
        String[] partes = hashSalvo.split("\\$");
        if (partes.length != 3) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        byte[] obtido = pbkdf2(senha.toCharArray(), salt);
        return java.security.MessageDigest.isEqual(esperado, obtido);
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new RegraNegocioException("A senha deve ter pelo menos 6 caracteres.");
        }
    }

    private byte[] pbkdf2(char[] senha, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(senha, salt, ITERACOES, TAMANHO_CHAVE);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Nao foi possivel gerar hash da senha.", e);
        }
    }
}

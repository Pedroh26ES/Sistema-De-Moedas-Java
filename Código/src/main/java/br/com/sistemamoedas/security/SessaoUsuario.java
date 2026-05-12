package br.com.sistemamoedas.security;

import br.com.sistemamoedas.domain.Perfil;

public record SessaoUsuario(String token, Long usuarioId, String nome, String email, Perfil perfil) {
}

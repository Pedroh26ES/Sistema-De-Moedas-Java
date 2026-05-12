package br.com.sistemamoedas.controller;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class Web {

    private Web() {
    }

    static Response redirect(String destino) {
        return Response.seeOther(URI.create(destino)).build();
    }

    static Response redirectComMensagem(String destino, String campo, String mensagem) {
        return redirect(destino + "?" + campo + "=" + encode(mensagem));
    }

    static Response redirectComCookie(String destino, String cookieHeader) {
        return Response.seeOther(URI.create(destino)).header(HttpHeaders.SET_COOKIE, cookieHeader).build();
    }

    static String cookieSessao(String nome, String token) {
        return nome + "=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=28800";
    }

    static String cookieExpirado(String nome) {
        return nome + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
    }

    private static String encode(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}

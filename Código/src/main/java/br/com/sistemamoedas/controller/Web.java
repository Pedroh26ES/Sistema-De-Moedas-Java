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
        return nome + "=" + token + atributosCookie("28800");
    }

    static String cookieExpirado(String nome) {
        return nome + "=" + atributosCookie("0");
    }

    private static String encode(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static String atributosCookie(String maxAge) {
        String sameSite = env("VALORIZA_COOKIE_SAME_SITE", "Lax");
        boolean secure = Boolean.parseBoolean(env("VALORIZA_COOKIE_SECURE", "false"));
        return "; Path=/; HttpOnly; SameSite=" + sameSite + "; Max-Age=" + maxAge + (secure ? "; Secure" : "");
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}

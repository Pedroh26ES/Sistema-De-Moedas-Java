package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.security.SessaoService;
import br.com.sistemamoedas.security.SessaoUsuario;
import br.com.sistemamoedas.service.RegraNegocioException;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class AuthController {

    @Inject
    @Location("login.html")
    Template login;

    @Inject
    SessaoService sessoes;

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance form(@QueryParam("erro") String erro, @QueryParam("mensagem") String mensagem) {
        return login.data("erro", erro).data("mensagem", mensagem);
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response entrar(@FormParam("email") String email, @FormParam("senha") String senha) {
        try {
            SessaoUsuario sessao = sessoes.login(email, senha);
            return Web.redirectComCookie(destino(sessao.perfil()), Web.cookieSessao(SessaoService.COOKIE, sessao.token()));
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/login", "erro", e.getMessage());
        }
    }

    @POST
    @Path("logout")
    public Response sair(@CookieParam(SessaoService.COOKIE) String token) {
        sessoes.logout(token);
        return Web.redirectComCookie("/", Web.cookieExpirado(SessaoService.COOKIE));
    }

    private String destino(Perfil perfil) {
        return switch (perfil) {
            case ALUNO -> "/aluno";
            case PROFESSOR -> "/professor";
            case EMPRESA -> "/empresa";
        };
    }
}

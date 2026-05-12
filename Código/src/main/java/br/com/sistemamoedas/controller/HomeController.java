package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.security.SessaoService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class HomeController {

    @Inject
    @Location("home.html")
    Template home;

    @Inject
    SessaoService sessoes;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index(@CookieParam(SessaoService.COOKIE) String token) {
        return home.data("sessao", sessoes.porToken(token).orElse(null));
    }

    @GET
    @Path("cadastro")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance cadastro(@CookieParam(SessaoService.COOKIE) String token) {
        return home.data("sessao", sessoes.porToken(token).orElse(null));
    }
}

package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.security.SessaoService;
import br.com.sistemamoedas.security.SessaoUsuario;
import br.com.sistemamoedas.service.AlunoService;
import br.com.sistemamoedas.service.RegraNegocioException;
import br.com.sistemamoedas.service.VantagemService;
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

@Path("/aluno")
public class AlunoController {

    @Inject
    @Location("aluno/dashboard.html")
    Template dashboard;

    @Inject
    SessaoService sessoes;

    @Inject
    AlunoService alunos;

    @Inject
    VantagemService vantagens;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance painel(@CookieParam(SessaoService.COOKIE) String token, @QueryParam("erro") String erro,
            @QueryParam("mensagem") String mensagem) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.ALUNO);
        return dashboard.data("sessao", sessao)
                .data("aluno", alunos.buscar(sessao.usuarioId()))
                .data("extrato", alunos.extrato(sessao.usuarioId()))
                .data("vantagens", vantagens.catalogo())
                .data("erro", erro)
                .data("mensagem", mensagem);
    }

    @POST
    @Path("resgates")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response resgatar(@CookieParam(SessaoService.COOKIE) String token,
            @FormParam("vantagemId") Long vantagemId) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.ALUNO);
            String codigo = vantagens.resgatar(sessao.usuarioId(), vantagemId);
            return Web.redirectComMensagem("/aluno", "mensagem", "Resgate realizado. Cupom: " + codigo);
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/aluno", "erro", e.getMessage());
        }
    }
}

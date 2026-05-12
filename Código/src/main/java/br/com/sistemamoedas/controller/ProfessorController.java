package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.security.SessaoService;
import br.com.sistemamoedas.security.SessaoUsuario;
import br.com.sistemamoedas.service.MoedaService;
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

@Path("/professor")
public class ProfessorController {

    @Inject
    @Location("professor/dashboard.html")
    Template dashboard;

    @Inject
    SessaoService sessoes;

    @Inject
    MoedaService moedas;

    @Inject
    AlunoRepository alunos;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance painel(@CookieParam(SessaoService.COOKIE) String token, @QueryParam("erro") String erro,
            @QueryParam("mensagem") String mensagem) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
        return dashboard.data("sessao", sessao)
                .data("professor", moedas.buscarProfessor(sessao.usuarioId()))
                .data("alunos", alunos.listAll())
                .data("extrato", moedas.extratoProfessor(sessao.usuarioId()))
                .data("erro", erro)
                .data("mensagem", mensagem);
    }

    @POST
    @Path("envios")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response enviar(@CookieParam(SessaoService.COOKIE) String token, @FormParam("alunoId") Long alunoId,
            @FormParam("valor") int valor, @FormParam("mensagem") String mensagem) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
            moedas.enviarMoedas(sessao.usuarioId(), alunoId, valor, mensagem);
            return Web.redirectComMensagem("/professor", "mensagem", "Moedas enviadas com sucesso.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/professor", "erro", e.getMessage());
        }
    }

    @POST
    @Path("credito-semestral")
    public Response creditar(@CookieParam(SessaoService.COOKIE) String token) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.PROFESSOR);
            moedas.creditarCotaSemestral(sessao.usuarioId());
            return Web.redirectComMensagem("/professor", "mensagem", "Cota semestral creditada.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/professor", "erro", e.getMessage());
        }
    }
}

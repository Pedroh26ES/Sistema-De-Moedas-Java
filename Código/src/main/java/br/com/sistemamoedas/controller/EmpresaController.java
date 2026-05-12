package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.domain.Perfil;
import br.com.sistemamoedas.security.SessaoService;
import br.com.sistemamoedas.security.SessaoUsuario;
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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/empresa")
public class EmpresaController {

    @Inject
    @Location("empresa/dashboard.html")
    Template dashboard;

    @Inject
    @Location("empresa/editar-vantagem.html")
    Template editarVantagem;

    @Inject
    SessaoService sessoes;

    @Inject
    VantagemService vantagens;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance painel(@CookieParam(SessaoService.COOKIE) String token, @QueryParam("erro") String erro,
            @QueryParam("mensagem") String mensagem) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        return dashboard.data("sessao", sessao)
                .data("empresa", vantagens.buscarEmpresa(sessao.usuarioId()))
                .data("vantagens", vantagens.vantagensDaEmpresa(sessao.usuarioId()))
                .data("erro", erro)
                .data("mensagem", mensagem);
    }

    @POST
    @Path("vantagens")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response criar(@CookieParam(SessaoService.COOKIE) String token, @FormParam("titulo") String titulo,
            @FormParam("descricao") String descricao, @FormParam("fotoUrl") String fotoUrl,
            @FormParam("custoMoedas") int custoMoedas) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
            vantagens.cadastrar(sessao.usuarioId(), titulo, descricao, fotoUrl, custoMoedas);
            return Web.redirectComMensagem("/empresa", "mensagem", "Vantagem cadastrada.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/empresa", "erro", e.getMessage());
        }
    }

    @GET
    @Path("vantagens/{id}/editar")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editar(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id,
            @QueryParam("erro") String erro) {
        SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
        return editarVantagem.data("sessao", sessao)
                .data("vantagem", vantagens.buscarDaEmpresa(sessao.usuarioId(), id))
                .data("erro", erro);
    }

    @POST
    @Path("vantagens/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response atualizar(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id,
            @FormParam("titulo") String titulo, @FormParam("descricao") String descricao,
            @FormParam("fotoUrl") String fotoUrl, @FormParam("custoMoedas") int custoMoedas,
            @FormParam("ativa") String ativa) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
            vantagens.atualizar(sessao.usuarioId(), id, titulo, descricao, fotoUrl, custoMoedas, "on".equals(ativa));
            return Web.redirectComMensagem("/empresa", "mensagem", "Vantagem atualizada.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/empresa/vantagens/" + id + "/editar", "erro", e.getMessage());
        }
    }

    @POST
    @Path("vantagens/{id}/remover")
    public Response remover(@CookieParam(SessaoService.COOKIE) String token, @PathParam("id") Long id) {
        try {
            SessaoUsuario sessao = sessoes.exigir(token, Perfil.EMPRESA);
            vantagens.remover(sessao.usuarioId(), id);
            return Web.redirectComMensagem("/empresa", "mensagem", "Vantagem desativada.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/empresa", "erro", e.getMessage());
        }
    }
}

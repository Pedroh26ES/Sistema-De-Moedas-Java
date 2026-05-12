package br.com.sistemamoedas.controller;

import br.com.sistemamoedas.service.CadastroService;
import br.com.sistemamoedas.service.RegraNegocioException;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class CadastroController {

    @Inject
    @Location("cadastro-aluno.html")
    Template cadastroAluno;

    @Inject
    @Location("cadastro-empresa.html")
    Template cadastroEmpresa;

    @Inject
    CadastroService cadastros;

    @GET
    @Path("alunos/novo")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance novoAluno(@QueryParam("erro") String erro) {
        return cadastroAluno.data("instituicoes", cadastros.instituicoesDisponiveis()).data("erro", erro);
    }

    @POST
    @Path("alunos")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response criarAluno(@FormParam("nome") String nome, @FormParam("email") String email,
            @FormParam("senha") String senha, @FormParam("cpf") String cpf, @FormParam("rg") String rg,
            @FormParam("endereco") String endereco, @FormParam("instituicaoId") Long instituicaoId,
            @FormParam("curso") String curso) {
        try {
            cadastros.cadastrarAluno(nome, email, senha, cpf, rg, endereco, instituicaoId, curso);
            return Web.redirectComMensagem("/login", "mensagem", "Aluno cadastrado. Entre com seu email e senha.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/alunos/novo", "erro", e.getMessage());
        }
    }

    @GET
    @Path("empresas/nova")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance novaEmpresa(@QueryParam("erro") String erro) {
        return cadastroEmpresa.data("erro", erro);
    }

    @POST
    @Path("empresas")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response criarEmpresa(@FormParam("nome") String nome, @FormParam("email") String email,
            @FormParam("senha") String senha, @FormParam("cnpj") String cnpj, @FormParam("endereco") String endereco,
            @FormParam("contato") String contato) {
        try {
            cadastros.cadastrarEmpresa(nome, email, senha, cnpj, endereco, contato);
            return Web.redirectComMensagem("/login", "mensagem", "Empresa cadastrada. Entre com seu email e senha.");
        } catch (RegraNegocioException e) {
            return Web.redirectComMensagem("/empresas/nova", "erro", e.getMessage());
        }
    }
}

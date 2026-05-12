package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Curso;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Instituicao;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.CursoRepository;
import br.com.sistemamoedas.repository.EmpresaParceiraRepository;
import br.com.sistemamoedas.repository.InstituicaoRepository;
import br.com.sistemamoedas.repository.UsuarioRepository;
import br.com.sistemamoedas.security.SenhaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CadastroService {

    @Inject
    UsuarioRepository usuarios;

    @Inject
    AlunoRepository alunos;

    @Inject
    EmpresaParceiraRepository empresas;

    @Inject
    InstituicaoRepository instituicoes;

    @Inject
    CursoRepository cursos;

    @Inject
    SenhaService senhas;

    public List<Instituicao> instituicoesDisponiveis() {
        return instituicoes.listAll();
    }

    @Transactional
    public Aluno cadastrarAluno(String nome, String email, String senha, String cpf, String rg, String endereco,
            Long instituicaoId, String curso) {
        validarObrigatorio(nome, "Nome");
        validarObrigatorio(email, "Email");
        validarObrigatorio(cpf, "CPF");
        validarObrigatorio(rg, "RG");
        validarObrigatorio(endereco, "Endereco");
        validarObrigatorio(curso, "Curso");
        if (usuarios.emailEmUso(email)) {
            throw new RegraNegocioException("Ja existe usuario com este email.");
        }
        if (alunos.cpfEmUso(cpf)) {
            throw new RegraNegocioException("Ja existe aluno com este CPF.");
        }
        if (instituicaoId == null) {
            throw new RegraNegocioException("Instituicao e obrigatoria.");
        }
        Instituicao instituicao = instituicoes.findByIdOptional(instituicaoId)
                .orElseThrow(() -> new RegraNegocioException("Instituicao invalida."));
        Curso cursoSelecionado = cursos.porInstituicaoENome(instituicao, curso)
                .orElseThrow(() -> new RegraNegocioException("Curso invalido para a instituicao selecionada."));
        Aluno aluno = new Aluno(nome.trim(), email.trim().toLowerCase(), senhas.gerarHash(senha), cpf.trim(), rg.trim(),
                endereco.trim(), instituicao, cursoSelecionado.nome);
        alunos.persist(aluno);
        return aluno;
    }

    @Transactional
    public EmpresaParceira cadastrarEmpresa(String nome, String email, String senha, String cnpj, String endereco,
            String contato) {
        validarObrigatorio(nome, "Nome");
        validarObrigatorio(email, "Email");
        validarObrigatorio(cnpj, "CNPJ");
        validarObrigatorio(endereco, "Endereco");
        validarObrigatorio(contato, "Contato");
        if (usuarios.emailEmUso(email)) {
            throw new RegraNegocioException("Ja existe usuario com este email.");
        }
        if (empresas.cnpjEmUso(cnpj)) {
            throw new RegraNegocioException("Ja existe empresa com este CNPJ.");
        }
        EmpresaParceira empresa = new EmpresaParceira(nome.trim(), email.trim().toLowerCase(), senhas.gerarHash(senha),
                cnpj.trim(), endereco.trim(), contato.trim());
        empresas.persist(empresa);
        return empresa;
    }

    private void validarObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " e obrigatorio.");
        }
    }
}

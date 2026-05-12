package br.com.sistemamoedas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alunos")
public class Aluno extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    public String cpf;

    @Column(nullable = false, length = 20)
    public String rg;

    @Column(nullable = false, length = 220)
    public String endereco;

    @Column(nullable = false, length = 120)
    public String curso;

    @ManyToOne(optional = false)
    public Instituicao instituicao;

    @Column(nullable = false)
    public int saldoMoedas = 0;

    protected Aluno() {
    }

    public Aluno(String nome, String email, String senhaHash, String cpf, String rg, String endereco,
            Instituicao instituicao, String curso) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = Perfil.ALUNO;
        this.cpf = cpf;
        this.rg = rg;
        this.endereco = endereco;
        this.instituicao = instituicao;
        this.curso = curso;
    }
}

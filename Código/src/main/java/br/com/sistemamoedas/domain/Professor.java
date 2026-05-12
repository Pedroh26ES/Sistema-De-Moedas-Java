package br.com.sistemamoedas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "professores")
public class Professor extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    public String cpf;

    @Column(nullable = false, length = 120)
    public String departamento;

    @ManyToOne(optional = false)
    public Instituicao instituicao;

    @Column(nullable = false)
    public int saldoMoedas = 0;

    @Column(length = 8)
    public String ultimoCreditoSemestral;

    protected Professor() {
    }

    public Professor(String nome, String email, String senhaHash, String cpf, String departamento,
            Instituicao instituicao) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = Perfil.PROFESSOR;
        this.cpf = cpf;
        this.departamento = departamento;
        this.instituicao = instituicao;
    }
}
